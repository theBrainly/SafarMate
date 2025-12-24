package com.example.mypp.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypp.R
import com.example.mypp.ui.theme.MyPPTheme
import java.text.SimpleDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = java.util.UUID.randomUUID().toString(),
    val isTyping: Boolean = false
)

/**
 * ChatBot Screen with voice recognition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(
        listOf(
            ChatMessage("Hello! I'm SafarMate assistant. How can I help you?", false)
        )
    )}

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText: String? = result.data?.getStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS
        )?.get(0)

        if (!spokenText.isNullOrBlank()) {
            messageText = spokenText
            sendMessage(
                messageText,
                messages,
                onMessagesUpdated = { messages = it },
                onInputCleared = { messageText = "" },
                coroutineScope = coroutineScope
            )
        }
    }

    // Scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Small bot avatar
                        Card(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp),
                            shape = CircleShape
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mainlogo),
                                contentDescription = "Assistant Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                "SafarMate Assistant",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Online",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
        ) {
            // Welcome banner with chat tips
            AnimatedVisibility(
                visible = messages.size <= 1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "How can I help you today?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Try asking about bus routes, ticket prices, or use the microphone to speak your question.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    // Add animation for new messages
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) +
                               slideInVertically(animationSpec = tween(300)) { it / 2 }
                    ) {
                        Column {
                            ChatBubble(message = message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Show typing indicator if the bot is "thinking"
                item {
                    AnimatedVisibility(
                        visible = messages.any { it.isTyping },
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Typing", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TypingDots()
                                }
                            }
                        }
                    }
                }
            }

            // Input field and buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Message input field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = {
                            Text(
                                "Type your question here...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    // Mic button with ripple effect
                    FilledIconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Handle error (no speech recognition available)
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Speech Recognition",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button with animation
                    val sendButtonColor = if (messageText.isBlank())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primary

                    FilledIconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                sendMessage(
                                    messageText,
                                    messages,
                                    onMessagesUpdated = { messages = it },
                                    onInputCleared = { messageText = "" },
                                    coroutineScope = coroutineScope
                                )
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = sendButtonColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = if (messageText.isBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isFromUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 160),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 320),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray.copy(alpha = alpha1)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray.copy(alpha = alpha2)))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray.copy(alpha = alpha3)))
    }
}


/**
 * Helper function to send a message and get a response
 */
private fun sendMessage(
    text: String,
    currentMessages: List<ChatMessage>,
    onMessagesUpdated: (List<ChatMessage>) -> Unit,
    onInputCleared: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    // Add user message
    val userMessage = ChatMessage(text = text, isFromUser = true)
    val updatedMessages = currentMessages + userMessage
    onMessagesUpdated(updatedMessages)
    onInputCleared()

    // Add typing indicator
    val typingMessage = ChatMessage(text = "", isFromUser = false, isTyping = true)
    onMessagesUpdated(updatedMessages + typingMessage)

    // Simulate bot thinking and responding
    // In a real app, this would be a network call to a chatbot service
    coroutineScope.launch {
        delay(1500) // Simulate network delay

        val botResponses = mapOf(
            "hi" to "Hello! How can I help you today? 👋",
            "hello" to "Hi there! I'm SafarMate assistant. How can I help you with your bus journey today? 🚌",
            "bus" to "You can find buses by entering your source and destination on the home screen. We update our schedules in real-time for your convenience.",
            "ticket" to "You can book tickets after selecting a route. We support UPI, cards, and digital wallets for hassle-free payments. Need help with a specific payment method?",
            "time" to "Bus timings depend on the route you select. Please check the ETA on the route map screen. We also send notifications before your bus arrives!",
            "help" to "I can help with bus information, routes, tickets, and general inquiries about using SafarMate. What would you like to know more about?",
            "route" to "You can view route details including stops, fare, and ETA on the route map screen. Our routes are optimized for efficiency and comfort.",
            "payment" to "We support UPI payments, credit/debit cards, and popular digital wallets. All transactions are secure and you'll receive an instant confirmation.",
            "cancel" to "To cancel a ticket, please go to your bookings section and select the ticket you wish to cancel. Cancellations made 1 hour before departure receive a full refund.",
            "refund" to "Refunds are processed within 5-7 business days depending on your payment method. You'll receive an email confirmation once the refund is initiated."
        )

        // Generate a response based on keywords in the message
        val userMessageLower = text.lowercase()
        val responseText = botResponses.entries.find { (key, _) ->
            userMessageLower.contains(key)
        }?.value ?: "I'm sorry, I don't understand that. You can ask for 'help' to see what I can do."

        val botMessage = ChatMessage(text = responseText, isFromUser = false)
        onMessagesUpdated(updatedMessages.filterNot { it.isTyping } + botMessage)
    }
}
