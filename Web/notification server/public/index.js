// Buton ve durum mesajı elementi seçiliyor
const sendBtn = document.getElementById('sendBtn');
const statusDiv = document.getElementById('status');

// Gönder butonuna tıklama olayı
sendBtn.addEventListener('click', () => {
  // Form alanlarındaki veriler alınır
  const title = document.getElementById('title').value.trim();
  const body = document.getElementById('message').value.trim();
  const image = document.getElementById('image').value.trim();

  // Başlık ve mesaj zorunlu kontrolü
  if (!title || !body) {
    alert('Başlık ve mesaj zorunludur.');
    return;
  }

  // Butonu devre dışı bırak ve durum mesajı göster
  sendBtn.disabled = true;
  statusDiv.textContent = "📤 Bildirim gönderiliyor...";

  // Sunucuya POST isteği gönder
  fetch('/sendNotification', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, body, image: image || undefined })
  })
  .then(async res => {
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Sunucu hatası');
    }
    return res.json();
  })
  .then(() => {
    // Başarılı olursa formu temizle
    statusDiv.textContent = "✅ Bildirim başarıyla gönderildi.";
    document.getElementById('title').value = '';
    document.getElementById('message').value = '';
    document.getElementById('image').value = '';
  })
  .catch(err => {
    // Hata mesajı göster
    statusDiv.textContent = "❌ Bildirim gönderilemedi: " + err.message;
  })
  .finally(() => {
    // Butonu tekrar aktif et
    sendBtn.disabled = false;
  });
});
