// src/app/components/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink } from '@angular/router'; // RouterLink'i buraya ekledik

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // RouterLink'i imports dizisine ekledik
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      usernameOrEmail: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Eğer kullanıcı zaten giriş yapmışsa home sayfasına yönlendir
    if (this.authService.isLoggedIn) {
      this.router.navigate(['/home']);
    }
  }

  onSubmit(): void {
    this.errorMessage = ''; // Hata mesajını temizle

    if (this.loginForm.invalid) {
      // Form geçersizse tüm alanlara dokunuldu olarak işaretle
      this.loginForm.markAllAsTouched();
      this.errorMessage = 'Lütfen kullanıcı adı/e-posta ve şifrenizi girin.';
      return;
    }

    const { usernameOrEmail, password } = this.loginForm.value;

    this.authService.login({ usernameOrEmail, password }).subscribe({
      next: data => {
        this.router.navigate(['/home']); // Başarılı giriş sonrası ana sayfaya yönlendir
      },
      error: err => {
        this.errorMessage = err.message || 'Giriş başarısız oldu. Lütfen bilgilerinizi kontrol edin.';
        console.error('Login hatası:', err);
      }
    });
  }
}
