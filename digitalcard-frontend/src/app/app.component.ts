// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ToastComponent } from './components/toast/toast.component'; // ToastComponent'i eklediğinizden emin olun
import { ThemeService, Theme } from './services/theme.service'; // ThemeService ve Theme enum'ını import edin

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    ToastComponent // ToastComponent'in burada import edildiğinden emin olun
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'digitalcard-frontend';
  currentTheme: Theme; // Mevcut temayı tutmak için

  constructor(private themeService: ThemeService) {
    // Servisten mevcut temayı al
    this.currentTheme = this.themeService.getTheme();
  }

  ngOnInit(): void {
    // Tema değişikliklerini dinle
    this.themeService.currentTheme$.subscribe(theme => {
      this.currentTheme = theme;
    });
  }

  // Temayı değiştiren metod
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
