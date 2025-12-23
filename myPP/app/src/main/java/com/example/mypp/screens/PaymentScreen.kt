package com.example.mypp.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypp.ui.theme.MyPPTheme

/**
 * Payment Screen with UPI integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    routeId: String?,
    amount: String?,
    onPaymentComplete: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var showPaymentSuccess by remember { mutableStateOf(false) }
    
    // UPI payment launcher using the activity result API
    val upiPaymentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Simulate successful payment for demo purposes
        showPaymentSuccess = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Make Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Payment Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bus Ticket Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Route: ${routeId ?: "Route1"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = "Amount: ₹${amount ?: "40.00"}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            
            // Payment Options
            Text(
                text = "Select Payment Method",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 16.dp)
            )
            
            // UPI Option
            PaymentMethodButton(
                title = "UPI Payment",
                description = "Pay using any UPI app",
                onClick = {
                    val upiId = "7266860224@naviaxis"      // Demo UPI ID
                    val name = "SafarMate Payments"
                    val payAmount = amount ?: "40.00"
                    val note = "Bus Ticket for Route ${routeId ?: "Route1"}"

                    val uri = Uri.parse("upi://pay?pa=$upiId&pn=$name&am=$payAmount&cu=INR&tn=$note")
                    val upiIntent = Intent(Intent.ACTION_VIEW, uri)

                    try {
                        upiPaymentLauncher.launch(upiIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            // Card Payment Option
            PaymentMethodButton(
                title = "Card Payment",
                description = "Pay using Credit/Debit Card",
                onClick = {
                    // Simulate successful payment for demo purposes
                    showPaymentSuccess = true
                }
            )
            
            // Wallet Payment Option
            PaymentMethodButton(
                title = "Wallet Payment",
                description = "Pay using Paytm/PhonePe/GooglePay",
                onClick = {
                    // Simulate successful payment for demo purposes
                    showPaymentSuccess = true
                }
            )
            
            if (showPaymentSuccess) {
                AlertDialog(
                    onDismissRequest = { 
                        showPaymentSuccess = false
                        onPaymentComplete()
                    },
                    title = { Text("Payment Successful") },
                    text = { Text("Your payment of ₹${amount ?: "40.00"} was successful. Your ticket has been booked.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPaymentSuccess = false
                                onPaymentComplete()
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    MyPPTheme {
        PaymentScreen(
            routeId = "Route1",
            amount = "40.00",
            onPaymentComplete = {},
            onBackPressed = {}
        )
    }
}