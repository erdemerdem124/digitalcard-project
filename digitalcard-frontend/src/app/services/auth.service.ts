// src/app/services/auth.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { ToastService } from './toast.service';

// Backend'den dönen kimlik doğrulama yanıtı arayüzü
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles?: string[];
}

// Kullanıcı modeli
export interface User {
  id: number;
  username: string;
  email: string;
  token: string; // AuthResponse'daki accessToken buraya map edilecek
}

// Giriş işlemi için kimlik bilgileri arayüzü
interface LoginCredentials {
  usernameOrEmail: string;
  password: string;
}

// Kayıt işlemi için kullanıcı bilgileri arayüzü
interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

// Şifre güncelleme isteği için DTO
export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

const AUTH_API = 'http://localhost:8080/api/auth/';
const JWT_TOKEN_KEY = 'jwt_token'; // JWT token'ı için yeni sabit anahtar
const CURRENT_USER_KEY = 'currentUser'; // Kullanıcı objesi için yeni sabit anahtar

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private isBrowser: boolean;
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
    private toastService: ToastService,
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    let storedUser: User | null = null;
    if (this.isBrowser) {
      const userString = localStorage.getItem(CURRENT_USER_KEY);
      if (userString) {
        try {
          storedUser = JSON.parse(userString);
          // Eğer storedUser varsa ama token'ı localStorage'da yoksa, token'ı ekle
          if (storedUser && !storedUser.token) {
            const tokenFromStorage = localStorage.getItem(JWT_TOKEN_KEY);
            if (tokenFromStorage) {
              storedUser.token = tokenFromStorage;
            } else {
              // Eğer ne user objesinde ne de ayrı token anahtarında token yoksa, oturumu sıfırla
              console.warn("AuthService: localStorage'dan yüklenen kullanıcı objesinde veya ayrı anahtarda token eksik. Oturum sıfırlanıyor.");
              storedUser = null;
              localStorage.removeItem(CURRENT_USER_KEY);
              localStorage.removeItem(JWT_TOKEN_KEY);
            }
          }
        } catch (e) {
          console.error("localStorage'dan kullanıcı bilgisi okunurken hata oluştu:", e);
          localStorage.removeItem(CURRENT_USER_KEY);
          localStorage.removeItem(JWT_TOKEN_KEY); // Token'ı da temizle
        }
      }
    }
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get isLoggedIn(): boolean {
    // Hem currentUserSubject'in değeri hem de localStorage'daki token kontrol edilebilir
    return this.currentUserSubject.value !== null && this.getToken() !== null;
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // Giriş işlemi
  login(credentials: LoginCredentials): Observable<User> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    
    return this.http.post<AuthResponse>(AUTH_API + 'login', credentials, httpOptions).pipe(
      map(response => {
        // AuthResponse'dan gelen verilerle User objesini oluştur
        const user: User = {
          id: response.id,
          username: response.username,
          email: response.email,
          token: response.accessToken // Token'ı User objesine ata
        };
        return user;
      }),
      tap(user => {
        if (this.isBrowser) {
          localStorage.setItem(JWT_TOKEN_KEY, user.token); // JWT token'ı ayrı bir anahtarla kaydet
          localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user)); // Kullanıcı objesini kaydet
          console.log('AuthService: JWT token ve kullanıcı bilgisi localStorage\'a kaydedildi.');
        }
        this.currentUserSubject.next(user);
        this.toastService.success('Başarıyla giriş yapıldı!');
      }),
      catchError(error => {
        console.error('AuthService: Giriş hatası:', error);
        this.currentUserSubject.next(null);
        if (this.isBrowser) {
          localStorage.removeItem(JWT_TOKEN_KEY); // Hata durumunda token'ı temizle
          localStorage.removeItem(CURRENT_USER_KEY); // Hata durumunda kullanıcı objesini temizle
          console.warn('AuthService: Giriş hatası nedeniyle localStorage temizlendi.');
        }
        const errorMessage = error.error?.message || error.message || 'Giriş başarısız oldu. Lütfen kullanıcı adı/e-posta ve şifrenizi kontrol edin.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  register(user: RegisterRequest): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    return this.http.post(AUTH_API + 'register', user, httpOptions).pipe(
      catchError(error => {
        console.error('Kayıt hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Kayıt işlemi başarısız oldu. Lütfen bilgilerinizi kontrol edin.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  updatePassword(userId: number, passwordUpdate: PasswordUpdateRequest): Observable<any> {
    // Bu metot zaten token'ı getToken() üzerinden alıyor, bu yüzden Authorization başlığını burada manuel eklemeye gerek yok.
    // AuthInterceptor zaten bunu yapacak.
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
        // 'Authorization' başlığı AuthInterceptor tarafından eklenecek
      })
    };
    return this.http.put(`${AUTH_API}users/${userId}/password`, passwordUpdate, httpOptions)
      .pipe(
        tap(() => {
          this.toastService.success('Şifreniz başarıyla güncellendi. Lütfen yeni şifrenizle tekrar giriş yapın.');
          // Şifre güncellendikten sonra oturumu kapatıp yeniden giriş yapılmasını sağlamak iyi bir güvenlik uygulamasıdır.
          this.logout();
        }),
        catchError(error => {
          console.error('Şifre güncelleme hatası:', error);
          const errorMessage = error.error?.message || error.message || 'Şifre güncelleme başarısız oldu.';
          this.toastService.error(errorMessage);
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  // Interceptor'ın doğrudan kullanacağı metod
  getToken(): string | null {
    if (this.isBrowser) {
      return localStorage.getItem(JWT_TOKEN_KEY); // Doğrudan localStorage'dan oku
    }
    return null;
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem(JWT_TOKEN_KEY); // JWT token'ı temizle
      localStorage.removeItem(CURRENT_USER_KEY); // Kullanıcı objesini temizle
      console.log('AuthService: localStorage temizlendi ve oturum kapatıldı.');
    }
    this.currentUserSubject.next(null);
    this.toastService.info('Oturumunuz kapatıldı.');
    this.router.navigate(['/login']);
  }
}
