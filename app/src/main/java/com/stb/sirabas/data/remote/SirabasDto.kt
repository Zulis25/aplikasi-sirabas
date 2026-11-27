package com.stb.sirabas.data.remote

// --- Auth DTOs ---
data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val nama_lengkap: String,
    val nomor_whatsapp: String,
    val nama_perusahaan: String? = null
)

data class RegisterResponse(
    val username: String,
    val role: String?,
    val status_aktivasi: String
)

data class UserProfileDto(
    val username: String,
    val nama_lengkap: String? = null,
    val nomor_whatsapp: String? = null,
    val role: String? = null,
    val status_aktivasi: String? = null,
    val nama_perusahaan: String? = null
)

data class ProfilePatchRequest(
    val nama_lengkap: String? = null,
    val nomor_whatsapp: String? = null,
    val nama_perusahaan: String? = null
)

// --- Bahan Baku DTOs ---
data class RawMaterialRequest(
    val tanggal: String,
    val jenis: String,
    val supplier: String,
    val asal_daerah: String,
    val berat_masuk_kg: Double,
    val kadar_air: Double? = null,
    val derajat_sosoh: Double? = null,
    val broken: Double? = null
)

data class RawMaterialCreatedDto(
    val nomor_penerimaan: String,
    val status: String
)

data class RawMaterialDto(
    val nomor_penerimaan: String,
    val tanggal: String,
    val jenis: String,
    val supplier: String,
    val asal_daerah: String,
    val berat_masuk_kg: Double,
    val sisa_kg: Double? = null,
    val kadar_air: Double? = null,
    val derajat_sosoh: Double? = null,
    val broken: Double? = null
)

// --- Produksi DTOs ---
data class ProductionOutputRequest(
    val kode_produk: String,
    val berat_kg: Double
)

data class SideOutputRequest(
    val nama_produk: String,
    val berat_kg: Double
)

data class ProductionRequest(
    val kode_batch: String,
    val tanggal: String,
    val nomor_penerimaan: String,
    val berat_input_kg: Double,
    val hasil_utama: List<ProductionOutputRequest>,
    val hasil_samping: List<SideOutputRequest> = emptyList()
)

data class BatchOutputDto(
    val kode_produk: String? = null,
    val berat_kg: Double? = null
)

data class SideOutputDto(
    val nama_produk: String? = null,
    val berat_kg: Double? = null
)

data class ProductionBatchDto(
    val kode_batch: String,
    val tanggal: String? = null,
    val nomor_penerimaan: String? = null,
    val berat_input_kg: Double? = 0.0,
    val total_hasil_utama_kg: Double? = 0.0,
    val total_hasil_samping_kg: Double? = 0.0,
    val hasil_utama: List<BatchOutputDto>? = emptyList(),
    val hasil_samping: List<SideOutputDto>? = emptyList()
)

data class LabelsRequest(
    val kode_produk: String,
    val berat_karung_kg: List<Double>
)

// --- Katalog & Penjualan DTOs ---
data class ProductDto(
    val kode: String,
    val nama: String,
    val jenis_produk: String? = null,
    val kemasan: String,
    val stok_tersedia: Int,
    val harga_katalog: Long,
    val gambar_path: String? = null
)

data class InvoiceItemRequest(
    val kode_produk: String,
    val jumlah: Int
)

data class InvoiceRequest(
    val nomor_invoice: String,
    val pelanggan: String,
    val items: List<InvoiceItemRequest>
)

data class ProductionOrderDto(
    val nomor_invoice: String,
    val status_pemenuhan: String? = null,
    val dibuat_pada: String? = null,
    val tanggal: String? = null,
    val pelanggan: String? = null,
    val dibuat_oleh_penjualan: String? = null,
    val kode_produk: String? = null,
    val nama_produk: String? = null,
    val kemasan: String? = null,
    val jumlah_diminta: Int = 0,
    val jumlah_dipenuhi: Int = 0,
    val jumlah_sisa: Int = 0
)

data class InvoiceItemDto(
    val kode_produk: String? = null,
    val nama_produk: String? = null,
    val kemasan: String? = null,
    val jumlah: Int = 0,
    val harga_satuan: Long = 0,
    val harga: Long? = null,
    val subtotal: Long? = null
)

data class InvoicePaymentDto(
    val waktu: String? = null,
    val jumlah: Long = 0,
    val metode_bayar: String? = null
)

data class InvoiceDto(
    val nomor_invoice: String,
    val tanggal: String? = null,
    val pelanggan: String? = null,
    val total: Long? = null,
    val total_tagihan: Long? = null,
    val jumlah_dibayar: Long = 0,
    val status_pembayaran: String? = null,
    val items: List<InvoiceItemDto> = emptyList(),
    val pembayaran: List<InvoicePaymentDto> = emptyList()
)

data class PaymentRequest(
    val jumlah: Long,
    val metode_bayar: String
)

data class ShipRequest(
    val kode_karung: List<String>
)

data class ScanLabelRequest(
    val kode_karung: String
)

// --- Trace & Retur DTOs ---
data class TraceDto(
    val kode_karung: String? = null,
    val kode_batch: String? = null,
    val nama_produk: String? = null,
    val kemasan: String? = null,
    val tanggal_produksi: String? = null,
    val supplier: String? = null,
    val asal_daerah: String? = null,
    val nomor_invoice: String? = null,
    val pelanggan: String? = null
)

data class ReturnRequest(
    val no_invoice: String? = null,
    val kode_produk: String,
    val pelanggan: String,
    val berat_kg: Double,
    val alasan: String
)

data class ReturnDto(
    val id: Long? = null,
    val no_invoice: String? = null,
    val kode_produk: String? = null,
    val pelanggan: String? = null,
    val berat_kg: Double? = null,
    val alasan: String? = null,
    val tanggal: String? = null
)
