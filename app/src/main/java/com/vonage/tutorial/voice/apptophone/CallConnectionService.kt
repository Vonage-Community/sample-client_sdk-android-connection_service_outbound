package com.vonage.tutorial.voice.apptophone

import android.net.Uri
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

class CallConnectionService : ConnectionService() {

    // CSDemo: (2) This gets called after you place the call. Create a connection here.
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val to = request?.extras?.getString("to")
        val connection = CallConnection(this).apply {
            setAddress(Uri.parse("tel:$to"), TelecomManager.PRESENTATION_ALLOWED)
            setCallerDisplayName(to, TelecomManager.PRESENTATION_ALLOWED)
            setDialing()
        }
        return connection
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

}