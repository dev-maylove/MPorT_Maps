# MPorT Maps

Aplikasi survei & pengukuran jarak/luas berbasis Google Maps (Compose).
Bilingual: **English** + **Bahasa Indonesia**.

## Fitur
- Ukur jarak (polyline) & luas (polygon)
- Undo / Redo
- GPS → tambah titik
- Long-press → Marker
- Map type: Normal / Satellite / Terrain / Hybrid
- Satuan: Meter, Kilometer, Mile, Feet, Nautical Mile, Yard
- Riwayat + buka kembali ke peta
- Export CSV / JSON / KML
- Marker & Catatan lokal
- Neon Dark / Light theme (DataStore)
- Settings: Saved List, Map Type, Units, GPS Settings, Language, About, Privacy Policy, License, Security
- **Language**: System / English / Bahasa Indonesia
- Icon + Splash dari logo MPorT Maps

## Setup
1. Buka di Android Studio
2. Opsional di `gradle.properties`: `MAPS_API_KEY=AIza...`
3. Sync → Run

## Perubahan v1.2.0
- Settings UI list-style
- Units dialog: m / km / mi / ft / nmi / yd
- GPS Settings → system Location settings
- Halaman About, Privacy Policy, License & Security
- **Bilingual EN + ID** dengan pilihan bahasa di Settings
- UnitMode diperluas + backward compatible

## Build
```bash
./gradlew :app:assembleDebug
```
