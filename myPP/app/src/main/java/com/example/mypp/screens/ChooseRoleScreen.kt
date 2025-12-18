package com.example.mypp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypp.R
import com.example.mypp.ui.theme.MyPPTheme
import com.example.mypp.viewmodels.UserAuthViewModel

@Composable
fun ChooseRoleScreen(
    onContinueClicked: () -> Unit,
    userAuthViewModel: UserAuthViewModel = viewModel()
) {
    var selectedRole by remember { mutableStateOf<String?>("Passenger") }

    // Set the selected role in the view model whenever it changes
    LaunchedEffect(selectedRole) {
        selectedRole?.let { userAuthViewModel.setSelectedRole(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Select your role to continue",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoleCard(
                role = "Passenger",
                imageRes = R.drawable.user_sample_2,
                isSelected = selectedRole == "Passenger",
                onRoleSelected = { selectedRole = it }
            )

            RoleCard(
                role = "Conductor",
                imageRes = R.drawable.user_sample,
                isSelected = selectedRole == "Conductor",
                onRoleSelected = { selectedRole = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                selectedRole?.let {
                    userAuthViewModel.setSelectedRole(it)
                    onContinueClicked()
                }
            },
            enabled = selectedRole != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }
    }
}

@Composable
fun RoleCard(
    role: String,
    imageRes: Int,
    isSelected: Boolean,
    onRoleSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .size(160.dp)
            .clickable { onRoleSelected(role) }
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "$role image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = role,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseRoleScreenPreview() {
    MyPPTheme {
        ChooseRoleScreen(onContinueClicked = {})
    }
}