package com.example.mymessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.row_user_details.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        //Change th title of support action Bar
        supportActionBar?.title = "Select User"

        //Setting up recycler view
//        val adapter = GroupAdapter<GroupieViewHolder>()
//
          fetchUsers()
//        //Adding contents to adapter to show
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        receyclerView_for_userList.adapter = adapter
    }

    companion object{
        val USERKEY = "USER KEY"
    }

    //Function to get the user details from database
    private fun fetchUsers(){




        val ref = FirebaseDatabase.getInstance().getReference("/Users")     //empty path as no users node, updated later
        //val image_ref = FirebaseStorage.getInstance().getReference("/images/")
        //Single value event refers to each o the user in the database for which info is gathered
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                //Creating adapter for recycler
                val adapter = GroupAdapter<GroupieViewHolder>()
                //p0 refers to the data snapshot of all the users collected one by one
                p0.children.forEach {
                    //from this we can extract all the details of a single user, for all users
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    //Creating adapter for recycler

                    if (user != null && user.uid != LatestMessagesActivity.currentUser?.uid) {
                        adapter.add(UserItem(user))
                        Log.d("NewMessage", "Adapter present")

                    }


                }
                    receyclerView_for_userList.adapter = adapter
                    //When a user item is clicked open chatLog
                    adapter.setOnItemClickListener { item, view ->
                        //Passing the user details to ChatLog Activity
                        val userItem = item as UserItem
                        val intent3 = Intent(view.context, ChatLogActivity::class.java)
                        intent3.putExtra(USERKEY, userItem._user)
                        startActivity(intent3)

                        finish()

                    }
                     receyclerView_for_userList.adapter = adapter


            }
            override fun onCancelled(p0: DatabaseError) {


            }

        })




    }
}

class UserItem(val _user :User ): Item<GroupieViewHolder>(){

    //Gets the layout to be displayed for each user row
    override fun getLayout(): Int {
        return R.layout.row_user_details

    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //Setting username and profile image got from the database
        viewHolder.itemView.textView_username_newMessage.text = _user.username
        Log.d("NewMessage","User uid $_user.uid ")

        if (_user.profileImageUrl.isNotEmpty()){
            Log.d("NewMessageActivity", "valid URL: $_user.profileImageUrl")


            Picasso.get().load(_user.profileImageUrl).into(viewHolder.itemView.imageView_newMessage)

        }else{
            Log.d("NewMessageActivity", "Invalid URL2: $_user.profileImageUrl")
        }

    }

}
