package mini.swipe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import mini.swipe.viewmodel.DefaultViewModel
import mini.swipe.R
import mini.swipe.adapter.ProductsAdapter
import mini.swipe.databinding.FragmentFirstBinding
import mini.swipe.model.SwipeData
import mini.swipe.model.SwipeDataItem
import mini.swipe.uistate.ProductFragUIState


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProductsFragment : Fragment() {

    private val TAG : String = ProductsFragment::class.java.name

    private val viewModel : DefaultViewModel by activityViewModels()

    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

      _binding = FragmentFirstBinding.inflate(inflater, container, false)
      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect {
                    handleUIState(it)
                }
            }
        }

        binding.fab.setOnClickListener { view ->
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            viewModel.onFirstFragToSec()
        }
    }

    /**
     * handle the fragments ui state using this method.
     */
    private fun handleUIState(firstFragState : ProductFragUIState){
        //if data has been loaded successfully, display it.
        if(firstFragState.isDataLoaded && !firstFragState.toDisplaySearchResult){
            displayData(state = firstFragState)
        }
        //handle search result.
        if(firstFragState.isSearchNonNull && firstFragState.toDisplaySearchResult){
            handleSearch(firstFragState)
        }
        //search field cleared
        if(!firstFragState.toDisplaySearchResult
            && firstFragState.isSearchCleared){
            handleSearchClear(firstFragState)
        }

        //if an error has occurred, handle the error.
        if(firstFragState.isError){
            handleError(state = firstFragState)
        }
    }

    /**
     * use this function to display the data once its downloaded.
     */
    private fun displayData(state : ProductFragUIState){
        //setup recycler view here.
        state.swipeData?.let {
            handleLoadingBar(isVisible = false)
            setUpProducts(it)
        }
        //notify view model that data has been displayed.
        viewModel.onDisplayData()
    }

    /**
     * when a user searches for an item, use this function to see if we have a match.
     */
    private fun handleSearch(state : ProductFragUIState){
        val newList = ArrayList<SwipeDataItem>()
        //wrap the search result in an array list.
        state.searchResult?.let { newList.add(it) }
        //a non null search result has occurred, we will display the first found item in the array list.
        prodAdapter.updateDataItems(newItems = newList)
        //notify the adapter that data set has changed.
        prodAdapter.notifyDataSetChanged()
        //notify view model that search result has been displayed.
        viewModel.onSearchDisplay()
    }

    /**
     * use this function to reload the item list, when a user clears the search.
     */
    private fun handleSearchClear(state : ProductFragUIState){
        //if the current fragment is visible.
        if(isVisible){
            //a non null search result has occurred, we will display the first found item in the array list.
            state.swipeData?.let { prodAdapter.updateDataItems(newItems = it) }
            //notify the adapter that data set has changed.
            prodAdapter.notifyDataSetChanged()
            //
            viewModel.onSearchClear()
        }
    }

    /**
     * handle any error using this method.
     */
    private fun handleError(state : ProductFragUIState){
        Log.d(TAG, "handleUIState/handleError: $state")
        handleLoadingBar(isVisible = false)
        //display a toast or snack-bar.
        Toast.makeText(requireContext(), state.errorMsg, Toast.LENGTH_SHORT).show()
        //notify the view model that the error has been displayed.
        viewModel.onErrorDisplay()
    }

    private lateinit var prodAdapter : ProductsAdapter
    private fun setUpProducts(swipeData:SwipeData){
        binding.prodRv.visibility = View.VISIBLE
        val itemsList = populateItems(swipeData)
        // Create adapter passing in the sample user data
        prodAdapter = ProductsAdapter(itemsList)
        // Attach the adapter to the recyclerview to populate items
        binding.prodRv.adapter = prodAdapter
        // Set layout manager to position the items
        binding.prodRv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun populateItems(swipeData:SwipeData):ArrayList<SwipeDataItem>{
        val arrayList = arrayListOf<SwipeDataItem>()
        swipeData.forEach {
            arrayList.add(it)
        }
        return arrayList
    }

    private fun handleLoadingBar(isVisible:Boolean){
        if(isVisible){
            binding.loadingBar.visibility = View.VISIBLE
        }else{
            binding.loadingBar.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}