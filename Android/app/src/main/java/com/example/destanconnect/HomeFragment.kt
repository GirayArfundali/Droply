package com.example.destanconnect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_home layout dosyasını inflate ederek view oluşturuluyor
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Buton referansı alınıyor
        val btnOpenSite = view.findViewById<Button>(R.id.btnOpenSite)

        // Butona tıklanma olayı atanıyor
        btnOpenSite.setOnClickListener {
            // WebActivity'yi başlatmak için Intent oluşturuluyor
            val intent = Intent(requireActivity(), WebActivity::class.java)
            // İlgili URL Intent içine ekstra veri olarak ekleniyor
            intent.putExtra("url", "https://kullaniciadi.github.io/projeadi/index.html");
            // WebActivity başlatılıyor
            startActivity(intent)
        }

        return view
    }
}
