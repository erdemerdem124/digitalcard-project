// src/app/components/profile-edit/profile-edit.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule } from '@angular/forms'; // ReactiveFormsModule import edildi
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserService, UserProfile, SocialLink, Project } from '../../services/user.service';
import { AuthService, User, PasswordUpdateRequest } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { Subscription } from 'rxjs';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule], // ReactiveFormsModule eklendi
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.scss' // Düzeltildi
})
export class ProfileEditComponent implements OnInit, OnDestroy {
  profileForm!: FormGroup;
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';
  private authSubscription!: Subscription;
  private profileSubscription!: Subscription;
  profileEditUrl: string = '';
  currentTheme: Theme;
  public Theme = Theme;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private route: ActivatedRoute,
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

    this.profileForm = this.fb.group({
      profileImageUrl: [''],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: [{ value: '', disabled: true }, Validators.required],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      bio: [''],
      title: [''],
      // company: [''], // Şirket alanı kaldırıldı
      phoneNumber: [''],
      website: [''],
      address: [''], // Adres alanı Konum olarak kullanılacak
      socialLinks: this.fb.array([]),
      projects: this.fb.array([]),
      currentPassword: [''],
      newPassword: ['', Validators.minLength(6)],
      confirmNewPassword: ['']
    }, { validator: this.newPasswordMatchValidator });

    this.authSubscription = this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.profileEditUrl = `/profile/edit/${user.username}`;
        this.loadUserProfile(user.username);
      } else {
        this.isLoading = false;
        this.router.navigate(['/login']);
      }
    });
  }

  newPasswordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmNewPassword = form.get('confirmNewPassword');

    if (newPassword?.value && confirmNewPassword?.value && newPassword.value !== confirmNewPassword.value) {
      confirmNewPassword.setErrors({ newPasswordsMismatch: true });
    } else {
      confirmNewPassword?.setErrors(null);
    }
    return null;
  }

  loadUserProfile(username: string): void {
    this.isLoading = true;
    this.profileSubscription = this.userService.getUserProfileByUsername(username).subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.profileForm.patchValue({
          profileImageUrl: profile.profileImageUrl || '',
          firstName: profile.firstName,
          lastName: profile.lastName,
          username: profile.username,
          email: profile.email,
          bio: profile.bio || '',
          title: profile.title || '',
          // company: profile.company || '', // Şirket alanı kaldırıldı
          phoneNumber: profile.phoneNumber || '',
          website: profile.website || '',
          address: profile.address || ''
        });

        this.setSocialLinks(profile.socialLinks);
        this.setProjects(profile.projects);
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading user profile:', err);
        this.errorMessage = 'Profil yüklenirken bir hata oluştu veya profiliniz bulunamadı. Lütfen bir profil oluşturun.';
        this.userProfile = null; // Profil yüklenemediğinde userProfile null kalır
        this.isLoading = false;
      }
    });
  }

  get socialLinksFormArray(): FormArray {
    return this.profileForm.get('socialLinks') as FormArray;
  }

  addSocialLink(): void {
    this.socialLinksFormArray.push(this.fb.group({
      platform: ['', Validators.required],
      url: ['', Validators.required]
    }));
  }

  removeSocialLink(index: number): void {
    this.socialLinksFormArray.removeAt(index);
  }

  private setSocialLinks(socialLinks: SocialLink[]): void {
    this.socialLinksFormArray.clear();
    socialLinks.forEach(link => {
      this.socialLinksFormArray.push(this.fb.group({
        platform: [link.platform, Validators.required],
        url: [link.url, Validators.required]
      }));
    });
  }

  get projectsFormArray(): FormArray {
    return this.profileForm.get('projects') as FormArray;
  }

  addProject(): void {
    this.projectsFormArray.push(this.fb.group({
      name: ['', Validators.required],
      description: [''],
      technologies: [''], // Başlangıçta boş string
      url: ['']
    }));
  }

  removeProject(index: number): void {
    this.projectsFormArray.removeAt(index);
  }

  private setProjects(projects: Project[]): void {
    this.projectsFormArray.clear();
    projects.forEach(project => {
      this.projectsFormArray.push(this.fb.group({
        name: [project.name, Validators.required],
        description: [project.description || ''],
        technologies: [project.technologies?.join(', ') || ''], // Array'i string'e çevir
        url: [project.url || '']
      }));
    });
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm gerekli alanları doğru şekilde doldurun.');
      return;
    }

    this.isLoading = true;
    const formValue = this.profileForm.getRawValue();

    const updatedProfile: UserProfile = {
      id: this.userProfile?.id || 0,
      userId: this.currentUser?.id || 0,
      profileImageUrl: formValue.profileImageUrl,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      username: formValue.username,
      email: formValue.email,
      bio: formValue.bio,
      title: formValue.title,
      // company: formValue.company, // Şirket alanı kaldırıldı
      phoneNumber: formValue.phoneNumber,
      website: formValue.website,
      address: formValue.address,
      socialLinks: formValue.socialLinks,
      projects: formValue.projects.map((p: any) => ({
        ...p,
        technologies: p.technologies ? p.technologies.split(',').map((tech: string) => tech.trim()) : [] // String'i Array'e çevir
      }))
    };

    if (this.userProfile?.id) { // Eğer profil ID'si varsa, güncelleme yap
      this.userService.updateUserProfile(this.userProfile.id, updatedProfile).subscribe({
        next: (profile) => {
          this.userProfile = profile;
          this.toastService.success('Profil başarıyla güncellendi!');
          this.isLoading = false;
          this.router.navigate(['/home']);
        },
        error: (err: any) => {
          console.error('Profil güncelleme hatası:', err);
          this.errorMessage = err.message || 'Profil güncellenirken bir hata oluştu.';
          this.toastService.error(this.errorMessage);
          this.isLoading = false;
        }
      });
    } else { // Eğer profil ID'si yoksa, yeni profil oluştur
      this.userService.createUserProfile(updatedProfile).subscribe({
        next: (profile) => {
          this.userProfile = profile;
          this.toastService.success('Profil başarıyla oluşturuldu!');
          this.isLoading = false;
          this.router.navigate(['/home']);
        },
        error: (err: any) => {
          console.error('Profil oluşturma hatası:', err);
          this.errorMessage = err.error?.message || err.message || 'Profil oluşturulurken bir hata oluştu.';
          this.toastService.error(this.errorMessage);
          this.isLoading = false;
        }
      });
    }
  }

  updatePassword(): void {
    const currentPassword = this.profileForm.get('currentPassword')?.value;
    const newPassword = this.profileForm.get('newPassword')?.value;
    const confirmNewPassword = this.profileForm.get('confirmNewPassword')?.value;

    if (!currentPassword || !newPassword || !confirmNewPassword) {
      this.toastService.error('Lütfen mevcut şifrenizi, yeni şifrenizi ve yeni şifre onayını doldurun.');
      return;
    }

    if (newPassword !== confirmNewPassword) {
      this.toastService.error('Yeni şifreler eşleşmiyor.');
      return;
    }

    if (newPassword.length < 6) {
      this.toastService.error('Yeni şifre en az 6 karakter olmalıdır.');
      return;
    }

    if (this.currentUser?.id) {
      const passwordUpdate: PasswordUpdateRequest = { currentPassword, newPassword };
      this.authService.updatePassword(this.currentUser.id, passwordUpdate).subscribe({
        next: () => {
          this.profileForm.patchValue({
            currentPassword: '',
            newPassword: '',
            confirmNewPassword: ''
          });
        },
        error: (err: any) => {
          console.error('Şifre güncelleme hatası:', err);
        }
      });
    } else {
      this.toastService.error('Şifre güncellemek için geçerli bir kullanıcı bulunamadı.');
    }
  }

  logout(): void {
    this.authService.logout();
  }

  confirmDeleteAccount(): void {
    const confirmDelete = window.confirm('Hesabınızı kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.');
    if (confirmDelete) {
      this.deleteAccount();
    }
  }

  deleteAccount(): void {
    if (this.currentUser?.id) {
      this.userService.deleteUserProfile(this.currentUser.id).subscribe({
        next: () => {
          this.toastService.success('Hesabınız başarıyla silindi.');
          this.authService.logout();
        },
        error: (err: any) => {
          console.error('Hesap silme hatası:', err);
          this.toastService.error('Hesap silinirken bir hata oluştu: ' + (err.message || 'Bilinmeyen hata'));
        }
      });
    } else {
      this.toastService.error('Hesap silmek için geçerli bir kullanıcı bulunamadı.');
    }
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.profileSubscription) {
      this.profileSubscription.unsubscribe();
    }
  }
}
