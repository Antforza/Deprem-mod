# Deprem Modu (Minecraft Java 26.2 — Fabric)

## Özellikler
- `/deprem start [şiddet] [derinlik_km] [uzaklık_km]` → depremi sen başlatırsın, kamera sallanır
  - Örnek: `/deprem start 5` · `/deprem start 6.5 10 15`
- `/deprem stop` → deprem durur
- **Kendiliğinden deprem:** sen komut vermesen de, evdeyken bile bir anda deprem olur
  - `/deprem otomatik ac` / `/deprem otomatik kapat`
  - `/deprem aralik 10 30` → depremler 10–30 dakika arayla (varsayılan)
  - `/deprem rastgele` → hemen rastgele bir deprem (test için)
  - `/deprem durum` → ayarları gösterir
- **Deprem Telefonu** (`deprem:telefon`): elinde tutup **sağ tıkla** → uygulama açılır:
  büyüklük, derinlik, uzaklık (+yön), sende hissedilen şiddet ve önceki depremler.
  Envanterinde telefon varsa deprem olunca sohbete bildirim de düşer.
  - Alma: `/give @s deprem:telefon`, Yaratıcı modda "Araçlar" sekmesi, veya craft:
    ```
    demir  cam-levha  demir
    demir  kızıltaş   demir
    demir  demir      demir
    ```
- Uzak veya zayıf depremler sallanma yapmaz; ama telefon yine de haber verir.
- Ayar dosyası: `config/deprem.properties`

## Kurulum (oyun için)
1. Fabric Loader (26.2) + **Fabric API** kurulu olmalı.
2. `deprem-1.0.0.jar` dosyasını `.minecraft/mods` klasörüne at.
(Komutlar için dünyada hile/komut açık olmalı.)

## Jar'ı derlemek

### Yol 1 — GitHub (bilgisayara bir şey kurmadan)
1. github.com'da ücretsiz hesap aç → **New repository**.
2. Bu klasörün **içindekileri** (`.github`, `src`, `build.gradle` ... hepsi) repoya yükle
   (Add file → Upload files; klasörleri sürükle-bırak).
3. **Actions** sekmesi → "Jar derle" çalışır (~3-5 dk). Bitince çalıştırmaya tıkla →
   en altta **Artifacts → deprem-mod-jar** → indir, zip içinden jar'ı çıkar.

### Yol 2 — Kendi bilgisayarında
1. JDK 25 ve Gradle 9.5+ kur.
2. Bu klasörde: `gradle build`
3. Jar: `build/libs/deprem-1.0.0.jar`
