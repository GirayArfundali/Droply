package com.example.destanconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Yeni FCM mesajı alındığında çağrılır
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Bildirim başlığı ve mesajı alınır, veri kısmında da olabilir
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Bildirim Başlığı"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Bildirim Mesajı"

        println("FCM Mesaj alındı -> Başlık: $title, Mesaj: $body")

        // Alınan mesaj bildirim olarak gösterilir
        showNotification(title, body)
    }

    // Yeni FCM token oluşturulduğunda çağrılır
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Yeni FCM token alındı: $token")

        // Token backend'e gönderilir
        sendTokenToServer(token)
    }

    // FCM token'ı backend servise gönderir
    private fun sendTokenToServer(token: String) {
        val url = "https://kullaniciadi.github.io/projeadi/files" // Backend API adresi
        val client = OkHttpClient()

        // JSON formatında token verisi oluşturulur
        val json = """{"token": "$token"}"""
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        // POST isteği hazırlanır
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        // Asenkron HTTP çağrısı yapılır
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Token backend’e gönderilemedi: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                println("Token backend’e gönderildi: ${response.code}")
                response.close()
            }
        })
    }

    // Cihazda bildirim gösterir
    private fun showNotification(title: String, message: String) {
        val channelId = "default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O ve üstü için bildirim kanalı oluşturulur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Varsayılan Kanal",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirime tıklandığında uygulama açılır
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Bildirim içeriği hazırlanır
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Basit info ikonu
            .setAutoCancel(true)                             // Tıklanınca kapanır
            .setContentIntent(pendingIntent)                 // Tıklama olayı
            .build()

        // Bildirim gösterilir (benzersiz id için zamanı kullandık)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
