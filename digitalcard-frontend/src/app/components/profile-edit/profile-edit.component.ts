// src/app/components/profile-edit/profile-edit.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserService, UserProfile, SocialLink, Project, UserRequestDTO } from '../../services/user.service';
import { AuthService, User, PasswordUpdateRequest } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { Subscription } from 'rxjs';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.scss'
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
      // KRİTİK DÜZELTME: phoneNumber yerine phone
      phone: [''],
      // KRİTİK DÜZELTME: website yerine portfolioUrl
      portfolioUrl: [''],
      // KRİTİK DÜZELTME: address yerine location
      location: [''],
      socialLinks: this.fb.array([]),
      projects: this.fb.array([]),
      currentPassword: [''],
      newPassword: ['', Validators.minLength(8)],
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

    if (!newPassword?.value && !confirmNewPassword?.value) {
      confirmNewPassword?.setErrors(null);
      return null;
    }

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
        console.log('DEBUG (profile-edit): Yüklenen profil (frontend formatı):', profile);
        this.profileForm.patchValue({
          profileImageUrl: profile.profileImageUrl || '',
          firstName: profile.firstName,
          lastName: profile.lastName,
          username: profile.username,
          email: profile.email,
          bio: profile.bio || '',
          title: profile.title || '',
          // KRİTİK DÜZELTME: phoneNumber yerine phone
          phone: profile.phone || '',
          // KRİTİK DÜZELTME: website yerine portfolioUrl
          portfolioUrl: profile.portfolioUrl || '',
          // KRİTİK DÜZELTME: address yerine location
          location: profile.location || ''
        });

        this.setSocialLinks(profile.socialLinks || []);
        this.setProjects(profile.projects || []);
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading user profile:', err);
        this.errorMessage = err.message || 'Profil yüklenirken bir hata oluştu veya profiliniz bulunamadı. Lütfen bir profil oluşturun.';
        this.userProfile = null;
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
      title: ['', Validators.required],
      description: [''],
      technologies: [''],
      projectUrl: [''],
      projectImageUrl: ['']
    }));
  }

  removeProject(index: number): void {
    this.projectsFormArray.removeAt(index);
  }

  private setProjects(projects: Project[]): void {
    this.projectsFormArray.clear();
    projects.forEach(project => {
      this.projectsFormArray.push(this.fb.group({
        title: [project.title, Validators.required],
        description: [project.description || ''],
        technologies: [project.technologies || ''],
        projectUrl: [project.projectUrl || ''],
        projectImageUrl: [project.projectImageUrl || '']
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
    const formValue = this.profileForm.getRawValue();

    this.isLoading = true;

    const updatedProfile: UserRequestDTO = {
      username: formValue.username,
      email: formValue.email,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      profileImageUrl: formValue.profileImageUrl,
      bio: formValue.bio,
      title: formValue.title,
      phone: formValue.phone,
      portfolioUrl: formValue.portfolioUrl,
      location: formValue.location,
      socialLinks: formValue.socialLinks,
      projects: formValue.projects.map((p: any) => ({
        title: p.title,
        description: p.description,
        technologies: p.technologies || '',
        projectUrl: p.projectUrl,
        projectImageUrl: p.projectImageUrl
      }))
    };

    console.log('DEBUG (profile-edit): UserService\'e gönderilecek profil objesi:', updatedProfile);

    if (this.userProfile?.id) {
      this.userService.updateUserProfile(this.userProfile.id, updatedProfile).subscribe({
        next: (profile) => {
          this.userProfile = profile;
          this.toastService.success('Profil başarıyla güncellendi!');
          this.isLoading = false;
          console.log('DEBUG (profile-edit): Profil başarıyla güncellendi. Yeni profil objesi:', profile);
          this.router.navigate(['/home']);
        },
        error: (err: any) => {
          console.error('Profil güncelleme hatası:', err);
          this.errorMessage = err.message || 'Profil güncellenirken bir hata oluştu.';
          this.toastService.error(this.errorMessage);
          this.isLoading = false;
        }
      });
    } else {
      this.toastService.error('Mevcut profiliniz bulunamadı. Lütfen giriş yapın veya kayıt olun.');
      this.isLoading = false;
    }
  }

  updatePassword(): void {
    const currentPassword = this.profileForm.get('currentPassword')?.value;
    const newPassword = this.profileForm.get('newPassword')?.value;
    const confirmNewPassword = this.profileForm.get('confirmNewPassword')?.value;

    if (!currentPassword || !newPassword || !confirmNewPassword) {
      this.toastService.error('Lütfen tüm şifre alanlarını doldurun.');
      return;
    }

    if (newPassword !== confirmNewPassword) {
      this.toastService.error('Yeni şifreler eşleşmiyor.');
      return;
    }

    if (newPassword.length < 8) {
      this.toastService.error('Yeni şifre en az 8 karakter olmalıdır.');
      return;
    }
    if (!currentPassword && (newPassword || confirmNewPassword)) {
      this.toastService.error('Mevcut şifrenizi girmeniz gerekmektedir.');
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
          this.toastService.success('Şifreniz başarıyla güncellendi. Lütfen yeni şifrenizle tekrar giriş yapın.');
        },
        error: (err: any) => {
          console.error('Şifre güncelleme hatası:', err);
          const errorMessage = err.error?.message || err.message || 'Şifre güncelleme başarısız oldu.';
          this.toastService.error(errorMessage);
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
