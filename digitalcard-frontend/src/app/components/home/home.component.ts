// src/app/components/home/home.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService, UserProfile, SocialLink, Project } from '../../services/user.service';
import { Observable, throwError, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

interface SafeUserProfile extends UserProfile {
  socialLinks: SocialLink[];
  projects: Project[];
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  userProfile: SafeUserProfile | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';
  currentUserUsername: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('HomeComponent ngOnInit başlatıldı.');

    this.authService.currentUser.pipe(
      switchMap(user => {
        if (user?.username) {
          this.currentUserUsername = user.username;
          return this.userService.getUserProfileByUsername(user.username);
        } else {
          this.router.navigate(['/login']);
          this.isLoading = false;
          return of(null);
        }
      }),
      catchError(error => {
        console.error('Kullanıcı profili yüklenirken hata:', error);
        this.errorMessage = error.message || 'Kullanıcı profili yüklenemedi.';
        this.isLoading = false;
        return throwError(() => new Error(this.errorMessage));
      }),
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe(
      (profile: UserProfile | null) => {
        if (profile) {
          this.userProfile = {
            ...profile,
            socialLinks: Array.isArray(profile.socialLinks) ? profile.socialLinks : [],
            projects: Array.isArray(profile.projects) ? profile.projects : []
          };
        } else {
          this.errorMessage = 'Profil bulunamadı. Lütfen giriş yapın veya profil oluşturun.';
        }
      },
      (error) => {
        console.error('Abonelik hatası:', error);
        this.isLoading = false;
      }
    );
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getSocialIcon(platform: string): string {
    switch (platform.toLowerCase()) {
      case 'linkedin': return 'fab fa-linkedin';
      case 'github': return 'fab fa-github';
      case 'twitter': return 'fab fa-twitter';
      case 'facebook': return 'fab fa-facebook';
      case 'instagram': return 'fab fa-instagram';
      case 'youtube': return 'fab fa-youtube';
      case 'website':
      case 'portfolio': return 'fas fa-globe';
      case 'email': return 'fas fa-envelope';
      case 'phone': return 'fas fa-phone';
      default: return 'fas fa-link';
    }
  }

  // 👇 Bu metot HTML'den çağrılır, dizinin dolu olup olmadığını güvenli şekilde kontrol eder
  isNonEmptyArray<T>(arr: T[] | null | undefined): boolean {
    return Array.isArray(arr) && arr.length > 0;
  }
}
