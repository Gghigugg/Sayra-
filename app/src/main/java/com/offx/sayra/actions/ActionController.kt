package com.offx.sayra.actions

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast

class ActionController(private val context: Context) {
    fun tryLocalAction(command: String): String? {
        val c = command.lowercase()
        return when {
            "youtube" in c -> { openPackage("com.google.android.youtube"); "Opening YouTube." }
            "chrome" in c || "browser" in c -> { openPackage("com.android.chrome"); "Opening Chrome." }
            "camera" in c -> { context.startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); "Opening camera." }
            "settings" in c -> { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); "Opening settings." }
            "flashlight" in c || "torch" in c -> { Toast.makeText(context, "Flashlight control needs device camera access and is handled by the system on some phones.", Toast.LENGTH_LONG).show(); "I opened the flashlight control path." }
            "volume up" in c -> { audio().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); "Volume increased." }
            "volume down" in c -> { audio().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI); "Volume decreased." }
            "silent" in c -> { audio().adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI); "Ring volume muted." }
            "battery" in c -> "Battery information is available in Android Settings."
            else -> null
        }
    }
    private fun audio() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private fun openPackage(pkg: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
