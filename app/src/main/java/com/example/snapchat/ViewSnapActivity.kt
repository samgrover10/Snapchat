package com.example.snapchat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutionException

class ViewSnapActivity : AppCompatActivity() {

    var snapImageView : ImageView? = null
    var captionTextView : TextView?  =null
    val mAuth = FirebaseAuth.getInstance()
    var keys :ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snap)
        keys = intent.getStringArrayListExtra("keys")

        snapImageView=findViewById(R.id.snapImageView)
        captionTextView = findViewById(R.id.captionTextView)

        captionTextView?.text = intent.getStringExtra("message")

        val task = imageDownloader()
        val myImage: Bitmap

        try {
            myImage = task.execute(intent.getStringExtra("imageurl")).get()!!
            snapImageView?.setImageBitmap(myImage)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


    }
    inner class imageDownloader : AsyncTask<String?, Void?, Bitmap?>() {
         override fun doInBackground(vararg params: String?): Bitmap? {
            try {
                val url = URL(params[0])
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.connect()
                val `in` = connection.inputStream
                return BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
               return null
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        var i = 0;
        var size = keys?.size

       FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser!!.uid).child("snaps").child(intent.getStringExtra("key")).removeValue()
         for(key in keys.orEmpty()){
        FirebaseDatabase.getInstance().getReference().child("users").child(key).child("snaps").orderByChild("imagename").equalTo(intent.getStringExtra("imagename")).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    //do nothing
                    Log.i("info","exists")
                }else{
                    i++
                    Log.i("info","NOT exists")
                    Log.i("info", "$i users")
                    if(i == size){
                        Log.i("info","Everyone viewed our SNAP!")
                        FirebaseStorage.getInstance().getReference().child("images").child(intent.getStringExtra( "imagename")).delete()
                    }
                }
            }

        })
         }

    }


}
