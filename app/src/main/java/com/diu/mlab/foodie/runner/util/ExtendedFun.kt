package com.diu.mlab.foodie.runner.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.documentfile.provider.DocumentFile
import coil.imageLoader
import coil.request.ImageRequest
import com.diu.mlab.foodie.runner.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("ClickableViewAccessibility")
fun View.setBounceClickListener(onClick: (() -> Unit)? = null){
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val scaleDownX = ObjectAnimator.ofFloat(
                    v, "scaleX", 0.9f
                )
                val scaleDownY = ObjectAnimator.ofFloat(
                    v, "scaleY", 0.9f
                )
                scaleDownX.duration = 200
                scaleDownY.duration = 200

                val scaleDown = AnimatorSet()
                scaleDown.play(scaleDownX).with(scaleDownY)
                scaleDown.start()

            }
            MotionEvent.ACTION_UP -> {
                val scaleDownX2 = ObjectAnimator.ofFloat(
                    v, "scaleX", 1f
                )
                val scaleDownY2 = ObjectAnimator.ofFloat(
                    v, "scaleY", 1f
                )
                scaleDownX2.duration = 200
                scaleDownY2.duration = 200

                val scaleDown2 = AnimatorSet()
                scaleDown2.play(scaleDownX2).with(scaleDownY2)

                scaleDown2.start()
//                v.background.setHotspot(event.x,event.y)
            }
        }
        false
    }
    this.setOnClickListener { onClick?.invoke() }
}

fun String.transformedEmailId(): String = this.replace('.','~')

@OptIn(DelicateCoroutinesApi::class)
fun String.getDrawable( success : (Drawable?) -> Unit) {
    GlobalScope.launch(Dispatchers.IO){
        try {
            val `is`: InputStream = URL(this@getDrawable).content as InputStream
            val drawable = Drawable.createFromStream(`is`, "dp")
            //val x = BitmapFactory.decodeStream(`is`)
            withContext(Dispatchers.Main) {
                success.invoke(drawable)
            }
        } catch (e: Exception) {
            Log.d("Exception", "getDrawable: $e")
            success.invoke(null)
        }
    }
}

fun ImageView.loadDrawable(url: String?) {
    val request = ImageRequest.Builder(this.context)
        .placeholder(R.drawable.ic_placeholder)
        .error(R.drawable.ic_placeholder)
        .setHeader("Cache-Control", "max-age=31536000")
        .data(url)
        .target(this)
        .build()
    context.imageLoader.enqueue(request)
}

fun Activity.changeStatusBarColor(colorId: Int, isLight: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = getColor(colorId)

    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
}

fun Context.copyUriToFile2(uri: Uri): File {
    val inputStream = contentResolver.openInputStream(uri)
    val fileName = DocumentFile.fromSingleUri(this, uri)?.name!!
    val outputFile = File(cacheDir, fileName)
    val outputStream = FileOutputStream(outputFile)

    inputStream.use { input ->
        outputStream.use { output ->
            val buffer = ByteArray(4 * 1024) // buffer size
            while (true) {
                val byteCount = input!!.read(buffer)
                if (byteCount < 0) break
                output.write(buffer, 0, byteCount)
            }
            output.flush()
        }
    }
    return outputFile
}

fun Context.copyUriToFile(uri: Uri): File {
    val result: String
    val cursor = contentResolver.query(uri, null, null, null, null)
    if (cursor == null) { // Source is Dropbox or other similar local file path
        result = uri.path.toString()
    } else {
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return File(result)
}

fun Long.toDateTime(): String{
    var date = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(this)
    val day = SimpleDateFormat("dd", Locale.US).format(System.currentTimeMillis())
    if (day.toInt() == date.split(" ")[0].toInt())
        date = date.split(", ")[1]
    return date.toString()
}

fun String.hasAlphabet(): Boolean {
    return this.matches("^[a-zA-Z1-9]*$".toRegex())
}