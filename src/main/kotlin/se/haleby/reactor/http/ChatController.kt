package se.haleby.reactor.http

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.haleby.reactor.Message
import se.haleby.reactor.logging.loggerFor
import se.haleby.reactor.mongo.ChatRepository
import javax.validation.Valid
import javax.validation.constraints.NotBlank


@RestController()
@RequestMapping(path = ["/api/messages"])
class ChatController(private val chatRepository: ChatRepository) {
    private val log = loggerFor<ChatController>()

    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    fun postMessage(@RequestBody @Valid message: HttpMessageDTO): Mono<ResponseEntity<Void>> = TODO()

    @GetMapping
    fun streamMessages(): Flux<ServerSentEvent<HttpMessageDTO>> = TODO("""
        Construct and return a ServerSentEvent object from a HttpMessageDTO with a event type message. For example:

        ServerSentEvent.builder<HttpMessageDTO>()
                            .id(message.id!!)
                            .event("message")
                            .data(message)
                            .build()
    """.trimIndent())
}

data class HttpMessageDTO(@NotBlank val from: String, @NotBlank val text: String, val id: String? = null)

private fun HttpMessageDTO.toDomain() = Message(from, text)
private fun Message.toDTO() = HttpMessageDTO(from, text, id)