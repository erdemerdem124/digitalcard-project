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

  passwordMatchValidator = (form: FormGroup) => {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (this.isLoginMode || !confirmPassword) {
      confirmPassword?.setErrors(null);
      return null;
    }

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
    this.authForm.get('username')?.clearValidators();
    this.authForm.get('firstName')?.clearValidators();
    this.authForm.get('lastName')?.clearValidators();
    this.authForm.get('email')?.clearValidators();
    this.authForm.get('password')?.clearValidators();
    this.authForm.get('confirmPassword')?.clearValidators();

    if (this.isLoginMode) {
      this.authForm.get('username')?.setValidators([Validators.required]);
      this.authForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.authForm.get('confirmPassword')?.setErrors(null);
    } else { // Register mode
      this.authForm.get('username')?.setValidators([Validators.required]);
      this.authForm.get('firstName')?.setValidators([Validators.required]);
      this.authForm.get('lastName')?.setValidators([Validators.required]);
      this.authForm.get('email')?.setValidators([Validators.required, Validators.email]);
      this.authForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.authForm.get('confirmPassword')?.setValidators([Validators.required]);
    }

    this.authForm.get('username')?.updateValueAndValidity();
    this.authForm.get('firstName')?.updateValueAndValidity();
    this.authForm.get('lastName')?.updateValueAndValidity();
    this.authForm.get('email')?.updateValueAndValidity();
    this.authForm.get('password')?.updateValueAndValidity();
    this.authForm.get('confirmPassword')?.updateValueAndValidity();

    this.authForm.updateValueAndValidity();
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.authForm.reset();
    this.setValidatorsByMode();
  }

  onSubmit(): void {
    this.errorMessage = '';

    console.log('Form geçerli mi:', this.authForm.valid);
    console.log('Form hataları:', this.authForm.errors);
    console.log('Form değerleri (geniş):', this.authForm.value);

    Object.keys(this.authForm.controls).forEach(key => {
      const control = this.authForm.get(key);
      console.log(`Kontrol: ${key}, Geçerli mi: ${control?.valid}, Hataları: ${control?.errors}, Dokunuldu mu: ${control?.touched}`);
    });

    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm gerekli alanları doğru şekilde doldurun.');
      this.isLoading = false;
      return;
    }

    this.isLoading = true;

    // Sadece gerekli alanları al: username ve password
    const { username, password } = this.authForm.value;

    if (this.isLoginMode) {
      // Backend'in beklediği 'username' alan adını kullanıyoruz.
      const loginPayload = { username: username, password: password };
      console.log('Login isteği için gönderilen payload:', loginPayload);

      this.authService.login(loginPayload).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Başarıyla giriş yapıldı!');
          this.router.navigate(['/home']);
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || err.message || 'Giriş başarısız oldu.';
          this.toastService.error(this.errorMessage);
        }
      });
    } else {
      // Register için RegisterRequest arayüzüne uygun olarak tüm alanları gönder
      const { firstName, lastName, email } = this.authForm.value;
      this.authService.register({ username, firstName, lastName, email, password }).subscribe({
        next: () => {
          this.isLoading = false;
          this.toastService.success('Kayıt başarılı! Şimdi giriş yapabilirsiniz.');
          this.toggleMode();
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || err.message || 'Kayıt başarısız oldu.';
          this.toastService.error(this.errorMessage);
        }
      });
    }
  }
}
