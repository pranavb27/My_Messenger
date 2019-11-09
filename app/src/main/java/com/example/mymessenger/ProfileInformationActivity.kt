package com.example.mymessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.dialog_new_username.*
import kotlinx.android.synthetic.main.profile_information.*
import java.util.*

class ProfileInformationActivity : AppCompatActivity(){

    var CurrentUser : User ?=null
    val activityTag = "ProfileInformation"
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
        val Actualusername  = findViewById<TextView>(R.id.actual_username)
        Actualusername.text = CurrentUser?.username
       // actual_email.text = CurrentUser?.????
        // user does not have email information
        val targetImageView = findViewById<CircleImageView>(R.id.info_profile_photo)
        Picasso.get().load(CurrentUser?.profileImageUrl).into(targetImageView)

        //On change profile Photo
        info_profile_photo.setOnClickListener {

            updateProfilePhoto()
        }



        //On edit username
        val editUsername : ImageView = imageViewEditUsername
        editUsername.setOnClickListener {
            editUsername()
        }

    }

    //Function to update username
    private fun editUsername(){
        //Function to open dialog required to enter new username
        openDialog()

        val NewUsername = UsernameDialog.newUsername.toString()
        Log.d(activityTag, "Got the username correctly here and passed $")

    }

    //Function to open Username Dialog
    private fun openDialog(){
        val dialog  = UsernameDialog()
        dialog.show(supportFragmentManager, "Username Dialog")

    }


    //Function to update profile photo
    private  fun updateProfilePhoto(){

        val intent = Intent(Intent.ACTION_PICK)     //Set intent action
        intent.type = "image/*"                     //Set the intent type
        startActivityForResult(intent,  1)
    }
    var newPhotoUri : Uri?= null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            //getting new photo uri
            newPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, newPhotoUri)
            //Setting the new photo in circulr image view in profile information
            info_profile_photo.setImageBitmap(bitmap)
            Log.d(activityTag,"New display photo set successfully")

            uploadNewPhotoToStorage(newPhotoUri!!)
        }
    }

    private fun uploadNewPhotoToStorage(photoUri : Uri){

        if(photoUri == null)
        {
            Log.d(activityTag, "Invalid Uri: ${photoUri}")
            return
        }
       // val imageFile = CurrentUser?.uid.toString()

        //First put the file into Firebase storage thn download URL and then save it to database
        val imageFile = UUID.randomUUID().toString()   //Generate unique identification code for the image
        //Get Firebase storage reference
        /*val fstorage = FirebaseStorage.getInstance().getReference("/images/${CurrentUser?.profileImageUrl}")
        //Delete old profile photo
        fstorage.delete()
            .addOnSuccessListener {
                Log.d(activityTag, "Successfully deleted old profile photo")
            }
            .addOnFailureListener {
                Log.d(activityTag, "Could not delete old profie photo: ${it.message}")
            }*/
        //Add the new photo
        val fstorage2 = FirebaseStorage.getInstance().getReference("/images/${imageFile}")
        fstorage2.putFile(photoUri)
            .addOnSuccessListener {
                Log.d(activityTag, "Successfully uploaded image with uri ${photoUri}")
                //Download URl
                fstorage2.downloadUrl.addOnSuccessListener {
                    val imageUrl = it.toString()

                    //Update URL in database
                    updateUrlTODatabase(imageUrl)
                }
            }
            .addOnFailureListener {
                Log.d(activityTag, "Could not upload image: ${it.message}")
            }



    }


    //Function to update image url in database
    private fun updateUrlTODatabase(imageUrl: String){
        if(imageUrl == null)
        {
            Log.d(activityTag, "Inavalid URL : $imageUrl")
            return
        }


        //Get the database reference
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child("${CurrentUser?.uid}")

        userRef.child("/profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d(activityTag, "Successfully updated new photo url to database: $imageUrl")
            }
            .addOnFailureListener {
                Log.d(activityTag, "Could not update image url: ${it.message}" )
            }

    }









}