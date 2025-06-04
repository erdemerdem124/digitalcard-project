// src/app/components/home/home.component.ts
import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';
import { UserService, UserProfile, SocialLink, Project } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';
import { Subscription } from 'rxjs';
import QRCodeStyling from 'qr-code-styling';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit, OnDestroy, AfterViewInit {
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  profileEditUrl: string = '/profile/edit';
  isLoading: boolean = true;
  // errorMessage: string = ''; // Kimlik doğrulama formu kaldırıldığı için bu özellik kaldırıldı
  private authSubscription!: Subscription;
  private profileSubscription!: Subscription;
  private qrCode: QRCodeStyling | undefined;
  currentTheme: Theme;
  public Theme = Theme;

  // isLoginMode: boolean = true; // Kimlik doğrulama formu kaldırıldığı için bu özellik kaldırıldı
  // authForm!: FormGroup; // Kimlik doğrulama formu kaldırıldığı için bu özellik kaldırıldı

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private toastService: ToastService,
    private themeService: ThemeService
    // private fb: FormBuilder // Kimlik doğrulama formu kaldırıldığı için FormBuilder kaldırıldı
  ) {
    this.currentTheme = this.themeService.getTheme();
  }

  ngOnInit(): void {
    this.themeService.currentTheme$.subscribe(theme => {
      this.currentTheme = theme;
      if (this.userProfile) {
        this.generateQrCode(this.userProfile.username);
      }
    });

    // Kimlik doğrulama formu kaldırıldığı için formGroup başlatma kaldırıldı
    // this.authForm = this.fb.group({
    //   username: ['', [Validators.required, Validators.minLength(3)]],
    //   firstName: [''],
    //   lastName: [''],
    //   email: ['', [Validators.email]],
    //   password: ['', [Validators.required, Validators.minLength(6)]],
    //   confirmPassword: ['']
    // }, { validator: this.passwordsMatchValidator });

    this.authSubscription = this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (user) {
        console.log('HomeComponent: currentUsername set to:', user.username);
        this.profileEditUrl = `/profile/edit/${user.username}`;
        this.loadUserProfile(user.username);
      } else {
        this.userProfile = null;
        this.isLoading = false;
        this.router.navigate(['/login']);
      }
    });
  }

  ngAfterViewInit(): void {
    // QR kodu oluşturma işlemi AfterViewInit içinde yapılmalı
    // Çünkü QR kodunun yerleşeceği DOM elementi burada garanti edilir.
    // Ancak ngOnInit'teki setTimeout ile de kontrol edildiği için burada tekrar çağırmaya gerek yok.
    // if (this.userProfile && this.userProfile.username) {
    //   this.generateQrCode(this.userProfile.username);
    // }
  }

  // Kimlik doğrulama formu kaldırıldığı için passwordsMatchValidator kaldırıldı
  // passwordsMatchValidator(form: FormGroup) {
  //   const password = form.get('password');
  //   const confirmPassword = form.get('confirmPassword');
  //   if (password?.value && confirmPassword?.value && password.value !== confirmPassword.value) {
  //     confirmPassword.setErrors({ passwordsMismatch: true });
  //   } else {
  //     confirmPassword?.setErrors(null);
  //   }
  //   return null;
  // }

  loadUserProfile(username: string): void {
    this.isLoading = true;
    this.profileSubscription = this.userService.getUserProfileByUsername(username).subscribe({
      next: (profile) => {
        this.userProfile = profile;
        console.log('User profile loaded:', profile);
        this.isLoading = false;
        if (this.userProfile && this.userProfile.username) {
          console.log('Attempting to generate QR code for:', this.userProfile.username);
          setTimeout(() => { // DOM'un güncellenmesini bekle
            this.generateQrCode(this.userProfile!.username);
          }, 0);
        }
      },
      error: (err: any) => { // Hata tipi 'any' olarak belirtildi
        console.error('Error loading user profile:', err);
        this.userProfile = null; // Profil bulunamazsa null yap
        this.isLoading = false;
        this.toastService.error('Profil yüklenirken bir hata oluştu veya profil bulunamadı.');
      }
    });
  }

  generateQrCode(username: string): void {
    console.log('generateQrCode method entered. Username:', username);
    const qrCodeContainer = document.getElementById('qrcode-container');

    if (!qrCodeContainer) {
      console.warn('QR Code Container element not found!');
      return;
    }
    console.log('QR Code Container element:', qrCodeContainer);

    if (typeof QRCodeStyling === 'undefined') {
      console.error('QRCode library is not defined. Make sure it is loaded correctly.');
      this.toastService.error('QR Kod kütüphanesi yüklenemedi. Lütfen konsolu kontrol edin.');
      return;
    }
    console.log('QRCode library is defined:', typeof QRCodeStyling !== 'undefined');

    // Mevcut QR kodunu temizle
    if (this.qrCode) { // qrCode tanımlıysa temizle
      this.qrCode.clear();
    }
    qrCodeContainer.innerHTML = ''; // Konteynerin içeriğini de temizle

    const profileUrl = `${window.location.origin}/profile/${username}`;
    console.log('QR Code URL:', profileUrl);

    this.qrCode = new QRCodeStyling({
      width: 200,
      height: 200,
      type: 'canvas',
      data: profileUrl,
      image: 'https://cdn-icons-png.flaticon.com/512/1053/1053210.png', // Küçük bir logo eklenebilir
      dotsOptions: {
        color: this.currentTheme === Theme.Dark ? '#e0e0e0' : '#333333', // Tema rengine göre QR kodu rengi
        type: 'rounded'
      },
      backgroundOptions: {
        color: this.currentTheme === Theme.Dark ? '#2a2a2a' : '#ffffff', // Tema rengine göre arka plan rengi
      },
      imageOptions: {
        crossOrigin: 'anonymous',
        margin: 5
      },
      cornersSquareOptions: {
        color: this.currentTheme === Theme.Dark ? '#7986cb' : '#2196f3', // Tema rengine göre köşe rengi
        type: 'extra-rounded'
      },
      cornersDotOptions: {
        color: this.currentTheme === Theme.Dark ? '#5c6bc0' : '#1976d2', // Tema rengine göre köşe nokta rengi
      }
    });

    this.qrCode.append(qrCodeContainer);
    console.log('QR Code generated successfully.');
  }

  getSocialIcon(platform: string): string {
    switch (platform.toLowerCase()) {
      case 'linkedin': return 'fab fa-linkedin';
      case 'github': return 'fab fa-github';
      case 'twitter': return 'fab fa-twitter';
      case 'instagram': return 'fab fa-instagram';
      case 'facebook': return 'fab fa-facebook';
      case 'website': return 'fas fa-globe';
      case 'phone': return 'fas fa-phone';
      case 'email': return 'fas fa-envelope';
      default: return 'fas fa-link';
    }
  }

  copyProfileLink(): void {
    if (this.userProfile?.username) {
      const profileUrl = `${window.location.origin}/profile/${this.userProfile.username}`;
      const el = document.createElement('textarea');
      el.value = profileUrl;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy'); // navigator.clipboard.writeText yerine execCommand
      document.body.removeChild(el);
      this.toastService.success('Profil bağlantısı panoya kopyalandı!');
    } else {
      this.toastService.error('Profil bağlantısı kopyalanamadı.');
    }
  }

  navigateToProfileEdit(): void {
    this.router.navigate([this.profileEditUrl]);
  }

  // confirmDeleteAccount(): void { // Kimlik doğrulama formu kaldırıldığı için bu metod kaldırıldı
  //   const confirmDelete = window.confirm('Hesabınızı kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.');
  //   if (confirmDelete) {
  //     this.deleteAccount();
  //   }
  // }

  // deleteAccount(): void { // Kimlik doğrulama formu kaldırıldığı için bu metod kaldırıldı
  //   if (this.currentUser?.id) {
  //     this.userService.deleteUserProfile(this.currentUser.id).subscribe({
  //       next: () => {
  //         this.toastService.success('Hesabınız başarıyla silindi.');
  //         this.authService.logout();
  //       },
  //       error: (err) => {
  //         console.error('Hesap silme hatası:', err);
  //         this.toastService.error('Hesap silinirken bir hata oluştu: ' + (err.message || 'Bilinmeyen hata'));
  //       }
  //     });
  //   } else {
  //     this.toastService.error('Hesap silmek için geçerli bir kullanıcı bulunamadı.');
  //   }
  // }

  logout(): void {
    this.authService.logout();
  }

  toggleTheme(): void {
    // ThemeService'in kendi toggleTheme metodunu kullanmak daha doğru
    this.themeService.toggleTheme();
    // this.currentTheme = this.themeService.getTheme(); // Zaten subscribe ile güncelleniyor
  }

  // onSubmit(): void { // Kimlik doğrulama formu kaldırıldığı için bu metod kaldırıldı
  //   this.isLoading = true;
  //   this.errorMessage = '';
  //   if (this.authForm.invalid) {
  //     this.authForm.markAllAsTouched();
  //     this.isLoading = false;
  //     return;
  //   }
  //   const { username, password, firstName, lastName, email } = this.authForm.value;
  //   if (this.isLoginMode) {
  //     this.authService.login(username, password).subscribe({
  //       next: () => {
  //         this.isLoading = false;
  //         this.toastService.success('Giriş başarılı!');
  //         this.router.navigate(['/home']);
  //       },
  //       error: (err) => {
  //         this.isLoading = false;
  //         this.errorMessage = err.message || 'Giriş başarısız. Lütfen kullanıcı adı ve şifrenizi kontrol edin.';
  //         this.toastService.error(this.errorMessage);
  //       }
  //     });
  //   } else {
  //     this.authService.register(username, password, firstName, lastName, email).subscribe({
  //       next: () => {
  //         this.isLoading = false;
  //         this.toastService.success('Kayıt başarılı! Şimdi giriş yapabilirsiniz.');
  //         this.isLoginMode = true; // Kayıt sonrası giriş moduna geç
  //         this.authForm.reset(); // Formu sıfırla
  //       },
  //       error: (err) => {
  //         this.isLoading = false;
  //         this.errorMessage = err.message || 'Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.';
  //         this.toastService.error(this.errorMessage);
  //       }
  //     });
  //   }
  // }

  // toggleMode(): void { // Kimlik doğrulama formu kaldırıldığı için bu metod kaldırıldı
  //   this.isLoginMode = !this.isLoginMode;
  //   this.errorMessage = ''; // Mod değiştiğinde hata mesajını temizle
  //   this.authForm.reset(); // Formu sıfırla
  // }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.profileSubscription) {
      this.profileSubscription.unsubscribe();
    }
  }
}
