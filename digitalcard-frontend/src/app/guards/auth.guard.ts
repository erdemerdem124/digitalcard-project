// src/app/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';
import { ToastService } from '../services/toast.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    return this.authService.currentUser.pipe(
      take(1),
      map(user => {
        if (user) {
          return true; // Kullanıcı giriş yapmışsa erişime izin ver
        } else {
          // Kullanıcı giriş yapmamışsa login sayfasına yönlendir
          this.toastService.error('Bu sayfaya erişmek için giriş yapmanız gerekiyor.');
          return this.router.createUrlTree(['/login']);
        }
      })
    );
  }
}
