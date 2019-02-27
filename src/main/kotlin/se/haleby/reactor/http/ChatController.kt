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
import java.time.Duration
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
    fun streamMessages(): Flux<ServerSentEvent<HttpMessageDTO>> {
        return chatRepository.subscribeToMessages()
                .bufferTimeout(1000, Duration.ofMillis(250))
                .map { list ->
                    list.takeLast(10)
                }
                .flatMap { list -> list.toFlux() }
                .doOnSubscribe {
                    log.info("Client connected")
                }
                .map(Message::toDTO)
                .map { message ->
                    ServerSentEvent.builder<HttpMessageDTO>()
                            .id(message.id!!)
                            .event("message")
                            .data(message)
                            .build()
                }
    }
}

data class HttpMessageDTO(@NotBlank val from: String, @NotBlank val text: String, val id: String? = null)

private fun HttpMessageDTO.toDomain() = Message(from, text)
private fun Message.toDTO() = HttpMessageDTO(from, text, id)