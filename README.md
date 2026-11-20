# Creator Pro — Android v1.1.0 (SELESAI)

Aplikasi Android pendamping kreator YouTube, dibuat dengan Kotlin dan Jetpack Compose.
Tema bersih putih + merah (seperti TubeCuan). **Semua tools jalan offline — tanpa API key, tanpa server, tanpa biaya bulanan.**

## Yang sudah selesai di v1.1.0

- **Beranda** — kartu sambutan, daftar 4 tools, info gratis
- **AI Tools (offline, 6 fitur)** — Ide Konten (10 ide), Judul YouTube (10 judul + skor 0–100), Deskripsi Video (lengkap + timestamps), Naskah Video (hook + 3 poin + penutup), Keyword & Tag (20 tag + hitung 500 karakter), Ide YouTube Shorts (10 ide)
- **Hasil bisa dipakai langsung** — tombol Salin dan Bagikan, plus Riwayat 10 hasil terakhir yang tersimpan di HP (ketuk untuk buka lagi, bisa dihapus)
- **Kalkulator (3 alat)** — Jam Tayang, Target Monetisasi (sisa jam + sisa subs + perkiraan hari tercapai), Estimasi Penghasilan (views × RPM)
- **Harga** — Gratis dulu (semua terbuka), paket nanti Rp2rb / Rp10rb (PALING LARIS) / Rp15rb, bayar DANA / GoPay
- **Mode online (opsional)** — kalau backend sudah dipasang, aplikasi coba server AI dulu; kalau server gagal, otomatis jatuh ke mode offline. `BASE_URL` masih placeholder = offline.
- **File yang tadinya hilang** — `res/values` (strings, colors, themes), ikon launcher, Gradle wrapper, `.gitignore`, `proguard-rules.pro`
- **Backend diperkuat** — endpoint `GET /health`, validasi input, rate limit 30 request / 10 menit per IP, timeout 45 detik

## Cara membuka di Android Studio (butuh laptop/PC)

1. Ekstrak ZIP ini.
2. Buka Android Studio versi stabil → **Open** → pilih folder `CreatorPro`.
3. Tunggu Gradle Sync selesai.
4. Jalankan di emulator atau HP Android (USB debugging).

Build APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. APK debug ada di `app/build/outputs/apk/debug/`.

## Cara dapat APK tanpa laptop (HP saja)

1. Buat repo GitHub baru, upload semua isi folder `CreatorPro` ini ke branch `main`.
2. Buka tab **Actions** di repo itu — workflow `Build APK (debug)` jalan otomatis.
3. Setelah selesai (tanda hijau), buka run-nya → **Artifacts** → unduh `app-debug`.
4. Ekstrak dan instal APK-nya di HP (izinkan "instal dari sumber tidak dikenal" sekali saja).

## Mengaktifkan AI online (opsional, tidak wajib)

1. Deploy folder `backend` ke server HTTPS (VPS / hosting Node.js).
2. Salin `backend/.env.example` menjadi `.env`, isi `OPENAI_API_KEY`.
3. Jalankan `npm install` lalu `npm start`.
4. Cek `https://alamat-server-kamu/health` — harus `{"ok":true,...}`.
5. Di `app/src/main/java/com/creatorpro/app/CreatorApi.kt`, ganti `BASE_URL` dengan alamat HTTPS backend.
6. Build ulang APK.

**Penting:** Jangan pernah memasukkan API key AI ke kode Android atau membagikan file `.env`. Backend contoh ini sudah ada rate limit sederhana, tapi tetap tambahkan autentikasi dan kuota per pengguna sebelum dijual ke publik.

## Catatan

- Versi ini belum terhubung ke akun/channel YouTube (sengaja — supaya tidak butuh login Google).
- Kalkulator dan estimasi penghasilan hanya perkiraan, bukan data resmi YouTube.
- Nama Creator Pro adalah nama kerja; periksa ketersediaan merek sebelum dipasarkan.
- Harga sekali bayar, bukan langganan. Kode akses Pro: TC-COBA2 / TC-LENGKAP10 / TC-FULL15 (disimpan penjual).
