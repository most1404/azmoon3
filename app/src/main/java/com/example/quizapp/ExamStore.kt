package com.example.quizapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Question(val q: String, val a: String)
data class Exam(val title: String, val questions: List<Question>)
data class Book(val name: String, val exams: List<Exam>)

/** ذخیره و بازیابی کتاب‌ها و آزمون‌ها روی خود گوشی (آفلاین) */
object ExamStore {
    private const val PREFS = "exam_store"
    private const val KEY = "books_v2"

    fun load(context: Context): List<Book> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val booksArr = JSONArray(raw)
            val books = mutableListOf<Book>()
            for (i in 0 until booksArr.length()) {
                val bObj = booksArr.getJSONObject(i)
                val examsArr = bObj.getJSONArray("exams")
                val exams = mutableListOf<Exam>()
                for (j in 0 until examsArr.length()) {
                    val eObj = examsArr.getJSONObject(j)
                    val qArr = eObj.getJSONArray("questions")
                    val questions = mutableListOf<Question>()
                    for (k in 0 until qArr.length()) {
                        val qObj = qArr.getJSONObject(k)
                        questions.add(Question(qObj.getString("q"), qObj.getString("a")))
                    }
                    exams.add(Exam(eObj.getString("title"), questions))
                }
                books.add(Book(bObj.getString("name"), exams))
            }
            books
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, books: List<Book>) {
        val booksArr = JSONArray()
        for (book in books) {
            val examsArr = JSONArray()
            for (exam in book.exams) {
                val qArr = JSONArray()
                for (question in exam.questions) {
                    qArr.put(JSONObject().put("q", question.q).put("a", question.a))
                }
                examsArr.put(JSONObject().put("title", exam.title).put("questions", qArr))
            }
            booksArr.put(JSONObject().put("name", book.name).put("exams", examsArr))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, booksArr.toString()).apply()
    }

    // ---- عملیات خالص روی فهرست (بدون تغییر فهرست اصلی) ----

    fun addBook(books: List<Book>, name: String): List<Book> =
        if (books.any { it.name == name }) books else books + Book(name, emptyList())

    fun renameBook(books: List<Book>, oldName: String, newName: String): List<Book> =
        books.map { if (it.name == oldName) it.copy(name = newName) else it }

    fun deleteBook(books: List<Book>, name: String): List<Book> =
        books.filter { it.name != name }

    /**
     * ذخیرهٔ آزمون در یک کتاب.
     * originalTitle: نام قبلی آزمون هنگام ویرایش (برای آزمون جدید null).
     * اگر کتاب وجود نداشته باشد ساخته می‌شود.
     */
    fun saveExam(books: List<Book>, bookName: String, originalTitle: String?, exam: Exam): List<Book> {
        val withBook = if (books.any { it.name == bookName }) books else books + Book(bookName, emptyList())
        return withBook.map { book ->
            if (book.name != bookName) {
                book
            } else {
                val list = book.exams.toMutableList()
                val idx = list.indexOfFirst { it.title == (originalTitle ?: exam.title) }
                if (idx >= 0) list[idx] = exam else list.add(exam)
                book.copy(exams = list.filterIndexed { i, e -> e.title != exam.title || e === exam })
            }
        }
    }

    fun deleteExam(books: List<Book>, bookName: String, title: String): List<Book> =
        books.map { book ->
            if (book.name == bookName) book.copy(exams = book.exams.filter { it.title != title }) else book
        }
}
