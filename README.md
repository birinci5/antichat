# AntiCheat Plugin

Paket seviyesinde (ProtocolLib destekli) gelismis anti-cheat eklentisi.
Paper 1.21+ ve Spigot 1.21+ ile uyumludur.

## Derleme (En Kolay Yol - Kurulum Gerektirmez)

Bu projede `.github/workflows/build.yml` hazir gelir. GitHub'in kendi
sunuculari internete tam erisimli oldugu icin sizin yerinize derler:

1. github.com'da yeni, bos bir repo olusturun (Public veya Private, fark etmez).
2. Bu klasordeki TUM dosyalari (README dahil, .github klasoru dahil) o repoya
   push edin. Git bilmiyorsaniz: repo sayfasinda "Add file > Upload files"
   deyip bu zip'in icindeki `anticheat-plugin` klasorunun TUM icerigini
   (klasor yapisini koruyarak) surukleyip birakin, "Commit changes" deyin.
3. Repo sayfasinda ust menuden **Actions** sekmesine girin. "AntiCheat Jar
   Derle" workflow'u otomatik calisir (birkac dakika surer).
4. Calisma bitince ayni sayfada **Artifacts** bolumunden "AntiCheat-jar"
   adli zip'i indirin. Icinde derlenmis `AntiCheat-1.0.0.jar` bulunur.
5. Bu jar'i sunucunuzun `plugins/` klasorune atin.

Workflow calismazsa (kirmizi X), Actions sekmesindeki log ciktisini
paylasin, hatayi birlikte cozeriz.

## Derleme (Alternatif - Kendi Bilgisayariniz)

Gerekli: JDK 21, internet baglantisi (Maven Central, repo.papermc.io, repo.dmulloy2.net).

```bash
# Proje klasorunde:
gradle wrapper          # sadece bir kere, gradle wrapper dosyalarini olusturur
./gradlew build         # Linux/Mac
gradlew.bat build       # Windows
```

Derlenen jar dosyasi: `build/libs/AntiCheat-1.0.0.jar`

Eger sisteminizde Gradle kurulu degilse:
- IntelliJ IDEA ile projeyi acin (File > Open > bu klasoru secin), IDE Gradle'i
  otomatik indirip yapilandiracaktir. Sag tarafta Gradle panelinden
  `Tasks > build > build` calistirin.
- Ya da https://gradle.org/install/ adresinden Gradle'i kurup yukaridaki
  `gradle wrapper` komutunu calistirin.

## Kurulum

1. `AntiCheat-1.0.0.jar` dosyasini sunucunuzun `plugins/` klasorune atin.
2. (Onerilir) ProtocolLib'i de `plugins/` klasorune ekleyin - Timer ve
   NoSwing kontrolleri icin gereklidir. ProtocolLib olmadan da eklenti
   calisir, sadece bu iki kontrol devre disi kalir (konsolda uyari gorursunuz).
3. Sunucuyu baslatin/reload edin. `plugins/AntiCheat/config.yml` otomatik olusur.
4. Yetkililere `anticheat.admin` iznini verin (varsayilan: op).

## Komutlar

- `/ac toggle <kontrol>` - bir kontrolu ac/kapat (reach, speed, fly, nofall, killaura, autoclicker, timer, noswing)
- `/ac vl <oyuncu>` - oyuncunun tum kontrollerdeki VL degerlerini gosterir
- `/ac exempt <oyuncu>` - oyuncuyu korumadan muaf tutar/muafiyeti kaldirir
- `/ac reset <oyuncu>` - oyuncunun VL'lerini sifirlar
- `/ac reload` - config.yml'i yeniden yukler
- `/ac config <kontrol> <ayar> <deger>` - orn: `/ac config reach max-distance 4.5`

## Onemli Notlar

- Paket ve player sayisi buyuk sunucularda `movement-tick-throttle` ve
  `violation-decay-seconds` degerleriyle performans/hassasiyet dengesini
  ayarlayin.
- ProtocolLib gerektiren kontroller (Timer, NoSwing) gercek TCP/UDP paket
  akisini okur; bu yuzden Skript'in erisemedigi bir seviyede tespit saglar.
- Bu proje, `com.anticheat` paket adi ve `com.anticheat` group ID ile
  hazirlanmistir; kendi projeniz icin degistirmeniz onerilir.
