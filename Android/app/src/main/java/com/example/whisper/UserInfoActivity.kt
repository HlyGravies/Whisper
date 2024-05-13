package com.example.whisper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.MyApplication.MyApplication

class UserInfoActivity : AppCompatActivity() {
    // 1-1. Declare the objects defined in the screen design as variables.
    private lateinit var userInfoText: TextView
    private lateinit var userImage: ImageView
    private lateinit var textView2: TextView
    private lateinit var linearLayout2: LinearLayout
    private lateinit var textView7: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRecycle: RecyclerView
    private lateinit var myApp: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // Initialize the variables
        userInfoText = findViewById(R.id.userInfoText)
        userImage = findViewById(R.id.userImage)
        textView2 = findViewById(R.id.textView2)
        linearLayout2 = findViewById(R.id.linearLayout2)
        textView7 = findViewById(R.id.textView7)
        radioGroup = findViewById(R.id.radioGroup)
        userRecycle = findViewById(R.id.userRecycle)
        myApp = application as MyApplication

        // 1-2. Get the target user ID from the Intent (previous screen).
        val userId = intent.getStringExtra("userId")

        // 1-3. Call the common execution method to execute the User Whisper Information Retrieval API.
        getUserWhisperInfo(myApp, userId, myApp.loginUserId, textView2, textView7, textView2, textView2, Button(this), userRecycle, radioGroup)

        // 1-4. Create a check change event listener for radioGroup.
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 1-4-1. Call the common execution method to execute the User Whisper Information Retrieval API and update to the latest state.
            getUserWhisperInfo(myApp, userId, myApp.loginUserId, textView2, textView7, textView2, textView2, Button(this), userRecycle, radioGroup)
        }

        // 1-5. Create a click event listener for followCntText.
        textView2.setOnClickListener {
            // 1-5-1. Set the target user ID in the intent.
            // 1-5-2. Set the string follow in the intent.
            // 1-5-3. Transition to the follow list screen.
        }

        // 1-6. Create a click event listener for followerText.
        textView2.setOnClickListener {
            // 1-6-1. Set the target user ID in the intent.
            // 1-6-2. Set the string follower in the intent.
            // 1-6-3. Transition to the follow list screen.
        }

        // 1-7. Create a click event listener for followButton.
        Button(this).setOnClickListener {
            // 1-7-1. Request the Follow Management Processing API to register or unregister the target user's follow.
            // 1-7-2. When a response is received normally (callback processing)
            // 1-7-2-1. If the JSON data is an error, display the received error message as a toast and end the process.
            // 1-7-2-2. Set the target user ID in the intent.
            // 1-7-2-3. Transition to the User Information screen.
            // 1-7-2-4. Close your own screen.
            // 1-7-3. When the request fails (callback processing)
            // 1-7-3-1. Display the error message as a toast.
        }
    }

    // 2. Common execution method for User Whisper Information Retrieval API
    private fun getUserWhisperInfo(myApp: MyApplication, userId: String?, loginUserId: String, userNameTx: TextView, userProfileTx: TextView, followCountTx: TextView, followerCountTx: TextView, followBtn: Button, userRecycle: RecyclerView, radioGroup: RadioGroup) {
        // 2-1. Request the User Whisper Information Retrieval API to retrieve the target user's whisper information and the information that the user likes.
        // 2-2. When a response is received normally (callback processing)
        // 2-2-1. If the JSON data is an error, display the received error message as a toast and end the process.
        // 2-2-2. Set the retrieved data to each object.
        // 2-2-3. followButton
        // 2-2-3-1. If the user is a follow user, display Following, otherwise display Follow.
        // 2-2-3-2. When the target user is the login user, hide the button.
        // 2-2-4. While there is whisper information list, repeat the following process
        // 2-2-4-1. Store the whisper information in the list.
        // 2-2-5. While there is a list of good information, repeat the following process
        // 2-2-5-1. Store the good information in the list.
        // 2-2-6. userRecycle
        // 2-2-6-1. When the radio button selects whisperRadio
        // Set the whisper information list to the whisper line information adapter.
        // 2-2-6-2. When the radio button selects goodInfoRadio
        // Set the good information list to the whisper line information adapter.
        // 2-3. When the request fails (callback processing)
        // 2-3-1. Display the error message as a toast.
    }
}