package com.stb.sirabas.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.screens.AccessCard
import com.stb.sirabas.ui.screens.AccessColors
import com.stb.sirabas.ui.screens.AccessPage
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun PendingActivationScreen(
    nama: String,
    username: String,
    whatsapp: String,
    statusAktivasi: String = "MENUNGGU_AKTIVASI", // "MENUNGGU_AKTIVASI", "AKTIF", "NON_AKTIF"
    onCekStatus: () -> Unit = {},
    onMasuk: () -> Unit = {}
) {
    val statusClean = statusAktivasi.uppercase().trim()
    val isAktif = statusClean == "AKTIF"
    val isNonAktif = statusClean == "NON_AKTIF" || statusClean == "TIDAK_AKTIF" || statusClean == "DITOLAK"

    val badgeText = when {
        isAktif -> "Aktif"
        isNonAktif -> "Non-aktif"
        else -> "Menunggu aktivasi"
    }

    val badgeBg = when {
        isAktif -> Color(0xFFE8F5E9)
        isNonAktif -> Color(0xFFFFEBEE)
        else -> AccessColors.SoftGold
    }

    val badgeColor = when {
        isAktif -> Color(0xFF2E7D32)
        isNonAktif -> Color(0xFFC62828)
        else -> AccessColors.GoldInk
    }

    val titleText = when {
        isAktif -> "Aktivasi berhasil"
        isNonAktif -> "Akun non-aktif"
        else -> "Menunggu aktivasi"
    }

    val descriptionText = when {
        isAktif -> "Akun Anda telah disetujui dan diaktifkan oleh Admin. Silakan tekan tombol di bawah untuk masuk ke aplikasi."
        isNonAktif -> "Akun Anda sedang dinonaktifkan atau belum disetujui oleh Admin. Silakan hubungi pengelola sistem untuk informasi lebih lanjut."
        else -> "Akun Anda sedang menunggu aktivasi oleh Admin. Pemberitahuan akan dikirim ke nomor WhatsApp yang Anda daftarkan setelah akun aktif."
    }

    AccessPage(
        title = "Status akun",
        onBack = onMasuk,
        headerTetap = true
    ) {
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // IKON DAN PEMBERITAHUAN UTAMA
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAktif) {
                        Text(
                            text = "✓",
                            color = badgeColor,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.sirabas_clock),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = badgeColor
                        )
                    }
                }
            }

            Text(
                text = titleText,
                color = AccessColors.Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = descriptionText,
                modifier = Modifier.padding(
                    horizontal = 8.dp
                ),
                color = AccessColors.Muted,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        // IDENTITAS AKUN
        AccessCard {
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    color = badgeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = nama,
                color = AccessColors.Ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = AccessColors.Border
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Username",
                    color = AccessColors.Muted,
                    fontSize = 12.sp
                )

                Text(
                    text = username,
                    color = AccessColors.Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Nomor WhatsApp",
                    color = AccessColors.Muted,
                    fontSize = 12.sp
                )

                Text(
                    text = whatsapp,
                    color = AccessColors.Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // TOMBOL AKSI
        if (!isAktif) {
            Button(
                onClick = onCekStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Cek status aktivasi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onMasuk,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Kembali ke login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccessColors.Ink
                )
            }
        } else {
            Button(
                onClick = onMasuk,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Masuk ke aplikasi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun PendingActivationScreenPreview() {
    SIRABASTheme {
        PendingActivationScreen(
            nama = "Budi Santoso",
            username = "Budi_01",
            whatsapp = "+6281234567890",
            statusAktivasi = "MENUNGGU_AKTIVASI"
        )
    }
}
