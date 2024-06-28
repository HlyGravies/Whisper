package com.example.whisper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.MyApplication.overMenu
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UserEditActivity : AppCompatActivity() {
    private lateinit var userNameEdit: EditText
    private lateinit var profileEdit: EditText
    private lateinit var changeButton: Button
    private lateinit var cancelButton: Button
    private lateinit var userImage: ImageView
    private lateinit var userIdText: TextView
    private lateinit var myApp: MyApplication
    private lateinit var overMenu: overMenu
    private var selectedImage: Uri? = null
    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        initViews()
        requestUserInfo()
        setupListeners()
    }

    private fun initViews() {
        userNameEdit = findViewById(R.id.userNameEdit)
        profileEdit = findViewById(R.id.profileEdit)
        changeButton = findViewById(R.id.changeButton)
        cancelButton = findViewById(R.id.cancelButton)
        userImage = findViewById(R.id.userImage)
        userIdText = findViewById(R.id.userIdText)
        myApp = application as MyApplication
        overMenu = overMenu(this)
    }

    private fun requestUserInfo() {
        val client = OkHttpClient()
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = "{\"userId\":\"${myApp.loginUserId}\"}"
        val request = Request.Builder()
            .url(myApp.apiUrl + "userInfo.php").post(requestBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseData = response.body?.string()
                    val json = JSONObject(responseData)

                    if (json.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@UserEditActivity, json.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            val userData = json.getJSONObject("userData")
                            val userName = userData.getString("userName")
                            val profile = userData.getString("profile")
                            val iconPath = userData.getString("iconPath")

                            userIdText.text = myApp.loginUserId
                            userNameEdit.text = Editable.Factory.getInstance().newEditable(userName)
                            profileEdit.text = Editable.Factory.getInstance().newEditable(profile)

                            if (iconPath.isNotEmpty()) {
                                myApp.iconPath = myApp.apiUrl + iconPath
                                Glide.with(this@UserEditActivity)
                                    .load(myApp.iconPath)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.avatar)
                                    .into(userImage)
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        userImage.setOnClickListener {
            val options = arrayOf("Thư viện", "Máy ảnh", "Hủy")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Chọn hình ảnh từ:")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
                    }
                    1 -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                        }
                    }
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }

        changeButton.setOnClickListener {
            val client = OkHttpClient()
            val userName = userNameEdit.text.toString()
            val profile = profileEdit.text.toString()

            val formBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", myApp.loginUserId)
                .addFormDataPart("userName", userName)
                .addFormDataPart("profile", profile)

             //Thêm ảnh vào form body nếu có chọn ảnh
            selectedImageBitmap?.let {
                val imageFile = createFileFromBitmap(it)
                formBodyBuilder.addFormDataPart("iconPath", imageFile.name, imageFile.asRequestBody("image/*".toMediaType()))
            }

            val requestBody = formBodyBuilder.build()
            Log.d("rrrrrrr", "onResponse: $requestBody")
            val request = Request.Builder()
                .url(myApp.apiUrl + "userUpdate.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    Log.d("llll", "onResponse: $responseData")
                    val json = JSONObject(responseData)

                    if (json.has("error")) {
                        runOnUiThread {
                            Toast.makeText(this@UserEditActivity, json.getString("error"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            // Start a new activity and finish the current one
                            val intent = Intent(this@UserEditActivity, UserInfoActivity::class.java)
                            intent.putExtra("userId", myApp.loginUserId)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            })
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun createFileFromBitmap(bitmap: Bitmap): File {
        val file = File(cacheDir, "${myApp.loginUserId}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        userImage.setImageURI(selectedImageUri)
                        // Convert the selected image to Bitmap
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    }
                }
                CAPTURE_IMAGE_REQUEST_CODE -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    userImage.setImageBitmap(photo)
                    // Save the captured image Bitmap for later use
                    selectedImageBitmap = photo
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Không cấp quyền thì không thể chụp ảnh cập nhật thông tin", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gọi onCreateOptionsMenu từ overMenu
        return overMenu.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gọi onOptionsItemSelected từ overMenu
        return overMenu.onOptionsItemSelected(item)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
        private const val CAPTURE_IMAGE_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102
    }
}
