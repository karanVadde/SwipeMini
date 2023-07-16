package mini.swipe.uistate

import com.google.gson.annotations.SerializedName

data class SecondFragUIState(@SerializedName("product_name")val prodName : String ?= null,
                             @SerializedName("product_type") val prodType : String ?= null,
                             @SerializedName("price") val prodPrice : String ?= null,
                             @SerializedName("tax") val prodTax : String ?= null,
                             @SerializedName("files[]") val file : String ?= null)



