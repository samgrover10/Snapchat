package com.example.snapchat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ChooseUsersActivity : AppCompatActivity() {

    var usersListView: ListView? = null
    var emails:ArrayList<String> = ArrayList()
    var keys :ArrayList<String> =  ArrayList()
    var imageName : String? = null
    var imageUrl : String? = null
    var message : String? = null
    var sendButton :Button? = null
    var keyToSend : ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_users)
        val intent = getIntent()
        imageName = intent.getStringExtra("imagename")
        imageUrl= intent.getStringExtra("imageurl")
        message = intent.getStringExtra("message")

        usersListView = findViewById(R.id.usersListView)
        usersListView?.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
        sendButton = findViewById(R.id.sendButton)
        sendButton?.visibility = View.INVISIBLE


        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,emails)

        usersListView?.adapter = adapter

        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
             var email = p0.child("email").value as String
                emails.add(email)
                keys.add(p0.key!!)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

        })

        usersListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var checkedTextView = view as CheckedTextView
            if(checkedTextView.isChecked){
                keyToSend.add(keys.get(position))
                sendButton?.visibility = View.VISIBLE
                checkedTextView.setTextColor(Color.BLUE)
            }else{
                keyToSend.remove(keys.get(position))
                checkedTextView.setTextColor(Color.BLACK)

                if(keyToSend.size == 0){
                    sendButton?.visibility = View.INVISIBLE
                }
            }
        }
    }


    fun sendClicked(view: View){
        val snapMap:Map<String,String> = mapOf("from" to FirebaseAuth.getInstance().currentUser!!.email.toString(),"imagename" to imageName!!,"imageurl" to imageUrl!!,"message" to message!!)

       for(key in keyToSend){
           FirebaseDatabase.getInstance().getReference().child("users").child(key).child("snaps").push().setValue(snapMap)
       }

        val intent = Intent(this,snapsActivity::class.java)
        intent.putExtra("keys",keys!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}
