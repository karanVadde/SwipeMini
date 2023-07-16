package mini.swipe.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mini.swipe.model.PostResponse
import mini.swipe.model.SwipeData
import mini.swipe.network.DataRepository
import mini.swipe.network.DataRepositoryImpl
import mini.swipe.uistate.ProductFragUIState
import mini.swipe.uistate.MainActivityUIState
import mini.swipe.uistate.AddNewProdUIState
import mini.swipe.uistate.NewProduct
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DefaultViewModel : ViewModel() {

    private val TAG = DefaultViewModel::class.java.name

    private var dataRepository : DataRepository = DataRepositoryImpl()
    //state variables.
    private val _uiState : MutableStateFlow<ProductFragUIState>
            = MutableStateFlow(ProductFragUIState())
    var uiState: StateFlow<ProductFragUIState> = _uiState.asStateFlow()

    //keep a reference of app data.
    private var masterSwipeData : SwipeData ?= null
    //use this field to populate product type in the second fragment.
    var uniqueProdSet = HashSet<String>()
    //use this field to show or hide the search edit text.
    val hideSearchEt = MutableLiveData<Boolean>()

    init {
        //load app data.
        loadData()
    }

    /**
     * a callback to keep track of whether the product addition was successful or not.
     */
    private val callBack = object : Callback<PostResponse?> {
        override fun onResponse(
            call: Call<PostResponse?>,
            response: Response<PostResponse?>
        ) {
            Log.d(TAG, "onResponse : $response")
            _addNewProdUiState.update {
                it.copy(displayProgressBar = false,
                    prodAddSuccess = true)
            }
        }

        override fun onFailure(call: Call<PostResponse?>, t: Throwable) {
            Log.d(TAG, "onFailure : ${t.printStackTrace()}")
            _addNewProdUiState.update {
                it.copy(displayProgressBar = false,
                    prodAddSuccess = false)
            }
        }
    }
    /**
     * use this function to add a new product.
     */
    fun addNewProduct(newProduct : NewProduct){
        Log.d(TAG, "addNewProduct: $newProduct")
        try{
            dataRepository.addNewProduct(newProduct)?.enqueue(callBack)
        }catch (e : java.lang.Exception){
            e.printStackTrace()
        }
    }

    /**
     * use this function to load data.
     */
    private fun loadData(){
        viewModelScope.launch(Dispatchers.IO) {
            getSwipeData()
        }
    }

    /**
     * what should happen when a user navigates from second frag to first fragment.
     */
    fun onSecFragToFirst(){
        //reload data.
        loadData()
        //inform main activity to show the search bar
        hideSearchEt.postValue(false)
    }

    /**
     * what should happen when a user navigates from first to second screen.
     */
    fun onFirstFragToSec(){
        //inform main activity to hide the search bar.
        hideSearchEt.postValue(true)
    }

    /**
     * use this function to get app data.
     */
    private fun getSwipeData(){
        val disObs = getDisposableObs()
        try {
            dataRepository.getSwipeData().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disObs);
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * use this function to keep track of the unique product types in AddProductFragment
     */
    private fun uniqueProdType(swipeData: SwipeData){
        swipeData.forEach {
            uniqueProdSet.add(it.product_type)
        }
    }

    /**
     * keep track of activity ui state using these fields.
     */
    private val _mainUiState : MutableStateFlow<MainActivityUIState>
            = MutableStateFlow(MainActivityUIState())
    var mainUiState: StateFlow<MainActivityUIState> = _mainUiState.asStateFlow()

    /**
     * notify main activity that user wants to add an image.
     */
    fun addImage(){
        Log.d(TAG, "addImage")
        _mainUiState.update{
            it.copy(toAddImage = true)
        }
    }

    /**
     * keep track of add new product frag state using these fields.
     */
    private val _addNewProdUiState : MutableStateFlow<AddNewProdUIState>
            = MutableStateFlow(AddNewProdUIState())
    var addNewProdUiState: StateFlow<AddNewProdUIState> = _addNewProdUiState.asStateFlow()
    fun onAddImageFinish(data: Intent?){
        Log.d(TAG, "onAddImageFinish")
        //also inform second frag to update button text.
        _addNewProdUiState.update {
            it.copy(updateImgBtnTxt = data?.data.toString(),
                toUpdateImgBtn = true)
        }
    }

    /**
     * what happens after an image is loaded into the view.
     */
    fun onAddImgFinish(){
        Log.d(TAG, "onAddImgFinish")
        _mainUiState.update{
            it.copy(toAddImage = false)
        }
    }

    private fun getDisposableObs(): DisposableObserver<SwipeData> {
        return  object : DisposableObserver<SwipeData>() {
            override fun onComplete() {
            }
            override fun onNext(swipeData: SwipeData) {
                Log.d(TAG, "swipeData: $swipeData")
                masterSwipeData = swipeData
                uniqueProdType(swipeData)
                //data loaded successfully, notify the frag.
                _uiState.update{
                    it.copy(isDataLoaded = true,
                        swipeData = swipeData)
                }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, e.message?:"")
                //notify the frag that an error has occurred.
                _uiState.update{
                    it.copy(isDataLoaded = false,
                        isError = true,
                        errorMsg = e.message?:"",
                        toDisplayError = true)
                }
            }
        }
    }

    fun onDisplayData(){
        _uiState.update{
            it.copy(isDataLoaded = false)
        }
    }

    fun onSearchDisplay(){
        //search  result has been displayed to the user, update the frag ui state. reset for the next search event.
        _uiState.update{
            it.copy(isSearchNonNull = false,
                toDisplaySearchResult = false,
                searchResult = null)
        }
    }

    fun onErrorDisplay(){
        //error has been displayed to the user, update the frag ui state. reset for the next error.
        _uiState.update{
            it.copy(isError = false,
                errorMsg = "",
                toDisplayError = false)
        }
    }

    fun onSearchClear(){
        Log.d(TAG, "onSearchClear")
        _uiState.update{
            it.copy(isSearchCleared = false,
                isDataLoaded = true,
                toDisplaySearchResult = false)
        }
    }

    /**
     * what happens when a text is entered or cleared in the search et box in the products screen
     */
    fun onTextChanged(prodToSearch : String):String{
        //the user has entered the above text in the search bar.
        //check if there is a product that matches the text.
        val foundProd = dataRepository.findProduct(prodToSearch)
        Log.d(TAG, "prodToSearch: $prodToSearch foundProd: $foundProd")
        //if the search returned a non null result. display it to the user.
        foundProd?.let { dataItem ->
            _uiState.update{
                it.copy(isSearchNonNull = true,
                    toDisplaySearchResult = true,
                    searchResult = dataItem)
            }
        }
        //
        if(prodToSearch == ""){
            //edit text has been cleared.
            Log.d(TAG, "edit text has been cleared.")
            _uiState.update{
                it.copy(isSearchCleared = true, isDataLoaded = true,
                    toDisplaySearchResult = false)
            }
        }
        return foundProd?.let { "Hello '$it' from $this" } ?: "Prod '$foundProd' not found!"
    }
}