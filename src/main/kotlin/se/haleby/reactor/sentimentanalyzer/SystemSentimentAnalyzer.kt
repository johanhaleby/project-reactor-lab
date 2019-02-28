package se.haleby.reactor.sentimentanalyzer

import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicReference

object SystemSentimentAnalyzer {
    private val systemSentimentAnalysis = AtomicReference(0)
    private val processor = DirectProcessor.create<SentimentAnalysis>().serialize()
    private val sink = processor.sink()

    fun changes(): Flux<SentimentAnalysis> = processor

    fun addMessageSentimentAnalysis(sentimentAnalysis: SentimentAnalysis) {
        val moodBeforeUpdate = systemSentimentAnalysis.get().sentimentAnalysis()
        val moodAfterUpdate = systemSentimentAnalysis.updateAndGet { currentMood ->
            when (sentimentAnalysis) {
                SentimentAnalysis.POSITIVE -> currentMood.inc()
                SentimentAnalysis.NEGATIVE -> currentMood.dec()
                SentimentAnalysis.NEUTRAL -> currentMood
            }
        }.sentimentAnalysis()

        if (moodAfterUpdate != moodBeforeUpdate) {
            sink.next(moodAfterUpdate)
        }
    }
}

fun Int.sentimentAnalysis() = when {
    this > 0 -> SentimentAnalysis.POSITIVE
    this == 0 -> SentimentAnalysis.NEUTRAL
    else -> SentimentAnalysis.NEGATIVE
}