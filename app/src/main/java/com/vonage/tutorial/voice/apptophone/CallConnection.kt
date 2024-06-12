package com.vonage.tutorial.voice.apptophone

import android.content.Context
import android.telecom.Connection

class CallConnection(context: Context) : Connection() {
    private var clientManager = ClientManager.getInstance(context)

    init {
        clientManager.setActiveCallConnection(this)
        audioModeIsVoip = true
    }

    // CSDemo: (3) Here you will get events back from the system UI.

    override fun onDisconnect() {
        clientManager.endOngoingCall()
    }

    override fun onAbort() {
        clientManager.cancelCall()
    }
}