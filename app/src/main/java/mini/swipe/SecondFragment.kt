package mini.swipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import mini.swipe.databinding.FragmentSecondBinding
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import mini.swipe.uistate.SecondFragUIState


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), OnItemSelectedListener {

    private val TAG = SecondFragment::class.java.name

    private var _binding: FragmentSecondBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel : DefaultViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

      _binding = FragmentSecondBinding.inflate(inflater, container, false)
      return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //
        binding.prodTypeSpinner.onItemSelectedListener = this
        populateSpinner()
        //
        binding.addProdBtn.setOnClickListener {
            //validate all the fields.
            if(!validateAllFields())return@setOnClickListener
            val secFragUIState = SecondFragUIState(prodName = binding.prdNmeEt.text.toString(),
                prodType = selProdType,
                prodPrice = binding.prdPrcEt.text.toString(),
                prodTax = binding.prdTaxEt.text.toString(),
                file = "")
            viewModel.performPostFun(secFragUIState)
        }
    }

    private fun validateAllFields():Boolean{
        var errMsg = ""
        var areFieldsValidated = true
        if(binding.prdTaxEt.text.toString() == ""){
            errMsg = "Enter product tax"
            areFieldsValidated = false
        }
        if(binding.prdPrcEt.text.toString() == ""){
            errMsg = "Enter product price"
            areFieldsValidated = false
        }
        if(binding.prdNmeEt.text.toString() == ""){
            errMsg = "Enter product name"
            areFieldsValidated = false
        }
        if(selProdIndex == 0){
            errMsg = "Select Product Type"
            areFieldsValidated = false
        }
        if(errMsg != "")Toast.makeText(requireContext(), errMsg, Toast.LENGTH_SHORT).show()
        return areFieldsValidated
    }


    private var prodTypeItems = ArrayList<String>().also { it.add("1. Select Product Type") }

    private fun populateSpinner(){

        prodTypeItems.addAll(viewModel.uniqueProdSet)

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item , prodTypeItems )
        binding.prodTypeSpinner.adapter = adapter
    }

    private var selProdType : String ?= null
    private var selProdIndex : Int = 0
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(p2 != 0){
            selProdIndex = p2
            selProdType = prodTypeItems[p2]
            Log.d(TAG, "p2: $p2 selProdType: $selProdType")
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}