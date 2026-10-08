package com.example.data

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionBadge: String? = null,
    val actionExecuted: Boolean = false,
    val pendingAction: PendingAction? = null,
    val isError: Boolean = false
)
