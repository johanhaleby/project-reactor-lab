# Project Reactor Lab

A skelleton project for a lab on [project reactor](https://projectreactor.io/) and [Spring Webflux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html).

The purpose is to write a simple chat application to get a feeling for reactive programming with Spring.

## Prerequisites 

1. Install and run mongodb
1. Clone this repository and open in it your favorite IDE

## Instructions

Essentially find all `TODO()`'s in the application and implement them:

1. Store chat messages in capped MongoDB collection
1. Stream from this collection and publish messages as server-sent events to connected JS clients
1. Only return the 30 last messages when joining the chat
1. Filter out swear words of choice (tip: `"Skånerost"`)
1. Realtime sentiment analysis, how does the system behave? (see [here](https://github.com/johanhaleby/rx-kdag#sentiment-analyzer) for instructions)
1. Lookup/translate words from external source

Start the app using: 

```bash
$ mvn spring-boot:run
```
