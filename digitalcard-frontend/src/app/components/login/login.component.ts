// src/app/components/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';

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
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.initForm();
    // Başlangıçta login modunda olduğu için doğrulayıcıları ayarla
    this.setValidatorsByMode();
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
  }

  // Özel şifre eşleşme doğrulayıcısı
  passwordMatchValidator = (form: FormGroup) => {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    // Eğer confirmPassword alanı yoksa veya login modundaysak, bu doğrulayıcıyı atla
    // Bu, login modunda confirmPassword'ın form geçerliliğini etkilememesini sağlar.
    if (this.isLoginMode || !confirmPassword) {
      confirmPassword?.setErrors(null); // Hata varsa temizle
      return null;
    }

    // Eğer confirmPassword'da zaten başka bir hata varsa, bu doğrulayıcıyı çalıştırma
    if (confirmPassword.errors && !confirmPassword.errors['passwordsMismatch']) {
      return null;
    }

    if (password?.value !== confirmPassword?.value) {
      confirmPassword?.setErrors({ passwordsMismatch: true });
    } else {
      confirmPassword?.setErrors(null);
    }
    return null;
  };


  setValidatorsByMode(): void {
    // Tüm alanların doğrulayıcılarını temizle
    this.authForm.get('username')?.clearValidators();
    this.authForm.get('firstName')?.clearValidators();
    this.authForm.get('lastName')?.clearValidators();
    this.authForm.get('email')?.clearValidators();
    this.authForm.get('password')?.clearValidators();
    this.authForm.get('confirmPassword')?.clearValidators();

    if (this.isLoginMode) {
      this.authForm.get('username')?.setValidators([Validators.required]);
      this.authForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      // Login modunda confirmPassword'ın hatalarını temizle
      this.authForm.get('confirmPassword')?.setErrors(null);
    } else { // Kayıt modu
      this.authForm.get('username')?.setValidators([Validators.required]);
      this.authForm.get('firstName')?.setValidators([Validators.required]);
      this.authForm.get('lastName')?.setValidators([Validators.required]);
      this.authForm.get('email')?.setValidators([Validators.required, Validators.email]);
      this.authForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.authForm.get('confirmPassword')?.setValidators([Validators.required]);
    }

    // Doğrulayıcı değişikliklerini uygulamak için tüm form kontrollerinin geçerliliğini güncelle
    this.authForm.get('username')?.updateValueAndValidity();
    this.authForm.get('firstName')?.updateValueAndValidity();
    this.authForm.get('lastName')?.updateValueAndValidity();
    this.authForm.get('email')?.updateValueAndValidity();
    this.authForm.get('password')?.updateValueAndValidity();
    this.authForm.get('confirmPassword')?.updateValueAndValidity();

    // Form grubunun doğrulayıcılarını da güncelle (passwordMatchValidator için)
    this.authForm.updateValueAndValidity();
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.authForm.reset(); // Mod değiştiğinde formu sıfırla
    this.setValidatorsByMode(); // Yeni moda göre doğrulayıcıları ayarla
  }

  onSubmit(): void {
    this.errorMessage = '';

    // Hata ayıklama için formun geçerlilik durumunu ve hatalarını logla
    console.log('Form geçerli mi:', this.authForm.valid);
    console.log('Form hataları:', this.authForm.errors);
    console.log('Form değerleri:', this.authForm.value);
    Object.keys(this.authForm.controls).forEach(key => {
      const control = this.authForm.get(key);
      console.log(`Kontrol: ${key}, Geçerli mi: ${control?.valid}, Hataları: ${control?.errors}, Dokunuldu mu: ${control?.touched}`);
    });


    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm gerekli alanları doğru şekilde doldurun.');
      this.isLoading = false; // Hata durumunda isLoading'i false yap
      return;
    }

    this.isLoading = true;

    const { username, firstName, lastName, email, password } = this.authForm.value;

    if (this.isLoginMode) {
      const usernameOrEmail = username;

      this.authService.login({ usernameOrEmail, password }).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Başarıyla giriş yapıldı!');
          this.router.navigate(['/home']);
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.message || 'Giriş başarısız oldu.';
          this.toastService.error(this.errorMessage);
        }
      });
    } else {
      this.authService.register({ username, firstName, lastName, email, password }).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Kayıt başarılı! Şimdi giriş yapabilirsiniz.');
          this.toggleMode();
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.message || 'Kayıt başarısız oldu.';
          this.toastService.error(this.errorMessage);
        }
      });
    }
  }
}
