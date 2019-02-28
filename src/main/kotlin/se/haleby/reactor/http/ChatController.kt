package se.haleby.reactor.http

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import se.haleby.reactor.Message
import se.haleby.reactor.logging.loggerFor
import se.haleby.reactor.mongo.ChatRepository
import se.haleby.reactor.sentimentanalyzer.SentenceSentimentAnalyzer
import se.haleby.reactor.sentimentanalyzer.SentimentAnalysis
import se.haleby.reactor.sentimentanalyzer.SentimentAnalysis.*
import se.haleby.reactor.sentimentanalyzer.SystemSentimentAnalyzer
import se.haleby.reactor.swearwords.SwearWordObfuscator
import java.time.Duration
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank


@RestController()
@RequestMapping(path = ["/api/messages"])
class ChatController(private val chatRepository: ChatRepository) {
    private val log = loggerFor<ChatController>()

    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    fun postMessage(@RequestBody @Valid message: HttpMessageDTO) =
            chatRepository.save(message.toDomain())
                    .doOnNext { log.info("[${it.from}] Posted '${it.text}'") }
                    .map { ResponseEntity.accepted().build<Unit>() }

    @GetMapping
    fun streamMessages(): Flux<ServerSentEvent<Any>> {

        val systemSystemSentimentChanges = SystemSentimentAnalyzer.changes().map { it.name }

        val loading = chatRepository.subscribeToMessages()
                .bufferTimeout(1000, Duration.ofMillis(500))
                .map { list ->
                    list.takeLast(10)
                }
                .limitRequest(1)

        val composed = loading.flatMap { list ->
            val continuous = chatRepository.subscribeToMessages()
                    .skipUntil { message -> message.id == list.last().id }
                    .skip(1)

            Flux.concat(list.toFlux(), continuous)
        }

        return composed
                .map { message -> message to SentenceSentimentAnalyzer.analyze(message.text) }
                .doOnNext { (_, analysis) ->
                    SystemSentimentAnalyzer.addMessageSentimentAnalysis(analysis)
                }
                .map { (message, analysis) -> addSentimentAnalysisToMessage(message, analysis) }
                .map(::obfuscateSwearWords)
                .map(Message::toDTO)
                .map { message ->
                    sseEvent("message", message.id!!, message)
                }
                .mergeWith(systemSystemSentimentChanges.map {
                    sseEvent("sentiment", UUID.randomUUID().toString(), it)
                })
                .doOnSubscribe {
                    log.info("Client connected")
                }
                .doOnCancel {
                    log.info("Client disconnected")
                }
    }
}

data class HttpMessageDTO(@NotBlank val from: String, @NotBlank val text: String, val id: String? = null)

private fun HttpMessageDTO.toDomain() = Message(from, text)
private fun Message.toDTO() = HttpMessageDTO(from, text, id)

private fun obfuscateSwearWords(message: Message): Message {
    val messageWithObfuscatedSwearWords = SwearWordObfuscator.obfuscateSwearWords(message.text)
    return message.copy(text = messageWithObfuscatedSwearWords)
}

private fun addSentimentAnalysisToMessage(message: Message, sentimentAnalysis: SentimentAnalysis): Message {
    val simley = when (sentimentAnalysis) {
        POSITIVE -> "\uD83D\uDE03"
        NEGATIVE -> "\uD83D\uDE41"
        NEUTRAL -> "\uD83D\uDE10"
    }
    return message.copy(text = message.text + " —[$simley]—")
}

private fun sseEvent(eventType: String, id: String, data: Any) = ServerSentEvent.builder<Any>()
        .id(id)
        .event(eventType)
        .data(data)
        .build()
