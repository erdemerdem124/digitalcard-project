// src/app/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service'; // AuthService'i import edin
import { map, take } from 'rxjs/operators'; // map ve take operatörlerini import edin

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    // currentUser Observable'ından kullanıcının oturum durumunu al
    return this.authService.currentUser.pipe(
      take(1), // Observable'dan sadece ilk değeri al ve tamamla (bellek sızıntısını önler)
      map(user => {
        if (user) {
          // Kullanıcı oturum açmışsa, rotaya erişime izin ver
          return true;
        } else {
          // Kullanıcı oturum açmamışsa, giriş sayfasına yönlendir
          // createUrlTree ile bir UrlTree oluşturarak yönlendirme yapıyoruz
          return this.router.createUrlTree(['/login']);
        }
      })
    );
  }
}
