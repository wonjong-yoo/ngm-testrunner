package com.github.wonjongyoo.ngm.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater

class NotificationUtils {
    companion object {
        fun show(
            message: String?,
            title: String = "",
            notificationType: NotificationType = NotificationType.INFORMATION,
            groupDisplayId: String = "",
            notificationAction: NotificationAction? = null
        ) {
            invokeLater {
                // this is because Notification doesn't accept empty messages
                val newMessage = if (message == null || message.trim().isBlank()) "[empty message]" else message

                val notification = Notification(groupDisplayId, title, newMessage, notificationType)
                if (notificationAction != null) {
                    notification.addAction(notificationAction)
                }
                ApplicationManager.getApplication().messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
            }
        }
    }
}
