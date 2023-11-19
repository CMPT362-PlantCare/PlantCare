package com.example.plantcare

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult

class PictureDialogFragment: DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val OPEN_CAMERA = 0
        const val OPEN_GALLERY = 1
    }
    private lateinit var optionsListView: ListView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_picture,
            null)
        builder.setTitle("Pick plant picture")
        builder.setView(view)
        ret = builder.create()

        optionsListView = view.findViewById(R.id.pictureOptions)
        optionsListView.setOnItemClickListener(){ parent: AdapterView<*>, view: View, position: Int, id: Long ->
            setFragmentResult("requestKey", bundleOf("bundleKey" to position))
            dismiss()
        }

        return ret
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        if (p1 == DialogInterface.BUTTON_POSITIVE) {

        } else if (p1 == DialogInterface.BUTTON_NEGATIVE) {

        }
    }
}