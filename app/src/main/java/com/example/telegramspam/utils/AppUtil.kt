package com.example.telegramspam.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions

fun log(vararg messages: Any?) {
    messages.forEach { msg ->
        if (msg != null) {
            Log.d("MEONER", msg.toString())
        }
    }
}

fun dbDirectory() = "${Environment.getExternalStorageDirectory()}/telegram-bot"


fun Activity.checkStoragePermission(): Boolean {
    if (
        ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1234
            )
        }
        return false
    } else{
        return true
    }
}