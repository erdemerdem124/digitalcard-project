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
export interface LoginCredentials {
  usernameOrEmail: string;
  password: string;
}

// Kayıt işlemi için kullanıcı bilgileri arayüzü
export interface RegisterRequest {
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
const JWT_TOKEN_KEY = 'jwt_token';
const CURRENT_USER_KEY = 'currentUser';

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

    let initialUser: User | null = null;
    if (this.isBrowser) {
      const token = localStorage.getItem(JWT_TOKEN_KEY);
      const userString = localStorage.getItem(CURRENT_USER_KEY);

      if (token && userString) {
        try {
          const parsedUser = JSON.parse(userString);
          if (parsedUser && parsedUser.id && parsedUser.username && parsedUser.email) {
            initialUser = {
              id: parsedUser.id,
              username: parsedUser.username,
              email: parsedUser.email,
              token: token
            };
          } else {
            console.warn("AuthService: localStorage'dan yüklenen kullanıcı objesi eksik veya hatalı. Oturum sıfırlanıyor.");
            this.clearLocalStorage();
          }
        } catch (e) {
          console.error("AuthService: localStorage'dan kullanıcı bilgisi okunurken hata oluştu:", e);
          this.clearLocalStorage();
        }
      } else if (token || userString) {
        console.warn("AuthService: localStorage'da kullanıcı objesi veya token eksik/tutarsız. Oturum sıfırlanıyor.");
        this.clearLocalStorage();
      }
    }
    this.currentUserSubject = new BehaviorSubject<User | null>(initialUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null && this.getToken() !== null;
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginCredentials): Observable<User> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    
    return this.http.post<AuthResponse>(AUTH_API + 'login', credentials, httpOptions).pipe(
      map(response => {
        const user: User = {
          id: response.id,
          username: response.username,
          email: response.email,
          token: response.accessToken
        };
        return user;
      }),
      tap(user => {
        if (this.isBrowser) {
          localStorage.setItem(JWT_TOKEN_KEY, user.token);
          localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
          console.log('AuthService: JWT token ve kullanıcı bilgisi localStorage\'a kaydedildi.');
        }
        this.currentUserSubject.next(user);
        this.toastService.success('Başarıyla giriş yapıldı!');
      }),
      catchError(error => {
        console.error('AuthService: Giriş hatası:', error);
        this.currentUserSubject.next(null);
        this.clearLocalStorage();
        console.warn('AuthService: Giriş hatası nedeniyle localStorage temizlendi.');
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
      tap(() => {
        this.toastService.success('Kayıt işlemi başarıyla tamamlandı. Şimdi giriş yapabilirsiniz.');
      }),
      catchError(error => {
        console.error('Kayıt hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Kayıt işlemi başarısız oldu. Lütfen bilgilerinizi kontrol edin.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  updatePassword(userId: number, passwordUpdate: PasswordUpdateRequest): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };
    return this.http.put(`${AUTH_API}users/${userId}/password`, passwordUpdate, httpOptions)
      .pipe(
        tap(() => {
          this.toastService.success('Şifreniz başarıyla güncellendi. Lütfen yeni şifrenizle tekrar giriş yapın.');
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

  getToken(): string | null {
    if (this.isBrowser) {
      return localStorage.getItem(JWT_TOKEN_KEY);
    }
    return null;
  }

  private clearLocalStorage(): void {
    if (this.isBrowser) {
      localStorage.removeItem(JWT_TOKEN_KEY);
      localStorage.removeItem(CURRENT_USER_KEY);
    }
  }

  logout(): void {
    this.clearLocalStorage();
    console.log('AuthService: Oturum kapatıldı ve localStorage temizlendi.');
    this.currentUserSubject.next(null);
    this.toastService.info('Oturumunuz kapatıldı.');
    this.router.navigate(['/login']);
  }
}
