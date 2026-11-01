# Mechi Puantaj & Personel Takip Otomasyonu (`my_mechi_personel_takip`)

> **ZKTeco Akıllı Matris Rapor Çözümleyici, Parametrik Mesai, Devamsızlık & Bordro Otomasyon Sistemi**  
> *Compose Multiplatform (Desktop / Android) ile geliştirilmiş yeni nesil kurumsal puantaj çözümü.*

---

## 🎯 Projenin Amacı ve Kapsamı

Bu proje, işletmelerde kullanılan **ZKTeco parmak izi okuyucu** cihazlarından dışa aktarılan ham Excel/XLS/XLSX (`Att.log report`) tablolarını yapay zekâ destekli sezgisel algoritmalarla ayrıştıran; vardiya saatlerini, fazla mesaileri, geç kalmaları, devamsızlıkları, avans ve geçmiş ay borçlarını hesaplayarak kurumsal standartta **A4 Bordro Dökümü** ve **Excel (.xlsx)** raporları üreten kapsamlı bir otomasyon sistemidir.

---

## 🚀 Yeni Sürümde Yapılan Mimari İyileştirmeler (Neler Daha İyi Yapıldı?)

1. **Modüler ve Temiz Mimari (Clean Architecture)**:
   - Eski tek parça devasa dosyalar (970+ satırlık modal vb.) yerine `domain/models`, `domain/calculator`, `domain/parser`, `presentation/screens`, `presentation/components` ve `theme` katmanlarına ayrıldı.
2. **Excel (.xlsx) Dışa Aktarma Motoru (Yeni Özellik)**:
   - Önceki sürümde boş olan "Excel'e Aktar" işlevi, Apache POI altyapısıyla profesyonel, renkli ve formatlanmış sütunlarla `.xlsx` dosyası üretecek şekilde sıfırdan yazıldı.
3. **Dinamik Takvim ve Hafta Sonu Tespiti**:
   - Sabit takvim varsayımı yerine, Zeller formülü ve dinamik tarih çözümleyici (`CalendarUtils`) entegre edildi. Hafta tatili (Cuma veya Pazar) dinamik olarak yönetilebilir hale getirildi.
4. **Çok Sayfalı A4 Yazdırma Motoru**:
   - `PrintService`, çok sayıda personelin bulunduğu durumlarda sayfalamayı (sayfa 1 / N) otomatik bölecek şekilde optimize edildi.
5. **Gelişmiş Canlı Arama ve Departman Filtresi**:
   - Bordro tablosunda ve detay inceleme ekranında anlık personel adı, ID ve departman bazlı hızlı filtreleme özellikleri eklendi.
6. **Android ve Desktop Çoklu Platform Desteği**:
   - Hem Windows Desktop (MSI / EXE paketleme) hem de mobil Android cihazlar için hazır mimari altyapı kuruldu.
7. **Birim Testleri (Unit Tests)**:
   - Maaş, devamsızlık kesintisi ve fazla mesai formüllerinin doğruluğunu garanti eden `PayrollCalculatorTest` test paketi eklendi.

---

## 🧠 ZKTeco Akıllı Çözümleme Motoru (ZKTecoParser)

ZKTeco cihazlarının matris rapor formatındaki belirsizlikleri çözmek için şu akıllı kurallar uygulanır:

1. **Gece Vardiyası Bağlama (Overnight Shift)**:
   - Gece `00:00 - 07:15` saatleri arasındaki ilk parmak izi tespiti, otomatik olarak **bir önceki günün çıkış saati** olarak bağlanır.
2. **Unutulan Sabah Girişi Tamamlama**:
   - Personel sabah giriş basmamış ancak gece `00:00 - 07:15` arası çıkış yapmışsa; sistem personelin girişini otomatik olarak tanımlı vardiya başlangıcına (örn: `09:00`) kurar.
3. **Tek Parmak İzi Akıllı Tamamlama**:
   - Personel gün içinde sadece bir kez parmak basmışsa:
     - Saat **15:00'den önce** ise → Giriş kabul edilir, çıkış saati `Giriş + Standart Vardiya Saati` olarak tamamlanır.
     - Saat **15:00'den sonra** ise → Çıkış kabul edilir, giriş saati `Çıkış - Standart Vardiya Saati` olarak tamamlanır.
   - Durum kartında *"Tek Parmak İzi (Otomatik)"* notu ile denetim şeffaflığı sağlanır.

---

## 📊 Bordro & Maaş Hesaplama Formülleri

- **Günlük Yevmiye**: $\text{Yevmiye} = \frac{\text{Taban Maaş}}{\text{Aydaki Toplam Gün (Sabit 30)}}$
- **Saatlik Ücret**: $\text{Saatlik} = \frac{\text{Günlük Yevmiye}}{\text{Standart Vardiya Saati (Örn: 9 Saat)}}$
- **Hafta İçi Mesai**: $(\text{Çalışılan Saat} - \text{Standart Saat}) \times \text{Hafta İçi Mesai Çarpanı (Varsayılan 2.0x)}$
- **Hafta Tatili (Cuma) Mesai**: $\text{Çalışılan Saat} \times \text{Hafta Sonu Mesai Çarpanı (Varsayılan 2.0x)}$
- **Devamsızlık Kesintisi**:
  - İzinsiz Devamsızlık: $\text{Günlük Yevmiye} \times 2.0\text{x}$
  - İzinli Devamsızlık: $\text{Günlük Yevmiye} \times 1.0\text{x}$ (Takvim kartında tek tıkla değiştirilebilir)
- **Geç Kalma**: Dakika bazlı saatlik ücretten düşüm (Tıklanarak muaf tutulabilir).
- **Net Maaş**: $\text{Taban Maaş} - \text{Kesintiler} + \text{Mesai Ücreti} - \text{Avans} - \text{Geçen Ay Borç}$
- **Bakiye Durumu**:
  - Net Maaş $\ge 0$ ise $\rightarrow$ `Net Alacak` (Ödenecek Tutar)
  - Net Maaş $< 0$ ise $\rightarrow$ `Devreden Borç` (Gelecek aya devreden tutar)

---

## 🛠️ Kurulum ve Çalıştırma

### Gereksinimler
- JDK 17 veya üzeri
- Android Studio Ladybug / Koala veya IntelliJ IDEA
- Windows (MSI/EXE çıktısı için WiX Toolset)

### Masaüstü Uygulamasını Başlatma
```bash
./gradlew run
```

### Windows MSI Kurulum Paketi Üretme
```bash
./gradlew packageMsi
```
*(Üretilen MSI dosyası `app/build/compose/binaries/main/msi/` altında yer alır).*

### Android Projesini Derleme
```bash
./gradlew assembleDebug
```

---

## 📁 Proje Dizin Yapısı

```
my_mechi_personel_takip/
├── app/
│   ├── src/
│   │   ├── commonMain/kotlin/com/mechi/personeltakip/
│   │   │   ├── domain/
│   │   │   │   ├── models/        # DailyPunch, EmployeeAttendance, PayrollModels
│   │   │   │   └── calculator/    # PayrollCalculator, CalendarUtils
│   │   │   └── presentation/
│   │   │       ├── components/    # KPICards, PremiumNavBar, TimePickerDialog
│   │   │       ├── screens/       # UploadScreen, SettingsScreen, PayrollTableScreen, ...
│   │   │       ├── theme/         # Colors, Slate950 Dark Theme
│   │   │       └── App.kt         # Ana orkestrasyon bileşeni
│   │   ├── desktopMain/kotlin/
│   │   │   ├── com/mechi/personeltakip/desktop/
│   │   │   │   ├── ZKTecoParser.kt       # Akıllı Excel çözümleyici
│   │   │   │   ├── ExcelExportService.kt # .xlsx dışa aktarıcı
│   │   │   │   ├── PrintService.kt       # Çok sayfalı A4 yazdırma
│   │   │   │   └── AppStatePersistence.kt# JSON durum saklama
│   │   │   └── main.kt                   # Masaüstü pencere başlatıcı
│   │   └── androidMain/
│   │       ├── AndroidManifest.xml
│   │       └── kotlin/.../MainActivity.kt
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---
*© 2026 Mechi — Kurumsal Puantaj ve Bordro Çözümleri.*
