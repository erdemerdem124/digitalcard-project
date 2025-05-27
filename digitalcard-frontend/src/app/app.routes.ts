// src/app/app.routes.ts (Güncellenecek kısım)

import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { ProfileEditComponent } from './components/profile-edit/profile-edit.component';
import { AuthGuard } from './guards/auth.guard'; // AuthGuard'ı import edin
import { PasswordUpdateComponent } from './components/password-update/password-update.component'; // Yeni bileşen import edildi

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'profile/edit/:username', component: ProfileEditComponent, canActivate: [AuthGuard] },
  // YENİ ROTA EKLENDİ
  { path: 'profile/password-update', component: PasswordUpdateComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/home' } // Bilinmeyen rotaları ana sayfaya yönlendir
];
