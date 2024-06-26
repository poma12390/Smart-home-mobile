package se.ifmo.ru.smartapp.ui.pages.login

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.VisualTransformation
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
import org.json.JSONException
import org.json.JSONObject
import se.ifmo.ru.smartapp.ui.pages.PageNames.MAIN_PAGE
import se.ifmo.ru.smartapp.ui.pages.PageNames.REGISTER_PAGE
import se.ifmo.ru.smartapp.ui.pages.PageUtils.Companion.moveToPage
import java.io.IOException


@Composable
fun LoginPage(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var canNavigate by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val client = OkHttpClient()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "not Smart Home",
                fontSize = 34.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 100.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Login form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Вход в аккаунт",
                    fontSize = 26.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    "Hello, hi, Whasap, bye",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim() },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.trim() },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        sendLoginRequest(username, password)
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Default.Visibility
                        else Icons.Default.VisibilityOff

                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )

                Button(
                    onClick = {
                        // Validate the inputs
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Поля не могут быть пустими"
                            return@Button
                        }

                        errorMessage = null
                        isLoading = true
                        val request = sendLoginRequest(username, password)

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                isLoading = false
                                errorMessage = "Ошибка сети"
                            }

                            override fun onResponse(call: Call, response: Response) {
                                isLoading = false
                                val body =
                                    response.body?.string() // Получаем тело ответа и преобразуем в строку

                                if (response.isSuccessful && body != null) {
                                    try {
                                        val token = JSONObject(body).getString("token")
                                        saveTokenToCache(context, token)
                                        if (canNavigate) {
                                            canNavigate = false
                                            moveToPage(coroutineScope, navController, MAIN_PAGE.pageName)
                                            canNavigate = true
                                        }
                                    } catch (e: JSONException) {
                                        errorMessage = "Failed to parse response"
                                    }
                                } else if (body != null) {
                                    errorMessage = try {
                                        JSONObject(body).getString("error")
                                    } catch (e: JSONException) {
                                        "Failed to parse error message"
                                    }
                                } else {
                                    errorMessage = "Страшная ошибка: ${response.code}"
                                }

                            }
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Вход", color = Color.White)
                    }
                }

                TextButton(onClick = {
                    if (canNavigate) {
                        canNavigate = false
                        moveToPage(coroutineScope, navController, REGISTER_PAGE.pageName)
                        canNavigate = true
                    }
                }) {
                    Text(
                        "Нету аккаунта? Присоединиться",
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }
    }
}

private fun sendLoginRequest(username: String, password: String): Request {
    val json = """
        {
            "username": "$username",
            "password": "$password"
        }
    """.trimIndent()

    val requestBody = json
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    return Request.Builder()
        .url("http://51.250.103.29:8080/api/auth/login")
        .post(requestBody)
        .build()
}

fun saveTokenToCache(context: Context, token: String) {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("auth_token", token)
        apply()
    }
}
