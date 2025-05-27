Dijital Kart Uygulaması
Proje Adı ve Açıklaması
Bu proje, kullanıcıların kişisel bilgilerini, profesyonel projelerini, kendilerine ait önemli hususları ve dijital varlıklarını estetik ve mobil uyumlu bir şekilde sergileyebilecekleri bir "Dijital Kart" web uygulamasıdır. Kullanıcılar, sosyal medya hesaplarını, iletişim bilgilerini ve portföy projelerini düzenli bir arayüzde sunarak kendilerini dijital ortamda etkili bir şekilde tanıtabilirler. Uygulama, modern web standartlarına uygun, kullanıcı dostu ve güvenli bir deneyim sunmayı hedefler.

Özellikler
Kullanıcı Kayıt ve Giriş Sistemi: Güvenli kimlik doğrulama ile kullanıcıların sisteme kaydolması ve giriş yapması.

Kişisel Profil Yönetimi: Kullanıcıların ad, soyad, kullanıcı adı, e-posta, biyografi, profil fotoğrafı gibi kişisel bilgilerini düzenleyebilmesi.

Sosyal Medya Bağlantıları Yönetimi: GitHub, LinkedIn, Twitter gibi sosyal medya hesaplarının linklerini ekleme, düzenleme ve silme.

Proje Portföyü Yönetimi: Kullanıcıların tamamladıkları veya üzerinde çalıştıkları projeleri (proje adı, açıklaması, linki, kullanılan teknolojiler vb.) ekleme, düzenleme ve silme.

Dinamik Dijital Kart Görüntüleme: Kullanıcıların girdiği tüm bilgilerin, tek bir sayfa üzerinde estetik ve düzenli bir dijital kart formatında otomatik olarak oluşturulması ve görüntülenmesi.

Mobil Uyumlu Tasarım: Uygulamanın tüm cihazlarda (masaüstü, tablet, mobil) sorunsuz ve estetik bir şekilde çalışması.

Güvenli API İletişimi: JWT (JSON Web Token) tabanlı kimlik doğrulama ile frontend ve backend arasındaki iletişimin güvenliğinin sağlanması.

"Bana Ulaş" Bölümü: Kullanıcının kartında belirttiği telefon ve e-posta bilgilerine göre dinamik olarak iletişim düğmeleri sunulması.

Teknolojiler
Bu tam yığın (full-stack) web uygulaması aşağıdaki teknolojileri kullanmaktadır:

Backend
Spring Boot: Java tabanlı, hızlı ve kolay RESTful API geliştirme çerçevesi.

Java: Uygulamanın temel programlama dili.

Maven: Proje bağımlılık yönetimi ve derleme aracı.

Spring Security & JWT (JSON Web Token): Güvenli kimlik doğrulama ve yetkilendirme için.

PostgreSQL: İlişkisel veritabanı sistemi.

Spring Data JPA & Hibernate: Veritabanı etkileşimleri için ORM (Object-Relational Mapping).

Lombok: Boilerplate kodunu azaltmak için yardımcı kütüphane.

Frontend
Angular: Tek Sayfa Uygulamaları (SPA) geliştirmek için kullanılan JavaScript çerçevesi.

TypeScript: JavaScript'in yazım denetimi ve daha iyi ölçeklenebilirlik sağlayan üst kümesi.

HTML5: Uygulamanın yapısal iskeleti.

SCSS (Sass): CSS ön işlemcisi ile daha güçlü ve düzenli stil yönetimi.

Angular CLI: Angular projelerini oluşturmak ve yönetmek için komut satırı arayacı.

RxJS: Asenkron veri akışlarını yönetmek için reaktif programlama kütüphanesi.

Font Awesome: İkonlar için.

Kurulum Talimatları
Projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları takip edin.

Ön Koşullar
Projeyi çalıştırmadan önce aşağıdaki yazılımların sisteminizde kurulu olduğundan emin olun:

Java Development Kit (JDK): Sürüm 17 veya üzeri.

OpenJDK İndir

Node.js: Sürüm 18.x veya üzeri.

Node.js İndir

npm: Node.js ile birlikte gelir.

Angular CLI: Global olarak yüklü olmalı.

npm install -g @angular/cli

PostgreSQL: Sürüm 12 veya üzeri.

PostgreSQL İndir

Maven: Sürüm 3.8.x veya üzeri.

Maven İndir

Veritabanı Kurulumu (PostgreSQL)
PostgreSQL Sunucusunu Başlatın: PostgreSQL servisinizin çalıştığından emin olun.

Veritabanı ve Kullanıcı Oluşturun: PostgreSQL'e bağlanın (örneğin psql komut satırı aracı veya DBeaver gibi bir GUI aracı kullanarak) ve aşağıdaki SQL komutlarını çalıştırın:

-- Yeni bir veritabanı oluşturun
CREATE DATABASE digitalcard_db;

-- Yeni bir kullanıcı oluşturun ve şifresini belirleyin
CREATE USER digitalcard_user WITH PASSWORD 'your_password'; -- 'your_password' yerine güvenli bir şifre belirleyin

-- Oluşturulan kullanıcıya veritabanı üzerinde tüm yetkileri verin
GRANT ALL PRIVILEGES ON DATABASE digitalcard_db TO digitalcard_user;

Önemli Not: your_password yerine güçlü ve güvenli bir şifre belirlemeyi unutmayın.

Hassas Bilgiler (application.properties):
digitalcard-backend/src/main/resources/ klasöründe bulunan application.properties dosyası, veritabanı bağlantı bilgileri ve JWT secret key gibi hassas veriler içerir. Bu dosya Git deposuna eklenmemiştir.

Projeyi çalıştırabilmek için, bu klasörde application.properties.example adında bir örnek dosya bulunmaktadır. Bu dosyayı kopyalayarak application.properties adında yeni bir dosya oluşturmanız ve içindeki yer tutucuları kendi veritabanı ve JWT secret key bilgilerinizle doldurmanız gerekmektedir.

digitalcard-backend/src/main/resources/application.properties.example içeriği (örnek):

spring.datasource.url=jdbc:postgresql://localhost:5432/digitalcard_db
spring.datasource.username=digitalcard_user
spring.datasource.password=your_password_here # Burayı kendi belirlediğiniz şifreyle değiştirin
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update # İlk çalıştırmada 'create' yapıp sonra 'update' yapabilirsiniz
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Secret Key ve Süresi
soliner.app.jwtSecret=YENI_BASE64_KODLU_SECRET_KEY_BURAYA # Kendi oluşturduğunuz Base64 key'i buraya yapıştırın
soliner.app.jwtExpirationMs=86400000 # 24 saat (milisaniye cinsinden)

YENI_BASE64_KODLU_SECRET_KEY_BURAYA kısmına, daha önce benimle paylaştığınız veya kendinizin oluşturduğu Base64 kodlu secret key'i yapıştırın. Örneğin: 
JBIbDCLPD1BRYREUSFDLKKJGFİADFSLGHP3aCqJgA4HWnW1hH0ISjnq5pH6cpYvI2zuGlSKFDLSvkq4eHq/cntTD/lVWsLzRŞDFSLFSDD400SzfbrMYvoJRg==

Backend Kurulumu ve Çalıştırma
Backend dizinine gidin:

cd digitalcard-backend

Bağımlılıkları yükleyin ve projeyi derleyin (testleri atlayarak):

mvn clean install -Dmaven.test.skip=true

Spring Boot uygulamasını başlatın:

mvn spring-boot:run

Uygulama başarıyla başladığında konsolda Started DigitalcardBackendApplication benzeri bir mesaj görmelisiniz. Backend varsayılan olarak http://localhost:8080 adresinde çalışacaktır.

Frontend Kurulumu ve Çalıştırma
Frontend dizinine gidin:

cd digitalcard-frontend

Bağımlılıkları yükleyin:

npm install

Angular uygulamasını başlatın:

ng serve --open

Bu komut, uygulamayı derleyecek ve varsayılan tarayıcınızda http://localhost:4200 adresinde açacaktır.

Kullanım
Uygulama açıldığında, önce bir kullanıcı hesabı oluşturmak için Kayıt Ol sayfasını kullanın.

Kayıt olduktan sonra, oluşturduğunuz kullanıcı adı ve şifre ile Giriş Yapın.

Giriş yaptıktan sonra, dijital kartınızı görüntüleyebileceğiniz Kartım sayfasına yönlendirileceksiniz.

Profili Düzenle veya Kartı Düzenle butonlarına tıklayarak kişisel bilgilerinizi, profil fotoğrafınızı, sosyal medya linklerinizi ve projelerinizi ekleyebilir/güncelleyebilirsiniz.

Değişiklikleri kaydettikten sonra, kartınızın güncellendiğini göreceksiniz.