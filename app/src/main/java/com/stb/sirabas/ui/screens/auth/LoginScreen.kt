package com.stb.sirabas.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.screens.AccessColors
import com.stb.sirabas.ui.theme.SIRABASTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun LoginScreen(
    onDaftar: () -> Unit = {},
    onMasuk: (
        username: String,
        password: String
    ) -> Unit = { _, _ -> }
) {
    var username by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var tampilkanPassword by remember {
        mutableStateOf(false)
    }

    var sudahMencobaMasuk by remember {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    val usernameKosong =
        sudahMencobaMasuk && username.isBlank()

    val passwordKosong =
        sudahMencobaMasuk && password.isEmpty()

    val prosesMasuk: () -> Unit = {
        sudahMencobaMasuk = true

        if (
            username.isNotBlank() &&
            password.isNotEmpty()
        ) {
            focusManager.clearFocus()

            // Callback ini nanti dihubungkan ke autentikasi.
            // Mengisi form tidak otomatis berarti login berhasil.
            onMasuk(
                username.trim(),
                password
            )
        }
    }

    val warnaKolom = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccessColors.Green,
        unfocusedBorderColor = AccessColors.Border,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = AccessColors.Ink,
        unfocusedTextColor = AccessColors.Ink,
        focusedLabelColor = AccessColors.Green,
        unfocusedLabelColor = AccessColors.Muted,
        cursorColor = AccessColors.Green
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 24.dp,
                    vertical = 32.dp
                )
        ) {
            // IDENTITAS APLIKASI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.logo_stb
                    ),
                    contentDescription = "Logo PT Sri Tani Berkah",
                    modifier = Modifier.size(56.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SIRABAS",
                        color = AccessColors.Ink,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "PT Sri Tani Berkah",
                        color = AccessColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(44.dp)
            )

            Text(
                text = "Selamat datang",
                color = AccessColors.Ink,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Masuk untuk melanjutkan aktivitas " +
                        "Anda di SIRABAS.",
                color = AccessColors.Muted,
                fontSize = 15.sp,
                lineHeight = 23.sp
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            // USERNAME
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                        .trimStart()
                        .replaceFirstChar { huruf ->
                            huruf.uppercaseChar()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Username")
                },
                placeholder = {
                    Text("Masukkan username")
                },
                singleLine = true,
                isError = usernameKosong,
                shape = RoundedCornerShape(12.dp),
                colors = warnaKolom,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(
                            FocusDirection.Next
                        )
                    }
                )
            )

            if (usernameKosong) {
                Text(
                    text = "Username wajib diisi.",
                    modifier = Modifier.padding(
                        start = 4.dp,
                        top = 6.dp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // KATA SANDI
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Kata sandi")
                },
                placeholder = {
                    Text("Masukkan kata sandi")
                },
                singleLine = true,
                isError = passwordKosong,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = warnaKolom,
                visualTransformation = if (
                    tampilkanPassword
                ) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            tampilkanPassword =
                                !tampilkanPassword
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (tampilkanPassword) {
                                    R.drawable.sirabas_eye
                                } else {
                                    R.drawable.sirabas_eye_off
                                }
                            ),
                            contentDescription = if (
                                tampilkanPassword
                            ) {
                                "Sembunyikan kata sandi"
                            } else {
                                "Tampilkan kata sandi"
                            },
                            modifier = Modifier.size(22.dp),
                            tint = AccessColors.Green
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        prosesMasuk()
                    }
                )
            )

            if (passwordKosong) {
                Text(
                    text = "Kata sandi wajib diisi.",
                    modifier = Modifier.padding(
                        start = 4.dp,
                        top = 6.dp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // TOMBOL MASUK
            Button(
                onClick = prosesMasuk,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Masuk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // NAVIGASI KE REGISTER
            TextButton(
                onClick = onDaftar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Belum punya akun? Daftar",
                    color = AccessColors.Green,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun LoginScreenPreview() {
    SIRABASTheme {
        LoginScreen()
    }
}