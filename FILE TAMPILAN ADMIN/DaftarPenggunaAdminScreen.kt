package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

data class PenggunaAktifAdminUi(
    val id: String,
    val nama: String,
    val username: String,
    val role: String
)

@Composable
fun DaftarPenggunaAdminScreen(
    daftarPengguna: List<PenggunaAktifAdminUi>
) {
    var pencarian by rememberSaveable { mutableStateOf("") }
    var filterRole by rememberSaveable { mutableStateOf("Semua") }
    var menuTerbuka by remember { mutableStateOf(false) }

    val pilihanRole = listOf(
        "Semua",
        "Admin",
        "Petugas Produksi",
        "Petugas Penjualan"
    )

    val kataKunci = pencarian.trim().removePrefix("@")

    val penggunaTampil = daftarPengguna.filter { pengguna ->
        val cocokDenganPencarian =
            pengguna.nama.contains(kataKunci, ignoreCase = true) ||
                    pengguna.username.contains(kataKunci, ignoreCase = true)

        val cocokDenganRole =
            filterRole == "Semua" || pengguna.role == filterRole

        cocokDenganPencarian && cocokDenganRole
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Daftar pengguna",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = pencarian,
                onValueChange = { pencarian = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari nama atau username") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Box {
                OutlinedButton(
                    onClick = { menuTerbuka = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (filterRole == "Semua") {
                                "Semua role"
                            } else {
                                filterRole
                            },
                            color = AccessColors.Ink
                        )
                        Text("▾", color = AccessColors.Green)
                    }
                }

                DropdownMenu(
                    expanded = menuTerbuka,
                    onDismissRequest = { menuTerbuka = false }
                ) {
                    pilihanRole.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Text(if (role == "Semua") "Semua role" else role)
                            },
                            onClick = {
                                filterRole = role
                                menuTerbuka = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "${penggunaTampil.size} pengguna aktif",
                color = AccessColors.Muted,
                fontSize = 14.sp
            )
        }

        if (penggunaTampil.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Text(
                        text = if (daftarPengguna.isEmpty()) {
                            "Belum ada pengguna aktif."
                        } else {
                            "Tidak ada pengguna yang cocok dengan pencarian atau role ini."
                        },
                        modifier = Modifier.padding(18.dp),
                        color = AccessColors.Muted
                    )
                }
            }
        }

        items(penggunaTampil, key = { it.id }) { pengguna ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, AccessColors.Border
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pengguna.nama,
                                color = AccessColors.Ink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${pengguna.username}",
                                color = AccessColors.Muted,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE2F2E8)
                        ) {
                            Text(
                                text = "Aktif",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                color = AccessColors.Green,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = pengguna.role,
                        color = AccessColors.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Admin · Daftar pengguna aktif",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun DaftarPenggunaAdminPreview() {
    val pengguna = listOf(
        PenggunaAktifAdminUi(
            id = "USR-001",
            nama = "Admin STB",
            username = "AdminSTB",
            role = "Admin"
        ),
        PenggunaAktifAdminUi(
            id = "USR-002",
            nama = "Rina Putri",
            username = "RinaPutri",
            role = "Petugas Penjualan"
        ),
        PenggunaAktifAdminUi(
            id = "USR-003",
            nama = "Dedi Saputra",
            username = "DediSaputra",
            role = "Petugas Produksi"
        )
    )

    SIRABASTheme {
        DaftarPenggunaAdminScreen(daftarPengguna = pengguna)
    }
}