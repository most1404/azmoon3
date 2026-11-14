package com.example.quizapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Book
import com.example.quizapp.Exam
import com.example.quizapp.ExamStore
import com.example.quizapp.ExamText
import com.example.quizapp.Question
import com.example.quizapp.QuestionParser
import com.example.quizapp.ui.theme.GreenPrimary

/** پنل معلم — مرحلهٔ ۳: افزودن یا ویرایش آزمون (نام، سوال‌ها، پاسخ‌ها) */
@Composable
fun ExamEditorScreen(
    books: List<Book>,
    bookName: String,
    originalTitle: String?,
    onBooksChange: (List<Book>) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val book = books.firstOrNull { it.name == bookName }
    val original = book?.exams?.firstOrNull { it.title == originalTitle }

    var title by remember(originalTitle) { mutableStateOf(original?.title ?: "") }
    var questionsText by remember(originalTitle) {
        mutableStateOf(original?.let { ExamText.numbered(it.questions.map { q -> q.q }) } ?: "")
    }
    var answersText by remember(originalTitle) {
        mutableStateOf(original?.let { ExamText.numbered(it.questions.map { q -> q.a }) } ?: "")
    }

    val questions = QuestionParser.parse(questionsText)
    val answers = QuestionParser.parse(answersText)
    val countsMatch = questions.size == answers.size
    val canSave = title.isNotBlank() && questions.isNotEmpty() && countsMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Text("کتاب: $bookName", color = Color.Gray)
        Text(
            if (originalTitle == null) "افزودن آزمون" else "ویرایش آزمون",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "سوال‌ها را یکجا در کادر اول و پاسخ‌ها را یکجا در کادر دوم بنویسید یا بچسبانید. " +
                "هر مورد را با شماره شروع کنید (مثل ۱. یا 2) ) یا با یک خط خالی جدا کنید. " +
                "پاسخ شمارهٔ ۱ مربوط به سوال شمارهٔ ۱ است.",
            color = Color.Gray,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("نام آزمون (مثلاً درس ۱)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = questionsText,
            onValueChange = { questionsText = it },
            label = { Text("همهٔ سوال‌ها") },
            minLines = 6,
            maxLines = 12,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = answersText,
            onValueChange = { answersText = it },
            label = { Text("همهٔ پاسخ‌ها") },
            minLines = 6,
            maxLines = 12,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text(
            "تعداد سوال‌ها: ${questions.size} | تعداد پاسخ‌ها: ${answers.size}",
            color = if (countsMatch) GreenPrimary else Color(0xFFC62828),
            fontSize = 14.sp
        )
        if (!countsMatch) {
            Text(
                "تعداد سوال‌ها و پاسخ‌ها باید برابر باشد.",
                color = Color(0xFFC62828),
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val newTitle = title.trim()
                val duplicate = book?.exams?.any { it.title == newTitle && it.title != originalTitle } == true
                if (duplicate) {
                    Toast.makeText(context, "آزمونی با این نام در این کتاب هست", Toast.LENGTH_LONG).show()
                } else {
                    val exam = Exam(newTitle, questions.zip(answers) { q, a -> Question(q, a) })
                    onBooksChange(ExamStore.saveExam(books, bookName, originalTitle, exam))
                    Toast.makeText(context, "ذخیره شد", Toast.LENGTH_SHORT).show()
                    onDone()
                }
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("💾 ذخیره", fontSize = 18.sp)
        }
    }
}
