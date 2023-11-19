package com.example.plantcare

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class GridItemAdapter(private val context: Context, private val arrayImg: Array<Int>, private val arrayTxt: Array<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return arrayImg.size
    }

    override fun getItem(position: Int): Any {
        return arrayImg[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
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
        viewHolder.imageView!!.setImageResource(arrayImg[position])
        viewHolder.textView!!.text = arrayTxt[position]

        return view
    }

    class ViewHolder {

        var imageView: ImageView? = null
        var textView: TextView? = null

    }
}