package com.example.plantcare

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.example.plantcare.databinding.ActivityAddplantBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


private const val EMPTY_STRING = ""
private const val INT_VAL_UNKNOWN = -1
private const val DEFAULT_POSITION = -1
private const val DEFAULT_INDEX = 1
private const val DEFAULT_THRESHOLD = 1
private const val CHECKED_TP_KEY = "checked_tp_key"
private const val CHECKED_DH_KEY = "checked_dh_key"
private const val NAME_KEY = "name_key"
private const val CALENDAR_TIME_MILLIS = "calendar_time_millis"
private const val SPECIES_KEY = "species_key"
private const val POT_SIZE_KEY = "pot_size_key"
private const val IMG_NAME_KEY = "img_name_key"
private const val RENDERED_PLANT_ENTRY = "rendered_plant_entry_key"
private const val IMAGE_NAME_SET = "image_name_set_key"
private const val TEMP_IMG_URI = "temp_img_uri_key"
private const val SAVED_IMG_URI = "saved_img_uri_key"
private const val DOB_TEXT_KEY = "dob_text_key"
private const val BYTE_ARRAY_SIZE = 1024
private const val ZERO = 0
private const val FILE_COPY_OFFSET = 0
private const val DOUBLE_ZERO = 0.0
private const val DEFAULT_FREQUENCY = 7
private const val TERRACOTTA_FACTOR = 0.5
private const val DRAINAGE_HOLE_FACTOR = 2
private const val POT_SIZE_NORMALIZATION_FACTOR = 5

class AddPlantActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    companion object {
        const val PLANT_ADD = 0
        const val PLANT_VIEW = 1
    }

    private lateinit var binding: ActivityAddplantBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val calendar = Calendar.getInstance()
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var tempImgFile: File
    private lateinit var copyImgFile: File
    private lateinit var tempImgUri: Uri
    private lateinit var copyImgUri: Uri
    private lateinit var imgToSave: Bitmap
    private lateinit var addPlantViewModel: AddPlantViewModel
    private lateinit var query: String
    private lateinit var navigationView: BottomNavigationView
    private var speciesId = EMPTY_STRING

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private var position: Int = DEFAULT_POSITION
    private var pageType: Int = PLANT_VIEW
    private var isRenderedPlantEntry: Boolean = false
    private var saveImgFlag: Boolean = false
    private var isImageNameSet: Boolean = false
    private var imageName: String = EMPTY_STRING
    private var calendarTimeMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child(getString(R.string.firebase_users_key)).child(firebaseAuth.currentUser?.uid!!)

        binding = ActivityAddplantBinding.inflate(layoutInflater)

        position = intent.getIntExtra(getString(R.string.position_key), DEFAULT_POSITION)
        pageType = intent.getIntExtra(getString(R.string.plant_page_type), PLANT_VIEW)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        navigationView = binding.bottomNavigation

        requestPermissions()
        initButtons()
        reviveUserInputs(savedInstanceState)

        when (pageType) {
            PLANT_VIEW -> {
                supportActionBar?.title = getString(R.string.edit_plant)
                if (!isRenderedPlantEntry) {
                    getPlantEntryAndPopulateFields()
                    isRenderedPlantEntry = true
                } else {
                    binding.imageView.setImageURI(tempImgUri)
                }
            }

            PLANT_ADD -> {
                supportActionBar?.title = getString(R.string.add_new_plant)
                setUpTempImage()
            }
        }

        setUpViewModel()

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = getBitmap(tempImgUri)
                binding.imageView
                if(bitmap != null){
                    setPicture(addPlantViewModel, bitmap)
                }
            }
        }

        galleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = result.data?.data
                    if (selectedImageUri != null) {
                        copyImage(selectedImageUri, tempImgUri)
                        val bitmap = getBitmap(tempImgUri)
                        if(bitmap != null) {
                            setPicture(addPlantViewModel, bitmap)
                        }
                    }
                }
            }

        setUpSpeciesTextWatcher()
        bottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        navigationView.menu.getItem(DEFAULT_INDEX).isChecked = true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var position = intent.getIntExtra(getString(R.string.position_key), DEFAULT_POSITION)
        return when (item.itemId) {
            R.id.action_delete -> {
                deletePlant(position)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHECKED_TP_KEY, binding.terracottaRadioRoup.checkedRadioButtonId)
        outState.putInt(CHECKED_DH_KEY, binding.drainageRadioGroup.checkedRadioButtonId)
        outState.putString(NAME_KEY, binding.nameEditText.text.toString())
        outState.putString(SPECIES_KEY, binding.speciesAutocomplete.text.toString())
        outState.putString(POT_SIZE_KEY, binding.sizeEditText.text.toString())
        outState.putBoolean(RENDERED_PLANT_ENTRY, isRenderedPlantEntry)
        outState.putBoolean(IMAGE_NAME_SET, isImageNameSet)
        outState.putString(TEMP_IMG_URI, tempImgUri.toString())
        outState.putString(IMG_NAME_KEY, imageName)
        outState.putString(DOB_TEXT_KEY, binding.dob.text.toString())
        outState.putLong(CALENDAR_TIME_MILLIS, calendarTimeMillis)
        if(pageType == PLANT_VIEW){
            outState.putString(SAVED_IMG_URI, copyImgUri.toString())
        }
    }

    private fun setUpViewModel() {
        binding.speciesAutocomplete.threshold = DEFAULT_THRESHOLD

        addPlantViewModel = ViewModelProvider(this)[AddPlantViewModel::class.java]
        addPlantViewModel.image.observe(this) {
            binding.imageView.setImageBitmap(it)
        }
        addPlantViewModel.species.observe(this) {
            ArrayAdapter(this, android.R.layout.simple_list_item_1, it).also { adapter ->
                binding.speciesAutocomplete.setAdapter(adapter)
            }
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(BYTE_ARRAY_SIZE)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != INT_VAL_UNKNOWN) {
            output.write(buffer, FILE_COPY_OFFSET, bytesRead)
        }
    }

    private fun copyImage(from: Uri, to: Uri) {
        try {
            val inputStream: InputStream? = this.contentResolver.openInputStream(from)
            if (inputStream != null) {
                val outputStream: OutputStream? =
                    this.contentResolver.openOutputStream(to)
                if (outputStream != null) {
                    copyStream(inputStream, outputStream)
                    outputStream.close()
                }
                inputStream.close()
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                getString(R.string.file_permission_error_message), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setUpSpeciesTextWatcher() {
        binding.speciesAutocomplete.onItemClickListener =
            OnItemClickListener { _, _, pos, _ ->
                if (addPlantViewModel.id.value != null && pos < addPlantViewModel.id.value!!.size) {
                    speciesId = addPlantViewModel.id.value?.get(pos) ?: EMPTY_STRING
                } else {
                    println(getString(R.string.invalid_autocomplete_position))
                }
            }

        binding.speciesAutocomplete.addTextChangedListener(object : TextWatcher {
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

    private fun deletePlant(position: Int) {
        if (position != DEFAULT_POSITION) {
            userRef.child(getString(R.string.plants_firebase_key))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (position >= ZERO && position < snapshot.childrenCount) {
                            val plantSnapshot = snapshot.children.toList()[position]
                            val plantId = plantSnapshot.key
                            if (plantId != null) {
                                // Delete image from Firebase Storage
                                CoroutineScope(Dispatchers.IO).launch {
                                    val firebaseStorageRef =
                                        Firebase.storage.reference.child(imageName)
                                    firebaseStorageRef.delete().addOnFailureListener { exception ->
                                        Log.e(
                                            javaClass.simpleName,
                                            getString(
                                                R.string.error_deleting_image_from_firebase_storage,
                                                exception.message
                                            ), exception
                                        )
                                    }.await()
                                }

                                // Delete image from local file system
                                deleteImage()

                                // Delete plant
                                CoroutineScope(Dispatchers.IO).launch {
                                    userRef.child(getString(R.string.plants_firebase_key))
                                        .child(plantId).removeValue().await()
                                }
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(
                            getString(R.string.tag),
                            getString(R.string.failed_to_read_value),
                            error.toException()
                        )
                    }
                })

            Toast.makeText(
                this,
                getString(R.string.plant_deleted_toast_message), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpTempImage() {
        // Load the default image drawable
        val defaultImageDrawable =
            ContextCompat.getDrawable(this, R.drawable.default_plant_profile_pic)

        // Set the default image to the ImageView
        binding.imageView.setImageDrawable(defaultImageDrawable)

        if (!isImageNameSet) {
            imageName = generateImageName()
            isImageNameSet = true
        }

        // Get the external files directory
        val externalFilesDir = getExternalFilesDir(null)

        if (externalFilesDir != null) {
            // Create a File for the temp image
            tempImgFile = File(externalFilesDir, imageName)
            if (!tempImgFile.exists()) {
                // Convert the default image drawable to a Bitmap
                val bitmap = defaultImageDrawable?.toBitmap()

                // Save the Bitmap to the tempImgFile
                try {
                    val stream = FileOutputStream(tempImgFile)
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                tempImgUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.com_example_plantcare),
                    tempImgFile
                )
            }
            else {
                tempImgUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.com_example_plantcare),
                    tempImgFile
                )
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getPlantEntryAndPopulateFields() {
        CoroutineScope(Dispatchers.IO).launch {
            userRef.child(getString(R.string.plants_firebase_key)).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (position != INT_VAL_UNKNOWN && position >= ZERO && position < snapshot.children.toList().size) {
                        val plantEntry =
                            snapshot.children.toList()[position].getValue(Plant::class.java)
                        if (plantEntry != null) {
                            populateFields(plantEntry)
                            speciesId = plantEntry.plantSpeciesId ?: EMPTY_STRING
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(getString(R.string.tag), getString(R.string.failed_to_read_value), error.toException())
                }
            })
        }
    }

    private fun generateImageName(): String {
        val dateFormat = SimpleDateFormat(getString(R.string.img_date_format), Locale.getDefault())
        val currentTimeStamp = dateFormat.format(Date())
        return getString(R.string.img_name_format, currentTimeStamp)
    }

    private fun populateFields(plantEntry: Plant) {
        runOnUiThread {
            if (plantEntry.imageName != null) {
                val imgName = plantEntry.imageName
                imageName = imgName!!
                val firebaseStorageRef = Firebase.storage.reference.child(imageName)
                val externalFilesDir = getExternalFilesDir(null)
                if (externalFilesDir != null) {
                    tempImgFile = File(externalFilesDir, imageName)
                    if (!tempImgFile.exists()) {
                        // If the file doesn't exist, proceed with the download
                        firebaseStorageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                            // Successfully downloaded the byte array
                            try {
                                val stream = FileOutputStream(tempImgFile)
                                stream.write(bytes)
                                stream.flush()
                                stream.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            tempImgUri = FileProvider.getUriForFile(
                                this,
                                getString(R.string.com_example_plantcare),
                                tempImgFile
                            )
                        }.addOnFailureListener { exception ->
                            // Handle any errors that occurred during the download
                            Log.e(javaClass.simpleName, getString(R.string.error_downloading_image, exception.message), exception)
                        }
                    } else {
                        // If the file already exists, use it directly
                        tempImgUri = FileProvider.getUriForFile(
                            this,
                            getString(R.string.com_example_plantcare),
                            tempImgFile
                        )
                    }
                    copyImgFile = File(externalFilesDir, "copy_" + imageName)
                    copyImgUri = FileProvider.getUriForFile(
                        this,
                        getString(R.string.com_example_plantcare),
                        copyImgFile
                    )
                    copyImage(tempImgUri, copyImgUri)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            binding.imageView.setImageURI(tempImgUri)

            binding.nameEditText.setText(plantEntry.plantName)

            binding.speciesAutocomplete.setText(plantEntry.plantSpecies)

            binding.sizeEditText.setText(plantEntry.potSize.toString())

            if (plantEntry.drainageHoles != null) {
                if (plantEntry.drainageHoles == true) {
                    binding.yesDrainageRadioButton.isChecked = true
                } else {
                    binding.noDrainageRadioButton.isChecked = true
                }
            }

            if (plantEntry.terracottaPot != null) {
                if (plantEntry.terracottaPot == true) {
                    binding.yesTerracottaRadioButton.isChecked = true
                } else {
                    binding.noTerracottaRadioButton.isChecked = true
                }
            }

            if(plantEntry.adoptionDate != null){
                calendarTimeMillis = plantEntry.adoptionDate!!
                val dateFormat = SimpleDateFormat(getString(R.string.dd_mmm_yyyy), Locale.getDefault())
                val formattedDate = dateFormat.format(Date(calendarTimeMillis))

                val dobText = getString(R.string.dob_f_string, formattedDate)
                binding.dob.text = dobText
            }
        }
    }

    private fun reviveUserInputs(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val checkedTpRadioButtonId = savedInstanceState.getInt(CHECKED_TP_KEY, INT_VAL_UNKNOWN)
            confirmAndCheckRadioButton(checkedTpRadioButtonId)

            val checkedDhRadioButtonId = savedInstanceState.getInt(CHECKED_DH_KEY, INT_VAL_UNKNOWN)
            confirmAndCheckRadioButton(checkedDhRadioButtonId)

            val name = savedInstanceState.getString(NAME_KEY, EMPTY_STRING)
            val species = savedInstanceState.getString(SPECIES_KEY, EMPTY_STRING)
            val potSize = savedInstanceState.getString(POT_SIZE_KEY, EMPTY_STRING)
            val dobText = savedInstanceState.getString(DOB_TEXT_KEY, EMPTY_STRING)

            binding.nameEditText.setText(name)
            binding.speciesAutocomplete.setText(species)
            binding.sizeEditText.setText(potSize)
            binding.dob.text = dobText


            isRenderedPlantEntry = savedInstanceState.getBoolean(RENDERED_PLANT_ENTRY, false)
            isImageNameSet = savedInstanceState.getBoolean(IMAGE_NAME_SET, false)
            imageName = savedInstanceState.getString(IMG_NAME_KEY, EMPTY_STRING)
            tempImgUri = Uri.parse(savedInstanceState.getString(TEMP_IMG_URI, EMPTY_STRING))
            calendarTimeMillis = savedInstanceState.getLong(CALENDAR_TIME_MILLIS, System.currentTimeMillis())
            if(pageType == PLANT_VIEW) {
                copyImgUri = Uri.parse(savedInstanceState.getString(SAVED_IMG_URI, EMPTY_STRING))
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val dateFormat = SimpleDateFormat(getString(R.string.dd_mmm_yyyy), Locale.getDefault())
        calendarTimeMillis = calendar.timeInMillis
        val formattedDate = dateFormat.format(calendarTimeMillis)
        val dobText = getString(R.string.dob_f_string, formattedDate)
        binding.dob.text = dobText
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (pageType) {
            PLANT_ADD -> menuInflater.inflate(R.menu.common_toolbar_menu, menu)
            PLANT_VIEW -> menuInflater.inflate(R.menu.delete_toolbar_menu, menu)
        }

        return true
    }

    private fun confirmAndCheckRadioButton(id: Int) {
        if (id != INT_VAL_UNKNOWN) {
            val radioButton = findViewById<RadioButton>(id)
            if (radioButton != null) {
                radioButton.isChecked = true
            }
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET
                ), ZERO
            )
        }
    }

    private fun goToDashboard() {
        val signupActivityIntent = Intent(this, SignupActivity::class.java)
        startActivity(signupActivityIntent)
    }

    private fun initButtons() {
        // Back Button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(pageType == PLANT_VIEW) {
                    if(copyImgUri != tempImgUri) {
                        copyImage(copyImgUri, tempImgUri)
                    }
                }
                cleanUp()
                goToDashboard()
                finish()
            }
        })

        // Photo Button
        binding.photoButton.setOnClickListener {
            val pictureDialog = PictureDialogFragment()
            pictureDialog.show(supportFragmentManager, getString(R.string.tag))

            supportFragmentManager
                .setFragmentResultListener(getString(R.string.request_key_name), this) { _, bundle ->
                    when (bundle.getInt(getString(R.string.bundle_key_name))) {
                        PictureDialogFragment.OPEN_CAMERA -> openCamera()
                        PictureDialogFragment.OPEN_GALLERY -> openGallery()
                    }
                }
        }

        // DOB Button
        binding.dobButton.setOnClickListener {
            handleDateInput()
        }

        // Cancel Button
        binding.cancelButton.setOnClickListener {
            if(pageType == PLANT_VIEW) {
                if(copyImgUri != tempImgUri) {
                    copyImage(copyImgUri, tempImgUri)
                }
            }
            cleanUp()
            goToDashboard()
            finish()
        }

        // Add / Update Buttons
        if (pageType == PLANT_VIEW) {
            binding.addButton.text = getString(R.string.update)
            binding.addButton.setOnClickListener {
                updatePlant()
            }
        } else {
            binding.addButton.text = getString(R.string.add)
            binding.addButton.setOnClickListener {
                savePlantToDatabase()
            }
        }
    }

    private fun cleanUp()
    {
        if(pageType == PLANT_ADD){
            deleteImage()
        }
        else {
            deleteImageCopy()
        }
    }

    private fun deleteImage() {
        if (tempImgFile.exists()) {
            tempImgFile.delete()
        } else {
            Log.d(getString(R.string.tag), getString(R.string.oops_missing_external_file_directory))
        }
    }

    private fun deleteImageCopy() {
        if(copyImgFile.exists()){
            copyImgFile.delete()
        }else {
            Log.d(getString(R.string.tag), getString(R.string.oops_missing_external_file_directory))
        }
    }

    private fun updatePlant() {
        CoroutineScope(Dispatchers.IO).launch {
            userRef.child(getString(R.string.plants_firebase_key)).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (position != INT_VAL_UNKNOWN && position >= ZERO && position < snapshot.childrenCount) {
                        val plantSnapshot = snapshot.children.toList()[position]
                        val plantId = plantSnapshot.key
                        val plantEntry = plantSnapshot.getValue(Plant::class.java)

                        if (plantEntry != null) {
                            setPlantEntryAttributes(plantEntry)
                        }

                        if (plantId != null) {
                            userRef.child(getString(R.string.plants_firebase_key)).child(plantId).setValue(plantEntry)
                            deleteImageCopy()
                            goToDashboard()
                            finish()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(getString(R.string.tag), getString(R.string.failed_to_read_value), error.toException())
                }
            })
        }
    }

    private fun savePlantToDatabase() {
        val plantEntry = Plant()
        setPlantEntryAttributes(plantEntry)
        plantEntry.lastWateredDate = System.currentTimeMillis()
        val plantId = userRef.child(getString(R.string.plants_firebase_key)).push().key
        if (plantId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                userRef.child(getString(R.string.plants_firebase_key)).child(plantId).setValue(plantEntry).await()
                goToDashboard()
                finish()
            }
        }
    }

    private fun calculateWaterFreq(freq: String, plantEntry: Plant) : Int {
        var ret = if (freq == EMPTY_STRING) DEFAULT_FREQUENCY else freq.toInt()
        if (plantEntry.potSize != null && plantEntry.potSize != DOUBLE_ZERO) ret *= plantEntry.potSize!!.toInt().div(
            POT_SIZE_NORMALIZATION_FACTOR
        )
        if (plantEntry.terracottaPot == false){
            ret = (ret * TERRACOTTA_FACTOR).toInt()
        }
        if (plantEntry.drainageHoles == false){
            ret *= DRAINAGE_HOLE_FACTOR
        }
        return ret
    }

    private fun setPlantEntryAttributes(plantEntry: Plant) {
        plantEntry.plantName = binding.nameEditText.text.toString()
        plantEntry.plantSpecies = binding.speciesAutocomplete.text.toString()
        plantEntry.plantSpeciesId = speciesId
        var potSize = DOUBLE_ZERO
        val potSizeString = binding.sizeEditText.text.toString()
        if (potSizeString.isNotEmpty()) {
            potSize = potSizeString.toDouble()
        }
        plantEntry.potSize = potSize
        val freq = Helpers.getWateringFreq(speciesId)
        plantEntry.wateringFreq = calculateWaterFreq(freq, plantEntry)

        storeImageToCloud()

        plantEntry.imageName = imageName

        plantEntry.adoptionDate = calendarTimeMillis
        plantEntry.terracottaPot = binding.yesTerracottaRadioButton.isChecked
        plantEntry.drainageHoles = binding.yesDrainageRadioButton.isChecked
    }

    private fun storeImageToCloud() {
        CoroutineScope(Dispatchers.IO).launch {
            val firebaseStorageRef = Firebase.storage.reference.child(imageName)

            try {
                firebaseStorageRef.putFile(tempImgUri)
                    .addOnFailureListener { exception ->
                        Log.e(javaClass.simpleName, exception.message, exception)
                    }
                    .await()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun handleDateInput() {
        val datePickerDialog: DatePickerDialog

        val selectedDate = Date(calendarTimeMillis)
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        datePickerDialog = DatePickerDialog(
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

    private fun getBitmap(imgUri: Uri): Bitmap? {
        try {
            val inputStream = contentResolver.openInputStream(imgUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val exif = ExifInterface(imgUri.path.toString())

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    matrix.setRotate(90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    matrix.setRotate(180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    matrix.setRotate(270f)
                }
                else -> {
                    return bitmap // No need to rotate
                }
            }

            return Bitmap.createBitmap(
                bitmap,
                ZERO,
                ZERO,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun setPicture(viewModel: AddPlantViewModel, bitmap: Bitmap) {
        viewModel.image.value = bitmap
        imgToSave = bitmap
        saveImgFlag = true
    }

    private fun bottomNavigation() {
        navigationView.selectedItemId = R.id.add_plant
        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard_home -> {
                    if(pageType == PLANT_VIEW) {
                        if(copyImgUri != tempImgUri) {
                            copyImage(copyImgUri, tempImgUri)
                        }
                    }
                    cleanUp()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.calender -> {
                    if(pageType == PLANT_VIEW) {
                        if(copyImgUri != tempImgUri) {
                            copyImage(copyImgUri, tempImgUri)
                        }
                    }
                    cleanUp()
                    val intent = Intent(this, ScheduleActivity::class.java)
                    startActivity(intent)

                    return@setOnNavigationItemSelectedListener true
                }

                R.id.reminder -> {
                    if(pageType == PLANT_VIEW) {
                        if(copyImgUri != tempImgUri) {
                            copyImage(copyImgUri, tempImgUri)
                        }
                    }
                    cleanUp()
                    val intent = Intent(this, CalenderActivity::class.java)
                    startActivity(intent)

                    return@setOnNavigationItemSelectedListener true
                }

                else -> false
            }
        }
    }

    inner class MyRunnable : Runnable {
        override fun run() {
            try {
                val apiKey = BuildConfig.PLANT_API_KEY
                val url = URL("https://perenual.com/api/species-list?key=$apiKey&q=$query")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = getString(R.string.get)
                    inputStream.bufferedReader().use {
                        val speciesList = ArrayList<String>()
                        val idList = ArrayList<String>()
                        it.lines().forEach { line ->
                            val response = JSONObject(line)
                            val valueArray = response.getJSONArray(getString(R.string.data_prenual_key))
                            for (t in ZERO until valueArray.length()) {
                                val name = valueArray.getJSONObject(t).getString(getString(R.string.common_name_prenual_key))
                                speciesList.add(name)
                                val id = valueArray.getJSONObject(t).getString(getString(R.string.id_prenual_key))
                                idList.add(id)
                            }
                        }
                        runOnUiThread {
                            this@AddPlantActivity.addPlantViewModel.species.value = speciesList
                            this@AddPlantActivity.addPlantViewModel.id.value = idList
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
