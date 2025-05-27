// src/app/components/profile-edit/profile-edit.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray, AbstractControl } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule, ParamMap } from '@angular/router';
import { Observable, of, switchMap, forkJoin, throwError } from 'rxjs';
import { tap, catchError, finalize } from 'rxjs/operators';

import { UserService, UserProfile, SocialLink, Project, UserRequest } from '../../services/user.service';
// SocialsLinkService yerine SocialLinkService olarak düzeltildi
import { SocialLinkService, SocialLinkRequest, SocialLinkResponse } from '../../services/social-link.service';
import { ProjectService, ProjectRequest, ProjectResponse } from '../../services/project.service';

import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.scss'
})
export class ProfileEditComponent implements OnInit {

  profileForm!: FormGroup;
  currentUserProfile: UserProfile | null = null;
  isLoading: boolean = true;
  currentUsername: string | null = null;
  currentUserId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    // SocialsLinkService yerine SocialLinkService olarak düzeltildi
    private socialLinkService: SocialLinkService,
    private projectService: ProjectService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: [{ value: '', disabled: true }, Validators.required], 
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      bio: [''],
      profileImageUrl: ['', Validators.pattern(/^(https?:\/\/.+\..+)$/)],
      title: [''],
      location: [''],
      phone: [''],
      portfolioUrl: ['', Validators.pattern(/^(https?:\/\/.+\..+)$/)],
      socialLinks: this.fb.array([]),
      projects: this.fb.array([])
    });

    this.route.paramMap.subscribe((params: ParamMap) => { 
      this.currentUsername = params.get('username');
      if (this.currentUsername) {
        this.loadUserProfileAndRelatedData(this.currentUsername);
      } else {
        this.toastService.error('Profil yüklemek için kullanıcı adı gerekli.');
        this.isLoading = false;
      }
    });
  }

  private loadUserProfileAndRelatedData(username: string): void {
    this.isLoading = true;

    this.userService.getUserProfileByUsername(username).pipe(
      switchMap(userProfile => {
        if (userProfile && userProfile.id) {
          const userId = userProfile.id;
          this.currentUserId = userId;

          return forkJoin({
            socialLinks: this.socialLinkService.getSocialLinksByUserId(userId),
            projects: this.projectService.getProjectsByUserId(userId)
          }).pipe(
            tap(({ socialLinks, projects }) => {
              this.currentUserProfile = { ...userProfile, socialLinks: socialLinks, projects: projects };

              this.profileForm.patchValue({
                firstName: userProfile.firstName,
                lastName: userProfile.lastName,
                username: userProfile.username,
                email: userProfile.email,
                bio: userProfile.bio || '',
                profileImageUrl: userProfile.profileImageUrl || '',
                title: userProfile.title || '',
                location: userProfile.location || '',
                phone: userProfile.phone || '',
                portfolioUrl: userProfile.portfolioUrl || ''
              });

              while (this.socialLinks.length !== 0) {
                this.socialLinks.removeAt(0);
              }
              // link parametresine tip verildi
              socialLinks.forEach((link: SocialLink) => this.addSocialLink(link));
              
              while (this.projects.length !== 0) {
                this.projects.removeAt(0);
              }
              projects.forEach((project: Project) => this.addProject(project));
            })
          );
        } else {
          this.router.navigate(['/login']);
          return throwError(() => new Error('Kullanıcı profili bulunamadı veya ID eksik.'));
        }
      }),
      // err parametresine tip verildi
      catchError((err: any) => {
        console.error('Profil ve ilgili veriler yüklenirken hata oluştu:', err);
        this.toastService.error(err.message || 'Profil ve ilgili veriler yüklenirken bir hata oluştu.');
        this.isLoading = false;
        return throwError(() => new Error(err.message || 'Bilinmeyen bir hata oluştu.'));
      }),
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe(
      () => { /* handled by tap and finalize */ },
      // err parametresine tip verildi
      (err: any) => {
        // Hata zaten catchError'da işlendiği için burada ek bir işlem yapmaya gerek yok
      }
    );
  }

  // --- Sosyal Medya Linkleri Yönetimi ---
  get socialLinks(): FormArray {
    return this.profileForm.get('socialLinks') as FormArray;
  }

  createSocialLinkGroup(link?: SocialLink): FormGroup {
    return this.fb.group({
      id: [link?.id || null],
      platform: [link?.platform || '', Validators.required],
      url: [link?.url || '', [Validators.required, Validators.pattern('https?://.+')]]
    });
  }

  addSocialLink(link?: SocialLink): void {
    this.socialLinks.push(this.createSocialLinkGroup(link));
    this.toastService.info('Yeni sosyal link alanı eklendi.');
  }

  removeSocialLink(index: number): void {
    const linkControl = this.socialLinks.at(index);
    if (linkControl && linkControl.get('id')?.value) {
      const linkId = linkControl.get('id')?.value;
      if (confirm('Bu sosyal linki silmek istediğinizden emin misiniz?')) {
        this.socialLinkService.deleteSocialLink(linkId).subscribe({
          next: () => {
            this.socialLinks.removeAt(index);
            this.toastService.success('Sosyal link başarıyla silindi.');
          },
          // err parametresine tip verildi
          error: (err: any) => {
            console.error('Sosyal link silme hatası:', err);
            this.toastService.error(err.message || 'Sosyal link silinirken bir hata oluştu.');
          }
        });
      }
    } else {
      this.socialLinks.removeAt(index);
      this.toastService.info('Sosyal link alanı kaldırıldı (kaydedilmemişse).');
    }
  }

  // --- Projeler Yönetimi ---
  get projects(): FormArray {
    return this.profileForm.get('projects') as FormArray;
  }

  createProjectGroup(project?: Project): FormGroup {
    return this.fb.group({
      id: [project?.id || null],
      title: [project?.title || '', Validators.required],
      description: [project?.description || ''],
      projectUrl: [project?.projectUrl || '', Validators.pattern('https?://.+')],
      technologies: [project?.technologies || '']
    });
  }

  addProject(project?: Project): void {
    this.projects.push(this.createProjectGroup(project));
    this.toastService.info('Yeni proje alanı eklendi.');
  }

  removeProject(index: number): void {
    const projectControl = this.projects.at(index);
    if (projectControl && projectControl.get('id')?.value) {
      const projectId = projectControl.get('id')?.value;
      if (confirm('Bu projeyi silmek istediğinizden emin misiniz?')) {
        this.projectService.deleteProject(projectId).subscribe({
          next: () => {
            this.projects.removeAt(index);
            this.toastService.success('Proje başarıyla silindi.');
          },
          // err parametresine tip verildi
          error: (err: any) => {
            console.error('Proje silme hatası:', err);
            this.toastService.error(err.message || 'Proje silinirken bir hata oluştu.');
          }
        });
      }
    } else {
      this.projects.removeAt(index);
      this.toastService.info('Proje alanı kaldırıldı (kaydedilmemişse).');
    }
  }

  // --- Form Gönderme (onSubmit) ---
  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.toastService.error('Lütfen tüm alanları doğru şekilde doldurun.');
      return;
    }

    if (this.currentUserId === null) {
      this.toastService.error('Kullanıcı ID\'si bulunamadı. Profil güncellenemedi.');
      return;
    }

    const userId = this.currentUserId;
    const formValue = this.profileForm.getRawValue();

    const userProfileUpdate: UserRequest = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      username: formValue.username,
      email: formValue.email,
      bio: formValue.bio,
      profileImageUrl: formValue.profileImageUrl,
      title: formValue.title,
      location: formValue.location,
      phone: formValue.phone,
      portfolioUrl: formValue.portfolioUrl
    };

    const updateObservables: Observable<any>[] = [];

    updateObservables.push(this.userService.updateUserProfile(userId, userProfileUpdate));

    const currentSocialLinks: SocialLink[] = this.currentUserProfile?.socialLinks || [];
    const formSocialLinks: (SocialLink & { id?: number })[] = formValue.socialLinks;

    formSocialLinks.forEach((formLink: SocialLink) => { // formLink parametresine tip verildi
      if (formLink.id) {
        const originalLink = currentSocialLinks.find(link => link.id === formLink.id);
        if (originalLink && (originalLink.platform !== formLink.platform || originalLink.url !== formLink.url)) {
          updateObservables.push(
            this.socialLinkService.updateSocialLink(formLink.id, {
              platform: formLink.platform,
              url: formLink.url,
              userId: userId
            })
          );
        }
      } else {
        updateObservables.push(
          this.socialLinkService.createSocialLink({
            platform: formLink.platform,
            url: formLink.url,
            userId: userId
          })
        );
      }
    });

    currentSocialLinks.forEach((originalLink: SocialLink) => { // originalLink parametresine tip verildi
      const existsInForm = formSocialLinks.some(formLink => formLink.id === originalLink.id);
      if (!existsInForm && originalLink.id) {
        updateObservables.push(this.socialLinkService.deleteSocialLink(originalLink.id));
      }
    });

    const currentProjects: Project[] = this.currentUserProfile?.projects || [];
    const formProjects: (Project & { id?: number })[] = formValue.projects;

    formProjects.forEach((formProject: Project) => { // formProject parametresine tip verildi
      if (formProject.id) {
        const originalProject = currentProjects.find(project => project.id === formProject.id);
        if (originalProject && (
            originalProject.title !== formProject.title ||
            originalProject.description !== formProject.description ||
            originalProject.projectUrl !== formProject.projectUrl ||
            originalProject.technologies !== formProject.technologies
          )) {
          updateObservables.push(
            this.projectService.updateProject(formProject.id, {
              title: formProject.title,
              description: formProject.description,
              projectUrl: formProject.projectUrl,
              technologies: formProject.technologies,
              userId: userId
            })
          );
        }
      } else {
        updateObservables.push(
          this.projectService.createProject({
            title: formProject.title,
            description: formProject.description,
            projectUrl: formProject.projectUrl,
            technologies: formProject.technologies,
            userId: userId
          })
        );
      }
    });

    currentProjects.forEach((originalProject: Project) => { // originalProject parametresine tip verildi
      const existsInForm = formProjects.some(formProject => formProject.id === originalProject.id);
      if (!existsInForm && originalProject.id) {
        updateObservables.push(this.projectService.deleteProject(originalProject.id));
      }
    });

    forkJoin(updateObservables).subscribe({
      next: (responses) => {
        this.toastService.success('Profiliniz başarıyla güncellendi!');
        this.loadUserProfileAndRelatedData(this.currentUsername!);
      },
      // err parametresine tip verildi
      error: (err: any) => {
        console.error('Profil güncelleme hatası:', err);
        this.toastService.error(err.message || 'Profil güncellenirken bir hata oluştu.');
      }
    });
  }

  private markAllAsTouched(formGroup: FormGroup | FormArray): void {
    Object.values(formGroup.controls).forEach((control: AbstractControl) => {
      if (control instanceof FormGroup || control instanceof FormArray) {
        this.markAllAsTouched(control);
      } else {
        control.markAsTouched();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
    this.toastService.info('Çıkış yapıldı.');
  }

  goToHome(): void {
    this.router.navigate(['/home']);
  }

  confirmAccountDeletion(): void {
    // confirm yerine daha şık bir modal kullanılabilir
    if (confirm('Hesabınızı kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.')) {
      this.deleteAccount();
    }
  }

  deleteAccount(): void {
    if (this.currentUserId === null) {
      this.toastService.error('Kullanıcı ID\'si bulunamadı. Hesap silinemedi.');
      return;
    }

    this.userService.deleteAccount(this.currentUserId).subscribe({
      next: () => {
        this.toastService.success('Hesabınız başarıyla silindi.');
        this.authService.logout(); // Kullanıcı çıkış yapsın
        this.router.navigate(['/register']); // Kullanıcıyı kayıt sayfasına yönlendir
      },
      // err parametresine tip verildi
      error: (err: any) => {
        console.error('Hesap silme hatası:', err);
        this.toastService.error(err.message || 'Hesap silinirken bir hata oluştu.');
      }
    });
  }
}
