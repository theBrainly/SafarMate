package com.example.mypp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to handle user authentication
 */
class UserAuthViewModel : ViewModel() {

    // User data class
    data class User(
        val id: String,
        val name: String,
        val email: String,
        val role: String = "Passenger" // Default role
    )

    // Authentication state
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // Auth state flow
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Stored user for auto-login
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Selected role for registration
    private val _selectedRole = MutableStateFlow("Passenger")
    val selectedRole: StateFlow<String> = _selectedRole

    // Dummy database of users (in a real app, this would be in a repository)
    private val dummyUsers = mutableListOf(
        User("1", "Akash Singh", "akash@example.com"),
        User("2", "Vaishnav", "vaishnav@example.com"),
        User("3", "Demo User", "demo@safarmate.com")
    )

    // Track if user is logged in
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }

            _authState.value = AuthState.Loading

            // Simulate network delay
            kotlinx.coroutines.delay(1000)

            // Check if user exists in our dummy database
            val user = dummyUsers.find { it.email == email }
            
            if (user != null) {
                // In a real app, we would verify the password hash here
                _authState.value = AuthState.Success(user)
                _currentUser.value = user
                _isLoggedIn.value = true
            } else {
                _authState.value = AuthState.Error("Invalid email or password")
            }
        }
    }

    /**
     * Register a new user
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("All fields must be filled")
                return@launch
            }

            _authState.value = AuthState.Loading

            // Simulate network delay
            kotlinx.coroutines.delay(1000)

            // Check if user already exists
            if (dummyUsers.any { it.email == email }) {
                _authState.value = AuthState.Error("User with this email already exists")
                return@launch
            }

            // Create new user
            val newId = (dummyUsers.size + 1).toString()
            val newUser = User(newId, name, email, _selectedRole.value)
            dummyUsers.add(newUser)

            _authState.value = AuthState.Success(newUser)
            _currentUser.value = newUser
            _isLoggedIn.value = true
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
        _isLoggedIn.value = false
    }

    /**
     * Set the selected role for registration
     */
    fun setSelectedRole(role: String) {
        _selectedRole.value = role
    }

    /**
     * Sign in with Google (simplified for demo)
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Simulate network delay
            kotlinx.coroutines.delay(1500)
            
            // Create a dummy Google user
            val googleUser = User(
                id = "g-${dummyUsers.size + 1}",
                name = "Google User",
                email = "google.user@gmail.com",
                role = _selectedRole.value
            )
            
            dummyUsers.add(googleUser)
            _currentUser.value = googleUser
            _authState.value = AuthState.Success(googleUser)
            _isLoggedIn.value = true
        }
    }
}