package com.coconutplace.wekit.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.splash.SplashActivity
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.PushTokenRegistrationStatus
import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.constant.StringSet
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference


class MyFirebaseMessagingService: SendBirdPushHandler() {
    private val TAG = "MyFirebaseMsgService"
    private val pushToken = AtomicReference<String>()

    override fun isUniquePushToken(): Boolean {
        return false
    }

    override fun onNewToken(token: String) {
        Log.i(TAG, "onNewToken($token)")
        pushToken.set(token)

        SendBird.registerPushTokenForCurrentUser(StringSet.token) { ptrs, e ->
            if (e != null) {
                // Handle error.
            }
            if (ptrs == PushTokenRegistrationStatus.PENDING) {
                // A token registration is pending.
                // Retry the registration after a connection has been successfully established.
            }
        }
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(context: Context?, remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(
                TAG,
                "Message Notification Body: " + remoteMessage.notification!!.body
            )
        }
        val channelUrl: String?
        try {
            if (remoteMessage.data.containsKey("sendbird")) {
                val sendBird = JSONObject(remoteMessage.data["sendbird"])
                val channel = sendBird["channel"] as JSONObject
                channelUrl = channel["channel_url"] as String
                SendBird.markAsDelivered(channelUrl)
                // Also if you intend on generating your own notifications as a result of a received FCM
                // message, here is where that should be initiated. See sendNotification method below.
                sendNotification(
                    context!!,
                    remoteMessage.data["message"],
                    channelUrl
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    // [END receive_message]
    private fun sendNotification(context: Context, messageBody: String?, channelUrl: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            val mChannel = NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra("groupChannelUrl", channelUrl)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icn_wekit_push_logo)
//                .setColor(Color.parseColor("#7469C4")) // small icon background color
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        context.resources,
//                        R.drawable.icn_small_profile
//                    )
//                )
                //.setContentTitle("WEKIT")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
        //if (PreferenceUtils.getNotificationsShowPreviews()) {
            notificationBuilder.setContentText(messageBody)
        //} else {
        //    notificationBuilder.setContentText("Somebody sent you a message.")
        //}
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}