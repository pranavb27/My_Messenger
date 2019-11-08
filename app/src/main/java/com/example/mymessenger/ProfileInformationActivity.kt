package com.example.mymessenger

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.profile_information.*

class ProfileInformationActivity : AppCompatActivity(){

    var CurrentUser : User ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Set the layout file made for profile information
        setContentView(R.layout.profile_information)
        //Toast.makeText(this, "Successfully reached profile info activity", Toast.LENGTH_SHORT).show()
        //Change the title of support Action Bar
        supportActionBar?.title = "Profile"
        //Get the currently logged in user details from latest Messages Activity
        // as they are required to display profile photo, username and email of current user
        CurrentUser = LatestMessagesActivity.currentUser

        //Loading Username, Email, profile photo
        actual_username.text = CurrentUser?.username
       // actual_email.text = CurrentUser?.????
        // user does not have email information
        val targetImageView = findViewById<CircleImageView>(R.id.info_profile_photo)
        Picasso.get().load(CurrentUser?.profileImageUrl).into(targetImageView)



        //On edit username
        val editUsername : ImageView = imageViewEditUsername
        editUsername.setOnClickListener {

            openDialog()
        }

    }


    private fun openDialog(){
        val dialog  = UsernameDialog()
        dialog.show(supportFragmentManager, "Username Dialog")

        val NewUsername : String ?= UsernameDialog.newUsername

        if (NewUsername != null)
            updateUsername(NewUsername)

    }


    private  fun updateUsername(newUserName : String){

        //Get the current user
        val user = FirebaseAuth.getInstance().currentUser
        val ProfileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUserName)
            .build()

        user?.updateProfile(ProfileUpdates)
            ?.addOnCompleteListener {
                val nu = CurrentUser?.username
                Log.d("ProfileInformation", "Successfully updated user Username $nu ${user.uid} ${CurrentUser?.uid}")
                actual_username.text = CurrentUser?.username
            }
            ?.addOnFailureListener {
                Log.d("ProfileInformation", "Error occurred : ${it.message}")
                Toast.makeText(this, "Username could not be updated!", Toast.LENGTH_SHORT).show()
            }

    }
}