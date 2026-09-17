package com.offx.sayra.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.offx.sayra.R

class SayraVoiceService : Service() {
    override fun onCreate() { super.onCreate(); createChannel(); startForeground(77, notification()) }
    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("sayra_voice", "SAYRA Voice", NotificationManager.IMPORTANCE_LOW))
    }
    private fun notification(): Notification = NotificationCompat.Builder(this, "sayra_voice").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("SAYRA is listening").setContentText("Voice assistant is active").setOngoing(true).build()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
