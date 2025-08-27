package com.example.destanconnect

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class FilesFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val fileNames = mutableListOf<String>()

    private var downloadManager: DownloadManager? = null
    private var downloadReceiver: BroadcastReceiver? = null
    private var currentDownloadId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)

        // ListView ve adapter kurulumu
        listView = view.findViewById(R.id.filesListView)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, fileNames)
        listView.adapter = adapter

        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        fetchFilesFromServer()

        // Dosya tıklama işlemi
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFileName = fileNames[position]
            val mimeType = getMimeType(selectedFileName)
            val fileUrl = "https://kullaniciadi.github.io/projeadi/uploads/$selectedFileName"


            when {
                mimeType == "application/pdf" -> openFileDirectly(fileUrl, mimeType)
                mimeType.startsWith("image/") -> downloadAndOpenFile(requireContext(), fileUrl, selectedFileName)
                else -> Toast.makeText(requireContext(), "Bu dosya tipi desteklenmiyor", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Sunucudan dosya listesini çek
    private fun fetchFilesFromServer() {
        val url = "https://proje-adi.onrender.com/files"
        val queue = Volley.newRequestQueue(requireContext())

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val filesArray = json.getJSONArray("files")
                        fileNames.clear()
                        for (i in 0 until filesArray.length()) {
                            fileNames.add(filesArray.getString(i))
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Dosya listesi alınamadı", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Veri işlenirken hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(requireContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    // PDF dosyasını direkt aç
    private fun openFileDirectly(fileUrl: String, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(fileUrl), mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Uygun bir görüntüleyici bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    // Görüntü dosyasını indir ve aç
    private fun downloadAndOpenFile(context: Context, fileUrl: String, fileName: String) {
        // Önceki alıcı varsa iptal et
        downloadReceiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
            downloadReceiver = null
        }

        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Dosya indiriliyor")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        currentDownloadId = downloadManager?.enqueue(request) ?: -1L

        // İndirme tamamlandığında dosyayı aç
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == currentDownloadId) {
                    val uri = downloadManager?.getUriForDownloadedFile(currentDownloadId)
                    if (uri != null) openFileWithUri(context, uri, fileName)
                    else Toast.makeText(context, "Dosya açılamadı", Toast.LENGTH_SHORT).show()
                    context.unregisterReceiver(this)
                    downloadReceiver = null
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(downloadReceiver, filter)
        }
    }

    // Uri ile dosyayı aç
    private fun openFileWithUri(context: Context, uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(fileName))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dosya açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Dosya uzantısına göre mime type belirle
    private fun getMimeType(fileName: String) = when {
        fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
        fileName.endsWith(".png", true) -> "image/png"
        fileName.endsWith(".gif", true) -> "image/gif"
        fileName.endsWith(".pdf", true) -> "application/pdf"
        else -> "*/*"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Broadcast receiver temizliği
        downloadReceiver?.let {
            try { requireContext().unregisterReceiver(it) } catch (_: Exception) {}
        }
    }
}
