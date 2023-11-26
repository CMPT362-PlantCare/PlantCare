package com.example.plantcare

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.RadioButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.plantcare.databinding.ActivityAddplantBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

private const val EMPTY_STRING = ""
private const val INT_VAL_UNKNOWN = -1
private const val CHECKED_TP_KEY = "checked_tp_key"
private const val CHECKED_DH_KEY = "checked_dh_key"

class AddPlantActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: ActivityAddplantBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val calendar = Calendar.getInstance()
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var tempImgFile: File
    private lateinit var tempImgUri: Uri
    private lateinit var imgToSave: Bitmap
    private var saveImgFlag = false
    private lateinit var myViewModel: MyViewModel
    private lateinit var query: String

    private lateinit var plantEntryDatabase: PlantEntryDatabase
    private lateinit var plantEntryDatabaseDao: PlantEntryDatabaseDao
    private lateinit var plantEntryRepository: PlantEntryRepository
    private lateinit var plantEntryViewModelFactory: PlantEntryViewModelFactory
    private lateinit var plantEntryViewModel: PlantEntryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = Firebase.auth
        binding = ActivityAddplantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        setUpPlantEntryDatabase()
        requestPermissions()

        tempImgFile = File(getExternalFilesDir(null), "tempImg")
        tempImgUri = FileProvider.getUriForFile(this,
            getString(R.string.com_example_plantcare), tempImgFile)

        val defaultImage = ContextCompat.getDrawable(this, R.drawable.flower_icon_green)
        binding.imageView.setImageDrawable(defaultImage)

        initButtons()
        reviveRadioButtonState(savedInstanceState)

        val speciesTextView = binding.speciesAutocomplete
        speciesTextView.threshold = 1


        myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        myViewModel.image.observe(this) {
            binding.imageView.setImageBitmap(it)
        }
        myViewModel.species.observe(this){
            ArrayAdapter(this, android.R.layout.simple_list_item_1, it).also { adapter ->
                speciesTextView.setAdapter(adapter)
            }
        }

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = getBitmap(this, tempImgUri)
                setPicture(myViewModel, bitmap)
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                val bitmap = getBitmap(this, result.data!!.data!!)
                setPicture(myViewModel, bitmap)
            }
        }


        speciesTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                query = s.toString()
                val r: Runnable = MyRunnable()
                val thread = Thread(r)
                thread.start()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHECKED_TP_KEY, binding.terracottaRadioRoup.checkedRadioButtonId)
        outState.putInt(CHECKED_TP_KEY, binding.drainageRadioGroup.checkedRadioButtonId)
    }

    private fun reviveRadioButtonState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val checkedGenderRadioButtonId =
                savedInstanceState.getInt(CHECKED_TP_KEY, INT_VAL_UNKNOWN)
                savedInstanceState.getInt(CHECKED_DH_KEY, INT_VAL_UNKNOWN)
            confirmAndCheckRadioButton(checkedGenderRadioButtonId)
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        calendar.set(Calendar.YEAR, p1)
        calendar.set(Calendar.MONTH, p2)
        calendar.set(Calendar.DAY_OF_MONTH, p3)
    }

    private fun confirmAndCheckRadioButton(checkedGenderRadioButtonId: Int) {
        if (checkedGenderRadioButtonId != INT_VAL_UNKNOWN) {
            val radioButton = findViewById<RadioButton>(checkedGenderRadioButtonId)
            if (radioButton != null) {
                radioButton.isChecked = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.common_toolbar_menu, menu)
        return true
    }

    private fun setUpPlantEntryDatabase() {
        plantEntryDatabase = PlantEntryDatabase.getInstance(this)
        plantEntryDatabaseDao = plantEntryDatabase.plantEntryDatabaseDao
        plantEntryRepository = PlantEntryRepository(plantEntryDatabaseDao)
        plantEntryViewModelFactory = PlantEntryViewModelFactory(plantEntryRepository)
        plantEntryViewModel = ViewModelProvider(this, plantEntryViewModelFactory)[PlantEntryViewModel::class.java]
    }

    private fun requestPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET
            ), 0)
        }
    }
    private fun initButtons(){
        binding.photoButton.setOnClickListener(){
            val pictureDialog = PictureDialogFragment()
            pictureDialog.show(supportFragmentManager, "tag")

            supportFragmentManager
                .setFragmentResultListener("requestKey", this) { requestKey, bundle ->
                    when (bundle.getInt("bundleKey")) {
                        PictureDialogFragment.OPEN_CAMERA -> openCamera()
                        PictureDialogFragment.OPEN_GALLERY -> openGallery()
                    }
                }
        }
        binding.dobButton.setOnClickListener(){
            handleDateInput()
        }
        binding.cancelButton.setOnClickListener(){
            finish()
        }
        binding.addButton.setOnClickListener(){
//            add plant to db
            finish()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun handleDateInput() {
        val datePickerDialog = DatePickerDialog(
            this, this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        galleryResult.launch(intent)
    }

    private fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun setPicture(viewModel: MyViewModel, bitmap: Bitmap) {
        viewModel.image.value = bitmap
        imgToSave = bitmap
        saveImgFlag = true
    }



    inner class MyRunnable : Runnable {
        override fun run() {
            try {
                val url = URL("https://perenual.com/api/species-list?key=sk-ACMp656268884beef3126&q=$query")
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    inputStream.bufferedReader().use {
                        val speciesList = ArrayList<String>()
                        it.lines().forEach { line ->
                            var response = JSONObject(line)
                            var valueArray = response.getJSONArray("data")
                            for(t in 0 until valueArray.length()){
                                var name = valueArray.getJSONObject(t).getString("common_name")
                                speciesList.add(name)
                            }
                        }
                        runOnUiThread {
                            this@AddPlantActivity.myViewModel.species.value = speciesList
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}