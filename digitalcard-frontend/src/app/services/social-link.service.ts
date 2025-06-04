// src/app/services/social-link.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

// SocialLink Arayüzü (UserService ile aynı olmalı)
export interface SocialLink {
  id?: number; // Backend'den gelen ID'ler number
  platform: string;
  url: string;
  userId?: number; // userId de number
}

@Injectable({
  providedIn: 'root'
})
export class SocialLinkService {
  private apiUrl = `${environment.apiUrl}/social-links`;

  constructor(private http: HttpClient) { }

  getSocialLinksByUserId(userId: number): Observable<SocialLink[]> {
    return this.http.get<SocialLink[]>(`${this.apiUrl}/user/${userId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createSocialLink(socialLink: Omit<SocialLink, 'id'>): Observable<SocialLink> {
    return this.http.post<SocialLink>(this.apiUrl, socialLink)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateSocialLink(linkId: number, socialLink: Partial<SocialLink>): Observable<SocialLink> {
    return this.http.put<SocialLink>(`${this.apiUrl}/${linkId}`, socialLink)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteSocialLink(linkId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${linkId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Bilinmeyen bir hata oluştu!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Hata: ${error.error.message}`;
    } else {
      errorMessage = `Hata Kodu: ${error.status}\nMesaj: ${error.message || error.statusText}`;
      if (error.status === 401) {
        errorMessage = 'Kimlik doğrulama başarısız oldu veya oturum süresi doldu. Lütfen tekrar giriş yapın.';
      } else if (error.status === 403) {
        errorMessage = 'Bu işlemi yapmaya yetkiniz yok.';
      } else if (error.status === 404) {
        errorMessage = 'Kaynak bulunamadı.';
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
