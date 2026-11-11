package com.stb.sirabas.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val hijauTua = Color(0xFF173F35)
private val hijauTombol = Color(0xFF20664F)
private val hijauMuda = Color(0xFFE5EFE4)
private val latar = Color(0xFFF7F8F3)
private val teksSekunder = Color(0xFF50645A)

private data class FiturOnboarding(
    val judul: String,
    val deskripsi: String
)

private val fitur = listOf(
    FiturOnboarding(
        judul = "Dari bahan masuk hingga beras siap jual",
        deskripsi = "Petugas Produksi mencatat bahan dari supplier, " +
                "menimbang hasil utama dan hasil samping, lalu melihat stok di gudang."
    ),
    FiturOnboarding(
        judul = "Pindai setiap karung sebelum dimuat",
        deskripsi = "Petugas Produksi memindai label untuk mencocokkan " +
                "jenis beras dan kemasan dengan pesanan dari Penjualan."
    ),
    FiturOnboarding(
        judul = "Catat pesanan dan terbitkan invoice",
        deskripsi = "Petugas Penjualan mencatat pembayaran lunas atau piutang. " +
                "Untuk transfer bank, bukti pembayaran bisa dilampirkan sebelum " +
                "invoice disimpan atau dicetak."
    )
)

@Composable
fun OnboardingScreen(
    onSelesai: () -> Unit = {}
) {
    IsiOnboarding(
        halamanAwal = 0,
        onSelesai = onSelesai
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IsiOnboarding(
    halamanAwal: Int,
    onSelesai: () -> Unit
) {
    val pager = rememberPagerState(
        initialPage = halamanAwal,
        pageCount = { fitur.size }
    )
    val scope = rememberCoroutineScope()
    val halaman = pager.currentPage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(latar)
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_stb),
                contentDescription = "Logo PT Sri Tani Berkah",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "SIRABAS",
                color = hijauTua,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.weight(1f))

            if (halaman < fitur.lastIndex) {
                TextButton(onClick = onSelesai) {
                    Text("Lewati", color = hijauTombol)
                }
            }
        }

        HorizontalPager(
            state = pager,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { indeks ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                IlustrasiFitur(
                    halaman = indeks,
                    aktif = indeks == pager.settledPage
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    text = fitur[indeks].judul,
                    color = hijauTua,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = fitur[indeks].deskripsi,
                    color = teksSekunder,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.semantics {
                    contentDescription = "Halaman ${halaman + 1} dari ${fitur.size}"
                },
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(fitur.size) { indeks ->
                    val lebar by animateDpAsState(
                        targetValue = if (indeks == halaman) 26.dp else 8.dp
                    )

                    Box(
                        modifier = Modifier
                            .size(width = lebar, height = 8.dp)
                            .background(
                                color = if (indeks == halaman) hijauTombol else Color(0xFFCFDBCF),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    if (halaman == fitur.lastIndex) {
                        onSelesai()
                    } else {
                        scope.launch { pager.animateScrollToPage(halaman + 1) }
                    }
                },
                enabled = !pager.isScrollInProgress,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = hijauTombol,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (halaman == fitur.lastIndex) "Masuk" else "Lanjut",
                    fontSize = 16.sp
                )
            }

            TextButton(
                enabled = halaman > 0 && !pager.isScrollInProgress,
                onClick = {
                    scope.launch { pager.animateScrollToPage(halaman - 1) }
                }
            ) {
                Text("Sebelumnya")
            }
        }
    }
}

@Composable
private fun IlustrasiFitur(
    halaman: Int,
    aktif: Boolean
) {
    // Preview menampilkan keadaan akhir agar setiap halaman siap di-screenshot.
    val untukPreview = LocalInspectionMode.current
    val animasi = remember { Animatable(if (untukPreview) 1f else 0f) }

    LaunchedEffect(aktif, untukPreview) {
        if (!untukPreview) {
            animasi.snapTo(0f)
            if (aktif) {
                delay(350)
                animasi.animateTo(1f, animationSpec = tween(1700))
            }
        }
    }

    val progres = animasi.value
    val selesai = progres >= 0.96f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = hijauMuda
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (halaman) {
                0 -> {
                    Text("Produksi", color = hijauTua, fontWeight = FontWeight.Bold)

                    Crossfade(targetState = selesai) { sudahDikemas ->
                        KartuIlustrasi {
                            if (sudahDikemas) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.premium_25kg_hijau),
                                        contentDescription = "Kemasan Beras Premium 25 kg hijau",
                                        modifier = Modifier.size(68.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Column {
                                        Text("Beras Premium", fontWeight = FontWeight.Bold)
                                        Text("25 kg hijau", color = teksSekunder)
                                    }
                                }
                            } else {
                                Text("Beras bahan baku", fontWeight = FontWeight.Bold)
                                Text("Lot BM/025 · 200 kg diolah", color = teksSekunder)
                            }
                        }
                    }

                    Text(
                        text = if (selesai) "Stok bertambah 7 karung · 175 kg"
                        else "Bahan masuk ke proses produksi",
                        color = hijauTua,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                1 -> {
                    Text("Persiapan pesanan", color = hijauTua, fontWeight = FontWeight.Bold)

                    KartuIlustrasi {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LabelBergerak(progres)
                            Column {
                                Text("MBG A · PS-001", fontWeight = FontWeight.Bold)
                                Text("Beras Premium · 25 kg", color = teksSekunder)
                                Text("PR001-K001", color = hijauTombol)
                            }
                        }
                    }

                    Text(
                        text = if (selesai) "1 dari 50 karung sesuai pesanan"
                        else "0 dari 50 karung dipindai",
                        color = hijauTua,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                2 -> {
                    Text("Penjualan", color = hijauTua, fontWeight = FontWeight.Bold)

                    Crossfade(targetState = progres >= 0.5f) { jadiInvoice ->
                        KartuIlustrasi {
                            if (jadiInvoice) {
                                Text("Invoice · MBG A", fontWeight = FontWeight.Bold)
                                Text("Rp700.000 · Transfer Bank", color = teksSekunder)
                            } else {
                                Text("Pesanan MBG A", fontWeight = FontWeight.Bold)
                                Text("2 karung × Rp350.000", color = teksSekunder)
                            }
                        }
                    }

                    Text(
                        text = if (selesai) "✓  Bukti transfer terlampir"
                        else "Total pesanan Rp700.000",
                        color = hijauTua,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun KartuIlustrasi(isi: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        contentColor = hijauTua
    ) {
        Column(
            modifier = Modifier.heightIn(min = 100.dp).padding(14.dp),
            verticalArrangement = Arrangement.Center,
            content = isi
        )
    }
}

@Composable
private fun LabelBergerak(progres: Float) {
    // Gambar ilustrasi label untuk onboarding.
    Canvas(
        modifier = Modifier
            .size(66.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        val unit = size.minDimension / 15f

        fun kotak(x: Int, y: Int, sisi: Int, warna: Color) {
            drawRect(
                color = warna,
                topLeft = Offset(x * unit, y * unit),
                size = Size(sisi * unit, sisi * unit)
            )
        }

        for ((x, y) in listOf(0 to 0, 10 to 0, 0 to 10)) {
            kotak(x, y, 5, hijauTua)
            kotak(x + 1, y + 1, 3, Color.White)
            kotak(x + 2, y + 2, 1, hijauTua)
        }

        for ((x, y) in listOf(
            6 to 1, 7 to 4, 5 to 6, 9 to 6, 1 to 7,
            3 to 8, 7 to 9, 10 to 10, 13 to 9, 9 to 13, 13 to 13
        )) {
            kotak(x, y, 2, hijauTua)
        }

        if (progres < 0.96f) {
            val y = size.height * progres
            drawLine(
                color = Color(0xFFD8A63D),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Preview(name = "01 · Produksi", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreenPreview() {
    SIRABASTheme { IsiOnboarding(halamanAwal = 0, onSelesai = {}) }
}

@Preview(name = "02 · Pelabelan", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingLabelPreview() {
    SIRABASTheme { IsiOnboarding(halamanAwal = 1, onSelesai = {}) }
}

@Preview(name = "03 · Penjualan", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPenjualanPreview() {
    SIRABASTheme { IsiOnboarding(halamanAwal = 2, onSelesai = {}) }
}