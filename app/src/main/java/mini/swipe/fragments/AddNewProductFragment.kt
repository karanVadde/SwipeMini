package mini.swipe.fragments

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import mini.swipe.viewmodel.DefaultViewModel
import mini.swipe.R
import mini.swipe.databinding.FragmentSecondBinding
import mini.swipe.uistate.NewProduct
import mini.swipe.uistate.AddNewProdUIState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddNewProductFragment : Fragment(), OnItemSelectedListener {

    private val TAG = AddNewProductFragment::class.java.name
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private val viewModel : DefaultViewModel by activityViewModels()
    private var prodTypeItems = ArrayList<String>().also { it.add("1. Select Product Type") }
    private var selProdType : String ?= null
    private var selProdIndex : Int = 0
    //use this field to upload an image.
    private var imgURI : Uri ?= null
    private var realPath : String ?= null

    private val clickListener = View.OnClickListener {
        when(it.id){
            R.id.add_prod_btn -> addProduct()
            R.id.prd_img_btn -> viewModel.addImage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      _binding = FragmentSecondBinding.inflate(inflater, container, false)
      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSpinner()
        //listen to life cycle changes using this method.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addNewProdUiState.collect {
                    handleSecUIState(it)
                }
            }
        }
        //
        binding.addProdBtn.setOnClickListener(clickListener)
        binding.prdImgBtn.setOnClickListener(clickListener)
    }

    /**
     * use this function to convert string to request-body.
     */
    private fun makeReqBody(text: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), text)
    }

    /**
     * use this function to make request body for an img file.
     */
    private fun makeImgReqBody(): MultipartBody.Part? {
        //pass it like this
        val file = File(realPath)
        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val x = MultipartBody.Part.createFormData("image", file.name, requestFile)
        Log.d(TAG, "x : $x")
        return x
    }

    /**
     * add a new product using this function.
     */
    private fun addProduct(){
        //validate all the fields.
        if(!validateAllFields())return
        //product to add.
        val newProd = NewProduct(
            prodName = makeReqBody(binding.prdNmeEt.text.toString()),
            prodType = selProdType?.let { makeReqBody(it) },
            prodPrice = makeReqBody(binding.prdPrcEt.text.toString()),
            prodTax = makeReqBody(binding.prdTaxEt.text.toString()),
            imageUri = null)
        viewModel.addNewProduct(newProd)
        //
        hideUI(true)
    }

    /**
     * use this function to hide or display ui when necessary.
     */
    private fun hideUI(toHide:Boolean){
        if(toHide){
            binding.prodTypeSpinner.visibility = View.INVISIBLE
            binding.prdNmeEt.visibility = View.INVISIBLE
            binding.prdPrcEt.visibility = View.INVISIBLE
            binding.prdTaxEt.visibility = View.INVISIBLE
            binding.prdImgBtn.visibility = View.INVISIBLE
            binding.addProdBtn.visibility = View.INVISIBLE
            binding.loadingBar.visibility = View.VISIBLE
        }else{
            binding.prodTypeSpinner.visibility = View.VISIBLE
            binding.prdNmeEt.visibility = View.VISIBLE
            binding.prdPrcEt.visibility = View.VISIBLE
            binding.prdTaxEt.visibility = View.VISIBLE
            binding.prdImgBtn.visibility = View.VISIBLE
            binding.addProdBtn.visibility = View.VISIBLE
            binding.loadingBar.visibility = View.INVISIBLE
        }
    }

    /**
     * given a uri, get an image's file path using this method.
     */
    private fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    /**
     * handle this fragments ui state using this method.
     */
    private fun handleSecUIState(secondFragUIState: AddNewProdUIState){
        if(secondFragUIState.toUpdateImgBtn){
            val imgUri = Uri.parse(secondFragUIState.updateImgBtnTxt)
            Log.d(TAG, "imgUri: $imgUri")
            if(checkImgValidity(imgUri)){
                binding.prdImgBtn.setImageURI(imgUri)
                imgURI = imgUri
                //realPath = getRealPathFromURI(requireContext(), imgURI)
                //Log.d(TAG, "realPath: $realPath")
            }else{
                Toast.makeText(requireContext(), "select an image with 1:1 ratio", Toast.LENGTH_SHORT).show()
            }
        }
        //
        if(!secondFragUIState.displayProgressBar){
            hideUI(false)
        }
        //
        if(secondFragUIState.prodAddSuccess){
            Toast.makeText(requireContext(), "product added successfully", Toast.LENGTH_SHORT).show()
            //
            binding.prodTypeSpinner.setSelection(0)
            binding.prdNmeEt.text.clear()
            binding.prdPrcEt.text.clear()
            binding.prdTaxEt.text.clear()
        }
    }

    /**
     * check whether the image is 1:1
     */
    private fun checkImgValidity(uri: Uri) : Boolean{
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var inputStream : InputStream ?= null
        try {
            inputStream = activity?.contentResolver?.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: FileNotFoundException) {
            // do something
            e.printStackTrace()
        }finally {
            inputStream?.close()
        }
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        Log.d(TAG, "imageHeight: $imageHeight imageWidth: $imageWidth")
        if(imageHeight == imageWidth)return true
        return false
    }

    /**
     * validate all the fields using this method.
     */
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

    /**
     * setup spinner using this method.
     */
    private fun setUpSpinner(){
        binding.prodTypeSpinner.onItemSelectedListener = this
        prodTypeItems.addAll(viewModel.uniqueProdSet)
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item , prodTypeItems )
        binding.prodTypeSpinner.adapter = adapter
    }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(p2 != 0){
            //the selected prod index and product type.
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