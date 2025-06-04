// src/app/services/user.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { ToastService } from './toast.service';

// Kullanıcı profili arayüzü
export interface UserProfile {
  id?: number; // Backend tarafından atanacak
  userId: number; // Authentication servisinden gelen kullanıcı ID'si
  profileImageUrl?: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  bio?: string;
  title?: string;
  // company?: string; // Şirket alanı kaldırıldı
  phoneNumber?: string;
  website?: string;
  address?: string; // Adres alanı Konum olarak kullanılacak
  socialLinks: SocialLink[];
  projects: Project[];
}

// Sosyal medya link arayüzü
export interface SocialLink {
  platform: string;
  url: string;
}

// Proje arayüzü
export interface Project {
  name: string;
  description?: string;
  technologies?: string[]; // Teknolojiler string dizisi olarak tanımlandı
  url?: string;
}

// Backend'e gönderilecek kullanıcı profili isteği arayüzü
export interface UserRequest {
  profileImageUrl?: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  bio?: string;
  title?: string;
  phoneNumber?: string;
  website?: string;
  address?: string; // Adres alanı Konum olarak kullanılacak
  socialLinks: SocialLink[];
  projects: Project[];
}

const API_URL = 'http://localhost:8080/api/users/'; // Backend API URL'si

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
    private toastService: ToastService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  // Kullanıcı profili oluşturma (POST isteği)
  // Backend UserController'daki @PostMapping("/")'e uyacak şekilde düzeltildi
  createUserProfile(profileData: UserProfile): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    return this.http.post<UserProfile>(API_URL, profileData, httpOptions).pipe(
      tap(response => {
        this.toastService.success('Profil başarıyla oluşturuldu!');
        console.log('Profil oluşturuldu:', response);
      }),
      catchError((error: any) => {
        console.error('Profil oluşturma hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Profil oluşturulurken bir hata oluştu.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Kullanıcı profilini kullanıcı adına göre getirme
  // Backend UserController'daki @GetMapping("/username/{username}")'e uyacak şekilde düzeltildi
  getUserProfileByUsername(username: string): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    return this.http.get<UserProfile>(`${API_URL}username/${username}`, httpOptions).pipe(
      tap(profile => console.log('Profil getirildi:', profile)),
      catchError((error: any) => {
        console.error('Profil getirme hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Profil getirilirken bir hata oluştu.';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Kullanıcı profilini güncelleme (PUT isteği)
  // Backend UserController'daki @PutMapping("/{id}")'e uyacak şekilde düzeltildi
  updateUserProfile(profileId: number, userRequest: UserRequest): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    return this.http.put<UserProfile>(`${API_URL}${profileId}`, userRequest, httpOptions).pipe(
      tap(response => {
        this.toastService.success('Profil başarıyla güncellendi!');
        console.log('Profil güncellendi:', response);
      }),
      catchError((error: any) => {
        console.error('Profil güncelleme hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Profil güncellenirken bir hata oluştu.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Kullanıcı profilini ID'ye göre silme
  // Backend UserController'daki @DeleteMapping("/{id}")'e uyacak şekilde düzeltildi
  deleteUserProfile(profileId: number): Observable<void> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    return this.http.delete<void>(`${API_URL}${profileId}`, httpOptions).pipe(
      tap(() => {
        this.toastService.success('Profil başarıyla silindi.');
        console.log('Profil silindi:', profileId);
      }),
      catchError((error: any) => {
        console.error('Profil silme hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Profil silinirken bir hata oluştu.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
