// src/app/services/project.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment'; // Doğrudan environment'ı import edin

// DTO'larımızı tanımlayalım
export interface ProjectRequest {
  title: string;
  description?: string; // Optional olabilir
  projectUrl?: string; // Optional olabilir
  technologies?: string; // Optional olabilir
  userId: number;
}

export interface ProjectResponse {
  id: number;
  title: string;
  description: string;
  projectUrl: string;
  technologies: string;
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = `${environment.apiUrl}/projects`; // Backend'deki base URL

  constructor(private http: HttpClient) { }

  /**
   * Belirli bir kullanıcıya ait tüm projeleri getirir.
   * GET /api/projects/user/{userId}
   */
  getProjectsByUserId(userId: number): Observable<ProjectResponse[]> {
    return this.http.get<ProjectResponse[]>(`${this.apiUrl}/user/${userId}`);
  }

  /**
   * Belirli bir projeyi ID'sine göre getirir.
   * GET /api/projects/{id}
   */
  getProjectById(id: number): Observable<ProjectResponse> {
    return this.http.get<ProjectResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Yeni bir proje oluşturur.
   * POST /api/projects
   */
  createProject(project: ProjectRequest): Observable<ProjectResponse> {
    return this.http.post<ProjectResponse>(this.apiUrl, project);
  }

  /**
   * Mevcut bir projeyi günceller.
   * PUT /api/projects/{id}
   */
  updateProject(id: number, project: ProjectRequest): Observable<ProjectResponse> {
    return this.http.put<ProjectResponse>(`${this.apiUrl}/${id}`, project);
  }

  /**
   * Belirli bir projeyi siler.
   * DELETE /api/projects/{id}
   */
  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}