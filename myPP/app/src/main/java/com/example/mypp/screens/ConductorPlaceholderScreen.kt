package com.example.mypp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypp.ui.theme.MyPPTheme

/**
 * Placeholder screen for the Conductor Journey
 * This will be replaced with full implementation later
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConductorPlaceholderScreen(
    onBackToLoginClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conductor Mode") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Conductor Journey Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "This feature will be implemented soon. As a conductor, you'll be able to:" +
                        "\n• Start and end bus journeys" +
                        "\n• Track passenger count" +
                        "\n• Validate tickets" +
                        "\n• Report issues" +
                        "\n• Update bus location",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onBackToLoginClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConductorPlaceholderScreenPreview() {
    MyPPTheme {
        ConductorPlaceholderScreen(onBackToLoginClicked = {})
    }
}