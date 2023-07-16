package mini.swipe

import android.util.Log
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
import mini.swipe.model.SwipeData
import mini.swipe.network.DataRepository
import mini.swipe.network.DataRepositoryImpl
import mini.swipe.uistate.FirstFragUIState
import mini.swipe.uistate.SecondFragUIState


class DefaultViewModel : ViewModel() {

    private val TAG = DefaultViewModel::class.java.name

    private var dataRepository : DataRepository = DataRepositoryImpl()
    //state variables.
    private val _uiState : MutableStateFlow<FirstFragUIState>
            = MutableStateFlow(FirstFragUIState())
    var uiState: StateFlow<FirstFragUIState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun performPostFun(secFragUIState : SecondFragUIState){
        dataRepository.performPostFun(secFragUIState)
    }

    private fun loadData(){
        viewModelScope.launch(Dispatchers.IO) {
            getSwipeData()
        }
    }

    fun onSecFragToFirst(){
        loadData()
    }

    fun onFirstFragToSec(){

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


    private var masterSwipeData : SwipeData ?= null

    var uniqueProdSet = HashSet<String>()
    private fun uniqueProdType(swipeData: SwipeData){
        swipeData.forEach {
            uniqueProdSet.add(it.product_type)
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