package com.example.scopedstorage_saveimage

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.scopedstorage_saveimage.databinding.ActivityMainBinding
import java.io.File
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {


    private val SDK_28_ABOVE = Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
    private val REQUEST_CODE =  101
//        private val PERMISSIONS_LEGACY = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        private val PERMISSIONS_SCOPED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//        private val PERMISSIONS = if (SDK_28_ABOVE) PERMISSIONS_SCOPED else PERMISSIONS_LEGACY


    private lateinit var takePhotoLauncher: ActivityResultLauncher<Void?>
    private lateinit var mainBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)


        lateInIt()

        mainBinding.btnCamera.setOnClickListener {
            takePhotoLauncher.launch(null)
        }
        mainBinding.btnUpload.setOnClickListener {
            val bitmap = mainBinding.ivFile.drawToBitmap()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            } else {
                val successful = saveImageToGallery(UUID.randomUUID().toString(),bitmap)
                // Permission already granted, proceed with the operation

                if (successful) {
                    Toast.makeText(this, "Photos saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }



            
        }


        
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            val bitmap = mainBinding.ivFile.drawToBitmap()
            val successful = saveImageToGallery(UUID.randomUUID().toString(),bitmap)
            // Permission already granted, proceed with the operation

            if (successful) {
                Toast.makeText(this, "Photos saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun lateInIt() {
        // camera photo
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                if (bitmap != null) {
                    mainBinding.ivFile.setImageBitmap(bitmap)

                }
            }
    }


    private fun saveImageToGallery(name:String,bitmap: Bitmap) : Boolean {
        val fos: OutputStream
        return try {
                val imageCollection = if (SDK_28_ABOVE) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                // metadata
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.WIDTH, bitmap.width)
                    put(MediaStore.Images.Media.HEIGHT, bitmap.height)
                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+ File.separator+"TestFolder")
                }

                val resolver = contentResolver
                val imageUri = resolver.insert(imageCollection,contentValues)
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
                bitmap.compress(Bitmap.CompressFormat.PNG,100,fos)
                Objects.requireNonNull<OutputStream>(fos)
                true

        }
        catch (e:Exception){
            Log.d("Usman Exception",e.toString())
            Toast.makeText(this,"Exception Occur",Toast.LENGTH_SHORT).show()
            false

        }


    }
}