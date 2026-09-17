package com.offx.sayra.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.offx.sayra.MainActivity
import com.offx.sayra.R
import com.offx.sayra.data.SayraPreferences
import com.offx.sayra.engine.SayraSpeechEngine

class SayraVoiceService : Service() {
    private lateinit var speech: SayraSpeechEngine
    override fun onCreate() { super.onCreate(); createChannel(); startForeground(77, notification()); speech = SayraSpeechEngine(this, { handle(it) }, { restartListening() }); restartListening() }
    private fun restartListening() { if (::speech.isInitialized) speech.start(SayraPreferences(this).language) }
    private fun handle(text: String) { val normalized = text.lowercase(); if (SayraPreferences(this).wakeWordEnabled && !normalized.contains("hey sayra") && !normalized.contains("हाय सायरा")) { restartListening(); return }; restartListening() }
    override fun onDestroy() { if (::speech.isInitialized) speech.stop(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() { if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("sayra_voice", "SAYRA Voice", NotificationManager.IMPORTANCE_LOW)) }
    private fun notification() = NotificationCompat.Builder(this, "sayra_voice").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("SAYRA is listening").setContentText("Voice assistant is active").setOngoing(true).setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)).build()
}
