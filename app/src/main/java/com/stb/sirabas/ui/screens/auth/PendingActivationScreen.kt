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
    onMasuk: () -> Unit = {}
) {
    AccessPage(
        title = "Aktivasi akun",
        onBack = onMasuk
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
                color = AccessColors.SoftGold,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.sirabas_clock
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = AccessColors.GoldInk
                    )
                }
            }

            Text(
                text = "Menunggu aktivasi",
                color = AccessColors.Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Akun Anda sedang menunggu aktivasi " +
                        "oleh Admin. Pemberitahuan akan dikirim " +
                        "ke nomor WhatsApp yang Anda daftarkan " +
                        "setelah akun aktif.",
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
                color = AccessColors.SoftGold,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Menunggu aktivasi",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    color = AccessColors.GoldInk,
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

        // KEMBALI KE HALAMAN LOGIN
        Button(
            onClick = onMasuk,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Kembali ke login",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Anda dapat masuk setelah akun diaktifkan.",
            modifier = Modifier.fillMaxWidth(),
            color = AccessColors.Muted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

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
            whatsapp = "+6281234567890"
        )
    }
}