package com.example.tradefunc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tradefunc.data.Item
import com.example.tradefunc.repository.ItemRepository
import com.example.tradefunc.repository.RentalRepository
import com.example.tradefunc.ui.theme.TradeFuncTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 익명 인증
        auth = FirebaseAuth.getInstance()
        authenticateUser()

        setContent {
            TradeFuncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ItemListScreen(modifier = Modifier.padding(innerPadding))
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
fun ItemListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val itemRepository = ItemRepository()
    val rentalRepository = RentalRepository(context)
    var itemList by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        itemRepository.getItems(
            onSuccess = { items ->
                itemList = items
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.message
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                val intent = Intent(context, RentalHistoryActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("View Rental History")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> LoadingView()
            errorMessage != null -> ErrorView(errorMessage)
            else -> ItemListView(itemList, rentalRepository, context)
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Loading...",
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
        )
    }
}

@Composable
fun ErrorView(errorMessage: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Error: ${errorMessage ?: "Unknown Error"}",
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
        )
    }
}

@Composable
fun ItemRow(
    item: Item,
    rentalRepository: RentalRepository,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${item.name}")
            Text(text = "Description: ${item.description}")
            Text(text = "Available: ${if (item.available) "Yes" else "No"}")

            Button(
                onClick = {
                    val userId = "testUser" // 실제 사용자 ID로 변경 필요
                    if (item.available) {
                        rentalRepository.addRentalHistory(
                            userId = userId,
                            itemId = item.id,
                            itemName = item.name,
                            onSuccess = {
                                rentalRepository.updateItemAvailability(
                                    itemId = item.id,
                                    available = false,
                                    onSuccess = {
                                        Toast.makeText(context, "Item rented!", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "Failed to update availability.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed to rent item.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        rentalRepository.updateItemAvailability(
                            itemId = item.id,
                            available = true,
                            onSuccess = {
                                Toast.makeText(context, "Item returned!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed to return item.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (item.available) "Rent" else "Return")
            }
        }
    }
}


@Composable
fun ItemListView(
    itemList: List<Item>,
    rentalRepository: RentalRepository,
    context: android.content.Context
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(itemList) { item ->
            ItemRow(item, rentalRepository, context)
        }
    }
}
