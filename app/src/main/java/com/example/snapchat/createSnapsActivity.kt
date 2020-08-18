package com.example.snapchat

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class createSnapsActivity : AppCompatActivity() {

    var imageView:ImageView? = null
    var captionEditText:EditText?= null
    val imageName = UUID.randomUUID().toString() + ".jpg"
    var selectedImage: Uri?= null


    fun getPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_snaps)

        imageView = findViewById(R.id.imageView)
        captionEditText = findViewById(R.id.captionEditText)
    }
    fun chooseImageClicked(view: View){
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 1)
        } else {
            getPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        selectedImage = data?.data
        if(requestCode==1 && resultCode== Activity.RESULT_OK &&data!=null){
           try {
               val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
               imageView?.setImageBitmap(bitmap)
           }catch (e: Exception ){
               e.printStackTrace()
           }

        }
    }

    fun nextClicked(view: View){
        Toast.makeText(this,"This might take a few seconds...",Toast.LENGTH_SHORT).show()
        // Get the data from an ImageView as bytes
        Log.i("info","Next Clicked")
        imageView?.isDrawingCacheEnabled = true
        imageView?.buildDrawingCache()
        val bitmap = (imageView?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = FirebaseStorage.getInstance().getReference().child("images").child(imageName).putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this,"Upload Failed!",Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            Toast.makeText(this,"Upload Success!",Toast.LENGTH_SHORT).show()
            Log.i("info","UploadSuccess")

          }

        val ref =FirebaseStorage.getInstance().getReference().child("images/"+imageName)
        uploadTask = ref.putFile(selectedImage!!)


        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.i("URL",downloadUri.toString())
                val intent = Intent(this,ChooseUsersActivity::class.java)
                intent.putExtra("imagename",imageName)
                intent.putExtra("imageurl",downloadUri.toString())
                intent.putExtra("message",captionEditText?.text.toString())
                startActivity(intent)
            } else {
                Log.i("URL","ERROR")
            }
        }
    }
}
