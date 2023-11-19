package com.example.plantcare

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.plantcare.databinding.ActivityAddplantBinding
import java.io.File

class AddPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddplantBinding
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var tempImgFile: File
    private lateinit var tempImgUri: Uri
    private lateinit var imgToSave: Bitmap
    private var saveImgFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("creating add plant activity")
        binding = ActivityAddplantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tempImgFile = File(getExternalFilesDir(null), "tempImg")
        tempImgUri = FileProvider.getUriForFile(this, "com.example.plantcare", tempImgFile)
        initButtons()

        val myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        myViewModel.image.observe(this) {
            println("changed pic")
            binding.imageView.setImageBitmap(it)
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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
//            add plant to db
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
}