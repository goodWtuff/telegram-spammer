package com.example.telegramspam.utils


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.telegramspam.R


const val CHANNEL_ID = "Telegram spammer channel"
const val PARSER_ID = 1234
const val SPAMMER_ID = 431
const val JOINER_ID = 23414
const val INVITER_ID = 324123
const val POST_WATCH_ID = 980

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel1 = NotificationChannel(
            "Telegram spammer channel",
            "Telegram spammer channel",
            NotificationManager.IMPORTANCE_LOW
        )
        channel1.description = "This is Channel 1"

        context.getSystemService(NotificationManager::class.java)?.apply {
            createNotificationChannel(channel1)
        }
    }
}

fun Context.sendNotificationInviter(   success:Int, errors:Int) {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Telegram Spam")
        .setContentText("Inviter finished. Success - $success, Errors - $errors")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(INVITER_ID + 1, notification)
}

fun Context.sendNotificationPostWatch(  success:Int, errors:Int) {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Telegram Spam")
        .setContentText("Watching posts finished. Success - $success, Errors  - $errors")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(POST_WATCH_ID + 1, notification)
}


fun Context.sendNotificationJoiner(  success:Int, errors:Int) {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Telegram Spam")
        .setContentText("Joiner finished. Success - $success, Errors - $errors")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(JOINER_ID + 1, notification)
}

fun Context.sendNotification(phone:String, message:String, id: Int) {
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Account +$phone")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(id, notification)
}


fun Context.sendNotificationService(phone:String,message:String, id:Int, stopIntent: Intent){
    val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pStopSelf =
        PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Account +$phone")
        .setContentText(message)
        .addAction(R.drawable.ic_app, "Stop", pStopSelf)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
    notificationManager.notify(id, notification)
}

fun Context.createServiceNotification( message:String, stopIntent: Intent) : Notification {
    val pStopSelf =
        PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_app)
        .setContentTitle("Telegram Spammer")
        .setContentText(message)
        .addAction(R.drawable.ic_app, "Stop all", pStopSelf)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .build()
}