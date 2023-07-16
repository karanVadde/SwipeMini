package mini.swipe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import mini.swipe.databinding.ActivityMainBinding
import mini.swipe.uistate.MainActivityUIState
import mini.swipe.viewmodel.DefaultViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: DefaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.hideSearchEt.observe(this, Observer {
            if(it){
                binding.searchEt.visibility = View.INVISIBLE
            }else{
                binding.searchEt.visibility = View.VISIBLE
            }
        })
        //
        setUpActionBar()
        setUpNavGraph()
        //
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.mainUiState.collect {
                    handleMainUIState(it)
                }
            }
        }
    }


    private val TAG = MainActivity::class.java.name
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        var data: Intent?=null
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            data = result.data
            Log.d(TAG, "data: $data")
        }
        //reset ui state.
        viewModel.onAddImageFinish(data)
    }

    private fun handleMainUIState(state : MainActivityUIState){
        Log.d(TAG, "handleMainUIState/state: ${state.toAddImage}")
        if(state.toAddImage){
            addImage()
        }
        //right after showing the intent.
        viewModel.onAddImgFinish()
    }


    private fun addImage(){
        //user wants to add an image.
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }


    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.searchEt.addTextChangedListener {
            viewModel.onTextChanged(it.toString())
        }
    }

    private fun setUpNavGraph(){
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.onSecFragToFirst()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}