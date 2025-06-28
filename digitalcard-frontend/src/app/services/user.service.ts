// src/app/services/user.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { ToastService } from './toast.service';
import { HttpErrorResponse } from '@angular/common/http';

// Frontend'de kullanılacak arayüzler - Backend DTO'ları ile eşleşmelidir.
// Backend'de @JsonProperty olan alanlar için JSON alan adı kullanılacaktır.
// Frontend tarafında JavaScript/TypeScript property isimleri olarak camelCase tercih edilir.

// Backend: com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest
export interface SocialLink {
  platform: string;
  url: string;
}

// Backend: com.soliner.digitalcard.webApi.dto.project.ProjectRequest
export interface Project {
  title: string; // Backend'de 'title' olarak bekleniyor
  description?: string;
  technologies?: string; // Frontend'de virgülle ayrılmış string olarak tutulacak (Backend ProjectRequest'e uygun)
  projectUrl?: string; // Backend'de 'projectUrl', JSON'da 'project_url'
  projectImageUrl?: string; // Backend'de 'projectImageUrl', JSON'da 'project_image_url'
}

// Backend: com.soliner.digitalcard.webApi.dto.user.UserRequest
// Bu DTO, frontend'den backend'e POST/PUT yapılırken gönderilen yapıdır.
export interface UserRequestDTO {
  username: string;
  email: string;
  password?: string; // Backend UserRequest'te hala @NotBlank var, bu bir sonraki adımda çözülmeli

  firstName: string;
  lastName: string;
  profileImageUrl?: string; // Backend'de 'profileImageUrl', JSON'da 'profile_image_url'
  bio?: string;
  title?: string; // Meslek ünvanı
  location?: string; // Backend'de 'location'
  phone?: string; // Backend'de 'phone'
  portfolioUrl?: string; // Backend'de 'portfolioUrl'

  socialLinks?: SocialLink[];
  projects?: Project[];
}


// Backend: com.soliner.digitalcard.webApi.dto.user.UserResponse
// Bu DTO, backend'den frontend'e GET/POST/PUT yanıtı olarak gelen yapıdır.
export interface UserProfile { // Backend'den gelen veriyi temsil eden UserProfile
  id?: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  profileImageUrl?: string; // JSON'dan gelen 'profile_image_url' buraya maplenecek
  bio?: string;
  title?: string;
  location?: string; // Backend'den 'location' olarak gelir
  phone?: string; // Backend'den 'phone_number' olarak gelir (mapping yapılacak)
  portfolioUrl?: string; // Backend'den 'website' olarak gelir (mapping yapılacak)
  socialLinks?: SocialLink[];
  projects?: Project[];
}


@Injectable({
  providedIn: 'root'
})
export class UserService {
  private isBrowser: boolean;
  private readonly API_BASE_URL = 'http://localhost:8080/api/users/'; 

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
    private toastService: ToastService
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  createUserProfile(profileData: UserProfile, password?: string): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };

    const backendCompatibleData = this.mapUserProfileToBackendRequest(profileData);
    if (password) {
      backendCompatibleData.password = password;
    } else {
      console.warn("createUserProfile çağrısında password alanı gönderilmedi. Backend NotBlank ise hata oluşabilir.");
    }

    return this.http.post<any>(this.API_BASE_URL, backendCompatibleData, httpOptions).pipe(
      map(response => this.mapBackendResponseToUserProfile(response)),
      tap(response => {
        this.toastService.success('Profil başarıyla oluşturuldu!');
        console.log('Profil oluşturuldu (frontend formatında):', response);
      }),
      catchError((error: any) => {
        console.error('Profil oluşturma hatası:', error);
        const errorMessage = error.error?.message || error.message || 'Profil oluşturulurken bir hata oluştu.';
        this.toastService.error(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getUserProfileByUsername(username: string): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    return this.http.get<any>(`${this.API_BASE_URL}username/${username}`, httpOptions).pipe(
      map(backendProfile => this.mapBackendResponseToUserProfile(backendProfile)),
      tap(profile => console.log('Profil getirildi (frontend formatında):', profile)),
      catchError(this.handleError<UserProfile>('getUserProfileByUsername')) // Hata yönetimini kullan
    );
  }

  updateUserProfile(profileId: number, userProfile: UserProfile): Observable<UserProfile> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    const backendCompatibleRequest = this.mapUserProfileToBackendRequest(userProfile);
    
    if (!backendCompatibleRequest.password) {
        backendCompatibleRequest.password = ""; 
    }

    console.log('Backend\'e gönderilecek veri (update):', backendCompatibleRequest);

    return this.http.put<any>(`${this.API_BASE_URL}${profileId}`, backendCompatibleRequest, httpOptions).pipe(
      map(response => this.mapBackendResponseToUserProfile(response)),
      tap(response => {
        this.toastService.success('Profil başarıyla güncellendi!');
        console.log('Profil güncellendi (frontend formatında):', response);
      }),
      catchError(this.handleError<UserProfile>('updateUserProfile')) // Hata yönetimini kullan
    );
  }

  deleteUserProfile(profileId: number): Observable<void> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      observe: 'body' as 'body'
    };
    return this.http.delete<void>(`${this.API_BASE_URL}${profileId}`, httpOptions).pipe(
      tap(() => {
        this.toastService.success('Profil başarıyla silindi.');
        console.log('Profil silindi:', profileId);
      }),
      catchError(this.handleError<void>('deleteUserProfile')) // Hata yönetimini kullan
    );
  }

  private mapBackendResponseToUserProfile(backendData: any): UserProfile {
    console.log('DEBUG (UserService): mapBackendResponseToUserProfile - Ham backend verisi:', backendData);

    return {
      id: backendData.id,
      username: backendData.username,
      email: backendData.email,
      firstName: backendData.firstName,
      lastName: backendData.lastName,
      profileImageUrl: backendData.profile_image_url || '',
      bio: backendData.bio || '',
      title: backendData.title || '',
      location: backendData.address || '', // KRİTİK DÜZELTME: Backend'den 'address' olarak geliyor
      phone: backendData.phone_number || '', // KRİTİK DÜZELTME: Backend'den 'phone_number' olarak geliyor
      portfolioUrl: backendData.website || '', // KRİTİK DÜZELTME: Backend'den 'website' olarak geliyor

      socialLinks: backendData.socialLinks ? backendData.socialLinks.map((link: any) => ({
        platform: link.platform,
        url: link.url
      })) : [],
      projects: backendData.projects ? backendData.projects.map((project: any) => ({
        title: project.title,
        description: project.description || '',
        technologies: project.technologies ? (project.technologies as string) : '',
        projectUrl: project.project_url || '', // KRİTİK DÜZELTME: Backend'den 'project_url' olarak geliyor
        projectImageUrl: project.project_image_url || '' // KRİTİK DÜZELTME: Backend'den 'project_image_url' olarak geliyor
      })) : []
    };
  }

  private mapUserProfileToBackendRequest(frontendData: UserProfile): UserRequestDTO {
    const backendRequest: UserRequestDTO = {
      username: frontendData.username,
      email: frontendData.email,
      firstName: frontendData.firstName,
      lastName: frontendData.lastName,
      profileImageUrl: frontendData.profileImageUrl,
      bio: frontendData.bio,
      title: frontendData.title,
      location: frontendData.location,
      phone: frontendData.phone,
      portfolioUrl: frontendData.portfolioUrl,
      socialLinks: frontendData.socialLinks?.map((link: SocialLink) => ({
        platform: link.platform,
        url: link.url,
      })),
      projects: frontendData.projects?.map((project: Project) => ({
        title: project.title,
        description: project.description,
        technologies: project.technologies ? project.technologies : '',
        projectUrl: project.projectUrl,
        projectImageUrl: project.projectImageUrl,
      }))
    };

    const finalBackendRequest: any = {};
    for (const key in backendRequest) {
      if (backendRequest.hasOwnProperty(key)) {
        const value = (backendRequest as any)[key];
        if (key === 'password') {
            if (value !== undefined && value !== null) {
                finalBackendRequest[key] = value;
            }
            continue;
        }

        if (value === null || value === undefined || (typeof value === 'string' && value.trim() === '') || (Array.isArray(value) && value.length === 0)) {
          // Boş veya null alanları, boş stringleri ve boş dizileri gönderme
          continue;
        }
        finalBackendRequest[key] = value;
      }
    }

    console.log('Backend\'e gönderilecek son veri:', finalBackendRequest);
    return finalBackendRequest;
  }

  private handleError<T>(operation = 'operation') {
    return (error: HttpErrorResponse): Observable<T> => {
      let errorMessage = `Profil ${operation} hatası: `;

      if (error.error instanceof ErrorEvent) {
        errorMessage += `İstemci veya ağ hatası: ${error.error.message}`;
      } else {
        console.error(`Backend returned code ${error.status}, body was: `, error.error);

        // ProblemDetails objesini kontrol et
        if (error.error && typeof error.error === 'object' && error.error.detail) {
          const problemDetails = error.error;
          errorMessage += `${problemDetails.detail || problemDetails.message || 'Doğrulama hatası oluştu.'}`;
          
          // Alan bazlı validasyon hatalarını ProblemDetails.errors map'inden al
          if (problemDetails.errors && Object.keys(problemDetails.errors).length > 0) {
            errorMessage += '\nDetaylar:\n';
            for (const fieldName in problemDetails.errors) {
              if (problemDetails.errors.hasOwnProperty(fieldName)) {
                errorMessage += `- ${fieldName}: ${problemDetails.errors[fieldName]}\n`;
              }
            }
          }
        } else if (error.error?.message) {
            errorMessage += `Sunucu hatası: ${error.status} ${error.statusText || ''}. Detay: ${error.error.message}`;
        } else {
            errorMessage += `Sunucu hatası: ${error.status} ${error.statusText || ''}.`;
        }
      }
      
      this.toastService.error(errorMessage);
      console.error(errorMessage);
      return throwError(() => new Error(errorMessage));
    };
  }
}
