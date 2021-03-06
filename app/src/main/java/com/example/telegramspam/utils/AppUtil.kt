package com.example.telegramspam.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import com.example.telegramspam.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList

fun String?.toArrayList():ArrayList<String>{
    val resultList = ArrayList<String>()
    this?.split(",")?.forEach {
        if(it.length > 5) resultList.add(it)
    }
    return resultList
}
fun logDb(msg:String){
    val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    FirebaseDatabase.getInstance().getReference("Logs").child(date).setValue(msg)
}
fun log(vararg messages: Any?) {
    messages.forEach { msg ->
        if (msg != null) {
            Log.d("MEONER", msg.toString())
        }
    }
}

fun Fragment.toast(msg: String) {
    Snackbar.make(this.requireView(), msg, 1500).show()
}

fun List<String>.removeEmpty() : List<String>{
    val result= ArrayList<String>()
    this.forEach {
        if(it.isNotBlank()){
            result.add(it)
        }
    }
    return result
}
fun View.toast(msg: String) {
    Snackbar.make(this, msg, 1500).show()
}
fun Activity.connected() : Boolean{
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
}

fun connected(view: View) : Boolean{
    val context = view.context
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
}
 fun List<String>.getRandom():String{
    val position =  Random().nextInt(this.size)
    return this[position]
}

fun generateRandomInt() : Int =  Random().nextInt(999999)


fun Context.copyToClipboard(string: String) {
    val clipboard =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("hello world", string))
    log(string)
}

fun ImageView.empty(){
    this.setBackgroundColor(this.context.resources.getColor(R.color.darkerGrey))
}
fun View.gone(){
    this.visibility = View.GONE
}

fun View.visible(){
    this.visibility = View.VISIBLE
}


