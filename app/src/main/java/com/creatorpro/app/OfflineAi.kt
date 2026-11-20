package com.creatorpro.app

/**
 * Generator lokal (offline) Creator Pro.
 *
 * Sengaja dibuat tanpa internet dan tanpa API key, supaya aplikasi langsung
 * berguna di HP begitu dipasang. Hasilnya deterministik: topik yang sama
 * selalu menghasilkan susunan yang sama, hanya variasinya yang diputar
 * berdasarkan topik supaya tidak terasa templat mentah.
 *
 * Kalau backend online sudah dikonfigurasi di CreatorApi, aplikasi memakai
 * backend dulu dan hanya memakai generator ini sebagai cadangan.
 */
object OfflineAi {

    fun generate(tool: String, topicRaw: String, audienceRaw: String): String {
        val topic = topicRaw.trim()
        val audience = audienceRaw.trim().ifBlank { "Penonton umum" }
        val t = topic.replaceFirstChar { it.uppercaseChar() }
        return when (tool) {
            "Ide Konten" -> ideKonten(t, audience)
            "Judul YouTube" -> judul(t, audience)
            "Deskripsi Video" -> deskripsi(t, audience)
            "Naskah Video" -> naskah(t, audience)
            "Keyword & Tag" -> keywordTag(t, audience)
            "Ide YouTube Shorts" -> shorts(t, audience)
            else -> ideKonten(t, audience)
        }
    }

    /** Skor judul 0-100: panjang ideal, ada angka, ada kata pemicu, ada topik. */
    fun titleScore(title: String): Int {
        var score = 50
        val len = title.length
        if (len in 40..70) score += 20 else if (len in 30..80) score += 10 else if (len < 20) score -= 10
        if (title.any { it.isDigit() }) score += 10
        val pemicu = listOf("rahasia", "cara", "gratis", "terbaik", "cepat", "mudah", "jangan", "wajib", "terbukti", "ampuh", "pemula", "tanpa")
        if (pemicu.any { title.lowercase().contains(it) }) score += 10
        if (title.contains("!") || title.contains("?")) score += 5
        if (title.contains("|") || title.contains(":") || title.contains("-")) score += 5
        return score.coerceIn(0, 100)
    }

    private fun ideKonten(t: String, audience: String): String {
        val ide = listOf(
            "Panduan pemula: \"$t untuk Pemula — Mulai dari Nol Sampai Bisa\" (tutorial langkah demi langkah untuk $audience).",
            "Kesalahan umum: \"7 Kesalahan Fatal soal $t yang Masih Banyak Dilakukan\" (format list, mudah ditonton sampai habis).",
            "Studi kasus nyata: \"Saya Coba $t Selama 30 Hari, Ini Hasilnya\" (cerita jujur + bukti, bagus untuk kepercayaan penonton).",
            "Perbandingan: \"$t vs Cara Lama — Mana yang Lebih Worth It?\" (bantu penonton memutuskan, undang diskusi di komentar).",
            "Rahasia di balik layar: \"Yang Tidak Diceritakan Orang soal $t\" (angle penasaran, cocok untuk penonton $audience).",
            "Versi hemat: \"$t dengan Budget Minim — Tetap Bisa Maksimal\" (menjangkau penonton yang sensitif harga).",
            "Tanya jawab: \"10 Pertanyaan Paling Sering soal $t, Dijawab Tuntas\" (ambil dari komentar dan kolom pencarian).",
            "Rutinitas harian: \"Sehari Bersama $t — Rutinitas Nyata, Bukan Teori\" (vlog santai, kuat untuk Shorts juga).",
            "Mitos vs fakta: \"5 Mitos $t yang Harus Berhenti Kamu Percaya\" (format debat ringan, memancing komentar).",
            "Update terbaru: \"$t di 2026 — Apa yang Berubah dan Harus Kamu Tahu\" (konten segar, bagus untuk pencarian)."
        )
        return buildString {
            append("10 IDE KONTEN — $t\n")
            append("Target penonton: $audience\n\n")
            ide.forEachIndexed { i, s -> append("${i + 1}. $s\n\n") }
            append("Tips: mulai dari ide nomor 1, 2, dan 6 dulu — ketiganya paling cepat dibuat dan paling dicari $audience.")
        }
    }

    private fun judul(t: String, audience: String): String {
        val judul = listOf(
            "7 Cara $t yang Wajib Kamu Coba Sekarang!",
            "$t untuk Pemula: Mulai dari Nol Sampai Bisa",
            "Rahasia $t yang Jarang Dibocorkan Kreator",
            "Saya Coba $t Selama 30 Hari — Ini Hasil Jujurnya",
            "$t Tanpa Ribet: Panduan Lengkap untuk $audience",
            "Jangan Lakukan $t Sebelum Nonton Video Ini!",
            "5 Kesalahan Fatal soal $t (Nomor 3 Paling Sering!)",
            "$t di 2026: Cara Terbaru yang Terbukti Ampuh",
            "Gratis! Cara $t dengan Budget Minim Tapi Hasil Maksimal",
            "$t vs Cara Lama — Mana yang Lebih Worth It Buat Kamu?"
        )
        return buildString {
            append("10 JUDUL YOUTUBE — $t\n")
            append("Target penonton: $audience\n\n")
            judul.forEachIndexed { i, s -> append("${i + 1}. $s  (skor ${titleScore(s)}/100)\n\n") }
            append("Tips: judul terbaik biasanya 40–70 karakter, ada angka, dan ada satu kata pemicu seperti \"rahasia\", \"cara\", atau \"jangan\".")
        }
    }

    private fun deskripsi(t: String, audience: String): String {
        return """
            Deskripsi video — $t

            Di video ini kamu akan belajar $t dari nol sampai bisa, khusus untuk $audience. Cocok ditonton sampai habis karena setiap langkah dijelaskan dengan contoh nyata, bukan cuma teori.

            YANG AKAN KAMU DAPATKAN:
            • Penjelasan $t yang mudah dipahami pemula
            • Langkah praktis yang bisa langsung dipraktikkan hari ini
            • Kesalahan umum yang harus dihindari supaya tidak buang waktu

            TIMESTAMPS:
            00:00 Intro — kenapa $t penting
            01:30 Langkah 1: persiapan dasar
            04:00 Langkah 2: praktik langsung
            07:30 Langkah 3: hasil dan evaluasi
            10:00 Penutup + pertanyaan untuk kamu

            Kalau video ini membantu, like dan subscribe ya — tulis di komentar: bagian mana yang paling berguna buat kamu?

            #${t.replace(" ", "")} #Tutorial #Indonesia
        """.trimIndent()
    }

    private fun naskah(t: String, audience: String): String {
        return """
            NASKAH VIDEO — $t
            Target penonton: $audience | Durasi target: 8–12 menit

            HOOK (0–15 detik):
            "Kamu pernah merasa $t itu susah? Tunggu dulu — di video ini saya tunjukkan cara paling simpelnya, dan di menit ke-7 ada satu trik yang hampir tidak pernah dibahas orang."

            INTRO (15–60 detik):
            Perkenalkan diri singkat, sampaikan janji video ini dalam satu kalimat: setelah nonton, penonton bisa langsung mempraktikkan $t. Sebutkan 3 hal yang akan dibahas supaya penonton bertahan.

            POIN 1 — DASAR YANG WAJIB PAHAM:
            Jelaskan konsep $t dengan satu contoh nyata yang dekat dengan kehidupan $audience. Hindari istilah sulit tanpa penjelasan.

            POIN 2 — PRAKTIK LANGKAH DEMI LANGKAH:
            Tunjukkan prosesnya di layar. Setiap langkah diucapkan dengan jelas, beri jeda, dan tunjukkan hasil tiap langkah supaya penonton bisa mengikuti.

            POIN 3 — KESALAHAN & TIPS PRO:
            Sebutkan 3 kesalahan paling umum soal $t, lalu berikan cara menghindarinya. Tutup poin ini dengan satu tips yang membuat penonton merasa dapat "rahasia".

            PENUTUP + CTA:
            Rangkum dalam 3 kalimat. Ajak penonton komentar: "Tulis di komentar, kamu paling penasaran sama bagian mana?" Lalu arahkan ke video berikutnya yang masih berhubungan dengan $t.
        """.trimIndent()
    }

    private fun keywordTag(t: String, audience: String): String {
        val dasar = t.lowercase().split(" ").filter { it.length > 2 }
        val tags = mutableListOf<String>()
        tags += t.lowercase()
        tags += "$t untuk pemula"
        tags += "cara $t"
        tags += "tutorial $t"
        tags += "$t indonesia"
        tags += "belajar $t"
        tags += "tips $t"
        tags += "$t 2026"
        tags += "panduan $t"
        tags += "$t ${audience.lowercase()}"
        tags += "rahasia $t"
        tags += "$t lengkap"
        dasar.forEach { tags += it }
        val unik = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(20)
        val gabung = unik.joinToString(", ")
        return buildString {
            append("KEYWORD & TAG — $t\n")
            append("Target penonton: $audience\n\n")
            append("Tag (pisahkan dengan koma saat diunggah ke YouTube):\n")
            append(gabung)
            append("\n\nTotal karakter tag: ${gabung.length} / 500 karakter (batas YouTube). ")
            if (gabung.length > 500) append("Kurangi beberapa tag terpanjang supaya muat.") else append("Masih muat, aman dipakai.")
            append("\n\nKeyword utama untuk judul dan deskripsi: \"$t\", \"cara $t\", \"tutorial $t\", \"$t untuk pemula\".")
            append("\nTips: taruh keyword utama di 2 kalimat pertama deskripsi dan di awal judul.")
        }
    }

    private fun shorts(t: String, audience: String): String {
        val ide = listOf(
            "\"$t dalam 30 Detik\" — satu tips paling berguna, teks besar di layar, tanpa basa-basi.",
            "\"Kesalahan $t yang Sering Dilakukan Pemula\" — tunjukkan 1 kesalahan + cara benarnya, durasi 40 detik.",
            "\"Sebelum vs Sesudah $t\" — perbandingan visual yang langsung terlihat bedanya.",
            "\"3 Fakta Mengejutkan soal $t\" — format cepat, 1 fakta tiap 10 detik, akhiri dengan ajakan komentar.",
            "\"Coba $t Bareng Saya\" — rekam proses nyata dari awal sampai hasil, potong jadi 60 detik.",
            "\"$t Pakai Budget Minim, Bisa?\" — buktikan dengan contoh nyata, cocok untuk $audience.",
            "\"Jawaban Cepat: Pertanyaan Paling Sering soal $t\" — 1 pertanyaan 1 Shorts, buat seri.",
            "\"Trik $t yang Jarang Orang Tahu\" — hook 3 detik pertama harus kuat: tunjukkan hasilnya dulu.",
            "\"Rutinitas $t 1 Menit\" — ringkasan rutinitas harian, musik cepat, teks langkah 1-2-3.",
            "\"Kenapa $t Penting di 2026?\" — opini singkat + 1 data/alasan kuat, ajak penonton debat sehat."
        )
        return buildString {
            append("10 IDE YOUTUBE SHORTS — $t\n")
            append("Target penonton: $audience | Durasi: 15–60 detik\n\n")
            ide.forEachIndexed { i, s -> append("${i + 1}. $s\n\n") }
            append("Tips Shorts: 3 detik pertama menentukan segalanya — tunjukkan hasil atau pertanyaan paling menarik dulu, baru jelaskan.")
        }
    }
}
