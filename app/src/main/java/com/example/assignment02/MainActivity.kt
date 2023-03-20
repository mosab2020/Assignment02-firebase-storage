package com.example.assignment02

import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.assignment02.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val storage = Firebase.storage
    private val ref = storage.reference

    private val reqCode: Int = 100
    lateinit var pdfPath: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnChoose.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, reqCode)
        }

        binding.btnUpload.setOnClickListener {
//
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading PDF")
            progressDialog.show()

            ref.child("pdf/" + UUID.randomUUID().toString())
                .putFile(pdfPath)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Upload Success", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Please Upload Image", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        binding.btnDownload.setOnClickListener {

            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Downloading File")
            progressDialog.show()

                ref.downloadUrl
                .addOnSuccessListener { uri ->
                    progressDialog.show()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
                .addOnFailureListener {
                    progressDialog.show()

                    Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
                }

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCode && resultCode == RESULT_OK) {
            pdfPath = data!!.data!!
        }


    }

}