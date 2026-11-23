package com.stb.sirabas.ui.screens

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class DrafPenerimaanUi(
    val id: String,
    val jenis: String,
    val supplier: String,
    val daerah: String,
    val tanggal: String,
    val berat: String,
    val air: String,
    val sosoh: String,
    val broken: String
) {
    val beratKg: Double
        get() = berat.trim().replace(',', '.').toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0.0 } ?: 0.0
}

internal fun bacaDrafPenerimaan(
    prefs: SharedPreferences
): List<DrafPenerimaanUi> {
    val data = JSONArray(prefs.getString("draf", "[]") ?: "[]")

    return buildList {
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            add(
                DrafPenerimaanUi(
                    id = item.getString("id"),
                    jenis = item.optString("jenis", "Gabah"),
                    supplier = item.optString("supplier"),
                    daerah = item.optString("daerah"),
                    tanggal = item.optString("tanggal"),
                    berat = item.optString("berat"),
                    air = item.optString("air"),
                    sosoh = item.optString("sosoh"),
                    broken = item.optString("broken")
                )
            )
        }
    }
}

internal fun tulisDrafPenerimaan(
    prefs: SharedPreferences,
    daftar: List<DrafPenerimaanUi>
) {
    val data = JSONArray()
    daftar.forEach { draf ->
        data.put(
            JSONObject()
                .put("id", draf.id)
                .put("jenis", draf.jenis)
                .put("supplier", draf.supplier)
                .put("daerah", draf.daerah)
                .put("tanggal", draf.tanggal)
                .put("berat", draf.berat)
                .put("air", draf.air)
                .put("sosoh", draf.sosoh)
                .put("broken", draf.broken)
        )
    }
    prefs.edit().putString("draf", data.toString()).apply()
}