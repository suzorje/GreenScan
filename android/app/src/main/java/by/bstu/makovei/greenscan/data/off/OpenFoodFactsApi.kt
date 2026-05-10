package by.bstu.makovei.greenscan.data.off

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OffV2Response
}

data class OffV2Response(
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: OffProductJson?
)

data class OffProductJson(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_ru") val productNameRu: String?,
    @SerializedName("generic_name") val genericName: String?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("ingredients_text_ru") val ingredientsTextRu: String?,
    @SerializedName("nutriments") val nutriments: JsonObject?
)
