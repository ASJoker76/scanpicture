package test.co.test

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import test.co.test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var type: Int = 0
    private lateinit var binding: ActivityMainBinding
    private var pDialog: SweetAlertDialog? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (type==0){
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    binding.imageView.setImageBitmap(imageBitmap)

                    // Proses pengenalan teks dari gambar
                    if (imageBitmap != null) {
                        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
                        pDialog!!.titleText = "Loading"
                        pDialog!!.setCancelable(false)
                        pDialog!!.show()
                        recognizeTextFromImage(imageBitmap)
                    }
                } else {
                    val imageUri: Uri? = data?.data
                    binding.imageView.setImageURI(imageUri)

                    if (imageUri != null) {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
                        pDialog!!.titleText = "Loading"
                        pDialog!!.setCancelable(false)
                        pDialog!!.show()
                        recognizeTextFromImage(imageBitmap)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectImageButton.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose an option")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePictureFromCamera()
                    1 -> pickImageFromGallery()
                }
                dialog.dismiss()
            }
            builder.show()
        }

    }

    private fun pickImageFromGallery() {
        type = 1
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun takePictureFromCamera() {
        type = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Dexter.withActivity(this)
                    .withPermissions(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.CAMERA,
                    ).withListener(object : MultiplePermissionsListener {

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report != null) {
                                when {
                                    report.areAllPermissionsGranted() -> {
                                        val takePictureIntent =
                                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                        pickImage.launch(takePictureIntent)
                                    }
                                    report.isAnyPermissionPermanentlyDenied -> {
                                        AlertDialog.Builder(
                                            this@MainActivity,
                                            R.style.Theme_Test
                                        ).apply {
                                            setMessage("please allow the required permissions")
                                                .setCancelable(false)
                                                .setPositiveButton("Settings") { _, _ ->
                                                    val reqIntent =
                                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                            .apply {
                                                                val uri = Uri.fromParts(
                                                                    "package",
                                                                    packageName,
                                                                    null
                                                                )
                                                                data = uri
                                                            }
                                                    //resultLauncher.launch(reqIntent)
                                                }
                                            setNegativeButton("Cancel") { dialog, _ ->
                                                dialog.cancel()
                                            }
                                            val alert = this.create()
                                            alert.show()
                                        }
                                    }
                                }
                            }
                        }
                    }).check()
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                pickImage.launch(takePictureIntent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Dexter.withActivity(this)
                    .withPermissions(
                        Manifest.permission.CAMERA
                    ).withListener(object : MultiplePermissionsListener {

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report != null) {
                                when {
                                    report.areAllPermissionsGranted() -> {
                                        val takePictureIntent =
                                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                        pickImage.launch(takePictureIntent)
                                    }
                                    report.isAnyPermissionPermanentlyDenied -> {
                                        AlertDialog.Builder(
                                            this@MainActivity,
                                            R.style.Theme_Test
                                        ).apply {
                                            setMessage("please allow the required permissions")
                                                .setCancelable(false)
                                                .setPositiveButton("Settings") { _, _ ->
                                                    val reqIntent =
                                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                            .apply {
                                                                val uri = Uri.fromParts(
                                                                    "package",
                                                                    packageName,
                                                                    null
                                                                )
                                                                data = uri
                                                            }
                                                    //resultLauncher.launch(reqIntent)
                                                }
                                            setNegativeButton("Cancel") { dialog, _ ->
                                                dialog.cancel()
                                            }
                                            val alert = this.create()
                                            alert.show()
                                        }
                                    }
                                }
                            }
                        }
                    }).check()
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                pickImage.launch(takePictureIntent)
            }
        }
    }

    private fun recognizeTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = TextRecognizerOptions.Builder()
            .build()

        val recognizer = TextRecognition.getClient(options)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                // Mengolah hasil pengenalan teks
                val recognizedText = text.text
                Log.d("TextRecognition", "Recognized Text: $recognizedText")
                // Lakukan tindakan berikutnya sesuai kebutuhan Anda dengan hasil teks yang dikenali.
                // Check if the recognized text contains operators (+, -, /, x)
                if (recognizedText.contains("+") || recognizedText.contains("-") ||
                    recognizedText.contains("/") || recognizedText.contains("x")
                ) {
                    // Use regular expressions to extract the two numbers and the operator
                    val pattern = "([0-9]+)([+\\-x/])([0-9]+)".toRegex()
                    val matchResult = pattern.find(recognizedText)

                    if (matchResult != null) {
                        val firstNumber = matchResult.groupValues[1].toInt()
                        val operator = matchResult.groupValues[2]
                        val secondNumber = matchResult.groupValues[3].toInt()

                        val result = when (operator) {
                            "+" -> firstNumber + secondNumber
                            "-" -> firstNumber - secondNumber
                            "x" -> firstNumber * secondNumber
                            "/" -> firstNumber / secondNumber
                            else -> 0
                        }

                        pDialog!!.dismissWithAnimation()
                        // Display the result in your TextView
                        binding.tvHasil.text =
                            "output = $firstNumber $operator $secondNumber = $result"
                    } else {
                        pDialog!!.dismissWithAnimation()
                        binding.tvHasil.text = recognizedText
                    }
                } else {
                    pDialog!!.dismissWithAnimation()
                    binding.tvHasil.text = recognizedText
                }
            }
            .addOnFailureListener { e ->
                pDialog!!.dismissWithAnimation()
                Log.e("TextRecognition", "Error recognizing text: ${e.message}", e)
            }
    }
}