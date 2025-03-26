package com.example.smsreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

class sms_receiver : BroadcastReceiver() {
    private var lastIncomingNumber: String? = null
    private var wasRinging = false
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Khi có cuộc gọi đến, lưu số điện thoại
                    wasRinging = true
                    lastIncomingNumber = incomingNumber
                    Log.d("sms_receiver", "Cuộc gọi đến từ : $lastIncomingNumber")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (wasRinging && lastIncomingNumber != null) {
                        sendAutoReplySMS(context, lastIncomingNumber!!)
                    }
                    wasRinging = false
                    lastIncomingNumber = null
                }
            }
        }
    }

    private fun sendAutoReplySMS(context: Context?, phoneNumber: String) {
        if (phoneNumber.isNullOrEmpty()) {
            Log.e("sms_receiver", "Số điện thoại không hợp lệ: $phoneNumber")
            return
        }

        Log.d("sms_receiver", "Chuẩn bị gửi SMS đến: $phoneNumber")

        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context?.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        val message = "Xin lỗi, tôi đang bận. Tôi sẽ gọi lại sau!"

        try {
            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("sms_receiver", "Đã gửi tin nhắn đến: $phoneNumber")
            showToast(context, "Đã gửi tin nhắn đến $phoneNumber")
        } catch (e: Exception) {
            Log.e("sms_receiver", "Lỗi gửi SMS: ${e.message}")
            showToast(context, "Gửi tin nhắn thất bại!")
        }
    }
    private fun showToast(context: Context?, message: String) {
        if (context != null) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
