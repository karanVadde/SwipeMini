package mini.swipe.uistate

import mini.swipe.model.SwipeData
import mini.swipe.model.SwipeDataItem

data class ProductFragUIState(
                            //error params
                            val isError: Boolean = false,
                            val errorMsg : String = "",
                            val toDisplayError : Boolean = false,
                            //search params
                            val isSearchNonNull : Boolean = false,
                            val toDisplaySearchResult : Boolean = false,
                            val searchResult : SwipeDataItem ?= null,
                            //search params
                            val isSearchCleared : Boolean = false,
                            //data loading params.
                            val isDataLoaded : Boolean = false,
                            val swipeData: SwipeData ?= null,)
