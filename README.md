# MPorT Maps

Aplikasi survei & pengukuran jarak/luas berbasis Google Maps (Compose).

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
- Settings: Saved List, Map Type, Units, GPS Settings, About, Privacy Policy
- Icon + Splash dari logo MPorT Maps

## Setup
1. Buka di Android Studio
2. Opsional di `gradle.properties`: `MAPS_API_KEY=AIza...`
3. Sync → Run

## Perubahan v1.2.0
- Settings UI list-style (Saved List, Map Type, Units, GPS Settings, About, Privacy Policy)
- Units dialog: Meter / Kilometer / Mile / Feet / Nautical Mile / Yard
- GPS Settings membuka sistem Location settings
- Halaman About & Privacy Policy
- UnitMode diperluas + backward compatible dengan METRIC/IMPERIAL lama
- Hapus menu Tips and Tricks, Rate Application, More Apps

## Bug fixes sebelumnya (v1.1.1)
- KML export menghasilkan XML valid (Polygon / LineString)
- Export aman di API 26–28
- Escape CSV / JSON / XML
- Theme dark/light mengikuti Settings
- Tombol "Buka" di Riwayat mengembalikan titik ke peta
- Setelah simpan survey, undo/redo stack di-reset
- Marker Compose diberi key stabil
- MainActivity: setTheme sebelum super.onCreate

## Build
```bash
./gradlew :app:assembleDebug
```
