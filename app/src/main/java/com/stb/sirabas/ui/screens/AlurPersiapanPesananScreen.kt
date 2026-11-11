package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun AlurPersiapanPesananScreen(
    daftarPesanan: List<NotifikasiPesananProduksiUi>,
    onPindaiKarung: (NotifikasiPesananProduksiUi) -> Unit,
    onPeriksaPengiriman: (NotifikasiPesananProduksiUi) -> Unit
) {
    var nomorTerpilih by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Ambil pesanan dari daftar terbaru agar angka pemindaian ikut berubah
    // ketika data pesanan diperbarui oleh layar pemanggil.
    val pesananTerpilih = daftarPesanan.firstOrNull {
        it.nomorPesanan == nomorTerpilih
    }

    BackHandler(enabled = pesananTerpilih != null) {
        nomorTerpilih = null
    }

    if (pesananTerpilih == null) {
        NotifikasiPesananProduksiScreen(
            daftarPesanan = daftarPesanan,
            onBukaPersiapan = { pesanan ->
                nomorTerpilih = pesanan.nomorPesanan
            }
        )
    } else {
        PersiapanPesananScreen(
            pesanan = pesananTerpilih,
            onKembali = { nomorTerpilih = null },
            onPindaiKarung = { onPindaiKarung(pesananTerpilih) },
            onPeriksaPengiriman = { onPeriksaPengiriman(pesananTerpilih) }
        )
    }
}

@Preview(
    name = "Produksi · Daftar ke persiapan pesanan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun AlurPersiapanPesananPreview() {
    val daftarPesanan = listOf(
        NotifikasiPesananProduksiUi(
            nomorPesanan = "PS-001",
            pelanggan = "MBG A",
            waktuPesanan = "11 November 2026 · 08.15",
            daftarItem = listOf(
                ItemPersiapanProduksiUi(
                    namaProduk = "Beras Premium",
                    kemasan = "5 kg",
                    jumlahPesanan = 50,
                    jumlahDipindai = 0
                )
            )
        ),
        NotifikasiPesananProduksiUi(
            nomorPesanan = "PS-002",
            pelanggan = "MBG B",
            waktuPesanan = "11 November 2026 · 09.30",
            daftarItem = listOf(
                ItemPersiapanProduksiUi(
                    namaProduk = "Beras Premium",
                    kemasan = "25 kg hijau",
                    jumlahPesanan = 2,
                    jumlahDipindai = 2
                )
            )
        )
    )

    SIRABASTheme {
        AlurPersiapanPesananScreen(
            daftarPesanan = daftarPesanan,
            onPindaiKarung = {},
            onPeriksaPengiriman = {}
        )
    }
}