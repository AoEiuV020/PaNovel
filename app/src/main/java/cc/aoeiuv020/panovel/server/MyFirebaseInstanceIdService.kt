package cc.aoeiuv020.panovel.server

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


/**
 *
 * Created by AoEiuV020 on 2018.04.10-18:14:49.
 */
class MyFirebaseInstanceIdService : FirebaseInstanceIdService(), AnkoLogger {
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token ?: return
        info { "Refreshed token: $refreshedToken" }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken)
    }

    private fun sendRegistrationToServer(refreshedToken: String) {
    }
}