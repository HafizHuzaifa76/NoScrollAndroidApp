package com.noscroll.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.noscroll.app.ControlledScrollAlertActivity
import com.noscroll.app.R

/**
 * Shows Controlled Scroll warn/block alerts in ways that work even when
 * another app is in the foreground (activity launch alone often fails).
 */
object ControlledScrollAlerter {

    private const val CHANNEL_ID = "controlled_scroll_alerts"
    private const val WARN_NOTIFICATION_ID = 1101
    private const val BLOCK_NOTIFICATION_ID = 1102

    fun showWarning(context: Context, appName: String) {
        val appCtx = context.applicationContext
        ensureChannel(appCtx)

        val message =
            "You've been scrolling $appName for over 10 minutes. At 15 minutes it will lock for 40 minutes."

        showToast(appCtx, "No Scroll: 10 min warning on $appName")
        postNotification(
            context = appCtx,
            notificationId = WARN_NOTIFICATION_ID,
            title = "You're wasting time on $appName",
            body = message,
            mode = ControlledScrollAlertActivity.MODE_WARN,
            appName = appName,
            blockedUntil = 0L,
        )
        launchAlertActivity(
            context = appCtx,
            mode = ControlledScrollAlertActivity.MODE_WARN,
            appName = appName,
            blockedUntil = 0L,
        )
    }

    fun showBlock(context: Context, appName: String, blockedUntilMs: Long) {
        val appCtx = context.applicationContext
        ensureChannel(appCtx)

        val message =
            "$appName is locked for 40 minutes because of scrolling. Open No Scroll for details."

        showToast(appCtx, "No Scroll: $appName locked for 40 min")
        postNotification(
            context = appCtx,
            notificationId = BLOCK_NOTIFICATION_ID,
            title = "Time's up — $appName locked",
            body = message,
            mode = ControlledScrollAlertActivity.MODE_BLOCK,
            appName = appName,
            blockedUntil = blockedUntilMs,
        )
        launchAlertActivity(
            context = appCtx,
            mode = ControlledScrollAlertActivity.MODE_BLOCK,
            appName = appName,
            blockedUntil = blockedUntilMs,
        )
    }

    private fun showToast(context: Context, text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchAlertActivity(
        context: Context,
        mode: String,
        appName: String,
        blockedUntil: Long,
    ) {
        val intent = Intent(context, ControlledScrollAlertActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
            putExtra(ControlledScrollAlertActivity.EXTRA_MODE, mode)
            putExtra(ControlledScrollAlertActivity.EXTRA_APP_NAME, appName)
            putExtra(ControlledScrollAlertActivity.EXTRA_BLOCKED_UNTIL, blockedUntil)
        }

        Handler(Looper.getMainLooper()).post {
            try {
                context.startActivity(intent)
            } catch (t: Throwable) {
                // Notification + toast still cover the alert.
            }
        }
    }

    private fun postNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        mode: String,
        appName: String,
        blockedUntil: Long,
    ) {
        val contentIntent = Intent(context, ControlledScrollAlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(ControlledScrollAlertActivity.EXTRA_MODE, mode)
            putExtra(ControlledScrollAlertActivity.EXTRA_APP_NAME, appName)
            putExtra(ControlledScrollAlertActivity.EXTRA_BLOCKED_UNTIL, blockedUntil)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS may be denied — toast/activity still attempt to warn.
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Scroll limit alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Warnings and lock alerts for Controlled Scrolling"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
