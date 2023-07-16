package mini.swipe.network

import android.util.Log
import io.reactivex.Observable
import mini.swipe.model.PostResponse
import mini.swipe.model.SwipeData
import mini.swipe.model.SwipeDataItem
import mini.swipe.uistate.NewProduct
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


sealed interface DataRepository {
    fun getSwipeData():Observable<SwipeData>
    fun findProduct(searchText:String):SwipeDataItem?
    fun addNewProduct(newProduct: NewProduct):Call<PostResponse>?
}

class DataRepositoryImpl : DataRepository {

    private val TAG = DataRepositoryImpl::class.java.name

    private val _products = arrayListOf<SwipeDataItem>()

    /**
     * use this function to get app data.
     */
    override fun getSwipeData(): Observable<SwipeData> {
        return ApiInstance.apiInstance.getData().doOnNext { swipeData ->
            Log.i(TAG, swipeData.toString())
            //add to product list for search function.
            swipeData.forEach {
                _products.add(it)
            }
        }.doOnError {
            Log.e(TAG, it.toString())
        }
    }

    /**
     * use this method to find a product when user enters text in the search bar.
     */
    override fun findProduct(searchText:String): SwipeDataItem? {
        val dataItem = _products.find {
            Log.d(TAG, "${it.product_name.lowercase()} ${searchText.lowercase()}")
            //user should be able to find the product even if he searches in lower case.
            it.product_name.lowercase() == searchText.lowercase()
        }
        Log.d(TAG, "searchText: $searchText dataItem: $dataItem")
        return dataItem
    }

    /**
     * use this method to add a new product.
     */
    override fun addNewProduct(newProduct: NewProduct): Call<PostResponse>? {
        Log.d(TAG, "addNewProduct: $newProduct")
        return ApiInstance.apiInstance.postData(newProduct.prodName!!,
            newProduct.prodType!!,
            newProduct.prodPrice!!,
            newProduct.prodTax!!)
    }

}