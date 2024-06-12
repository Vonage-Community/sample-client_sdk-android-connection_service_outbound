package com.vonage.tutorial.voice.apptophone

import android.Manifest
import android.content.Context
import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.vonage.android_core.VGClientConfig
import com.vonage.clientcore.core.api.ClientConfigRegion
import com.vonage.clientcore.core.api.LegStatus
import com.vonage.voice.api.*

class ClientManager(private val context: Context) {
    private var activeCall: CallId? = null
    private var activeConnection: CallConnection? = null

    private var token = "ALICE_JWT"

    private lateinit var client: VoiceClient
    private lateinit var telecomManager: TelecomManager
    private lateinit var phoneAccountHandle: PhoneAccountHandle

    companion object {
        // Volatile will guarantee a thread-safe & up-to-date version of the instance
        @Volatile
        private var instance: ClientManager? = null

        fun getInstance(context: Context): ClientManager {
            return instance ?: synchronized(this) {
                instance ?: ClientManager(context).also { instance = it }
            }
        }
    }

    init {
        val config = VGClientConfig(ClientConfigRegion.US)
        client = VoiceClient(context)
        client.setConfig(config)
        setListeners()

        val componentName = ComponentName(context, CallConnectionService::class.java)
        telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        phoneAccountHandle = PhoneAccountHandle(componentName, "Vonage Voip Calling")
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Vonage Voip Calling")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
        telecomManager.registerPhoneAccount(phoneAccount)
    }

    private fun setListeners() {
        client.setOnLegStatusUpdate { callId, legId, status ->
            if (callId == activeCall) {
                if(status == LegStatus.answered) {
                    activeConnection?.setActive()
                }
            }
        }

        client.setOnCallHangupListener { callId, callQuality, reason ->
            if (callId == activeCall) {
                cleanupCall(DisconnectCause(DisconnectCause.REMOTE))
            }
        }
    }

    fun login(callback: ((String) -> Unit)) {
        client.createSession(token) { err, sessionId ->
            when {
                err != null -> {
                    callback.invoke(err.localizedMessage)
                }
                else -> {
                    callback.invoke("Connected")
                }
            }
        }
    }

    fun setActiveCallConnection(connection: CallConnection) {
        activeConnection = connection
    }

    fun startOutGoingCall(number: String, callback: ((String?) -> Unit)) {
        client.serverCall(mapOf("to" to number)) { err, callId ->
            err?.let {
                callback.invoke("Error starting outbound call: $it")
            } ?: callId?.let {
                activeCall = it
                placeOutgoingCall(it, number)
            }
        }
    }

    // CSDemo: (4) Handle events from the system UI
    fun endOngoingCall() {
        activeCall?.let {
            client.hangup(it) { err ->
                when {
                    err != null -> {
                        println("Error starting outbound call: $err")
                    }

                    else -> {
                        cleanupCall(DisconnectCause(DisconnectCause.LOCAL))
                    }
                }
            }
        }
    }

    fun cancelCall() {
        cleanupCall(DisconnectCause(DisconnectCause.ERROR))
    }

    private fun cleanupCall(cause: DisconnectCause) {
        activeConnection?.setDisconnected(cause)
        activeConnection?.destroy()
        activeCall = null
        activeConnection = null
    }

    /* CSDemo: (1) Place the outgoing call with the Telecom manager. Your Phone Account Handle
        MUST be added as an extra here.
     */
    private fun placeOutgoingCall(callId: String, to: String) {
        val rootExtras = Bundle()
        val extras = Bundle()

        extras.putString("to", to)
        extras.putString("callId", callId)
        rootExtras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
        rootExtras.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras)

        if (telecomManager.isOutgoingCallPermitted(phoneAccountHandle) && context.checkSelfPermission(
                Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED) {
            telecomManager.placeCall(Uri.parse("tel:$to"), rootExtras)
        }
    }
}