package se.haleby.reactor.swearwords

object SwearWordObfuscator {
    private val swearWords = setOf("skånerost", "fulkaffe", "mörkrost")

    fun obfuscateSwearWords(message: String): String =
            message.splitToSequence(" ").map { word: String ->
                if (swearWords.contains(word.toLowerCase())) {
                    (0..word.length).joinToString(separator = "*") { "" }
                } else {
                    word
                }
            }.joinToString(" ")
}