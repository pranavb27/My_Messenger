package com.example.mymessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*


class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser : User ?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recycler_view_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view_latest_messages.adapter = adapter
        //Check whether user is logged in or not
        verifyUserLoggedIn()
        fetchCurrentUser()
        listenLatestMessages()

        //Set item click listener on the adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d("LatestMessages", "Attempt to open chatLog")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as  LatestMessageRow
            intent.putExtra(NewMessageActivity.USERKEY,row.user)
            startActivity(intent)
        }

    }

    /*
    override fun onStart() {
        super.onStart()
        verifyUserLoggedIn()
    } */

    val adapter = GroupAdapter<GroupieViewHolder>()




    class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>() {

        var user: User?=null
        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.MessageLatestMessageRow.text = chatMessage.text
            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/Users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    user = p0.getValue(User :: class.java)
                    viewHolder.itemView.UsernameLatestMessageRow.text = user?.username
                    Picasso.get().load(user?.profileImageUrl).into(viewHolder.itemView.ProfilePhotoLatestMessageRow)
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })


        }
    }


        val latestMessageMap = HashMap<String, ChatMessage>()

        private fun refreshrecyclerviewmessage() {
            adapter.clear()
            latestMessageMap.values.forEach {
                adapter.add(LatestMessageRow(it))
            }
        }


    private fun listenLatestMessages()
    {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage :: class.java)?:return
                latestMessageMap[p0.key!!] = chatMessage
                refreshrecyclerviewmessage()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage :: class.java)?:return
                latestMessageMap[p0.key!!] = chatMessage
                refreshrecyclerviewmessage()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    //Getting the details of currently logged in user
    private  fun fetchCurrentUser()
    {
        val currentUserUid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$currentUserUid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                //Extracting the details of currently signed user
                currentUser  = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current User: ${currentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    //Check whether user is logged in or not
    private fun verifyUserLoggedIn()
    {
        //Check whether user is logged in or not by checking uid
        val uid = FirebaseAuth.getInstance().uid
        if(uid==null)   //if user is not logged in jump to register activity
        {
            Log.d("LatestMessagesActivity", "User is not signed in")
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clears the activity stack
            startActivity(intent2)

            finish()
        }
        else{
            Log.d("LatestMessagesActivity", "User is signed in")
        }



    }

    //Inflating the menu to create options like sign out, new messages etc
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigator_menu,menu)        //Set the new inflated menu from the menu directory
        //Navigator_menu
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //This is basically a switch case to handle all the options in the menu
        when(item.itemId)
        {
            R.id.menu_new_messages -> {
                //Launching the new messages activity
                val intent3 = Intent(this, NewMessageActivity :: class.java)
                startActivity(intent3)
            }

            R.id.menu_sign_out -> {
                //Perform Sign Out
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logged out successfully",Toast.LENGTH_SHORT).show()
                //Verify whether sign out was successful and go to Register Activity
                verifyUserLoggedIn()

            }

            R.id.ProfileMenu -> {
                Toast.makeText(this, "Show user information", Toast.LENGTH_SHORT).show()
                val intentP  = Intent(this, ProfileInformationActivity::class.java)
                startActivity(intentP)
            }

            R.id.SettingsMenu -> {
                Toast.makeText(this, "Show settings information", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
