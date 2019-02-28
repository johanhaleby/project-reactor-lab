package se.haleby.reactor.sentimentanalyzer

import se.haleby.reactor.sentimentanalyzer.SentimentAnalysis.*

enum class SentimentAnalysis {
    POSITIVE, NEGATIVE, NEUTRAL
}

object SentenceSentimentAnalyzer {
    private val POSITIVE_WORDS = setOf(":)", ":D", "yay", "good", "great", "<3", "yes", "nice", "awesome")
    private val NEGATIVE_WORDS = setOf(":(", ":/", "no", "argh", "darn", "dammit", "ugly", "workaround", "sad", "skånerost")

    fun analyze(sentence: String): SentimentAnalysis {
        val words = sentence.toLowerCase().split(" ")
        val positiveWords = words.count(POSITIVE_WORDS::contains)
        val negativeWords = words.count(NEGATIVE_WORDS::contains)

        return when {
            positiveWords > negativeWords -> POSITIVE
            positiveWords < negativeWords -> NEGATIVE
            else -> NEUTRAL
        }
    }
}