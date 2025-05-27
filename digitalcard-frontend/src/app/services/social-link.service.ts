// src/app/services/social-link.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment'; // Doğrudan environment'ı import edin

// DTO'larımızı tanımlayalım (Bunları daha sonra ayrı bir 'models' klasöründe de tutabiliriz)
export interface SocialLinkRequest {
  platform: string;
  url: string;
  userId: number; // Angular'da Long karşılığı number'dır
}

export interface SocialLinkResponse {
  id: number;
  platform: string;
  url: string;
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class SocialLinkService {
  private apiUrl = `${environment.apiUrl}/sociallinks`; // Backend'deki base URL

  constructor(private http: HttpClient) { }

  /**
   * Belirli bir kullanıcıya ait tüm sosyal linkleri getirir.
   * GET /api/sociallinks/user/{userId}
   */
  getSocialLinksByUserId(userId: number): Observable<SocialLinkResponse[]> {
    return this.http.get<SocialLinkResponse[]>(`${this.apiUrl}/user/${userId}`);
  }

  /**
   * Belirli bir sosyal linki ID'sine göre getirir.
   * GET /api/sociallinks/{id}
   */
  getSocialLinkById(id: number): Observable<SocialLinkResponse> {
    return this.http.get<SocialLinkResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Yeni bir sosyal link oluşturur.
   * POST /api/sociallinks
   */
  createSocialLink(socialLink: SocialLinkRequest): Observable<SocialLinkResponse> {
    return this.http.post<SocialLinkResponse>(this.apiUrl, socialLink);
  }

  /**
   * Mevcut bir sosyal linki günceller.
   * PUT /api/sociallinks/{id}
   */
  updateSocialLink(id: number, socialLink: SocialLinkRequest): Observable<SocialLinkResponse> {
    return this.http.put<SocialLinkResponse>(`${this.apiUrl}/${id}`, socialLink);
  }

  /**
   * Belirli bir sosyal linki siler.
   * DELETE /api/sociallinks/{id}
   */
  deleteSocialLink(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 