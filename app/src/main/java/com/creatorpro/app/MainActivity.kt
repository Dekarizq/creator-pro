package com.creatorpro.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tema bersih ala TubeCuan (putih + merah YouTube) — sesuai pilihan Bang Medhol.
private val Red = Color(0xFFE62117)
private val Orange = Color(0xFFFF7A00)
private val Bg = Color(0xFFF6F6F6)
private val CardBg = Color.White
private val Ink = Color(0xFF1A1A1A)
private val Grey = Color(0xFF6B7280)

private const val WA_NUMBER = "628989369554" // 08989369554
private const val PREFS = "creatorpro"
private const val HISTORY_KEY = "history_v1"

data class HistoryItem(val tool: String, val topic: String, val result: String, val time: Long)

private fun loadHistory(context: Context): List<HistoryItem> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY_KEY, "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("\n<<<ENTRY>>>\n").mapNotNull { block ->
        val parts = block.split("\n<<<PART>>>\n")
        if (parts.size >= 4) {
            HistoryItem(parts[0], parts[1], parts[3], parts[2].toLongOrNull() ?: 0L)
        } else null
    }.sortedByDescending { it.time }
}

private fun saveHistory(context: Context, item: HistoryItem) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val current = loadHistory(context).take(9)
    val all = listOf(item) + current
    val raw = all.joinToString("\n<<<ENTRY>>>\n") {
        listOf(it.tool, it.topic, it.time.toString(), it.result).joinToString("\n<<<PART>>>\n")
    }
    prefs.edit().putString(HISTORY_KEY, raw).apply()
}

private fun clearHistory(context: Context) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(HISTORY_KEY).apply()
}

private fun copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Creator Pro", text))
    Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan hasil"))
}

private fun openWhatsApp(context: Context) {
    val url = "https://wa.me/$WA_NUMBER?text=" + Uri.encode("Halo Bang Medhol, saya mau beli Creator Pro")
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun formatTime(time: Long): String =
    SimpleDateFormat("dd MMM HH:mm", Locale("in", "ID")).format(Date(time))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CreatorProApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorProApp() {
    var selected by remember { mutableStateOf("Beranda") }
    val items = listOf("Beranda", "AI Tools", "Kalkulator", "Harga")
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Red, background = Bg, surface = CardBg,
            onBackground = Ink, onSurface = Ink
        )
    ) {
        Scaffold(
            containerColor = Bg,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("CREATOR PRO", fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, color = Ink)
                            Text("YOUTUBE CREATOR TOOLKIT", fontSize = 10.sp, color = Grey)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg),
                    actions = {
                        IconButton(onClick = { selected = "Harga" }) {
                            Icon(Icons.Default.Settings, contentDescription = "Harga & bantuan")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = CardBg) {
                    items.forEach { item ->
                        val icon = when (item) {
                            "Beranda" -> Icons.Default.Home
                            "AI Tools" -> Icons.Default.AutoAwesome
                            "Kalkulator" -> Icons.Default.Calculate
                            else -> Icons.Default.Star
                        }
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Red, selectedTextColor = Red)
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                "AI Tools" -> AiToolsScreen(Modifier.padding(padding))
                "Kalkulator" -> CalculatorScreen(Modifier.padding(padding))
                "Harga" -> HargaScreen(Modifier.padding(padding))
                else -> HomeScreen(Modifier.padding(padding)) { selected = it }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navigate: (String) -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Selamat datang, Creator!", color = Grey)
        Box(
            Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Red, Orange)), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.AutoAwesome, tint = Color.White, contentDescription = null, modifier = Modifier.size(32.dp))
                Text("Kembangkan channel YouTube-mu", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Buat ide, judul, deskripsi, dan naskah video — langsung di HP, tanpa API key, tanpa biaya bulanan.", color = Color.White)
                Button(
                    onClick = { navigate("AI Tools") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Red),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Mulai gunakan AI", fontWeight = FontWeight.Bold) }
            }
        }
        Text("TOOLS CREATOR", fontWeight = FontWeight.Bold)
        val tools = listOf(
            "Generator Ide Konten" to "10 ide video siap pakai",
            "Judul & Deskripsi SEO" to "Judul + skor 0-100, deskripsi lengkap",
            "Naskah Video" to "Hook, 3 poin utama, dan penutup",
            "Kalkulator Jam Tayang" to "Jam tayang, monetisasi & penghasilan"
        )
        tools.forEach { (title, desc) ->
            Card(onClick = { navigate(if (title.contains("Kalkulator")) "Kalkulator" else "AI Tools") },
                colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, tint = Red, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.SemiBold)
                        Text(desc, color = Grey, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, tint = Orange, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Versi ini GRATIS dulu — semua tools terbuka. Tidak perlu login, tidak perlu kartu kredit.", fontSize = 13.sp)
            }
        }
        Text("Creator Pro adalah alat bantu independen dan bukan aplikasi resmi YouTube.", color = Grey, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tool by remember { mutableStateOf("Ide Konten") }
    var topic by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("Penonton umum") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(loadHistory(context)) }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("AI CREATOR ASSISTANT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Pilih kebutuhan, masukkan topik, lalu buat. Mode offline — hasil langsung muncul tanpa internet dan tanpa API key.")
        if (CreatorApi.isOnlineConfigured()) {
            Text("Mode online aktif: hasil dari server AI. Kalau server gagal, otomatis pakai mode offline.", color = Grey, fontSize = 12.sp)
        }
        Text("Jenis bantuan", fontWeight = FontWeight.SemiBold)
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = tool, onValueChange = {}, readOnly = true, label = { Text("Fitur AI") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Ide Konten", "Judul YouTube", "Deskripsi Video", "Naskah Video", "Keyword & Tag", "Ide YouTube Shorts").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { tool = it; expanded = false })
                }
            }
        }
        OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik atau niche channel") },
            placeholder = { Text("Contoh: suara hujan untuk tidur") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = audience, onValueChange = { audience = it }, label = { Text("Target penonton") }, modifier = Modifier.fillMaxWidth())
        Button(
            enabled = topic.isNotBlank() && !loading,
            onClick = {
                loading = true; result = ""; error = ""
                scope.launch {
                    try {
                        result = CreatorApi.generate(tool, topic, audience)
                        saveHistory(context, HistoryItem(tool, topic.trim(), result, System.currentTimeMillis()))
                        history = loadHistory(context)
                    } catch (e: Exception) {
                        error = e.message ?: "Terjadi kesalahan. Coba lagi."
                    } finally { loading = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            else Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (loading) "Sedang membuat..." else "Buat dengan AI")
        }
        if (error.isNotBlank()) Text(error, color = Red)
        if (result.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HASIL AI — $tool", fontWeight = FontWeight.Bold, color = Red)
                    Text(result)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { copyText(context, result) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Salin")
                        }
                        OutlinedButton(onClick = { shareText(context, result) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Bagikan")
                        }
                    }
                }
            }
        }
        if (history.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RIWAYAT TERAKHIR", fontWeight = FontWeight.Bold)
                TextButton(onClick = { clearHistory(context); history = emptyList() }) { Text("Hapus semua") }
            }
            history.take(5).forEach { item ->
                Card(onClick = { result = item.result; tool = item.tool; topic = item.topic },
                    colors = CardDefaults.cardColors(containerColor = CardBg)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${item.tool} — ${item.topic}", fontWeight = FontWeight.SemiBold)
                        Text(item.result.take(120) + if (item.result.length > 120) "…" else "", color = Grey, fontSize = 12.sp)
                        Text(formatTime(item.time), color = Grey, fontSize = 11.sp)
                    }
                }
            }
            Text("Ketuk riwayat untuk membuka lagi hasilnya.", color = Grey, fontSize = 11.sp)
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var views by remember { mutableStateOf("1000") }
    var minutes by remember { mutableStateOf("5") }
    val totalHours = ((views.toDoubleOrNull() ?: 0.0) * (minutes.toDoubleOrNull() ?: 0.0)) / 60.0

    var jamSekarang by remember { mutableStateOf("1200") }
    var subsSekarang by remember { mutableStateOf("350") }
    var jamPerHari by remember { mutableStateOf("20") }
    val sisaJam = (4000.0 - (jamSekarang.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
    val sisaSubs = (1000 - (subsSekarang.toIntOrNull() ?: 0)).coerceAtLeast(0)
    val persenJam = ((jamSekarang.toDoubleOrNull() ?: 0.0) / 4000.0 * 100.0).coerceIn(0.0, 100.0)
    val hariPerkiraan = if ((jamPerHari.toDoubleOrNull() ?: 0.0) > 0) sisaJam / (jamPerHari.toDoubleOrNull() ?: 1.0) else 0.0

    var viewsBulan by remember { mutableStateOf("50000") }
    var rpm by remember { mutableStateOf("5000") }
    val penghasilan = ((viewsBulan.toDoubleOrNull() ?: 0.0) / 1000.0) * (rpm.toDoubleOrNull() ?: 0.0)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("KALKULATOR CREATOR", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Kalkulator Jam Tayang", fontWeight = FontWeight.Bold)
        OutlinedTextField(views, { views = it.filter(Char::isDigit) }, label = { Text("Jumlah penayangan") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(minutes, { minutes = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Rata-rata durasi tonton (menit)") }, modifier = Modifier.fillMaxWidth())
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("ESTIMASI JAM TAYANG", color = Grey)
                Text(String.format(Locale("in", "ID"), "%.2f jam", totalHours), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Red)
            }
        }

        Text("Target Monetisasi YouTube", fontWeight = FontWeight.Bold)
        Text("Syarat: 1.000 subscriber dan 4.000 jam tayang dalam 12 bulan terakhir.", color = Grey, fontSize = 12.sp)
        OutlinedTextField(jamSekarang, { jamSekarang = it.filter(Char::isDigit) }, label = { Text("Jam tayang kamu sekarang") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(subsSekarang, { subsSekarang = it.filter(Char::isDigit) }, label = { Text("Subscriber kamu sekarang") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(jamPerHari, { jamPerHari = it.filter(Char::isDigit) }, label = { Text("Tambahan jam tayang per hari (estimasi)") }, modifier = Modifier.fillMaxWidth())
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sisa jam tayang: ${String.format(Locale("in", "ID"), "%.0f jam", sisaJam)} (${String.format(Locale("in", "ID"), "%.0f", persenJam)}%)", fontWeight = FontWeight.SemiBold)
                Text("Sisa subscriber: $sisaSubs")
                Text("Perkiraan tercapai dalam ±${String.format(Locale("in", "ID"), "%.0f hari", hariPerkiraan)} lagi, kalau konsisten.", color = Grey, fontSize = 13.sp)
            }
        }

        Text("Estimasi Penghasilan", fontWeight = FontWeight.Bold)
        OutlinedTextField(viewsBulan, { viewsBulan = it.filter(Char::isDigit) }, label = { Text("Views per bulan") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rpm, { rpm = it.filter(Char::isDigit) }, label = { Text("RPM (Rp per 1.000 views)") }, modifier = Modifier.fillMaxWidth())
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("ESTIMASI PER BULAN", color = Grey)
                Text("Rp ${String.format(Locale("in", "ID"), "%,.0f", penghasilan)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Red)
                Text("RPM tiap channel berbeda (umumnya Rp2.000–Rp20.000). Ini hanya estimasi, bukan janji penghasilan.", color = Grey, fontSize = 12.sp)
            }
        }
        Text("Hasil ini hanya estimasi. Jam tayang yang memenuhi syarat monetisasi ditentukan YouTube dan tidak selalu sama dengan total jam tayang di Analytics.", color = Grey, fontSize = 12.sp)
    }
}

@Composable
fun HargaScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("HARGA CREATOR PRO", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Semua tools di versi ini GRATIS dulu — tanpa login dan tanpa batas. Paket di bawah ini untuk nanti, saat versi Pro dijual.")
        val paket = listOf(
            Triple("GRATIS — Coba Dulu", "Rp0", "Semua 6 tools AI offline • Kalkulator lengkap • Riwayat tersimpan di HP"),
            Triple("COBA — Rp2.000", "Rp2.000", "Dukungan prioritas via WA • Update template judul terbaru"),
            Triple("LENGKAP — Rp10.000 ⭐ PALING LARIS", "Rp10.000", "Semua di paket Coba • Template naskah Pro • Ide konten 30 hari"),
            Triple("FULL + BONUS — Rp15.000", "Rp15.000", "Semua di paket Lengkap • Checklist upload Pro • Grup kreator")
        )
        paket.forEach { (nama, harga, isi) ->
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(nama, fontWeight = FontWeight.Bold)
                    Text(harga, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Red)
                    Text(isi, color = Grey, fontSize = 13.sp)
                }
            }
        }
        Text("CARA BELI (nanti saat Pro dijual)", fontWeight = FontWeight.Bold)
        Text("1. Chat WhatsApp penjual di bawah ini.\n2. Bayar via DANA / GoPay.\n3. Kirim bukti bayar, lalu penjual kirim kode akses.", fontSize = 14.sp)
        Button(onClick = { openWhatsApp(context) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Chat WhatsApp Penjual — 08989369554")
        }
        Text("Kode akses Pro (disimpan penjual, jangan disebar): TC-COBA2 (Rp2rb) • TC-LENGKAP10 (Rp10rb) • TC-FULL15 (Rp15rb).", color = Grey, fontSize = 11.sp)
        Text("Pembayaran DANA / GoPay. Harga sekali bayar, bukan langganan bulanan.", color = Grey, fontSize = 12.sp)
    }
}
