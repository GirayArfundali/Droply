// 📌 Gerekli modüller
const express = require('express');     // Web sunucusu framework'ü
const multer = require('multer');       // Dosya yükleme işlemleri için
const path = require('path');           // Dosya/dizin yolları
const cors = require('cors');           // CORS izinleri
const fs = require('fs');               // Dosya sistemi işlemleri

const app = express();

// 📌 Yüklemelerin kaydedileceği klasör
const uploadDir = path.join(__dirname, 'uploads');

// Eğer klasör yoksa oluştur
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir);
}

// 📌 Multer depolama ayarları
const storage = multer.diskStorage({
  // Dosyaların kaydedileceği klasör
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  // Dosya adını belirleme
  filename: (req, file, cb) => {
    const originalName = file.originalname;        // Orijinal dosya adı
    const ext = path.extname(originalName);        // Uzantı (.jpg, .pdf, vb.)
    const nameWithoutExt = path.basename(originalName, ext); // Uzantısız ad

    let finalName = originalName; // Kaydedilecek dosya adı
    let counter = 1;

    // Aynı isimde dosya varsa sonuna (1), (2) ekle
    while (fs.existsSync(path.join(uploadDir, finalName))) {
      finalName = `${nameWithoutExt}(${counter})${ext}`;
      counter++;
    }

    cb(null, finalName);
  }
});

// 📌 Multer middleware tanımı
const upload = multer({ storage: storage });

// 📌 CORS aktif et
app.use(cors());

// 📌 Statik dosyaları sun
app.use(express.static(path.join(__dirname, 'public'))); // Frontend dosyaları
app.use('/uploads', express.static(uploadDir)); // Yüklenen dosyaları görüntüleme

// 📌 Yüklenen tüm dosyaları listeleme endpoint'i
app.get('/files', (req, res) => {
  fs.readdir(uploadDir, (err, files) => {
    if (err) {
      return res.status(500).json({ success: false, message: "Dosyalar okunamadı" });
    }
    res.json({ success: true, files });
  });
});

// 📌 Dosya yükleme endpoint'i
app.post('/upload', upload.array('files'), (req, res) => {
  if (!req.files || req.files.length === 0) {
    return res.status(400).json({ success: false, message: "Dosya bulunamadı" });
  }
  console.log('Yüklenen dosyalar:', req.files);
  res.json({ success: true, files: req.files });
});

// 📌 Sunucuyu başlat
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Sunucu http://0.0.0.0:${PORT} adresinde çalışıyor`);
});
