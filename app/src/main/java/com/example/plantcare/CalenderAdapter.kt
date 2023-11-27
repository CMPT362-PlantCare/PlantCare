package com.example.plantcare


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import com.google.android.material.textview.MaterialTextView

private const val PLANT_VIEW = 1
class CalenderAdapter(private val context: Context,
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

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: ViewHolder
        var view = convertView
        var isWateredToday = true

        if(view == null){
            val inflater = (context as Activity).layoutInflater
            view = inflater.inflate(R.layout.activity_calender_recycler_item, parent, false)

            viewHolder = ViewHolder()
            viewHolder.relativeLayout = view!!.findViewById(R.id.expandableView)
            viewHolder.circularRevealCardView = view!!.findViewById(R.id.cardV)
            viewHolder.imageButton = view!!.findViewById(R.id.arrowBtn)
            viewHolder.imageButton2 = view!!.findViewById(R.id.watercan)
            viewHolder.materialTextView = view!!.findViewById(R.id.status)

            viewHolder.imageView = view!!.findViewById(R.id.plantImage)
            viewHolder.textView = view!!.findViewById(R.id.plantName)


            view.tag = viewHolder
        }
        else{
            viewHolder = view.tag as ViewHolder
        }

        val imageUri = Uri.parse(plantEntryList[position].imageUri)
        viewHolder.imageView!!.setImageURI(imageUri)
        viewHolder.textView!!.text = plantEntryList[position].plantName

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
                viewHolder.materialTextView!!.setText("All done")
                viewHolder.materialTextView!!.setBackgroundColor(R.color.green)
            } else {
                viewHolder.imageButton2!!.setBackgroundResource(R.drawable.wateringcan);
                viewHolder.materialTextView!!.setText("todo")
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