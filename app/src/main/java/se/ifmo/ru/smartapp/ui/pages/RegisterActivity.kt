package se.ifmo.ru.smartapp.ui.pages

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage
import se.ifmo.ru.smartapp.ui.pages.PageNames.LOGIN_PAGE
import java.io.IOException

class MainActivity : ComponentActivity() {

}
@Composable
fun RegisterPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var canNavigate by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(onClick = {
                    if (canNavigate) {
                        moveToPage(coroutineScope, navController, LOGIN_PAGE.pageName)
                    }

                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f)) // для выравнивания заголовка и кнопки по бокам
                // Если у вас есть заголовок страницы, он может быть здесь
            }
            Text(
                "not Smart Home",
                fontSize = 34.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 50.dp, bottom = 16.dp)
            )
            Text(
                "Создать аккаунт",
                fontSize = 26.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Hello, hi, Whasap, bye",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Логин") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Повторите пароль") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // Handle done action
                })
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Пароли не совпадают"
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    val client = OkHttpClient()

                    val request = sendRegisterRequest(email, password)
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            isLoading = false
                            errorMessage = "Ошибка подключения"
                        }

                        override fun onResponse(call: Call, response: Response) {
                            isLoading = false
                            if (response.isSuccessful) {
                                moveToPage(coroutineScope, navController, LOGIN_PAGE.pageName)
                            } else {
                                errorMessage = "Произошла страшная ошибка: ${response.code}"
                            }
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Sign up", fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}

private fun sendRegisterRequest(email: String, password: String): Request {
    val json = """
        {
            "username": "$email",
            "password": "$password"
        }
    """.trimIndent()

    val requestBody = json
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    return Request.Builder()
        .url("http://51.250.103.29:8080/api/auth/register")
        .post(requestBody)
        .build()
}


