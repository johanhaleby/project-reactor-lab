package se.haleby.reactor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@SpringBootApplication
@EnableReactiveMongoRepositories
class ReactorLabApplication


// Redirect from / to /index.html since this is not done automatically in Spring Boot Reactive
// see https://github.com/spring-projects/spring-boot/issues/9785
@Component
class RedirectWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
            if (exchange.request.uri.path == "/") {
                chain.filter(exchange.mutate().request(exchange.request.mutate().path("/index.html").build()).build())
            } else chain.filter(exchange)
}

fun main(args: Array<String>) {
    runApplication<ReactorLabApplication>(*args)
}
