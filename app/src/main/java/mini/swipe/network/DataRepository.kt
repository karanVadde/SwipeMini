package mini.swipe.network

import android.util.Log
import io.reactivex.Observable
import mini.swipe.model.PostResponse
import mini.swipe.model.SwipeData
import mini.swipe.model.SwipeDataItem
import mini.swipe.uistate.SecondFragUIState
import okhttp3.RequestBody


sealed interface DataRepository {
    fun getSwipeData():Observable<SwipeData>
    fun findProduct(searchText:String):SwipeDataItem?
    fun performPostFun(secondFragUIState: SecondFragUIState):Observable<PostResponse>?
}

class DataRepositoryImpl : DataRepository {

    private val TAG = DataRepositoryImpl::class.java.name

    private val _products = arrayListOf<SwipeDataItem>()

    override fun getSwipeData(): Observable<SwipeData> {
        return ApiInstance.apiInstance.getData().doOnNext { swipeData ->
            Log.i(javaClass.simpleName, swipeData.toString())
            //add to product list for search function.
            swipeData.forEach {
                _products.add(it)
            }
        }.doOnError {
            Log.e(javaClass.simpleName, it.toString())
        }
    }

    override fun findProduct(searchText:String): SwipeDataItem? {
        val dataItem = _products.find {
            Log.d(TAG, "${it.product_name.lowercase()} ${searchText.lowercase()}")
            //user should be able to find the product even if he searches in lower case.
            it.product_name.lowercase() == searchText.lowercase()
        }
        Log.d(TAG, "searchText: $searchText dataItem: $dataItem")
        return dataItem
    }

    override fun performPostFun(secondFragUIState: SecondFragUIState): Observable<PostResponse>? {
        Log.d(TAG, "performPostFun/secondFragUIState: $secondFragUIState")
        return ApiInstance.apiInstance.postData(secondFragUIState.prodName?:"",
            secondFragUIState.prodType?:"",
            secondFragUIState.prodPrice?:"",
            secondFragUIState.prodTax?:"",
            secondFragUIState.file?:"").doOnNext { postResp ->
            Log.i(TAG, postResp.toString())
        }.doOnError {
            Log.e(TAG, it.toString())
        }
    }

}