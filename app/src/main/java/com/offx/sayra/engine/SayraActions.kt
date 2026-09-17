package com.offx.sayra.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class SayraActions(private val context: Context) {
    fun openUrl(url: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openSettings() { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openCamera() { context.startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openGallery() { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun openYouTube() { openUrl("https://www.youtube.com") }
}
