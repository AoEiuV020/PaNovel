package cc.aoeiuv020.panovel.server

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2018.04.10-19:38:28.
 */
class MyFirebaseMessagingService : FirebaseMessagingService(), AnkoLogger {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val TAG = "MFMS"
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: " + it.body)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
}