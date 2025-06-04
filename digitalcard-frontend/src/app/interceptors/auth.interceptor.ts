// src/app/interceptors/auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const currentUser = this.authService.currentUserValue;
    const isLoggedIn = currentUser && currentUser.token;
    const isApiUrl = request.url.startsWith('http://localhost:8080/api/');

    // Kimlik doğrulama (login/register) endpointlerini hariç tut
    const isAuthEndpoint = request.url.includes('/api/auth/login') || request.url.includes('/api/auth/register');

    // Eğer kullanıcı giriş yapmışsa, API URL'si ise VE kimlik doğrulama endpointi değilse token ekle
    if (isLoggedIn && isApiUrl && !isAuthEndpoint) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${currentUser.token}`
        }
      });
      console.log('AuthInterceptor: Token eklendi. Hedef URI:', request.url); // Hangi URI'ye token eklendiğini logla
    } else if (isAuthEndpoint) {
      console.log('AuthInterceptor: Kimlik doğrulama endpointine token eklenmedi. URI:', request.url);
    } else {
      console.log('AuthInterceptor: Kullanıcı giriş yapmamış veya API URL\'si değil, token eklenmedi. URI:', request.url);
    }

    return next.handle(request);
  }
}
