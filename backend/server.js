import express from "express";
import cors from "cors";
import dotenv from "dotenv";

dotenv.config();
const app = express();
app.use(cors());
app.use(express.json({ limit: "32kb" }));

// Rate limit sederhana tanpa dependensi tambahan: maks 30 request
// per IP per 10 menit. Cukup untuk prototipe; untuk dijual ke publik
// pasang autentikasi + kuota per pengguna sebelum go-live.
const hits = new Map();
const WINDOW_MS = 10 * 60 * 1000;
const MAX_HITS = 30;
function rateLimit(req, res, next) {
  const now = Date.now();
  const key = req.ip || "unknown";
  const list = (hits.get(key) || []).filter((t) => now - t < WINDOW_MS);
  if (list.length >= MAX_HITS) {
    return res.status(429).json({ error: "Terlalu banyak permintaan. Coba lagi 10 menit lagi." });
  }
  list.push(now);
  hits.set(key, list);
  next();
}

app.get("/health", (req, res) => {
  res.json({ ok: true, aiConfigured: Boolean(process.env.OPENAI_API_KEY), time: new Date().toISOString() });
});

const ALLOWED_TOOLS = ["Ide Konten", "Judul YouTube", "Deskripsi Video", "Naskah Video", "Keyword & Tag", "Ide YouTube Shorts"];

app.post("/api/generate", rateLimit, async (req, res) => {
  try {
    const { tool, topic, audience } = req.body ?? {};
    if (!tool || !topic || !audience) {
      return res.status(400).json({ error: "tool, topic, dan audience wajib diisi." });
    }
    if (!ALLOWED_TOOLS.includes(tool)) {
      return res.status(400).json({ error: "tool tidak dikenal." });
    }
    if (typeof topic !== "string" || topic.trim().length < 2 || topic.length > 200) {
      return res.status(400).json({ error: "topic harus 2–200 karakter." });
    }
    if (typeof audience !== "string" || audience.length > 200) {
      return res.status(400).json({ error: "audience maksimal 200 karakter." });
    }
    if (!process.env.OPENAI_API_KEY) {
      return res.status(503).json({ error: "API AI belum dikonfigurasi di server." });
    }

    const prompt = `Kamu adalah asisten profesional untuk kreator YouTube pemula berbahasa Indonesia.
Bantu pengguna untuk: ${tool}
Topik/niche: ${topic.trim()}
Target penonton: ${audience.trim()}
Berikan hasil praktis, orisinal, terstruktur, dan sesuai kebijakan YouTube.
Jangan menjanjikan views, subscriber, atau monetisasi. Jangan menyarankan spam atau metadata menyesatkan.`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 45000);
    let upstream;
    try {
      upstream = await fetch("https://api.openai.com/v1/chat/completions", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: process.env.OPENAI_MODEL || "gpt-4.1-mini",
          messages: [{ role: "user", content: prompt }],
          temperature: 0.7
        }),
        signal: controller.signal
      });
    } finally {
      clearTimeout(timeout);
    }

    const data = await upstream.json().catch(() => ({}));
    if (!upstream.ok) {
      console.error("AI provider error:", upstream.status);
      return res.status(502).json({ error: "Layanan AI sedang bermasalah." });
    }
    const result = data.choices?.[0]?.message?.content?.trim();
    if (!result) return res.status(502).json({ error: "AI tidak mengembalikan hasil." });
    res.json({ result });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Kesalahan internal server." });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Creator Pro backend listening on ${port}`));
