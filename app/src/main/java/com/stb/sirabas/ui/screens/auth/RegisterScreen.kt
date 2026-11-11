package com.stb.sirabas.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.screens.AccessColors
import com.stb.sirabas.ui.screens.AccessPage
import com.stb.sirabas.ui.theme.SIRABASTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun RegisterScreen(
    onMasuk: () -> Unit = {},
    onPendaftaranDiajukan: (
        nama: String,
        username: String,
        whatsapp: String
    ) -> Unit = { _, _, _ -> }
) {
    var nama by rememberSaveable {
        mutableStateOf("")
    }

    var username by rememberSaveable {
        mutableStateOf("")
    }

    var whatsapp by rememberSaveable {
        mutableStateOf("")
    }

    // Kata sandi tidak disimpan dalam saved state.
    var password by remember {
        mutableStateOf("")
    }

    var konfirmasi by remember {
        mutableStateOf("")
    }

    var lihatPassword by remember {
        mutableStateOf(false)
    }

    var lihatKonfirmasi by remember {
        mutableStateOf(false)
    }

    var pernahDikirim by remember {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    // VALIDASI NAMA
    val namaError = when {
        nama.isBlank() ->
            "Nama lengkap wajib diisi."

        !nama.trim().first().isLetter() ->
            "Nama harus diawali huruf."

        else -> null
    }

    // VALIDASI USERNAME
    val usernameError = when {
        username.isBlank() ->
            "Username wajib diisi."

        !username.first().isLetter() ->
            "Username harus diawali huruf."

        username.any {
            !it.isLetterOrDigit() && it != '_'
        } ->
            "Gunakan huruf, angka, atau garis bawah; tanpa spasi."

        else -> null
    }

    // VALIDASI WHATSAPP
    val whatsappNormal = normalisasiWhatsApp(whatsapp)

    val whatsappError = if (whatsappNormal == null) {
        "Masukkan nomor Indonesia: 08…, 628…, atau +628…."
    } else {
        null
    }

    // VALIDASI KATA SANDI
    val passwordValid =
        password.length >= 8 &&
                password.any { it.isLetter() } &&
                password.any { it in '0'..'9' }

    val passwordError = if (!passwordValid) {
        "Minimal 8 karakter, mengandung huruf dan angka."
    } else {
        null
    }

    val konfirmasiError = when {
        konfirmasi.isEmpty() ->
            "Ulangi kata sandi."

        konfirmasi != password ->
            "Konfirmasi kata sandi belum sama."

        else -> null
    }

    val formValid =
        namaError == null &&
                usernameError == null &&
                whatsappNormal != null &&
                passwordValid &&
                konfirmasiError == null

    // AKSI TOMBOL PENDAFTARAN
    val ajukan: () -> Unit = {
        pernahDikirim = true

        if (formValid && whatsappNormal != null) {
            focusManager.clearFocus()

            password = ""
            konfirmasi = ""

            // SIMULASI:
            // Belum mengirim data ke API atau membuat akun.
            onPendaftaranDiajukan(
                kapitalAwal(nama.trim()),
                kapitalAwal(username.trim()),
                whatsappNormal
            )
        }
    }

    AccessPage(
        title = "Buat akun",
        onBack = onMasuk
    ) {
        Text(
            text = "Daftarkan identitas Anda sebagai staf " +
                    "PT Sri Tani Berkah. Akses diberikan setelah " +
                    "aktivasi oleh Admin.",
            color = AccessColors.Muted,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        RegistrationField(
            label = "Nama lengkap",
            value = nama,
            onValueChange = {
                nama = kapitalAwal(it.trimStart())
            },
            placeholder = "Contoh: Budi Santoso",
            error = namaError.takeIf {
                pernahDikirim || nama.isNotEmpty()
            },
            helper = "Huruf pertama otomatis kapital.",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )

        RegistrationField(
            label = "Username",
            value = username,
            onValueChange = {
                username = kapitalAwal(it.trimStart())
            },
            placeholder = "Contoh: Budi_01",
            error = usernameError.takeIf {
                pernahDikirim || username.isNotEmpty()
            },
            helper = "Awalan kapital; gunakan huruf, angka, " +
                    "atau garis bawah.",
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )

        RegistrationField(
            label = "Nomor WhatsApp",
            value = whatsapp,
            onValueChange = {
                whatsapp = it
            },
            placeholder = "Contoh: 081234567890",
            error = whatsappError.takeIf {
                pernahDikirim
            },
            helper = "Gunakan nomor WhatsApp aktif untuk " +
                    "pemberitahuan aktivasi.",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Perusahaan",
                fontWeight = FontWeight.SemiBold,
                color = AccessColors.Ink,
                fontSize = 14.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AccessColors.SoftGreen,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "PT Sri Tani Berkah",
                    modifier = Modifier.padding(16.dp),
                    color = AccessColors.Ink
                )
            }
        }

        RegistrationField(
            label = "Kata sandi",
            value = password,
            onValueChange = {
                password = it
            },
            placeholder = "Masukkan kata sandi",
            error = passwordError.takeIf {
                pernahDikirim
            },
            helper = if (passwordValid) {
                "Syarat huruf, angka, dan panjang sudah terpenuhi."
            } else {
                "Minimal 8 karakter dengan huruf dan angka. " +
                        "Simbol boleh digunakan."
            },
            visualTransformation = if (lihatPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            trailingIcon = {
                PasswordEye(
                    visible = lihatPassword,
                    fieldName = "kata sandi",
                    onClick = {
                        lihatPassword = !lihatPassword
                    }
                )
            }
        )

        RegistrationField(
            label = "Konfirmasi kata sandi",
            value = konfirmasi,
            onValueChange = {
                konfirmasi = it
            },
            placeholder = "Ulangi kata sandi",
            error = konfirmasiError.takeIf {
                pernahDikirim || konfirmasi.isNotEmpty()
            },
            visualTransformation = if (lihatKonfirmasi) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    ajukan()
                }
            ),
            trailingIcon = {
                PasswordEye(
                    visible = lihatKonfirmasi,
                    fieldName = "konfirmasi kata sandi",
                    onClick = {
                        lihatKonfirmasi = !lihatKonfirmasi
                    }
                )
            }
        )

        Button(
            onClick = ajukan,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Ajukan pendaftaran",
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onMasuk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sudah punya akun? Masuk",
                color = AccessColors.Green
            )
        }
    }
}

// KOMPONEN KOLOM INPUT
@Composable
private fun RegistrationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    helper: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation =
        VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = AccessColors.Ink,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                },
            placeholder = {
                Text(placeholder)
            },
            singleLine = true,
            isError = error != null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccessColors.Green,
                unfocusedBorderColor = AccessColors.Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = AccessColors.Ink,
                unfocusedTextColor = AccessColors.Ink,
                cursorColor = AccessColors.Green,
                focusedLabelColor = AccessColors.Green
            )
        )

        val explanation = error ?: helper

        if (explanation != null) {
            Text(
                text = explanation,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    AccessColors.Muted
                }
            )
        }
    }
}

// TOMBOL MATA
@Composable
private fun PasswordEye(
    visible: Boolean,
    fieldName: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(
                if (visible) {
                    R.drawable.sirabas_eye
                } else {
                    R.drawable.sirabas_eye_off
                }
            ),
            contentDescription = if (visible) {
                "Sembunyikan $fieldName"
            } else {
                "Tampilkan $fieldName"
            },
            modifier = Modifier.size(22.dp),
            tint = AccessColors.Green
        )
    }
}

// MENGUBAH HURUF PERTAMA MENJADI KAPITAL
private fun kapitalAwal(value: String): String {
    return value.replaceFirstChar {
        it.uppercaseChar()
    }
}

// MEMERIKSA FORMAT NOMOR, BUKAN STATUS WHATSAPP-NYA
private fun normalisasiWhatsApp(value: String): String? {
    val compact = value.filterNot {
        it.isWhitespace() ||
                it == '-' ||
                it == '(' ||
                it == ')'
    }

    val digits = when {
        compact.startsWith("+62") -> compact.drop(1)
        compact.startsWith("62") -> compact
        compact.startsWith("08") -> "62" + compact.drop(1)
        else -> return null
    }

    return if (
        Regex("628[0-9]{8,11}").matches(digits)
    ) {
        "+$digits"
    } else {
        null
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun RegisterScreenPreview() {
    SIRABASTheme {
        RegisterScreen()
    }
}