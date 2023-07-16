package mini.swipe.uistate

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody

data class NewProduct(@SerializedName("product_name")val prodName : RequestBody ?= null,
                      @SerializedName("product_type") val prodType : RequestBody ?= null,
                      @SerializedName("price") val prodPrice : RequestBody ?= null,
                      @SerializedName("tax") val prodTax : RequestBody ?= null,
                      @SerializedName("files[]") val imageUri : MultipartBody.Part?= null)



