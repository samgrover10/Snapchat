package com.example.snapchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    var emailEditText: EditText? = null
    var passEditText: EditText?= null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.emailEditText)
        passEditText = findViewById(R.id.passEditText)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            logIn()
        }
    }

    fun goClicked(view: View){
        auth.signInWithEmailAndPassword(emailEditText?.text.toString(), passEditText?.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i("info","login")
                    logIn()
                } else {
                    // If sign in fails, display a message to the user.
                   auth.createUserWithEmailAndPassword(emailEditText?.text.toString(),passEditText?.text.toString())
                       .addOnCompleteListener(this){ task ->
                           if(task.isSuccessful){
                               FirebaseDatabase.getInstance().getReference().child("users").child(task.result?.user?.uid!!).child("email").setValue(emailEditText?.text.toString())
                               Log.i("info","signup")
                               logIn()
                           }else{
                               Toast.makeText(this,"SignUp Failed!",Toast.LENGTH_SHORT)
                           }
                       }
                }

            }
    }

    fun logIn(){
        //GO TO NEXT ACTIVITY
        val intent = Intent(this,snapsActivity::class.java)
        startActivity(intent)
    }
}
