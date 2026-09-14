package com.example.sayra.engine

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import com.example.sayra.data.model.ToolExecutionInfo

class AndroidActionController(private val context: Context) {

    private val cameraManager by lazy { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    /**
     * Finds and launches an app by common name or query.
     */
    fun openApp(appName: String): ToolExecutionInfo {
        val query = appName.trim().lowercase()
        val pm = context.packageManager

        // Known direct package mappings for instant accuracy
        val directPackage = when {
            query.contains("youtube") -> "com.google.android.youtube"
            query.contains("instagram") || query.contains("insta") -> "com.instagram.android"
            query.contains("whatsapp") -> "com.whatsapp"
            query.contains("spotify") -> "com.spotify.music"
            query.contains("chrome") -> "com.android.chrome"
            query.contains("maps") || query.contains("google maps") -> "com.google.android.apps.maps"
            query.contains("gmail") || query.contains("mail") -> "com.google.android.gm"
            query.contains("camera") -> {
                val camIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return try {
                    context.startActivity(camIntent)
                    ToolExecutionInfo("openApp", "Camera", "Opened Camera application", true, "camera")
                } catch (e: Exception) {
                    ToolExecutionInfo("openApp", "Camera", "Could not open Camera: ${e.message}", false)
                }
            }
            query.contains("settings") || query.contains("setting") -> {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return ToolExecutionInfo("openApp", "Settings", "Opened Android Settings", true, "settings")
            }
            else -> null
        }

        if (directPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(directPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ToolExecutionInfo("openApp", appName, "Opened $appName successfully", true, "launch")
            }
        }

        // Search installed launcher activities
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            if (label.contains(query) || query.contains(label)) {
                val targetPkg = info.activityInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(targetPkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ToolExecutionInfo("openApp", info.loadLabel(pm).toString(), "Opened ${info.loadLabel(pm)}", true, "launch")
                }
            }
        }

        return ToolExecutionInfo("openApp", appName, "Could not find application '$appName' installed on this device.", false)
    }

    /**
     * Opens native Android Settings pages
     */
    fun openSettings(settingType: String): ToolExecutionInfo {
        val query = settingType.trim().lowercase()
        val action = when {
            query.contains("wifi") || query.contains("wi-fi") || query.contains("internet") -> Settings.ACTION_WIFI_SETTINGS
            query.contains("bluetooth") || query.contains("bt") -> Settings.ACTION_BLUETOOTH_SETTINGS
            query.contains("display") || query.contains("screen") || query.contains("brightness") -> Settings.ACTION_DISPLAY_SETTINGS
            query.contains("sound") || query.contains("audio") || query.contains("volume") -> Settings.ACTION_SOUND_SETTINGS
            query.contains("battery") || query.contains("power") -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            query.contains("app") || query.contains("application") -> Settings.ACTION_APPLICATION_SETTINGS
            query.contains("notification") -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            query.contains("accessibility") -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionInfo("openSettings", settingType, "Opened $settingType settings screen", true, "settings")
        } catch (e: Exception) {
            ToolExecutionInfo("openSettings", settingType, "Could not open $settingType settings: ${e.message}", false)
        }
    }

    /**
     * Reads battery percentage and charging state
     */
    fun getBatteryStatus(): ToolExecutionInfo {
        return try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, batteryFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else level
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val message = "Your battery is at $batteryPct%${if (isCharging) " and currently charging" else ""}."
            ToolExecutionInfo("getBatteryStatus", "Battery Level", message, true, "battery")
        } catch (e: Exception) {
            ToolExecutionInfo("getBatteryStatus", "Battery", "Unable to read battery level: ${e.message}", false)
        }
    }

    /**
     * Controls device flashlight / torch
     */
    fun controlFlashlight(turnOn: Boolean): ToolExecutionInfo {
        return try {
            var targetCameraId: String? = null
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    targetCameraId = id
                    break
                }
            }

            if (targetCameraId == null && cameraManager.cameraIdList.isNotEmpty()) {
                targetCameraId = cameraManager.cameraIdList[0]
            }

            if (targetCameraId != null) {
                cameraManager.setTorchMode(targetCameraId, turnOn)
                val statusText = if (turnOn) "Flashlight turned on" else "Flashlight turned off"
                ToolExecutionInfo("controlFlashlight", "Flashlight", statusText, true, "flashlight")
            } else {
                ToolExecutionInfo("controlFlashlight", "Flashlight", "Flashlight hardware not available on this device", false)
            }
        } catch (e: CameraAccessException) {
            ToolExecutionInfo("controlFlashlight", "Flashlight", "Flashlight error: ${e.message}", false)
        } catch (e: Exception) {
            ToolExecutionInfo("controlFlashlight", "Flashlight", "Flashlight control unavailable: ${e.message}", false)
        }
    }

    /**
     * Controls media playback volume
     */
    fun controlVolume(direction: String, level: Int? = null): ToolExecutionInfo {
        return try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            when {
                level != null -> {
                    val target = ((level.toFloat() / 100f) * maxVol).toInt().coerceIn(0, maxVol)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                    ToolExecutionInfo("controlVolume", "Volume", "Volume set to $level%", true, "volume")
                }
                direction.equals("up", ignoreCase = true) || direction.contains("raise") || direction.contains("increase") -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    ToolExecutionInfo("controlVolume", "Volume", "Volume increased", true, "volume")
                }
                direction.equals("down", ignoreCase = true) || direction.contains("lower") || direction.contains("decrease") -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    ToolExecutionInfo("controlVolume", "Volume", "Volume decreased", true, "volume")
                }
                direction.contains("mute") -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                    ToolExecutionInfo("controlVolume", "Volume", "Media muted", true, "volume")
                }
                else -> {
                    val pct = (currentVol * 100 / maxVol)
                    ToolExecutionInfo("controlVolume", "Volume", "Current volume is $pct%", true, "volume")
                }
            }
        } catch (e: Exception) {
            ToolExecutionInfo("controlVolume", "Volume", "Volume control error: ${e.message}", false)
        }
    }

    /**
     * Adjusts screen brightness (0-100%)
     */
    fun controlBrightness(levelPercent: Int): ToolExecutionInfo {
        val clamped = levelPercent.coerceIn(0, 100)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
                val brightnessValue = (clamped * 255 / 100).coerceIn(0, 255)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
                )
                ToolExecutionInfo("controlBrightness", "Brightness", "Screen brightness set to $clamped%", true, "brightness")
            } else {
                val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ToolExecutionInfo("controlBrightness", "Brightness", "Opened Display Settings to adjust brightness to $clamped%", true, "brightness")
            }
        } catch (e: Exception) {
            ToolExecutionInfo("controlBrightness", "Brightness", "Brightness control error: ${e.message}", false)
        }
    }

    /**
     * Safely opens a web URL in browser
     */
    fun openUrl(url: String): ToolExecutionInfo {
        return try {
            val targetUrl = if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                "https://$url"
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionInfo("openUrl", "Open Link", "Opening $targetUrl", true, "url")
        } catch (e: Exception) {
            ToolExecutionInfo("openUrl", "Web Link", "Could not open link: ${e.message}", false)
        }
    }

    /**
     * Initiates phone dialer or call
     */
    fun makePhoneCall(phoneNumber: String, contactName: String? = null): ToolExecutionInfo {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(callIntent)
            val nameDisplay = if (!contactName.isNullOrBlank()) contactName else phoneNumber
            ToolExecutionInfo("makePhoneCall", "Call $nameDisplay", "Opening dialer for $nameDisplay", true, "phone")
        } catch (e: Exception) {
            ToolExecutionInfo("makePhoneCall", "Phone Call", "Could not start call: ${e.message}", false)
        }
    }

    /**
     * Composes SMS message
     */
    fun composeSms(phoneNumber: String?, message: String): ToolExecutionInfo {
        return try {
            val uri = if (!phoneNumber.isNullOrBlank()) Uri.parse("smsto:${phoneNumber.trim()}") else Uri.parse("smsto:")
            val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            ToolExecutionInfo("composeSms", "Compose SMS", "Opening SMS composer with your message", true, "message")
        } catch (e: Exception) {
            ToolExecutionInfo("composeSms", "SMS", "Could not open messaging: ${e.message}", false)
        }
    }

    /**
     * Sets a reminder or alarm
     */
    fun createReminder(title: String, time: String): ToolExecutionInfo {
        return try {
            // Attempt to open clock / alarm intent
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(alarmIntent)
            ToolExecutionInfo("createReminder", "Reminder: $title", "Set reminder for '$title' at $time", true, "alarm")
        } catch (_: Exception) {
            // Fallback to calendar insert
            try {
                val calIntent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, title)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(calIntent)
                ToolExecutionInfo("createReminder", "Calendar: $title", "Opened Calendar to set reminder '$title'", true, "alarm")
            } catch (e2: Exception) {
                ToolExecutionInfo("createReminder", "Reminder", "Could not set reminder: ${e2.message}", false)
            }
        }
    }

    /**
     * Searches the web using user's query
     */
    fun searchWeb(query: String): ToolExecutionInfo {
        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            ToolExecutionInfo("searchWeb", "Search Web", "Searching Google for '$query'", true, "search")
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ToolExecutionInfo("searchWeb", "Search Web", "Searching Google for '$query'", true, "search")
            } catch (e2: Exception) {
                ToolExecutionInfo("searchWeb", "Search", "Could not search web: ${e2.message}", false)
            }
        }
    }

    /**
     * Returns real device hardware and system information
     */
    fun getDeviceInfo(): ToolExecutionInfo {
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val androidVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)
        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular data"
            else -> "Offline or disconnected"
        }

        val info = "$manufacturer $model running Android $androidVersion (API $apiLevel). Network: $networkType."
        return ToolExecutionInfo("getDeviceInfo", "Device Info", info, true, "info")
    }
}
