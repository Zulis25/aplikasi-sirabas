package com.stb.sirabas.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

internal data class MenuPanduanSirabas(
    val judul: String,
    val ikon: Int,
    val penjelasan: String
)

private const val UNDANGAN_TUR = -2
private const val TUR_SELESAI = -1

private object PosisiTurSirabas : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}

@Composable
internal fun NavigasiPanduanSirabas(
    username: String,
    peran: String,
    daftarMenu: List<MenuPanduanSirabas>,
    indeksAktif: Int,
    onPilihMenu: (Int) -> Unit
) {
    val warna = MaterialTheme.colorScheme
    val posisiMenu = remember { mutableStateMapOf<Int, Rect>() }

    Column {
        HorizontalDivider(color = warna.outlineVariant)

        NavigationBar(
            containerColor = warna.surface,
            contentColor = warna.onSurface,
            tonalElevation = 0.dp
        ) {
            daftarMenu.forEachIndexed { indeks, menu ->
                val aktif = indeks == indeksAktif

                NavigationBarItem(
                    selected = aktif,
                    onClick = { onPilihMenu(indeks) },
                    modifier = Modifier.onGloballyPositioned { koordinat ->
                        val awal = koordinat.localToScreen(Offset.Zero)

                        if (awal.x.isFinite() && awal.y.isFinite()) {
                            posisiMenu[indeks] = Rect(
                                left = awal.x,
                                top = awal.y,
                                right = awal.x + koordinat.size.width,
                                bottom = awal.y + koordinat.size.height
                            )
                        }
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
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            fontWeight = if (aktif) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
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

    val identitas = username.trim()

    if (identitas.isNotEmpty() && daftarMenu.isNotEmpty()) {
        val kunciAkun = "$peran:$identitas"

        key(kunciAkun) {
            PanduanUntukAkun(
                kunciAkun = kunciAkun,
                peran = peran,
                daftarMenu = daftarMenu,
                posisiMenu = posisiMenu,
                indeksAktif = indeksAktif,
                onPilihMenu = onPilihMenu
            )
        }
    }
}

@Composable
private fun PanduanUntukAkun(
    kunciAkun: String,
    peran: String,
    daftarMenu: List<MenuPanduanSirabas>,
    posisiMenu: Map<Int, Rect>,
    indeksAktif: Int,
    onPilihMenu: (Int) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val preferensi = remember(context) {
        context.getSharedPreferences(
            "panduan_menu_sirabas",
            Context.MODE_PRIVATE
        )
    }

    var langkah by rememberSaveable {
        mutableStateOf(
            if (preferensi.getBoolean(kunciAkun, false)) {
                TUR_SELESAI
            } else {
                UNDANGAN_TUR
            }
        )
    }

    val pilihMenuTerbaru by rememberUpdatedState(onPilihMenu)

    // Saat langkah berubah, buka halaman asli yang sedang dijelaskan.
    LaunchedEffect(langkah, indeksAktif) {
        if (langkah in daftarMenu.indices && langkah != indeksAktif) {
            pilihMenuTerbaru(langkah)
        }
    }

    val selesai: () -> Unit = {
        preferensi.edit().putBoolean(kunciAkun, true).apply()
        langkah = TUR_SELESAI

        // Sesudah tur, petugas mulai bekerja dari Beranda.
        if (indeksAktif != 0) {
            pilihMenuTerbaru(0)
        }
    }

    if (langkah == UNDANGAN_TUR) {
        val namaPeran = if (peran == "produksi") "Produksi" else "Penjualan"

        AlertDialog(
            onDismissRequest = selesai,
            title = { Text("Kenali menu SIRABAS") },
            text = {
                Text(
                    "Lihat cara menggunakan menu Petugas $namaPeran. " +
                            "Setiap langkah akan membuka halaman yang dijelaskan."
                )
            },
            confirmButton = {
                Button(onClick = { langkah = 0 }) {
                    Text("Mulai tur")
                }
            },
            dismissButton = {
                TextButton(onClick = selesai) {
                    Text("Lewati")
                }
            }
        )
    } else {
        val menu = daftarMenu.getOrNull(langkah)
        val target = posisiMenu[langkah]

        if (menu != null && target != null && indeksAktif == langkah) {
            val terakhir = langkah == daftarMenu.lastIndex

            SorotanMenuSirabas(
                menu = menu,
                target = target,
                langkah = langkah,
                jumlahMenu = daftarMenu.size,
                terakhir = terakhir,
                onKembali = {
                    langkah = if (langkah > 0) langkah - 1 else UNDANGAN_TUR
                },
                onLanjut = {
                    if (terakhir) selesai() else langkah++
                },
                onLewati = selesai
            )
        }
    }
}

@Composable
private fun SorotanMenuSirabas(
    menu: MenuPanduanSirabas,
    target: Rect,
    langkah: Int,
    jumlahMenu: Int,
    terakhir: Boolean,
    onKembali: () -> Unit,
    onLanjut: () -> Unit,
    onLewati: () -> Unit
) {
    val warna = MaterialTheme.colorScheme
    val density = LocalDensity.current

    Popup(
        popupPositionProvider = PosisiTurSirabas,
        onDismissRequest = onKembali,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        var asalPopup by remember { mutableStateOf<Offset?>(null) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { koordinat ->
                    val posisi = koordinat.localToScreen(Offset.Zero)

                    if (posisi.x.isFinite() && posisi.y.isFinite()) {
                        asalPopup = posisi
                    }
                }
        ) {
            val asal = asalPopup

            if (asal != null) {
                val sorotan = target.translate(-asal.x, -asal.y)
                val jarakBawah = with(density) {
                    (maxHeight - sorotan.top.toDp() + 8.dp)
                        .coerceIn(0.dp, maxHeight)
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val batasNavigasi = sorotan.top.coerceIn(0f, size.height)

                    // Dashboard tetap terlihat selama petugas membaca panduan.
                    drawRect(
                        color = Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset.Zero,
                        size = Size(size.width, batasNavigasi)
                    )

                    // Sisakan area terang tepat di posisi menu yang dijelaskan.
                    val lapisanNavigasi = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(
                            Rect(
                                left = 0f,
                                top = batasNavigasi,
                                right = size.width,
                                bottom = size.height
                            )
                        )
                        addRoundRect(
                            RoundRect(sorotan, CornerRadius(14.dp.toPx()))
                        )
                    }

                    drawPath(
                        path = lapisanNavigasi,
                        color = Color.Black.copy(alpha = 0.68f)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = jarakBawah)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = warna.surface,
                    contentColor = warna.onSurface,
                    shadowElevation = 4.dp
                ) {
                    key(langkah) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Panduan SIRABAS · ${langkah + 1} dari $jumlahMenu",
                                color = warna.primary,
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                text = menu.judul,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = menu.penjelasan,
                                style = MaterialTheme.typography.bodyMedium,
                                color = warna.onSurfaceVariant
                            )

                            Button(
                                onClick = onLanjut,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (terakhir) "Selesai" else "Lanjut")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = onKembali) {
                                    Text("Kembali")
                                }

                                TextButton(onClick = onLewati) {
                                    Text("Lewati")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}