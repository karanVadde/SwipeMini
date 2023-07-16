package mini.swipe.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("message") val message: String,
    @SerializedName("product_details") val product_details: ProductDetails,
    @SerializedName("product_id") val product_id: Int,
    @SerializedName("success") val success: Boolean
)