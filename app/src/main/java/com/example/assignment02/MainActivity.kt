package com.example.assignment02

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.assignment02.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {


    private lateinit var progressDialog: ProgressDialog

    private lateinit var selectedFileRef: StorageReference

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

//                ref.downloadUrl
//                .addOnSuccessListener { uri ->
//                    progressDialog.show()
//
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.setDataAndType(uri, "application/pdf")
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    startActivity(intent)
//                }
//                .addOnFailureListener {
//                    progressDialog.show()
//
//                    Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
//                }

        }



        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Downloading...")
        progressDialog.setCancelable(false)

        displayFiles()

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val fileName = binding.listView.getItemAtPosition(position).toString()
            downloadFile(fileName)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCode && resultCode == RESULT_OK) {
            pdfPath = data!!.data!!
        }

    }

    private fun displayFiles() {
        val fileList = mutableListOf<String>()
        ref.listAll()
            .addOnSuccessListener { listResult ->
                for (file in listResult.items) {
                    if (file.name.endsWith(".pdf")) {
                        fileList.add(file.name)
                    }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileList)
                binding.listView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to get file list", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadFile(fileName: String) {
        selectedFileRef = ref.child("pdf/$fileName")
        val file = File.createTempFile(fileName.split('.')[0], ".pdf")
        selectedFileRef.getFile(file)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Download successful", Toast.LENGTH_SHORT).show()
                openPDF(file)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                progressDialog.setMessage("Downloading ${progress.toInt()}%")
            }
        progressDialog.show()
    }

    private fun openPDF(file: File) {
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(Uri.fromFile(file), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }

}