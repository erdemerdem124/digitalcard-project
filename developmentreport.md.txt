Dijital Kart Uygulaması - Geliştirme Raporu
1. Giriş
Bu doküman, Soliner stajyer case çalışması kapsamında geliştirilen "Dijital Kart Uygulaması" projesinin çalışma prensiplerini, izlenen geliştirme sürecini ve alınan teknik tercihleri açıklamaktadır. Proje, kullanıcıların kişisel ve profesyonel bilgilerini dijital bir kart formatında sergileyebilmelerini sağlayan bir tam yığın (full-stack) web uygulamasıdır.

2. Çalışma Prensibi ve Mimari
Uygulama, standart bir istemci-sunucu (client-server) mimarisiyle tasarlanmıştır.

Frontend (İstemci): Kullanıcı arayüzünü (UI) ve kullanıcı etkileşimlerini yönetir. Angular framework'ü ile geliştirilmiştir. Backend API'sine HTTP istekleri göndererek veri alır ve gönderir.

Backend (Sunucu): İş mantığını, veritabanı etkileşimlerini ve güvenliği yöneten RESTful API servislerini sağlar. Spring Boot framework'ü ile Java kullanılarak geliştirilmiştir.

Veritabanı: Tüm kullanıcı bilgileri, sosyal medya linkleri ve proje detayları PostgreSQL ilişkisel veritabanında saklanır. Spring Data JPA, Hibernate ORM aracı olarak kullanılarak veritabanı işlemlerini kolaylaştırır.

İletişim Akışı:

Kullanıcı frontend uygulaması üzerinden (örneğin giriş yapma, profil güncelleme) bir eylem başlattığında, Angular servisi ilgili verileri bir HTTP isteği (GET, POST, PUT, DELETE) ile backend API'sine gönderir.

Backend, gelen isteği işler. Eğer istek kimlik doğrulama veya yetkilendirme gerektiriyorsa, JWT (JSON Web Token) kullanılarak doğrulanır.

Backend, gerekli iş mantığını uyguladıktan sonra (örneğin veritabanından veri okuma/yazma), HTTP yanıtını frontend'e geri gönderir.

Frontend, gelen yanıtı işler ve kullanıcı arayüzünü buna göre günceller.

3. Geliştirme Süreci
Projenin geliştirme süreci aşağıdaki adımları izlemiştir:

Gereksinim Analizi ve Tasarım: Stajyer case tanımı dikkatlice incelenerek, projenin temel özellikleri ve istenen mimari belirlenmiştir. Backend ve frontend için temel modüller ve API endpoint'leri kabaca tasarlanmıştır.

Backend Geliştirme (Spring Boot):

Veritabanı Modelleri: Kullanıcı (User), Sosyal Link (SocialLink) ve Proje (Project) gibi temel entity'ler ve onların veritabanı şemaları (PostgreSQL) oluşturulmuştur.

Güvenlik Katmanı: Spring Security ile JWT (JSON Web Token) tabanlı kimlik doğrulama ve yetkilendirme altyapısı kurulmuştur. Kullanıcı kayıt, giriş ve token doğrulama mekanizmaları implemente edilmiştir.

RESTful API Geliştirme: Her bir entity için CRUD (Create, Read, Update, Delete) operasyonlarını destekleyen RESTful API endpoint'leri yazılmıştır. (Örnek: /api/users, /api/sociallinks, /api/projects).

Servis Katmanı: İş mantığı, Controller katmanından ayrılarak servis katmanına (Service Layer) taşınmış, böylece kodun temizliği ve test edilebilirliği artırılmıştır.

Frontend Geliştirme (Angular):

Proje Yapılandırması: Angular CLI kullanılarak yeni bir Angular projesi oluşturulmuş ve temel navigasyon (routing) yapılandırılmıştır.

Giriş/Kayıt Modülleri: Kullanıcı kimlik doğrulama ve kayıt formları, ilgili servislerle (AuthService) haberleşerek implemente edilmiştir.

Profil Yönetimi ve Kart Görüntüleme: Kullanıcının profil bilgilerini (kullanıcı adı, e-posta, biyografi, profil fotoğrafı) düzenleyebileceği sayfalar ve bu bilgilerin dijital kart olarak görüntülendiği komponentler geliştirilmiştir.

Sosyal Link ve Proje Yönetimi: Kullanıcının sosyal medya linkleri ve projeleri ekleyip düzenleyebileceği dinamik formlar ve listeler oluşturulmuştur.

Interceptor Kullanımı: AuthInterceptor ile tüm API isteklerine otomatik olarak JWT token'ının eklenmesi sağlanmıştır, bu da kod tekrarını önlemiştir.

Hata ve Bildirim Yönetimi: ToastService kullanılarak kullanıcıya geri bildirimler (başarı, hata, bilgi mesajları) sunulmuştur.

Entegrasyon ve Test: Frontend ve backend arasındaki API entegrasyonları test edilmiştir. Özellikle kimlik doğrulama ve yetkilendirme akışları detaylıca incelenmiştir.

4. Yapılan Tercihler ve Nedenleri
Geliştirme sürecinde alınan bazı önemli kararlar ve bu kararların arkasındaki nedenler aşağıda açıklanmıştır:

Spring Boot (Backend) ve Angular (Frontend) Seçimi:

Neden: Bu iki teknoloji, modern web uygulamaları geliştirmek için sektörde oldukça yaygın, güçlü ve iyi belgelenmiş çerçevelerdir. Farklı ekiplerin (backend/frontend) paralel çalışmasına olanak tanıyan ayrı bir mimari sağlarlar.

Alternatifler: Node.js (Express), React/Vue.js.

JWT (JSON Web Token) Kullanımı:

Neden: Durumsuz (stateless) bir kimlik doğrulama mekanizmasıdır. Her istekte sunucunun oturum bilgisi tutmasına gerek kalmaz, bu da ölçeklenebilirliği artırır. Mobil uygulamalar ve mikro servisler için de uygun bir yapıdır.

Alternatifler: Session-based authentication (daha çok geleneksel monolith uygulamalarda kullanılır).

PostgreSQL Veritabanı Seçimi:

Neden: Güçlü, açık kaynaklı, güvenilir ve geniş özellik setine sahip ilişkisel bir veritabanıdır. Esneklik, performans ve veri bütünlüğü açısından tercih edilmiştir.

Alternatifler: MySQL, H2 (geliştirme/test için), MongoDB (NoSQL).

application.properties Dosyasının .gitignore'a Eklenmesi:

Neden: Veritabanı şifreleri, JWT secret key gibi hassas bilgilerin kaynak kontrol sistemine (GitHub) yüklenmesini engellemek ve güvenlik açıklarını önlemek için hayati bir adımdır.

Çözüm: Geliştiricilerin projeyi kolayca çalıştırabilmesi için application.properties.example dosyası sağlanmıştır.

AuthInterceptor Kullanımı:

Neden: Her HTTP isteğine JWT token'ını manuel olarak eklemek yerine, merkezi bir Interceptor kullanarak bu işlemi otomatik hale getirmek, kod tekrarını önler ve temizlik sağlar.

ToastService Kullanımı:

Neden: Kullanıcıya geri bildirimlerin (başarı, hata, bilgi mesajları) tutarlı ve anlaşılır bir şekilde sunulması, kullanıcı deneyimini önemli ölçüde artırır.

5. Karşılaşılan Zorluklar ve Çözümleri
Geliştirme sürecinde karşılaşılan bazı zorluklar ve bunlara getirilen çözümler aşağıdadır:

Token'ın LocalStorage'dan Okunması Sorunu (AuthInterceptor: Token bulunamadı):

Zorluk: Geliştirme sırasında, kullanıcı giriş yaptıktan sonra AuthInterceptor'ın localStorage'dan JWT token'ını okuyamadığı ve dolayısıyla yetkilendirme başlığının eklenemediği gözlemlenmiştir. Bu durum, "Profil yükleniyor..." hatasına yol açmıştır.

Çözüm: AuthService içindeki login metodunun tap operatöründe token'ın localStorage.setItem ile doğru JWT_TOKEN_KEY anahtarıyla kaydedildiğinden emin olunmuştur. Ayrıca, getToken() metodunun da aynı anahtarı kullandığı teyit edilmiştir. Tarayıcı önbelleği ve localStorage'ın temizlenmesi, bu tür sorunları gidermede sıkça başvurulan bir çözüm olmuştur. (Bu kısım, daha önceki konuşmalarımızdaki debug sürecine atıfta bulunuyor.)

CORS (Cross-Origin Resource Sharing) Problemleri:

Zorluk: Frontend ve backend farklı portlarda çalıştığı için başlangıçta tarayıcı güvenlik politikalarından kaynaklanan CORS hataları alınmıştır.

Çözüm: Spring Boot backend'inde (DigitalcardBackendApplication.java veya ayrı bir konfigürasyon sınıfında) @CrossOrigin anotasyonları veya global CORS yapılandırması eklenerek bu sorun aşılmıştır. (@CrossOrigin(origins = "http://localhost:4200") gibi).

JWT Token Süre Yönetimi:

Zorluk: Kullanıcı oturumu sona erdiğinde veya token geçersiz hale geldiğinde oturumun düzgün bir şekilde sonlandırılması ve kullanıcının tekrar giriş yapmasının sağlanması.

Çözüm: Hem backend'de token'ın süresinin dolması hem de frontend'de (AuthInterceptor'da 401/403 hatalarının yakalanması ve AuthService.logout() çağrılması) ile oturum yönetiminin doğru şekilde yapılması sağlanmıştır.

6. Gelecek Geliştirmeler ve İyileştirmeler
Projeye gelecekte eklenebilecek bazı özellikler ve yapılabilecek iyileştirmeler:

Profil Fotoğrafı Yükleme: Mevcut haliyle URL ile fotoğraf ekleniyor. Kullanıcının doğrudan dosya yükleyebileceği bir mekanizma eklenmesi.

Şifre Güncelleme Fonksiyonelliği: Kullanıcıların mevcut şifrelerini girerek yeni bir şifre belirleyebilmesi. (Eğer bunu implemente ettiyseniz bu maddeyi güncelleyin).

Yetkilendirme (Authorization) Rolleri: Admin/Kullanıcı gibi farklı roller tanımlayarak, belirli özelliklere sadece belirli rollerin erişmesini sağlamak.

Daha Kapsamlı Testler: Birim (Unit), entegrasyon (Integration) ve uçtan uca (End-to-End) test kapsamının artırılması.

Hata Yönetimi ve Loglama: Detaylı hata loglama mekanizmaları ve merkezi hata yönetimi.

E-posta Doğrulama/Şifre Sıfırlama: Kullanıcı kayıtlarında e-posta doğrulaması ve şifremi unuttum akışı eklenmesi.

Daha Gelişmiş UI/UX: Tasarımın daha modern bileşenlerle zenginleştirilmesi, animasyonlar ve geçişler eklenmesi.

Performans Optimizasyonu: API yanıt sürelerini ve frontend yükleme hızlarını optimize etmek.

Bu doküman, projenin teknik yapısı ve geliştirme sürecine dair genel bir bakış sunmaktadır.