// src/app/services/theme.service.ts
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';

export enum Theme {
  Light = 'light-theme',
  Dark = 'dark-theme'
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private currentThemeSubject: BehaviorSubject<Theme>;
  public currentTheme$: Observable<Theme>;
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    let initialTheme = Theme.Light;
    if (this.isBrowser) {
      const storedTheme = localStorage.getItem('theme');
      if (storedTheme === Theme.Dark) {
        initialTheme = Theme.Dark;
      }
    }
    this.currentThemeSubject = new BehaviorSubject<Theme>(initialTheme);
    this.currentTheme$ = this.currentThemeSubject.asObservable();

    // Uygulama yüklendiğinde body sınıfını ayarla
    if (this.isBrowser) {
      this.applyTheme(initialTheme);
    }
  }

  private applyTheme(theme: Theme): void {
    if (this.isBrowser) {
      document.body.classList.remove(Theme.Light, Theme.Dark);
      document.body.classList.add(theme);
    }
  }

  loadTheme(): void {
    if (this.isBrowser) {
      const storedTheme = localStorage.getItem('theme');
      if (storedTheme === Theme.Dark) {
        this.setTheme(Theme.Dark);
      } else {
        this.setTheme(Theme.Light);
      }
    }
  }

  setTheme(theme: Theme): void {
    if (this.isBrowser) {
      localStorage.setItem('theme', theme);
      this.applyTheme(theme);
      this.currentThemeSubject.next(theme);
    }
  }

  toggleTheme(): void {
    if (this.currentThemeSubject.value === Theme.Light) {
      this.setTheme(Theme.Dark);
    } else {
      this.setTheme(Theme.Light);
    }
  }

  getTheme(): Theme {
    return this.currentThemeSubject.value;
  }
}
