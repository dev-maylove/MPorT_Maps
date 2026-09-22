# MPorT Maps v1.1.1 — Merged (bug-fixed)

Aplikasi peta & survey lapangan offline berbasis peta.

## Fitur
- Ukur jarak (polyline) & luas (polygon)
- Undo / Redo
- GPS → tambah titik
- Long-press → Marker
- Map type: Normal / Satellite / Terrain / Hybrid
- Satuan Metric / Imperial
- Riwayat + buka kembali ke peta
- Export CSV / JSON / KML
- Marker & Catatan lokal
- Neon Dark / Light theme (DataStore)
- Icon + Splash dari logo MPorT Maps

## Setup
1. Buka di Android Studio
2. Opsional di `gradle.properties`: `MAPS_API_KEY=AIza...`
3. Sync → Run

## Bug fixes in v1.1.1
- KML export menghasilkan XML valid (Polygon / LineString)
- Export aman di API 26–28 (fallback ke app external files)
- Escape CSV / JSON / XML pada nama & catatan
- Theme dark/light mengikuti Settings (DataStore)
- Tombol "Buka" di Riwayat mengembalikan titik ke peta + pindah tab Survey
- Setelah simpan survey, undo/redo stack di-reset bersih
- Marker Compose diberi `key` stabil agar tidak flicker
- MainActivity: setTheme sebelum super.onCreate (splash lebih bersih)

## Build
```bash
./gradlew :app:assembleDebug
```

## GitHub Actions

Workflows di `.github/workflows/`:

| Workflow | Trigger | Fungsi |
|----------|---------|--------|
| `android-ci.yml` | push / PR / manual | Build debug APK + upload artifact |
| `android-lint.yml` | push / PR / manual | Android Lint report |
| `android-release.yml` | manual only | Build release APK |

### Secrets (opsional)
Di repo Settings → Secrets → Actions, tambahkan:
- `MAPS_API_KEY` — Google Maps SDK key

### Toolchain (CI) — actions latest
| Action | Versi |
|--------|--------|
| `actions/checkout` | **v7** |
| `actions/setup-java` | **v6** |
| `actions/upload-artifact` | **v7** |
| `android-actions/setup-android` | **v4** |
| Runner | `ubuntu-latest` |
| JDK | **17** (Temurin) |
| Gradle | **8.11.1** |
| AGP / Kotlin / Compose | 8.7.3 / 2.0.21 / 2024.12.01 |
