// ==== DOM Elemanları ====
const filesInput = document.getElementById('files');               // Dosya seçme input'u
const fileList = document.getElementById('fileList');               // Seçilen dosyaların listesi
const uploadBtn = document.getElementById('uploadBtn');             // "Yükle" butonu
const progressContainer = document.getElementById('progressContainer'); // İlerleme çubuğu kutusu
const progressBarInner = document.getElementById('progressBarInner');   // İlerleme çubuğunun iç kısmı
const progressText = document.getElementById('progressText');       // Yüzdelik ilerleme metni
const loadingSpinner = document.getElementById('loadingSpinner');   // Dönen yükleme simgesi
const loadingText = document.getElementById('loadingText');         // Yükleme durumu metni

// ==== Durum Değişkenleri ====
let selectedFiles = [];       // Seçilen dosyalar
let loadingTimeoutId = null;  // Yükleme simülasyonu için timeout ID

// ==== Seçilen dosyaları listeye ekle ====
function updateFileList() {
  fileList.innerHTML = ''; // Listeyi temizle

  selectedFiles.forEach((file, index) => {
    const li = document.createElement('li');
    li.textContent = file.name;

    // Kaldırma butonu
    const removeBtn = document.createElement('button');
    removeBtn.textContent = '×';
    removeBtn.title = "Dosyayı kaldır";

    removeBtn.onclick = () => {
      selectedFiles.splice(index, 1); // Diziden kaldır
      updateFileList();               // Listeyi güncelle
      resetLoadingState();            // Yükleme durumunu sıfırla
      checkUploadBtn();               // Buton durumunu kontrol et
    };

    li.appendChild(removeBtn);
    fileList.appendChild(li);
  });
}

// ==== Yükleme durumunu sıfırla ====
function resetLoadingState() {
  if (loadingTimeoutId) {
    clearTimeout(loadingTimeoutId);
    loadingTimeoutId = null;
  }
  loadingText.textContent = '';
  loadingSpinner.style.display = 'none';
}

// ==== Yükle butonunu aktif/pasif yap ====
function checkUploadBtn() {
  uploadBtn.disabled = !(selectedFiles.length > 0 && loadingSpinner.style.display === 'none');
}

// ==== Dosya seçildiğinde ====
filesInput.addEventListener('change', () => {
  selectedFiles = [...selectedFiles, ...filesInput.files]; // Mevcut listeye ekle
  updateFileList();
  resetLoadingState();

  uploadBtn.disabled = true;
  loadingSpinner.style.display = 'inline-block';
  loadingText.textContent = 'Dosya seçildi';

  // Yükleme simülasyonu
  loadingTimeoutId = setTimeout(() => {
    loadingText.textContent = 'Yüklemeye hazır';
    loadingSpinner.style.display = 'none';
    checkUploadBtn();
    loadingTimeoutId = null;
  }, 2500);

  filesInput.value = ''; // Aynı dosyayı tekrar seçebilmek için input'u sıfırla
});

// ==== Dosya yükleme işlemi ====
async function uploadFiles() {
  if (selectedFiles.length === 0) {
    alert('Lütfen en az bir dosya seçin.');
    return;
  }

  // Yükleme başlangıcı
  loadingText.textContent = 'Yükleme başladı...';
  progressContainer.style.display = 'block';
  progressBarInner.style.width = '0%';
  progressText.textContent = '0%';
  uploadBtn.disabled = true;

  const formData = new FormData();
  selectedFiles.forEach(file => formData.append('files', file));

  try {
    // === İlerleme simülasyonu ===
    await new Promise((resolve) => {
      let progress = 0;
      const interval = setInterval(() => {
        progress = Math.min(progress + 2, 100);
        progressBarInner.style.width = progress + '%';
        progressText.textContent = progress + '%';

        if (progress === 100) {
          clearInterval(interval);
          resolve();
        }
      }, 50);
    });

    // === Sunucuya gönderme ===
    const response = await fetch('/upload', { method: 'POST', body: formData });
    if (!response.ok) throw new Error('Yükleme başarısız oldu');

    loadingText.textContent = 'Yükleme tamamlandı!';
  } catch (err) {
    loadingText.textContent = 'Yükleme başarısız oldu!';
    console.error(err);
  }

  progressContainer.style.display = 'none';

  // 2 saniye sonra temizle
  setTimeout(() => {
    loadingText.textContent = '';
    selectedFiles = [];
    updateFileList();
    checkUploadBtn();
  }, 2000);
}

// ==== "Yükle" butonu ====
uploadBtn.addEventListener('click', uploadFiles);

// ==== Sayfa ilk yüklendiğinde ====
checkUploadBtn();
