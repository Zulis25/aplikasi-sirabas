package com.stb.sirabas.data.repository

import com.stb.sirabas.data.remote.InvoiceDto
import com.stb.sirabas.data.remote.InvoiceRequest
import com.stb.sirabas.data.remote.PaymentRequest
import com.stb.sirabas.data.remote.ProductDto
import com.stb.sirabas.data.remote.ReturnDto
import com.stb.sirabas.data.remote.ReturnRequest
import com.stb.sirabas.data.remote.ShipRequest
import com.stb.sirabas.data.remote.SirabasApi

class PenjualanRepository(private val api: SirabasApi) {

    suspend fun getProducts(): ApiResult<List<ProductDto>> {
        return try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil katalog produk (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun getInvoices(): ApiResult<List<InvoiceDto>> {
        return try {
            val response = api.getInvoices()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil daftar invoice (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun getInvoiceDetail(nomor: String): ApiResult<InvoiceDto> {
        return try {
            val response = api.getInvoiceDetail(nomor)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil detail invoice (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun createInvoice(request: InvoiceRequest): ApiResult<Unit> {
        return try {
            val response = api.createInvoice(request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal membuat invoice: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun addPayment(nomorInvoice: String, jumlah: Long, metodeBayar: String): ApiResult<Unit> {
        return try {
            val response = api.addPayment(nomorInvoice, PaymentRequest(jumlah, metodeBayar))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal mencatat pembayaran: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun shipInvoice(nomorInvoice: String, kodeKarungList: List<String>): ApiResult<Unit> {
        return try {
            val response = api.shipInvoice(nomorInvoice, ShipRequest(kodeKarungList))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal mengirim karung: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun getReturns(): ApiResult<List<ReturnDto>> {
        return try {
            val response = api.getReturns()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil daftar retur (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun createReturn(request: ReturnRequest): ApiResult<Unit> {
        return try {
            val response = api.createReturn(request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal mencatat retur: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }
}
