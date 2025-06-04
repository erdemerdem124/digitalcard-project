// src/app/services/toast.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { take } from 'rxjs/operators';

// ToastMessage arayüzü, ToastComponent'in beklediği isimle dışa aktarıldı
export interface ToastMessage {
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number; // duration özelliği eklendi, böylece ToastComponent'te kullanılabilir
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  // BehaviorSubject'in tipi ToastMessage olarak güncellendi
  private toastSubject: BehaviorSubject<ToastMessage | null> = new BehaviorSubject<ToastMessage | null>(null);
  // toast$ Observable'ın tipi ToastMessage olarak güncellendi
  public toast$: Observable<ToastMessage | null> = this.toastSubject.asObservable();
  private nextId = 0; // Bu ID, toast'ları benzersiz yapmak için kullanılabilir, ancak şu an ToastComponent'te kullanılmıyor.

  constructor() { }

  // ToastComponent'in beklediği getToastMessages metodu eklendi
  public getToastMessages(): Observable<ToastMessage | null> {
    return this.toast$;
  }

  // show metodu, ToastMessage arayüzüne uygun olarak güncellendi
  private show(message: string, type: 'success' | 'error' | 'info', duration: number = 3000): void {
    const id = this.nextId++; // Benzersiz ID oluştur
    this.toastSubject.next({ message, type, duration }); // duration da gönderildi

    timer(duration).pipe(take(1)).subscribe(() => {
      // Sadece gösterilen toast'ın ID'si mevcut toast'ın ID'si ile eşleşiyorsa temizle
      // Bu, hızlıca art arda gelen toast'ların birbirini erken kapatmasını engeller
      if (this.toastSubject.value?.message === message && this.toastSubject.value?.type === type) {
          this.toastSubject.next(null);
      }
    });
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error', 5000); // Hata mesajları daha uzun kalabilir
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  clear(): void {
    this.toastSubject.next(null);
  }
}
