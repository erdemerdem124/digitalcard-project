// src/app/components/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router'; // Router import edildi
import { ToastService } from '../../services/toast.service'; // ToastService import edildi

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  authForm!: FormGroup;
  isLoginMode = true;
  isLoading = false;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router, // Router enjekte edildi
    private toastService: ToastService // ToastService enjekte edildi
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  initForm(): void {
    this.authForm = this.fb.group({
      username: ['', Validators.required],
      firstName: [''],
      lastName: [''],
      email: [''],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['']
    }, { validator: this.passwordMatchValidator });

    this.setValidatorsByMode();
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    // Eğer confirmPassword'da zaten bir hata varsa ve bu hata passwordsMismatch değilse, doğrulama yapma
    if (confirmPassword?.errors && !confirmPassword.errors['passwordsMismatch']) {
      return;
    }

    if (password?.value !== confirmPassword?.value) {
      confirmPassword?.setErrors({ passwordsMismatch: true });
    } else {
      confirmPassword?.setErrors(null);
    }
  }

  setValidatorsByMode(): void {
    if (this.isLoginMode) {
      this.authForm.get('firstName')?.clearValidators();
      this.authForm.get('lastName')?.clearValidators();
      this.authForm.get('email')?.clearValidators();
      this.authForm.get('confirmPassword')?.clearValidators();
    } else {
      this.authForm.get('firstName')?.setValidators([Validators.required]);
      this.authForm.get('lastName')?.setValidators([Validators.required]);
      this.authForm.get('email')?.setValidators([Validators.required, Validators.email]);
      this.authForm.get('confirmPassword')?.setValidators([Validators.required]);
    }

    this.authForm.get('firstName')?.updateValueAndValidity();
    this.authForm.get('lastName')?.updateValueAndValidity();
    this.authForm.get('email')?.updateValueAndValidity();
    this.authForm.get('confirmPassword')?.updateValueAndValidity();
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.authForm.reset(); // Mod değiştiğinde formu sıfırla
    this.setValidatorsByMode();
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm gerekli alanları doğru şekilde doldurun.'); // Hata mesajı toast ile gösterildi
      return;
    }

    this.isLoading = true;

    const { username, firstName, lastName, email, password } = this.authForm.value;

    if (this.isLoginMode) {
      const usernameOrEmail = username; // veya email kullanmak istersen email yaz

      this.authService.login({ usernameOrEmail, password }).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Başarıyla giriş yapıldı!'); // Toast mesajı burada da gösterilebilir
          this.router.navigate(['/home']); // Başarılı giriş sonrası home sayfasına yönlendirme
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.message || 'Giriş başarısız oldu.';
          this.toastService.error(this.errorMessage); // Hata mesajı toast ile gösterildi
        }
      });
    } else {
      this.authService.register({ username, firstName, lastName, email, password }).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Kayıt başarılı! Şimdi giriş yapabilirsiniz.'); // Kayıt başarılı toast mesajı
          this.toggleMode(); // Kayıt sonrası giriş moduna geç
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.message || 'Kayıt başarısız oldu.';
          this.toastService.error(this.errorMessage); // Hata mesajı toast ile gösterildi
        }
      });
    }
  }
}
