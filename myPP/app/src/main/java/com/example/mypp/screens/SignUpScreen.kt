package com.example.mypp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypp.ui.theme.MyPPTheme
import com.example.mypp.viewmodels.UserAuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClicked: () -> Unit,
    userAuthViewModel: UserAuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Password match error state
    var passwordsMatch by remember { mutableStateOf(true) }
    
    // Collect auth state and role
    val authState by userAuthViewModel.authState.collectAsState()
    val selectedRole by userAuthViewModel.selectedRole.collectAsState()
    
    // Show a snackbar for auth errors or success
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Effect to handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is UserAuthViewModel.AuthState.Success -> {
                snackbarHostState.showSnackbar("Account created successfully!")
                onSignUpSuccess()
            }
            is UserAuthViewModel.AuthState.Error -> {
                snackbarHostState.showSnackbar((authState as UserAuthViewModel.AuthState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 48.dp)
            )
            
            Text(
                text = "Welcome to SafarMate",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordsMatch = it == confirmPassword || confirmPassword.isEmpty()
                },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = !passwordsMatch,
                singleLine = true
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    passwordsMatch = password == it || it.isEmpty()
                },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = !passwordsMatch,
                singleLine = true
            )
            
            if (!passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Text(
                text = "Account type: $selectedRole",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = { 
                    if (password == confirmPassword) {
                        userAuthViewModel.register(name, email, password) 
                    } else {
                        passwordsMatch = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                enabled = name.isNotBlank() && 
                         email.isNotBlank() && 
                         password.isNotBlank() && 
                         confirmPassword.isNotBlank() && 
                         passwordsMatch &&
                         authState !is UserAuthViewModel.AuthState.Loading
            ) {
                if (authState is UserAuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Sign Up")
            }

            Text(
                text = "OR",
                modifier = Modifier.padding(vertical = 24.dp)
            )

            OutlinedButton(
                onClick = { userAuthViewModel.signInWithGoogle() },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is UserAuthViewModel.AuthState.Loading
            ) {
                Text("Continue with Google")
            }

            Spacer(modifier = Modifier.weight(1f))

            ClickableText(
                text = AnnotatedString("Already have an account? Log in here"),
                onClick = { onLoginClicked() },
                modifier = Modifier.padding(bottom = 40.dp),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MyPPTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onLoginClicked = {}
        )
    }
}