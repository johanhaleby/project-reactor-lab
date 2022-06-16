package se.haleby.reactor.mongo

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.mongodb.repository.Tailable
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.haleby.reactor.Message
import se.haleby.reactor.logging.loggerFor

interface ChatRepository {
    fun save(message: Message): Mono<Message>
    fun subscribeToMessages(): Flux<Message>
}

internal interface SpringReactiveChatRepository : ReactiveMongoRepository<MongoMessageDTO, String> {
    @Tailable
    @Query("{}")
    fun findAllMessages(): Flux<MongoMessageDTO>
}

@Component
internal class ChatRepositoryImpl(private val repo: SpringReactiveChatRepository, mongo: ReactiveMongoTemplate) : ChatRepository {
    private val log = loggerFor<ChatRepositoryImpl>()

    init {
        mongo.collectionExists<MongoMessageDTO>()
            .flatMap { collectionExists ->
                if (collectionExists) {
                    log.info("messages collection already exists - won't recreate it")
                    Mono.empty()
                } else {
                    log.info("messages collection doesn't exists - will create it")
                    val options = CollectionOptions.empty().capped().maxDocuments(1000).size(1_000_000)
                    mongo.createCollection<MongoMessageDTO>(options).then()
                }
            }
            .subscribe()
    }

    override fun save(message: Message): Mono<Message> = TODO()
    override fun subscribeToMessages(): Flux<Message> = TODO()
}

@Document("messages")
@TypeAlias("Message")
internal data class MongoMessageDTO(val id: String? = null, val from: String, val text: String)

private fun MongoMessageDTO.toDomain() = Message(from, text, id)