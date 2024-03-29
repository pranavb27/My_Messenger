package com.example.mymessenger

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_new_username.*
import kotlinx.android.synthetic.main.profile_information.*
import org.w3c.dom.Text

/* This is the dialog in which the user will insert the new username */

class UsernameDialog : AppCompatDialogFragment(){

    companion object {
        var newUsername : String?=null
    }
    var CurrentUser: User ?=null



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



                        if (newUsername.toString().isEmpty())
                        {
                            return@OnClickListener
                        }

                        Log.d("UsernameDialog", "New user name is : $newUsername")
                        dialog.dismiss()
                        //getDialog()?.dismiss()
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        //getDialog()?.cancel()
                        dialog.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }



    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
       if (newUsername?.isEmpty()!!)
       {
           return
           //actual_username.text = CurrentUser?.username
       }
       else
       {
           updateUsername(newUsername.toString())


       }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        return
    }


    private  fun updateUsername(newUsername : String) {
        CurrentUser = LatestMessagesActivity.currentUser

        Log.d("UsernameDialog", "Got the username correctly $newUsername")

        //Get the current user
        //val userRef = FirebaseDatabase.getInstance().getReference("/Users/${CurrentUser?.uid}")
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("${CurrentUser?.uid}")
        Log.d("UsernameDialog", "Uid: ${CurrentUser?.uid}")


        userRef.child("/username").setValue(newUsername)
            .addOnCompleteListener {
                Log.d(
                    "UsernameDialog",
                    "Successfully updated username:  ${CurrentUser?.uid} ${CurrentUser?.username}"
                )
            }
            .addOnFailureListener {
                Log.d("PUsernameDialog", "Username update failed: ${it.message}")
            }

    }


}

