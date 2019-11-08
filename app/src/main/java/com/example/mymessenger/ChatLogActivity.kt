package com.example.mymessenger

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
//import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    var toUser : User ?=null
     val tag = "ChatLogActivity"
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        //supportActionBar?.title = "Chat Log"
        //Getting the user details from new message activity
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USERKEY)
        supportActionBar?.title = toUser?.username


        recycler_view_chatlog.adapter = adapter

        //setupDummyText()
        listenForMessages()

        //Sending messages
        button_send_chatLog.setOnClickListener {

            Log.d(tag, "Attempt to send message...")
            PerformSendMessage()

        }



    }

    private fun listenForMessages(){
        val mcontext = this


        val fromId = LatestMessagesActivity.currentUser?.uid.toString()
        val toId = toUser?.uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/User-Messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatMessage ::class.java)
              if(chatMessage != null) {
                  Log.d(tag, chatMessage.text)


                  if (chatMessage.fromId == LatestMessagesActivity.currentUser?.uid) {
                      val currentUser = LatestMessagesActivity.currentUser
                      adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                  } else {
                      //val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USERKEY)
                      adapter.add(ChatToItem(chatMessage.text, toUser!!))
                  }

              }
                else
              {
                  //Show Toast
                  Toast.makeText(mcontext, "No messages to show!",Toast.LENGTH_SHORT).show()
              }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })



    }



    private fun PerformSendMessage() {
        //    val reference = FirebaseDatabase.getInstance().getReference("/Messages").push()
        val user =
            intent.getParcelableExtra<User>(NewMessageActivity.USERKEY) //getting user to access receivers uid
        val fromId = FirebaseAuth.getInstance().uid.toString()
        val toId = user.uid.toString()
        val text = enter_message_chatLog.text.toString()
        if (text.isEmpty())
        {
            Log.d("ChatLog", "Empty message!")
            return
        }


        val reference =
            FirebaseDatabase.getInstance().getReference("/User-Messages/$fromId/$toId").push()
        //reference to the user who we are sending message
        val toReference =
            FirebaseDatabase.getInstance().getReference("/User-Messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(
            reference.key.toString(),
            text,
            fromId,
            toId,
            System.currentTimeMillis() / 1000
        )


        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(tag, "Saved our chat message: ${reference.key}")
                //This statement clears the text in enter message chatLog after send button is pressed
                enter_message_chatLog.text.clear()
                //This automatically scrolls the messages to extreme down
                recycler_view_chatlog.scrollToPosition(adapter.itemCount - 1)

            }

        toReference.setValue(chatMessage)

        val latestMessageReference  = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageReference.setValue(chatMessage)
        val latestMessagetoReference  = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessagetoReference.setValue(chatMessage)
}


}

class ChatFromItem(val Text: String, val currentUser : User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewFromRow.text=Text
        //Load profile photo
        val uri = currentUser.profileImageUrl
        val targetImageView = viewHolder.itemView.ProfilePhotoFromRow
        Picasso.get().load(uri).into(targetImageView)


    }


}

class ChatToItem(val Text : String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewToRow.text = Text

        //Load profile Photo

        //Load profile photo
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.ProfilePhotoToRow
        Picasso.get().load(uri).into(targetImageView)

       // Glide.with(this as Context).load(uri).into(targetImageView)
        //Log.d("ChatLogActivity" , "Done loading ProfilePhoto:  $uri")


    }


}
