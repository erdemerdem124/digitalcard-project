// src/app/services/user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
// import { AuthService } from './auth.service'; // AuthService import'u kaldırıldı

// Mevcut arayüzleriniz
export interface UserProfile {
  id?: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  bio?: string;
  profileImageUrl?: string;
  title?: string;
  location?: string;
  phone?: string;
  portfolioUrl?: string;
  socialLinks?: SocialLink[];
  projects?: Project[];
}

export interface UserRequest {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  bio?: string;
  profileImageUrl?: string;
  title?: string;
  location?: string;
  phone?: string;
  portfolioUrl?: string;
}

export interface SocialLink {
  id?: number;
  platform: string;
  url: string;
  userId?: number;
}

export interface Project {
  id?: number;
  title: string;
  description?: string;
  projectUrl?: string;
  technologies?: string;
  userId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;
  private socialLinkApiUrl = `${environment.apiUrl}/social-links`;
  private projectApiUrl = `${environment.apiUrl}/projects`;

  // constructor(private http: HttpClient, private authService: AuthService) { } // AuthService enjeksiyonu kaldırıldı
  constructor(private http: HttpClient) { } // Sadece HttpClient enjekte edildi

  // getAuthHeaders metodu kaldırıldı, çünkü Interceptor bu işi yapacak
  // private getAuthHeaders(): HttpHeaders {
  //   const token = this.authService.getToken();
  //   let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
  //   if (token) {
  //     headers = headers.set('Authorization', `Bearer ${token}`);
  //     console.log("UserService: Token başarıyla alındı ve başlığa eklendi.");
  //   } else {
  //       console.warn("UserService: Token bulunamadı. Yetkilendirme başlığı eklenmedi.");
  //   }
  //   return headers;
  // }

  getUserProfileByUsername(username: string): Observable<UserProfile> {
    console.log("UserService: getUserProfileByUsername çağrıldı. İstenen kullanıcı adı:", username);
    // Başlıklar artık Interceptor tarafından eklenecek, bu yüzden burada headers objesi göndermiyoruz.
    return this.http.get<UserProfile>(`${this.apiUrl}/username/${username}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateUserProfile(userId: number, userRequest: UserRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/${userId}`, userRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteAccount(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${userId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getSocialLinksByUserId(userId: number): Observable<SocialLink[]> {
    return this.http.get<SocialLink[]>(`${this.socialLinkApiUrl}/user/${userId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createSocialLink(socialLinkRequest: SocialLink): Observable<SocialLink> {
    return this.http.post<SocialLink>(this.socialLinkApiUrl, socialLinkRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateSocialLink(linkId: number, socialLinkRequest: SocialLink): Observable<SocialLink> {
    return this.http.put<SocialLink>(`${this.socialLinkApiUrl}/${linkId}`, socialLinkRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteSocialLink(linkId: number): Observable<any> {
    return this.http.delete(`${this.socialLinkApiUrl}/${linkId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getProjectsByUserId(userId: number): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.projectApiUrl}/user/${userId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createProject(projectRequest: Project): Observable<Project> {
    return this.http.post<Project>(this.projectApiUrl, projectRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateProject(projectId: number, projectRequest: Project): Observable<Project> {
    return this.http.put<Project>(`${this.projectApiUrl}/${projectId}`, projectRequest)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteProject(projectId: number): Observable<any> {
    return this.http.delete(`${this.projectApiUrl}/${projectId}`)
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
