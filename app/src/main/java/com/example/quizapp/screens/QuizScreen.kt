package com.example.quizapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Exam
import com.example.quizapp.ui.theme.BlueSecondary
import com.example.quizapp.ui.theme.GreenDark
import com.example.quizapp.ui.theme.GreenLight
import com.example.quizapp.ui.theme.GreenPrimary

/** هر سوال در یک صفحه؛ دو دکمه پایین: «پاسخ» و «سوال بعدی» */
@Composable
fun QuizScreen(exam: Exam, count: Int, onFinish: () -> Unit) {
    val questions = remember(exam, count) { exam.questions.take(count) }
    val total = questions.size

    var index by remember(exam, count) { mutableIntStateOf(0) }
    var showAnswer by remember(exam, count) { mutableStateOf(false) }

    val current = index.coerceIn(0, total - 1)
    val question = questions[current]
    val isLast = current == total - 1

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // بخش قابل اسکرول: سوال و پاسخ
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "${exam.title} — سوال ${current + 1} از $total",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = question.q,
                    color = Color.Black,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }

            // پاسخ زیر سوال نمایش داده می‌شود
            if (showAnswer) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("پاسخ:", color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = question.a,
                            color = GreenDark,
                            fontSize = 18.sp,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // گزینه اول: پاسخ
        Button(
            onClick = { showAnswer = true },
            enabled = !showAnswer,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBDBDBD),
                disabledContentColor = Color(0xFF616161)
            )
        ) {
            Text("پاسخ", fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        // گزینه دوم: سوال بعدی (در سوال آخر: پایان آزمون)
        Button(
            onClick = {
                if (isLast) {
                    onFinish()
                } else {
                    index = current + 1
                    showAnswer = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BlueSecondary,
                contentColor = Color.Black
            )
        ) {
            Text(if (isLast) "پایان آزمون" else "سوال بعدی", fontSize = 18.sp)
        }
    }
}
