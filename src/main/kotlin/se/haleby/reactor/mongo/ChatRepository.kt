package se.haleby.reactor.mongo

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.mongodb.repository.Tailable
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.haleby.reactor.Message

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
internal class ChatRepositoryImpl(private val repo: SpringReactiveChatRepository, mongo: MongoTemplate) : ChatRepository {

    init {
        if (!mongo.collectionExists(MongoMessageDTO::class.java)) {
            val options = CollectionOptions.empty().capped().maxDocuments(1000).size(1_000_000)
            mongo.createCollection(MongoMessageDTO::class.java, options)
        }
    }

    override fun save(message: Message): Mono<Message> = repo.save(MongoMessageDTO(message.id, message.from, message.text)).map(MongoMessageDTO::toDomain)
    override fun subscribeToMessages(): Flux<Message> = repo.findAllMessages().map(MongoMessageDTO::toDomain).repeat()
}

@Document("messages")
@TypeAlias("Message")
internal data class MongoMessageDTO(val id: String? = null, val from: String, val text: String)

private fun MongoMessageDTO.toDomain() = Message(from, text, id)