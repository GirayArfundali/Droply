package com.example.destanconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 13+ için bildirim izni iste
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // FCM token'ını al ve backend'e gönder
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                println("Mevcut token: $token")
                sendTokenToServer(token)
            } else {
                println("Token alınamadı: ${task.exception}")
            }
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Başlangıçta HomeFragment yüklenir
        loadFragment(HomeFragment())
        bottomNavigationView.selectedItemId = R.id.nav_upload

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_files -> {
                    loadFragment(FilesFragment())
                    true
                }
                R.id.nav_upload -> {
                    loadFragment(HomeFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Backend'e token gönderme fonksiyonu
    private fun sendTokenToServer(token: String?) {
        token ?: return

        val url = "https://kullaniciadi.github.io/projeadi/files"
        val client = OkHttpClient()

        val json = """{"token": "$token"}"""
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // İzin sonucu kontrolü
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("POST_NOTIFICATIONS izni verildi.")
            } else {
                println("POST_NOTIFICATIONS izni reddedildi.")
            }
        }
    }
}
