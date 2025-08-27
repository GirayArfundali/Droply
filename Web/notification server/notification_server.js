// Gerekli modüller yükleniyor
const express = require('express'); // Web sunucusu oluşturmak için
const admin = require('firebase-admin'); // Firebase Admin SDK (Push bildirim göndermek için)
const fs = require('fs'); // Dosya okuma/yazma işlemleri
const path = require('path'); // Dosya ve dizin yollarını yönetmek için

// Firebase servis hesabı bilgilerini içeren dosya
const serviceAccount = require(path.resolve(__dirname, 'service_account.json'));

// Firebase Admin SDK başlatılıyor
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const app = express();
app.use(express.json()); // JSON formatındaki istekleri parse etmek için (body-parser yerine)

// Public klasörünü statik olarak sunma (HTML, CSS, JS dosyaları buradan servis edilir)
app.use(express.static(path.join(__dirname, 'public')));

// Tokenların kayıtlı tutulduğu dosya
const TOKEN_FILE = path.resolve(__dirname, 'tokens.json');

// Tokenları bellekte set olarak saklamak (benzersiz olması için)
let deviceTokens = new Set();

// Sunucu başlatılırken daha önce kayıtlı tokenlar dosyadan yükleniyor
if (fs.existsSync(TOKEN_FILE)) {
  deviceTokens = new Set(JSON.parse(fs.readFileSync(TOKEN_FILE, 'utf8')));
}

// Tokenları dosyaya kaydetme fonksiyonu
function saveTokens() {
  fs.writeFileSync(TOKEN_FILE, JSON.stringify(Array.from(deviceTokens), null, 2));
}

// 🔹 Token kayıt endpoint’i
app.post('/registerToken', (req, res) => {
  const token = req.body.token;
  if (token && !deviceTokens.has(token)) {
    deviceTokens.add(token); // Yeni token set’e eklenir
    saveTokens(); // Dosyaya kaydedilir
    console.log('Yeni token kaydedildi:', token);
    res.status(200).send({ message: 'Token kaydedildi' });
  } else {
    res.status(400).send({ message: 'Token eksik veya zaten kayıtlı' });
  }
});

// 🔹 Bildirim gönderme endpoint’i
app.post('/sendNotification', async (req, res) => {
  const { title, body, image } = req.body;

  // Zorunlu alan kontrolü
  if (!title || !body) {
    return res.status(400).send({ message: 'title ve body zorunlu' });
  }

  // Bildirim içeriği oluşturma
  const messagePayload = {
    notification: { title, body, ...(image && { image }) },
    data: { title, body, image: image || "" },
    android: {
      priority: "high",
      notification: {
        channel_id: "default_channel",
        sound: "default",
        ...(image && { image }),
      },
    },
    apns: {
      payload: { aps: { sound: "default" } },
      ...(image && { fcm_options: { image } }),
    },
  };

  try {
    // Kayıtlı tüm cihazlara bildirim gönder
    for (const token of deviceTokens) {
      const message = { ...messagePayload, token };
      const response = await admin.messaging().send(message);
      console.log(`Bildirim gönderildi: ${response} -> Token: ${token}`);
    }
    res.status(200).send({ message: "Bildirimler gönderildi" });
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
    res.status(500).send({ error: "Bildirim gönderilemedi" });
  }
});

// Sunucuyu başlat
const PORT = 8080;
app.listen(PORT, () => {
  console.log(`Server ${PORT} portunda çalışıyor.`);
});
