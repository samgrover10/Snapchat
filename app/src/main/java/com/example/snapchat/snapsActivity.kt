package com.example.snapchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class snapsActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    var snapsListView : ListView? = null
    var emails : ArrayList<String> = ArrayList()
    var snaps : ArrayList<DataSnapshot> = ArrayList()
    var keys :ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snaps)
        keys = intent.getStringArrayListExtra("keys")
        snapsListView= findViewById(R.id.snapsListView)
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,emails)
        snapsListView?.adapter = adapter

        FirebaseDatabase.getInstance().getReference().child("users").child(auth.currentUser!!.uid).child("snaps").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                emails.add(p0.child("from").value as String)
                snaps.add(p0)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {
                var index = 0
                 for(snap : DataSnapshot in snaps){
                     if(snap.key==p0.key){
                         snaps.removeAt(index)
                         emails.removeAt(index)
                     }
                     index++
                 }
                adapter.notifyDataSetChanged()
            }

        })
        snapsListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val snap = snaps.get(position)

            val intent = Intent(this,ViewSnapActivity::class.java)
            intent.putExtra("imagename",snap.child("imagename").value as String)
            intent.putExtra("imageurl",snap.child("imageurl").value as String)
            intent.putExtra("message",snap.child("message").value as String)
            intent.putExtra("key",snap.key)
            intent.putExtra("keys",keys)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.snap_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.create_snap){
          val intent = Intent(this,createSnapsActivity::class.java)
            startActivity(intent)
        }else if(item.itemId == R.id.logout){
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        auth.signOut()
        super.onBackPressed()
    }
}
