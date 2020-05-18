package com.example.telegramspam.utils


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.LocusId
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.telegramspam.R

const val CHANNEL_ID = "Telegram spammer channel"
const val PARSER_ID = 1234
const val SPAMMER_ID = 431

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel1 = NotificationChannel(
            "Telegram spammer channel",
            "Telegram spammer channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel1.description = "This is Channel 1"

        context.getSystemService(NotificationManager::class.java)?.apply {
            createNotificationChannel(channel1)
        }
    }
}

fun Context.sendNotification(title:String, message:String, id: Int) {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(id, notification)
}