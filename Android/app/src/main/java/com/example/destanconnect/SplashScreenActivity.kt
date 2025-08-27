package com.example.destanconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Splash ekranını 2 saniye gösterip ardından MainActivity'e geçiş yapar
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Splash ekranını kapatır, geri dönüşü engeller
        }, 2000) // 2000 milisaniye = 2 saniye
    }
}
