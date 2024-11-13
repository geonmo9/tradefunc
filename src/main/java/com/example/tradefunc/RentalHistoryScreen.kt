package com.example.tradefunc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tradefunc.data.RentalHistory
import com.example.tradefunc.repository.RentalRepository
import com.example.tradefunc.ui.theme.TradeFuncTheme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class RentalHistoryActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 익명 인증
        auth = FirebaseAuth.getInstance()
        authenticateUser()

        setContent {
            TradeFuncTheme {
                // Scaffold에서 padding을 받아서 사용하도록 수정
                Scaffold { padding ->
                    RentalHistoryScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }

    private fun authenticateUser() {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Authenticated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun RentalHistoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rentalRepository = RentalRepository(context)
    var historyList by remember { mutableStateOf<List<RentalHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Firestore에서 대여 이력 가져오기
    LaunchedEffect(Unit) {
        rentalRepository.getUserRentalHistory(
            userId = "testUser",
            onSuccess = { history ->
                historyList = history
                isLoading = false
            },
            onFailure = { exception ->
                Toast.makeText(context, "Failed to get history: ${exception.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Text("Loading...")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(historyList) { history ->
                    RentalHistoryRow(history)
                }
            }
        }
    }
}

@Composable
fun RentalHistoryRow(history: RentalHistory) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // rentalDate와 returnDate를 사람이 읽기 쉬운 형식으로 변환
    val rentalDateString = history.rentalDate?.let {
        dateFormat.format(it.toDate())
    } ?: "Unknown"

    val returnDateString = history.returnDate?.let {
        dateFormat.format(it.toDate())
    } ?: "Not yet returned"

    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Item: ${history.itemName}")
            Text(text = "Rented on: $rentalDateString")
            Text(text = "Returned on: $returnDateString")
        }
    }
}
