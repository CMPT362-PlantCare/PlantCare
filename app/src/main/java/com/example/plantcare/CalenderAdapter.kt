package com.example.plantcare


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CalenderAdapter(private val context: Context,
                      private var plantEntryList: List<Plant>) : BaseAdapter() {

    private lateinit var tempImgFile: File
    private lateinit var tempImgUri: Uri

    override fun getCount(): Int {
        return plantEntryList.size
    }

    override fun getItem(position: Int): Any {
        return plantEntryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: ViewHolder
        var view = convertView
        var isWateredToday = true
        //var status = 0

        if(view == null){
            val inflater = (context as Activity).layoutInflater
            view = inflater.inflate(R.layout.activity_gridview_calender_item, parent, false)

            viewHolder = ViewHolder()
            viewHolder.relativeLayout = view!!.findViewById(R.id.expandableView)
            viewHolder.circularRevealCardView = view!!.findViewById(R.id.cardV)
            viewHolder.imageButton = view!!.findViewById(R.id.arrowBtn)
            viewHolder.imageButton2 = view!!.findViewById(R.id.watercan)
            viewHolder.materialTextView = view!!.findViewById(R.id.txtwaterfreq)

            viewHolder.imageView = view!!.findViewById(R.id.plantImage)
            viewHolder.textView = view!!.findViewById(R.id.plantName)


            view.tag = viewHolder
        }
        else{
            viewHolder = view.tag as ViewHolder
        }

        val imageName = plantEntryList[position].imageName

        val firebaseStorageRef = FirebaseStorage.getInstance().reference.child(imageName!!)

        val externalFilesDir = context.getExternalFilesDir(null)
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
                        context,
                        context.getString(R.string.com_example_plantcare),
                        tempImgFile
                    )
                    viewHolder.imageView!!.setImageURI(tempImgUri)
                }.addOnFailureListener { exception ->
                    // Handle any errors that occurred during the download
                    Log.e(javaClass.simpleName, context.getString(R.string.error_downloading_image, exception.message), exception)
                }
            } else {
                // If the file already exists, use it directly
                tempImgUri = FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.com_example_plantcare),
                    tempImgFile
                )
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
            ).show()
        }

        viewHolder.textView!!.text = plantEntryList[position].plantName
        val wfreq = plantEntryList[position].wateringFreq.toString()
        val waterFreq = "In " + wfreq  + " days / Every " +  wfreq + " days"
        viewHolder.materialTextView!!.text = waterFreq


        viewHolder.imageButton!!.setOnClickListener { view ->
            // If the CardView is already expanded, set its visibility
            // to gone and change the expand less icon to expand more.
            if (viewHolder.relativeLayout!!.getVisibility() === View.VISIBLE) {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(
                    viewHolder.circularRevealCardView,
                    AutoTransition()
                )
                viewHolder.relativeLayout!!.setVisibility(View.GONE)
                viewHolder.imageButton!!.setBackgroundResource(R.drawable.arrowdown);
            } else {
                TransitionManager.beginDelayedTransition(
                    viewHolder.circularRevealCardView,
                    AutoTransition()
                )
                viewHolder.relativeLayout!!.setVisibility(View.VISIBLE)
                viewHolder.imageButton!!.setBackgroundResource(R.drawable.arrowup);
            }
        }

        viewHolder.imageButton2!!.setOnClickListener { view ->
            if (isWateredToday) {
                //val bgcolor = ContextCompat.getColor(R.color.grey)
                viewHolder.imageButton2!!.setBackgroundResource(R.drawable.tickk);
                plantEntryList[position].status = 1
                viewHolder.circularRevealCardView!!.setVisibility(View.GONE)

            } else {
                viewHolder.imageButton2!!.setBackgroundResource(R.drawable.wateringcan);
                plantEntryList[position].status = 0
            }

            //isWateredToday = !isWateredToday; // reverse
        }

        view.setOnClickListener {

        }
        return view
    }

    fun replace(newPlantList: List<Plant>) {
        plantEntryList = newPlantList
    }
    internal class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
        var relativeLayout: RelativeLayout? = null
        var circularRevealCardView: CircularRevealCardView? = null
        var imageButton: ImageButton? = null
        var imageButton2: ImageButton? = null
        var materialTextView: MaterialTextView? = null

    }

}