// src/app/interceptors/token.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core'; // inject fonksiyonunu import edin
import { AuthService } from '../services/auth.service'; // AuthService'i import edin

// TokenInterceptor artık bir sınıf değil, bir fonksiyondur.
// HttpInterceptorFn tipi, bu fonksiyonun HttpInterceptor arayüzünü karşıladığını belirtir.
export const TokenInterceptor: HttpInterceptorFn = (req, next) => {
  // AuthService'i inject fonksiyonu ile alıyoruz
  const authService = inject(AuthService);
  const authToken = authService.getToken(); // Token'ı al

  // Eğer token varsa, isteğe Authorization başlığını ekle
  if (authToken) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${authToken}` // `Bearer` kelimesiyle birlikte token'ı ekle
      }
    });
  }

  // Değiştirilmiş isteği veya orijinal isteği (token yoksa) bir sonraki işleyiciye ilet
  return next(req);
};
export default null; // <-- BU SATIR KRİTİK!
