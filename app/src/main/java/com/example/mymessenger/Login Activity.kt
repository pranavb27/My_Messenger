package com.example.mymessenger

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Back to register screen
        back_to_register_textview.setOnClickListener {
            finish()
        }
        //Signing in without showing progress bar
   /*     button_login.setOnClickListener {
            userSignIn()
        }*/
    }

    private fun userSignIn()
    {
        val emailString = email_textview_login.text.toString()
        val passwordString = password_textview_login.text.toString()

        //Check if email and password are proper
       /* if(emailString.isEmpty() || passwordString.isEmpty())
        {
            Toast.makeText(this, "Enter text in email and password!",Toast.LENGTH_SHORT).show()
            return
        }
       */
        Log.d("LoginActivity", "Email is: $emailString")
        Log.d("LoginActivity", "Password is: $passwordString")


        //FireBase Authentication for login
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailString, passwordString)
            .addOnCompleteListener {
                if ( !it.isSuccessful){     //If login Failed
                    val e  = it.exception as  FirebaseException
                    Toast.makeText(this, "Login Failed: $e.message",Toast.LENGTH_LONG).show()
                    Log.d("LoginActivity","Login Failed: $e.message" )

                }
                else    //If login successful
                {
                    Toast.makeText(this, "Logged in successfully!",Toast.LENGTH_LONG).show()
                    Log.d("LoginActivity", "User logged in successfully with uid: $it.result.user.uid")

                    //Start the messages Activity
                    val intent2 = Intent(this, LatestMessagesActivity::class.java)
                    intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent2)
                }

            }

    }
    // Function to show a progress bar while logging in user account
    fun b1(view: View) {
        val emailString = email_textview_login.text.toString()
        val passwordString = password_textview_login.text.toString()

        //Check if email and password are empty, if not do not show progress bar and return with message
        if(emailString.isEmpty() || passwordString.isEmpty())
        {
            Toast.makeText(this, "Enter text in email and password!",Toast.LENGTH_SHORT).show()
            return
        }

        var progress : ProgressDialog ?=null
        progress = ProgressDialog(this)
        progress.setTitle("Logging in...")
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
                // Calling the user Sign in function
                userSignIn()

            }
        }).start()
        progress.show()


    }
}