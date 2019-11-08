package com.example.mymessenger


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import android.R.id.message
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Registration of new user by displaying progress bar
        button_register.setOnClickListener {
            //Function to display progress bar
            displayProgressBar(it)
        }


        //Launch Login activity
        already_have_account_textview.setOnClickListener {
            //Create intent
            //already_have_account_textview.setTextColor()
            val intent = Intent(this, LoginActivity::class.java)
            //Requires intent parameter
            startActivity(intent)
        }

        //Add the profile photo
        button_select_photo.setOnClickListener {

            val _intent = Intent(Intent.ACTION_PICK)
            _intent.type = "image/*"
            startActivityForResult(_intent,0 )
        }

    }

    fun displayProgressBar(view: View) {

        val emailString = email_textview_register.text.toString()
        val passwordString = password_textview_register.text.toString()
        //Check if email and password are empty, if empty do not show bar and give a message
        if(emailString.isEmpty() || passwordString.isEmpty())
        {
            Toast.makeText(this, "Enter valid credentials!",Toast.LENGTH_SHORT).show()
            return
        }

        var progress : ProgressDialog?=null
        progress = ProgressDialog(this)
        progress.setTitle("Registering...")
        progress.setMessage("Please wait.Checking user credentials...")
        progress.setCancelable(false)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        Thread(Runnable{
            run(){
                try {
                    Thread.sleep(3000)
                }catch (e: Exception){
                    e.printStackTrace()
                }
                progress.dismiss()
                // Calling the create user function
                //Firebase authentication create users
                createUser()


            }
        }).start()
        progress.show()

    }

    var selected_photo_uri : Uri?=null


    //This method is called after finishing of startActivityForResult
    //The purpose of calling this method here is to attach the profile photo in the select_photo button
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check whether photo selection was successful
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data !=null)
        {
            //Take uri which gives the path of the image selected
            selected_photo_uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selected_photo_uri)  //val bitmapdrawable = BitmapDrawable(bitmap)
           // button_select_photo.setBackgroundDrawable(bitmapdrawable)

            // Using Circular Image View
            circular_imageView_profile_photo.setImageBitmap(bitmap)
            //Hide the Select Photo button
            button_select_photo.alpha =  0f
        }


    }

    //Function to create new user
    private fun createUser()
    {
        val emailString = email_textview_register.text.toString()
        val passwordString = password_textview_register.text.toString()


        Log.d("MainActivity", "Email is: $emailString")
        Log.d("MainActivity", "Password is: $passwordString")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailString,passwordString)
            .addOnCompleteListener {
                if(!it.isSuccessful){       //if user is not registered successfully notify it
                    Toast.makeText(this, "User creation failed", Toast.LENGTH_SHORT).show()
                    //Check the error occurred and display it in logcat
                    val e = it.exception as FirebaseException
                    Toast.makeText(this,"Failed Registration: " + e.message, Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity","Failed Registration: " + e.message )
                    return@addOnCompleteListener
                }
                //if registration is successful
                else{
                Toast.makeText(this, "Registered successfully",Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "User creation successful with uid: $it.result.user.uid ")
                uploadProfilePhoto()
                val intent2 = Intent(this, LatestMessagesActivity::class.java)
                intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent2)
            }
    }
    }
    //Function to upload profile photo
    private fun uploadProfilePhoto()
    {
        if (selected_photo_uri != null)  //Check whether valid uri is present that is photo is supplied or not
        {
            Log.d("MainActivity", "Valid uri: $selected_photo_uri")
            //return
            val filename =  UUID.randomUUID().toString() //Generate unique identification code for the image
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename") //generate reference to upload
            //Uploading the image to database
            ref.putFile(selected_photo_uri!!)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Image uploaded : ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        val IMAGE_URL = it.toString()
                        Log.d("MainActivity", "File Location: : ${it.toString()}")

                        //Function to save user details (username, uid, profileImageUrl) to database
                        saveDataToDatabase(IMAGE_URL)       //it gives the url of the image uploaded here
                        //which is required for saving into database
                    }
                }
        }else
        {
            //Save URL of the dummy image to database
            val dummyImageUrl : String= "https://image.flaticon.com/icons/svg/149/149071.svg"
            Log.d("MainActivity", "Saving dummy  Image URL to database as no photo supplied ")
            saveDataToDatabase(dummyImageUrl)
        }

    }

    private fun saveDataToDatabase(profileImageUrl: String)
    {
        val userUid = FirebaseAuth.getInstance().uid //Gives the unique identity uid of user
        //Check whether proper userUid is available
        if(userUid == null)
        {
            Log.d("MainActivity", "Invalid UID")
            return
        }
        //Generate reference for storing info as created while storing Image
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$userUid")
        //Structure storing user details to store on database
        val user = User(username_textview_register.text.toString(), userUid.toString(),profileImageUrl)
        //Store the user details using the created reference and user structure
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "Successfully stored user details on Firebase database")
            }
            .addOnFailureListener{
                Log.d("MainActivity", "Could not store user details on database: $it.message")
            }

    }




}



