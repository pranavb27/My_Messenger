package com.example.mymessenger

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_new_username.*
import org.w3c.dom.Text

/* This is the dialog in which the user will insert the new username */

class UsernameDialog : AppCompatDialogFragment(){

    companion object {
        var newUsername : String?=null
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view =  R.layout.dialog_new_username


            builder.setView(inflater.inflate(view, null))
                .setTitle("Enter Username ")
                // Add action buttons
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        //Getting the new Username
                        newUsername= getDialog()?.findViewById<EditText>(R.id.new_username)?.text.toString()

                        getDialog()?.dismiss()
                        Log.d("UsernameDialog", "New user name is : $newUsername")
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


}

