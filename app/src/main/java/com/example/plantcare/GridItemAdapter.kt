package com.example.plantcare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

private const val PLANT_VIEW = 1
class GridItemAdapter(private val context: Context,
                      private var plantEntryList: List<PlantEntry>) : BaseAdapter() {

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
            viewHolder.textView = view!!.findViewById(R.id.textView)

            view.tag = viewHolder
        }
        else{
            viewHolder = view.tag as ViewHolder
        }

        val imageUri = Uri.parse(plantEntryList[position].imageUri)
        viewHolder.imageView!!.setImageURI(imageUri)
        viewHolder.textView!!.text = plantEntryList[position].plantName

        view.setOnClickListener {
            val intent = Intent(context, AddPlantActivity::class.java)
            intent.putExtra(context.getString(R.string.position_key), position)
            intent.putExtra(context.getString(R.string.plant_page_type), PLANT_VIEW )
            context.startActivity(intent)
        }

        return view
    }

    fun replace(newPlantList: List<PlantEntry>) {
        plantEntryList = newPlantList
    }

    internal class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

}