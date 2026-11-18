package com.stb.sirabas.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class MenuPanduanSirabas(
    val judul: String,
    val ikon: Int,
    val penjelasan: String
)

@Composable
internal fun NavigasiPanduanSirabas(
    username: String,
    peran: String,
    daftarMenu: List<MenuPanduanSirabas>,
    indeksAktif: Int,
    onPilihMenu: (Int) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val preferensi = remember(context) {
        context.getSharedPreferences("panduan_menu_sirabas", Context.MODE_PRIVATE)
    }

    val identitas = username.trim()
    val kunciAkun = "$peran:$identitas"
    val warna = MaterialTheme.colorScheme

    key(kunciAkun) {
        var langkah by rememberSaveable {
            mutableStateOf(
                if (identitas.isNotEmpty() && !preferensi.getBoolean(kunciAkun, false)) {
                    0
                } else {
                    -1
                }
            )
        }

        val panduan = daftarMenu.getOrNull(langkah)

        fun tutupPanduan() {
            preferensi.edit().putBoolean(kunciAkun, true).apply()
            langkah = -1
        }

        BackHandler(enabled = panduan != null) {
            if (langkah > 0) langkah-- else tutupPanduan()
        }

        Column {
            HorizontalDivider(color = warna.outlineVariant)

            if (panduan != null) {
                Surface(
                    color = warna.secondaryContainer,
                    contentColor = warna.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Panduan menu · ${langkah + 1} dari ${daftarMenu.size}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = panduan.judul,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = panduan.penjelasan,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { tutupPanduan() }) {
                                Text("Lewati")
                            }
                            Button(
                                onClick = {
                                    if (langkah == daftarMenu.lastIndex) {
                                        tutupPanduan()
                                    } else {
                                        langkah++
                                    }
                                }
                            ) {
                                Text(
                                    if (langkah == daftarMenu.lastIndex) "Selesai"
                                    else "Lanjut"
                                )
                            }
                        }
                    }
                }
            }

            NavigationBar(
                containerColor = warna.surface,
                contentColor = warna.onSurface,
                tonalElevation = 0.dp
            ) {
                daftarMenu.forEachIndexed { indeks, menu ->
                    val aktif = indeks == indeksAktif
                    val disorot = panduan != null && indeks == langkah

                    NavigationBarItem(
                        selected = aktif,
                        onClick = {
                            if (panduan != null) {
                                langkah = indeks
                            } else {
                                onPilihMenu(indeks)
                            }
                        },
                        modifier = if (disorot) {
                            Modifier.border(
                                width = 2.dp,
                                color = warna.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Modifier
                        },
                        icon = {
                            Icon(
                                painter = painterResource(menu.ikon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = menu.judul,
                                maxLines = 1,
                                fontSize = 12.sp,
                                fontWeight = if (aktif || disorot) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = warna.onSecondaryContainer,
                            selectedTextColor = warna.primary,
                            indicatorColor = warna.secondaryContainer,
                            unselectedIconColor = warna.onSurfaceVariant,
                            unselectedTextColor = warna.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}