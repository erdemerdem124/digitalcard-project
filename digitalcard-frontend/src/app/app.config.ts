// src/app/app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http'; // HTTP_INTERCEPTORS import edildi

import { routes } from './app.routes';
import { AuthInterceptor } from './interceptors/auth.interceptor'; // AuthInterceptor import edildi

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // HTTP istemcisini DI'dan interceptor'ları alacak şekilde yapılandır
    provideHttpClient(withInterceptorsFromDi()), // withInterceptorsFromDi() doğru çağrıldı
    // AuthInterceptor sınıfını HTTP_INTERCEPTORS token'ı ile kaydet
    // multi: true, birden fazla interceptor olabileceğini belirtir.
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ]
};
