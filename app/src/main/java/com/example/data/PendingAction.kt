package com.example.data

sealed class PendingAction {
    data class CallPhone(
        val phoneNumber: String,
        val recipientName: String? = null
    ) : PendingAction()

    data class SendSms(
        val phoneNumber: String,
        val messageText: String
    ) : PendingAction()
}
