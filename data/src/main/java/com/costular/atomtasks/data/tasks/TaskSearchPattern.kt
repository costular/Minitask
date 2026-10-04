package com.costular.atomtasks.data.tasks

// SQLite's LIKE/NOCASE only fold ASCII. GLOB character sets preserve Kotlin's
// simple Unicode case matching without loading task names or folding accents.
private val caseVariants: Map<Char, String> by lazy {
    buildMap {
        for (character in Char.MIN_VALUE..Char.MAX_VALUE) {
            val uppercase = character.uppercaseChar()
            if (uppercase != character || character.lowercaseChar() != character) {
                val folded = uppercase.lowercaseChar()
                val variants = get(folded) ?: folded.toString()
                if (character !in variants) put(folded, variants + character)
            }
        }
    }
}

/** Encodes a literal, case-insensitive substring as a bound SQLite GLOB parameter. */
fun taskNameSearchPattern(query: String): String = buildString {
    append('*')
    for (character in query) {
        when (character) {
            '*' -> append("[*]")
            '?' -> append("[?]")
            '[' -> append("[[]")
            else -> {
                val variants = caseVariants[character.uppercaseChar().lowercaseChar()]
                if (variants == null) append(character)
                else append('[').append(variants).append(']')
            }
        }
    }
    append('*')
}
