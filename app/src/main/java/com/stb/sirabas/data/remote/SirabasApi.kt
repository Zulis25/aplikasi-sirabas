package com.stb.sirabas.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SirabasApi {

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<TokenResponse>

    @GET("me")
    suspend fun me(): Response<UserProfileDto>

    @PATCH("me")
    suspend fun updateProfile(
        @Body body: ProfilePatchRequest
    ): Response<UserProfileDto>

    @GET("raw-materials")
    suspend fun getRawMaterials(): Response<List<RawMaterialDto>>

    @POST("raw-materials")
    suspend fun createRawMaterial(
        @Body body: RawMaterialRequest
    ): Response<RawMaterialCreatedDto>

    @GET("production/batches")
    suspend fun getProductionBatches(): Response<List<ProductionBatchDto>>

    @GET("production/orders")
    suspend fun getProductionOrders(): Response<List<ProductionOrderDto>>

    @POST("production/orders/{nomorInvoice}/scan")
    suspend fun scanOrderLabel(
        @Path("nomorInvoice") nomorInvoice: String,
        @Body body: ScanLabelRequest
    ): Response<Map<String, Any>>

    @POST("production/batches")
    suspend fun createProductionBatch(
        @Body body: ProductionRequest
    ): Response<Map<String, Any>>

    @POST("production/batches/{kodeBatch}/labels")
    suspend fun createLabels(
        @Path("kodeBatch") kodeBatch: String,
        @Body body: LabelsRequest
    ): Response<Map<String, Any>>

    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @POST("invoices")
    suspend fun createInvoice(
        @Body body: InvoiceRequest
    ): Response<Map<String, Any>>

    @GET("invoices")
    suspend fun getInvoices(): Response<List<InvoiceDto>>

    @GET("invoices/{nomor}")
    suspend fun getInvoiceDetail(
        @Path("nomor") nomor: String
    ): Response<InvoiceDto>

    @POST("invoices/{nomor}/payments")
    suspend fun addPayment(
        @Path("nomor") nomor: String,
        @Body body: PaymentRequest
    ): Response<Map<String, Any>>

    @POST("invoices/{nomor}/ship")
    suspend fun shipInvoice(
        @Path("nomor") nomor: String,
        @Body body: ShipRequest
    ): Response<Map<String, Any>>

    @GET("labels/{kodeKarung}/trace")
    suspend fun traceLabel(
        @Path("kodeKarung") kodeKarung: String
    ): Response<TraceDto>

    @GET("returns")
    suspend fun getReturns(): Response<List<ReturnDto>>

    @POST("returns")
    suspend fun createReturn(
        @Body body: ReturnRequest
    ): Response<Map<String, Any>>
}
