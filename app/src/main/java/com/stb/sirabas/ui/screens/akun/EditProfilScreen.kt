package com.stb.sirabas.ui.screens.akun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun EditProfilScreen(
    profil: ProfilAkunUi,
    onKembali: () -> Unit,
    onSimpan: (ProfilAkunUi) -> Unit
) {
    var nama by rememberSaveable(profil) {
        mutableStateOf(profil.namaLengkap)
    }
    var username by rememberSaveable(profil) {
        mutableStateOf(profil.username)
    }
    var nomorWhatsApp by rememberSaveable(profil) {
        mutableStateOf(profil.nomorWhatsApp)
    }

    val namaBersih = nama.trim()
    val usernameBersih = username.trim()
    val nomorBersih = nomorWhatsApp.trim()

    val namaValid = namaBersih.firstOrNull()?.isUpperCase() == true
    val usernameValid = usernameBersih.firstOrNull()?.isUpperCase() == true
    val nomorValid = nomorBersih.matches(Regex("""\+?[0-9]{8,15}"""))

    val adaPerubahan = namaBersih != profil.namaLengkap ||
            usernameBersih != profil.username ||
            nomorBersih != profil.nomorWhatsApp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke akun")
        }

        Text(
            text = "Edit profil",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = profil.role,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama lengkap") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            isError = nama.isNotBlank() && !namaValid,
            supportingText = {
                if (nama.isNotBlank() && !namaValid) {
                    Text("Awali nama lengkap dengan huruf kapital.")
                }
            }
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            isError = username.isNotBlank() && !usernameValid,
            supportingText = {
                if (username.isNotBlank() && !usernameValid) {
                    Text("Awali username dengan huruf kapital.")
                }
            }
        )

        OutlinedTextField(
            value = nomorWhatsApp,
            onValueChange = { nomorWhatsApp = it },
            label = { Text("Nomor WhatsApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            isError = nomorWhatsApp.isNotBlank() && !nomorValid,
            supportingText = {
                if (nomorWhatsApp.isNotBlank() && !nomorValid) {
                    Text("Gunakan 8–15 angka. Awalan + boleh digunakan.")
                }
            }
        )

        Button(
            onClick = {
                onSimpan(
                    profil.copy(
                        namaLengkap = namaBersih,
                        username = usernameBersih,
                        nomorWhatsApp = nomorBersih
                    )
                )
            },
            enabled = adaPerubahan && namaValid && usernameValid && nomorValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simpan perubahan")
        }
    }
}

@Preview(
    name = "Akun · Edit profil",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun EditProfilPreview() {
    SIRABASTheme {
        EditProfilScreen(
            profil = ProfilAkunUi(
                namaLengkap = "Dedi",
                username = "Dedi",
                nomorWhatsApp = "081234560000",
                role = "Petugas Produksi",
                namaPerusahaan = "PT. Sri Tani Berkah",
                alamatPerusahaan = "Belum diisi",
                teleponPerusahaan = "Belum diisi"
            ),
            onKembali = {},
            onSimpan = {}
        )
    }
}