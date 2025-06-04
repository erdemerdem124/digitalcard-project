// src/app/services/project.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators'; // <-- 'map' buraya eklendi
import { environment } from '../../environments/environment';

// Project Arayüzü (UserService ile aynı olmalı ve numeric ID'ler kullanmalı)
export interface Project {
  id?: number; // Backend'den gelen ID'ler number
  title: string;
  description?: string;
  projectUrl?: string;
  technologies?: string;
  projectImageUrl?: string;
  userId?: number; // userId de number
  // createdAt ve updatedAt alanları backend'den geliyorsa burada tanımlanmalı,
  // ancak Firebase'e özgü 'Timestamp' tipi yerine string veya Date olmalı.
  // Eğer backend'iniz bu alanları döndürmüyorsa kaldırabilirsiniz.
  // createdAt?: string; // Örnek: ISO 8601 formatında string
  // updatedAt?: string; // Örnek: ISO 8601 formatında string
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = `${environment.apiUrl}/projects`;

  constructor(private http: HttpClient) { }

  /**
   * Belirli bir kullanıcıya ait projeleri backend API'den çeker.
   * @param userId Projelerin ait olduğu kullanıcının ID'si.
   * @returns Proje listesini içeren bir Observable.
   */
  getProjectsByUserId(userId: number): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/user/${userId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Yeni bir proje oluşturur.
   * @param project Oluşturulacak projenin verileri (ID hariç).
   * @returns Oluşturulan projeyi içeren bir Observable.
   */
  createProject(project: Omit<Project, 'id'>): Observable<Project> {
    // Backend'e gönderilecek ProjectRequest DTO'su ile uyumlu olmalı
    return this.http.post<Project>(this.apiUrl, project)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Mevcut bir projeyi günceller.
   * @param projectId Güncellenecek projenin ID'si.
   * @param project Güncelleme için proje verileri.
   * @returns Güncellenen projeyi içeren bir Observable.
   */
  updateProject(projectId: number, project: Partial<Project>): Observable<Project> {
    // Backend'e gönderilecek ProjectRequest DTO'su ile uyumlu olmalı
    return this.http.put<Project>(`${this.apiUrl}/${projectId}`, project)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Belirli bir ID'ye sahip projeyi siler.
   * @param projectId Silinecek projenin ID'si.
   * @returns Silme işleminin sonucunu içeren bir Observable.
   */
  deleteProject(projectId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${projectId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Belirli bir ID'ye sahip projeyi getirir.
   * @param projectId Getirilecek projenin ID'si.
   * @returns Projeyi içeren bir Observable veya null.
   */
  getProjectById(projectId: number): Observable<Project | null> {
    return this.http.get<Project>(`${this.apiUrl}/${projectId}`)
      .pipe(
        catchError(this.handleError),
        map((response: Project) => response || null) // <-- 'response' parametresine tip verildi
      );
  }

  /**
   * API hatalarını işler.
   * @param error HTTP hata yanıtı.
   * @returns Hata mesajını içeren bir Observable.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Bilinmeyen bir hata oluştu!';
    if (error.error instanceof ErrorEvent) {
      // Client-side errors
      errorMessage = `Hata: ${error.error.message}`;
    } else {
      // Server-side errors
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
