// src/app/components/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router'; // Düzeltme burada
import { AuthService, RegisterRequest } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;
  currentTheme: Theme;
  public Theme = Theme; // Theme enum'unu HTML'de kullanabilmek için public olarak tanımlandı

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService,
    private themeService: ThemeService
  ) {
    this.currentTheme = this.themeService.getTheme();
  }

  ngOnInit(): void {
    this.themeService.currentTheme$.subscribe(theme => {
      this.currentTheme = theme;
    });

    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    return form.get('password')?.value === form.get('confirmPassword')?.value
      ? null : { 'passwordsMismatch': true };
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm gerekli alanları doğru şekilde doldurun.');
      return;
    }

    this.isLoading = true;
    const { firstName, lastName, username, email, password } = this.registerForm.value;

    const userData: RegisterRequest = { firstName, lastName, username, email, password };
    this.authService.register(userData).subscribe({
      next: () => {
        this.toastService.success('Kayıt başarılı! Şimdi giriş yapabilirsiniz.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Kayıt başarısız. Lütfen farklı bir kullanıcı adı veya e-posta deneyin.';
        this.toastService.error(this.errorMessage);
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
