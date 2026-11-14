package com.example.quizapp

/**
 * تبدیل متن یکجا به فهرست سوال‌ها (یا پاسخ‌ها).
 * قانون تشخیص:
 *  ۱) اگر خط اول با شماره شروع شود (مثل «1.» «۲)» «3-») هر شماره یک مورد جدید است
 *  ۲) در غیر این صورت، موردها با خط خالی جدا می‌شوند
 *  ۳) اگر خط خالی هم نباشد، هر خط یک مورد است
 */
object QuestionParser {

    private val numberPrefix = Regex("^\\s*\\d{1,4}\\s*[.)\\-–—:،]\\s*")

    private fun normalizeDigits(s: String): String {
        val sb = StringBuilder()
        for (ch in s) {
            when (ch) {
                in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parse(text: String): List<String> {
        val lines = text.replace("\r", "").split("\n")
        val firstLine = lines.firstOrNull { it.isNotBlank() } ?: return emptyList()

        // حالت ۱: شماره‌دار
        if (numberPrefix.containsMatchIn(normalizeDigits(firstLine))) {
            val items = mutableListOf<StringBuilder>()
            for (line in lines) {
                if (line.isBlank()) continue
                val match = numberPrefix.find(normalizeDigits(line))
                if (match != null) {
                    // طول رشته بعد از نرمال‌سازی ارقام تغییر نمی‌کند
                    items.add(StringBuilder(line.substring(match.range.last + 1).trim()))
                } else if (items.isNotEmpty()) {
                    items.last().append("\n").append(line.trim())
                }
            }
            return items.map { it.toString().trim() }.filter { it.isNotEmpty() }
        }

        // حالت ۲: جداشده با خط خالی
        if (lines.any { it.isBlank() }) {
            val items = mutableListOf<String>()
            val current = mutableListOf<String>()
            for (line in lines) {
                if (line.isBlank()) {
                    if (current.isNotEmpty()) {
                        items.add(current.joinToString("\n"))
                        current.clear()
                    }
                } else {
                    current.add(line.trim())
                }
            }
            if (current.isNotEmpty()) items.add(current.joinToString("\n"))
            return items
        }

        // حالت ۳: هر خط یک مورد
        return lines.map { it.trim() }.filter { it.isNotEmpty() }
    }
}
