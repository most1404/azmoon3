package com.example.quizapp

data class SharedExam(val book: String, val exam: Exam)

/** قالب متنی برای ارسال آزمون به دانش‌آموزان، و دریافت آن */
object ExamText {
    private const val BOOK_PREFIX = "کتاب:"
    private const val TITLE_PREFIX = "آزمون:"
    private const val Q_MARK = "[سوالات]"
    private const val A_MARK = "[پاسخنامه]"

    /** شماره‌گذاری: «1. متن» در هر خط */
    fun numbered(items: List<String>): String =
        items.mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")

    fun toShareText(bookName: String, exam: Exam): String {
        val sb = StringBuilder()
        sb.append(BOOK_PREFIX).append(' ').append(bookName).append('\n')
        sb.append(TITLE_PREFIX).append(' ').append(exam.title).append("\n\n")
        sb.append(Q_MARK).append('\n')
        sb.append(numbered(exam.questions.map { it.q })).append("\n\n")
        sb.append(A_MARK).append('\n')
        sb.append(numbered(exam.questions.map { it.a })).append('\n')
        return sb.toString()
    }

    /** اگر متن معتبر نباشد null برمی‌گرداند */
    fun fromShareText(text: String): SharedExam? {
        val qIdx = text.indexOf(Q_MARK)
        val aIdx = text.indexOf(A_MARK)
        if (qIdx < 0 || aIdx < 0 || aIdx < qIdx) return null

        val headerLines = text.substring(0, qIdx).lines()
        val book = headerLines.firstOrNull { it.contains(BOOK_PREFIX) }
            ?.substringAfter(BOOK_PREFIX)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "کتاب دریافتی"
        val title = headerLines.firstOrNull { it.contains(TITLE_PREFIX) }
            ?.substringAfter(TITLE_PREFIX)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "آزمون دریافتی"

        val questions = QuestionParser.parse(text.substring(qIdx + Q_MARK.length, aIdx))
        val answers = QuestionParser.parse(text.substring(aIdx + A_MARK.length))
        if (questions.isEmpty() || questions.size != answers.size) return null

        return SharedExam(book, Exam(title, questions.zip(answers) { q, a -> Question(q, a) }))
    }
}
