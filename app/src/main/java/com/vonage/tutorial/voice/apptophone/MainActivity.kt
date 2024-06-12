package com.vonage.tutorial.voice.apptophone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vonage.android_core.VGClientConfig
import com.vonage.clientcore.core.api.ClientConfigRegion
import com.vonage.voice.api.CallId
import com.vonage.voice.api.VoiceClient

class MainActivity : AppCompatActivity() {

    private lateinit var clientManager: ClientManager

    private lateinit var telecomButton: Button
    private lateinit var startCallButton: Button
    private lateinit var connectionStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clientManager = ClientManager.getInstance(applicationContext)

        // request permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE),
            123
        );

        // init views
        startCallButton = findViewById(R.id.makeCallButton)
        telecomButton = findViewById(R.id.telecomButton)
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView)

        startCallButton.setOnClickListener {
            startCall()
        }

        telecomButton.setOnClickListener {
            enableTelecomPermission()
        }

        clientManager.login { status ->
            runOnUiThread {
                if (status == "Connected") {
                    telecomButton.visibility = View.VISIBLE
                    startCallButton.visibility = View.VISIBLE
                    connectionStatusTextView.text = "Connected"
                } else {
                    telecomButton.visibility = View.INVISIBLE
                    startCallButton.visibility = View.INVISIBLE
                    connectionStatusTextView.text = status
                }
            }
        }
    }

    private fun startCall() {
        clientManager.startOutGoingCall("PHONE_NUMBER") { err ->
            err?.let {
                runOnUiThread {
                    connectionStatusTextView.text = err
                }
            }
        }
    }

    // CSDemo: The user needs to explicitly allow your application to handle calls.
    private fun enableTelecomPermission() {
        val intent = Intent()
        intent.setClassName(
            "com.android.server.telecom",
            "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
        )
        startActivity(intent)
    }
}
