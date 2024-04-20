package se.ifmo.ru.smartapp.ui.pages

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


@Composable
fun LoginPage(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var canNavigate by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "not Smart Home",
                fontSize = 34.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 100.dp)
            )

            // Login form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    "Login Account",
                    fontSize = 26.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text("Hello, hi, Whasap, bye", fontSize = 16.sp, color = Color.Gray)

                OutlinedTextField(
                    value = username, // Используйте переменную состояния
                    onValueChange = { username = it }, // Обновляйте переменную состояния
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password, // Используйте переменную состояния
                    onValueChange = { password = it }, // Обновляйте переменную состояния
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        // Handle done action
                    })
                )


                Button(
                    onClick = {
                        Log.d("LoginActivity", "Sign In button clicked. Username: $username")
                        isLoading = true
                        val client = OkHttpClient()
                        val requestBody = FormBody.Builder()
                            .add("username", username)
                            .add("password", password)
                            .build()
                        val request = Request.Builder()
                            .url("http://51.250.103.29:8080/api/auth/login")
                            .post(requestBody)
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                isLoading = false
                                Log.e("LoginActivity", "Network call failed", e)
                                // Показываем Toast в UI потоке
                                // Обновите UI в главном потоке
                            }

                            override fun onResponse(call: Call, response: Response) {
                                isLoading = false
                                if (response.isSuccessful) {
                                    Log.d("LoginActivity", "Network call successful")
                                    // Предполагаем, что токен приходит в формате JSON { "token": "your_token_here" }
                                    // Обновите UI в главном потоке
                                } else {
                                    Log.e("LoginActivity", "Login failed: ${response.code}")
                                    // Показываем Toast в UI потоке
                                    // Обновите UI в главном потоке
                                }
                            }
                        })
                    },
                    enabled = !isLoading,
                    // ... остальные параметры кнопки
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Sign In")
                    }
                }


                val showRegisterPage = remember { mutableStateOf(false) }

                if (showRegisterPage.value) {
                    RegisterPage(navController)
                } else {
                    TextButton(onClick = {
                        if (canNavigate) {
                            coroutineScope.launch {
                                canNavigate = false
                                navController.navigate("register")
                                delay(500) // задержка в 500 мс перед следующим нажатием
                                canNavigate = true
                            }
                        }
                    }) {
                        Text("Don't have an account? Join Us", color = Color.Gray)
                    }
                }
            }
        }
    }
}

fun saveTokenToCache(context: Context, token: String) {
    // Здесь используйте DataStore или SharedPreferences для сохранения токена
}

