package com.example.plantcare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GridItemAdapter(private val context: Context,
                      private var plantEntryList: List<Plant>) : BaseAdapter() {

    override fun getCount(): Int {
        return plantEntryList.size
    }

    override fun getItem(position: Int): Any {
        return plantEntryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: ViewHolder
        var view = convertView

        if(view == null){
            val inflater = (context as Activity).layoutInflater
            view = inflater.inflate(R.layout.activity_dashboard_grid_item, parent, false)

            viewHolder = ViewHolder()
            viewHolder.imageView = view!!.findViewById(R.id.imageView)
            viewHolder.textView = view.findViewById(R.id.textView)

            view.tag = viewHolder
        }
        else{
            viewHolder = view.tag as ViewHolder
        }

        setImage(plantEntryList[position].imageName!!, viewHolder)

        viewHolder.textView!!.text = plantEntryList[position].plantName

        view.setOnClickListener {
            val intent = Intent(context, PlantInfoActivity::class.java)
            intent.putExtra(context.getString(R.string.position_key), position)
            context.startActivity(intent)
        }
        return view
    }

    private fun setImage(imageName: String, viewHolder: ViewHolder) {
        val firebaseStorageRef = Firebase.storage.reference.child(imageName!!)
        val externalFilesDir = context.getExternalFilesDir(null)
        if (externalFilesDir != null) {
            var tempImgFile = File(externalFilesDir, imageName)
            // Check if the file exists
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
                    var tempImgUri = FileProvider.getUriForFile(
                        context,
                        context.getString(R.string.com_example_plantcare),
                        tempImgFile
                    )
                    viewHolder.imageView!!.setImageURI(tempImgUri)
                }.addOnFailureListener { exception ->
                    // Errors that occurred during the download
                    Log.e(javaClass.simpleName,
                        context.getString(R.string.error_downloading_image, exception.message), exception)
                }
            } else {
                // If the file already exists, use it directly
                var tempImgUri = FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.com_example_plantcare),
                    tempImgFile
                )
                viewHolder.imageView!!.setImageURI(tempImgUri)
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun replace(newPlantList: List<Plant>) {
        plantEntryList = newPlantList
    }

    internal class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

}