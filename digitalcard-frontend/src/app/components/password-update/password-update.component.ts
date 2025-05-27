// src/app/components/password-update/password-update.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, PasswordUpdateRequest, User } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { Subscription } from 'rxjs';
import { filter, take, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-password-update',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './password-update.component.html',
  styleUrl: './password-update.component.scss'
})
export class PasswordUpdateComponent implements OnInit, OnDestroy {

  passwordForm!: FormGroup;
  isLoading: boolean = false;
  currentUserId: number | null = null;
  private userSubscription!: Subscription;
  isFormReady: boolean = false; // Formun kullanıma hazır olup olmadığını belirtir

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router
  ) { }

  ngOnInit(): void {
    console.log('PasswordUpdateComponent: ngOnInit başlatıldı.');

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]],
      confirmNewPassword: ['', Validators.required]
    }, {
      validator: this.passwordMatchValidator('newPassword', 'confirmNewPassword')
    });

    // Formu başlangıçta tamamen devre dışı bırak
    this.passwordForm.disable();
    this.isFormReady = false;
    console.log('PasswordUpdateComponent: Form başlangıçta devre dışı bırakıldı. isFormReady:', this.isFormReady);

    // AuthService'den currentUser Observable'ına abone ol
    // startWith(this.authService.currentUserValue) ile mevcut değeri hemen al
    // filter ile sadece geçerli bir kullanıcı ID'si olduğunda devam et
    // take(1) ile sadece ilk geçerli değeri al ve aboneliği kapat (ngOnInit'te tek seferlik kontrol için)
    this.userSubscription = this.authService.currentUser
      .pipe(
        startWith(this.authService.currentUserValue), // Abonelik kurulur kurulmaz mevcut değeri yay
        filter(user => {
          const isValidUser = user !== null && user.id !== undefined && user.id !== null;
          console.log(`PasswordUpdateComponent: currentUser pipe - user: ${user ? user.username : 'null'}, isValid: ${isValidUser}, user.id: ${user?.id}`); // Debugging
          return isValidUser;
        }),
        take(1) // Sadece ilk geçerli kullanıcı bilgisini al
      )
      .subscribe(user => {
        console.log('PasswordUpdateComponent: currentUser geçerli bir ID ile algılandı:', user);
        this.currentUserId = user!.id;
        console.log('PasswordUpdateComponent: currentUserId güncellendi:', this.currentUserId);

        // Kullanıcı ID'si varsa formu etkinleştir
        this.passwordForm.enable();
        this.isFormReady = true;
        this.toastService.info('Şifre güncelleme formu etkinleştirildi.');
        console.log('PasswordUpdateComponent: Form etkinleştirildi. isFormReady:', this.isFormReady);
      },
      error => {
        console.error('PasswordUpdateComponent: currentUser aboneliğinde hata:', error);
        this.toastService.error('Kullanıcı oturumu bilgisi alınamadı. Lütfen tekrar giriş yapın.');
        this.navigateToLogin();
      });

    // Zaman aşımı kontrolünü biraz daha esnek hale getirelim veya kaldırabiliriz
    // Eğer `startWith` ve `filter` doğru çalışıyorsa, bu setTimeout'a genellikle gerek kalmaz.
    // Ancak yine de bir fallback olarak tutabiliriz, ancak süreyi artırabiliriz.
    setTimeout(() => {
        if (!this.isFormReady && this.currentUserId === null) {
            console.warn('PasswordUpdateComponent: Belirli bir süre içinde kullanıcı oturumu bilgisi alınamadı (7 saniye). Yönlendiriliyor.');
            this.toastService.error('Şifre güncelleme için kullanıcı oturumu bulunamadı. Lütfen tekrar giriş yapın.');
            this.navigateToLogin();
        }
    }, 7000); // Süreyi 7 saniyeye çıkardık
  }

  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
      console.log('PasswordUpdateComponent: userSubscription aboneliği sonlandırıldı.');
    }
  }

  passwordMatchValidator(controlName: string, matchingControlName: string) {
    return (formGroup: FormGroup) => {
      const control = formGroup.controls[controlName];
      const matchingControl = formGroup.controls[matchingControlName];

      if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
        return;
      }

      if (control.value !== matchingControl.value) {
        matchingControl.setErrors({ mustMatch: true });
      } else {
        matchingControl.setErrors(null);
      }
    };
  }

  get currentPassword() { return this.passwordForm.get('currentPassword'); }
  get newPassword() { return this.passwordForm.get('newPassword'); }
  get confirmNewPassword() { return this.passwordForm.get('confirmNewPassword'); }

  onSubmit(): void {
    this.isLoading = true;
    console.log('PasswordUpdateComponent: onSubmit çağrıldı.');
    console.log('PasswordUpdateComponent: Form durumu:', this.passwordForm.status);
    console.log('PasswordUpdateComponent: currentUserId (onSubmit başlangıcı):', this.currentUserId);
    console.log('PasswordUpdateComponent: isFormReady (onSubmit başlangıcı):', this.isFormReady);

    if (!this.isFormReady || this.passwordForm.invalid || this.currentUserId === null) {
      this.passwordForm.markAllAsTouched();
      if (this.passwordForm.errors?.['mustMatch']) {
        this.toastService.error('Yeni şifreler eşleşmiyor.');
      } else if (this.currentUserId === null) {
        this.toastService.error('Şifre güncelleme için kullanıcı oturumu bulunamadı. Lütfen tekrar giriş yapın.');
        this.navigateToLogin();
      } else {
        this.toastService.error('Lütfen tüm alanları doğru doldurun.');
      }
      this.isLoading = false;
      console.error('PasswordUpdateComponent: Geçersiz form durumu veya kullanıcı ID eksik. Gönderme iptal edildi.');
      return;
    }

    const { currentPassword, newPassword } = this.passwordForm.value;
    const passwordUpdateRequest: PasswordUpdateRequest = { currentPassword, newPassword };

    this.authService.updatePassword(this.currentUserId, passwordUpdateRequest).subscribe({
      next: () => {
        this.toastService.success('Şifreniz başarıyla güncellendi. Lütfen yeni şifrenizle tekrar giriş yapın.');
        this.passwordForm.reset();
        this.isLoading = false;
        console.log('PasswordUpdateComponent: Şifre güncelleme başarılı. Oturum sonlandırılıyor ve login sayfasına yönlendiriliyor.');

        this.authService.logout();
        this.navigateToLogin();
      },
      error: (err: any) => {
        this.isLoading = false;
        console.error('PasswordUpdateComponent: Şifre güncelleme hatası:', err);
      },
      complete: () => {
        this.isLoading = false;
        console.log('PasswordUpdateComponent: Şifre güncelleme isteği tamamlandı.');
      }
    });
  }

  goToProfileEdit(): void {
    this.router.navigate(['/home']);
    console.log('PasswordUpdateComponent: Profil düzenleme sayfasına yönlendiriliyor.');
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
    console.log('PasswordUpdateComponent: Login sayfasına yönlendiriliyor.');
  }
}
