package com.example.actions

import com.example.data.PendingAction

sealed class SystemAction {
    data class LaunchApp(val packageName: String, val appLabel: String) : SystemAction()
    object OpenSettings : SystemAction()
    object OpenWifiSettings : SystemAction()
    object OpenBluetoothSettings : SystemAction()
    object OpenCamera : SystemAction()
    data class OpenBrowser(val url: String = "https://www.google.com") : SystemAction()
    data class WebSearch(val query: String) : SystemAction()
    data class RequestCallConfirmation(val phoneNumber: String, val name: String? = null) : SystemAction()
    data class RequestSmsConfirmation(val phoneNumber: String, val messageText: String) : SystemAction()
    object None : SystemAction()
}
