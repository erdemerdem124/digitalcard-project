// src/app/components/toast/toast.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service'; // ToastMessage import'ı kaldırıldı
import { Subscription } from 'rxjs';

// ToastMessage arayüzü, ToastService içinde tanımlanmalı veya ayrı bir dosyada olmalı.
// Şimdilik burada 'any' olarak varsayıyoruz, ancak toast.service.ts düzeltildiğinde daha spesifik bir tip kullanılacak.
interface ToastMessage {
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number;
}

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss'
})
export class ToastComponent implements OnInit, OnDestroy {
  currentToast: ToastMessage | null = null;
  private toastSubscription!: Subscription;
  private timeoutId: any;

  constructor(private toastService: ToastService) { }

  ngOnInit(): void {
    // getToastMessages metodunun ToastService'te var olduğu varsayılıyor.
    // ToastMessage tipini burada açıkça belirtmek TS7006 hatasını çözer.
    this.toastSubscription = this.toastService.getToastMessages().subscribe((toast: ToastMessage | null) => {
      if (toast) {
        this.currentToast = toast;
        // Mevcut bir zamanlayıcı varsa temizle
        if (this.timeoutId) {
          clearTimeout(this.timeoutId);
        }
        this.timeoutId = setTimeout(() => {
          this.currentToast = null;
        }, toast.duration || 3000); // Varsayılan 3 saniye
      } else {
        this.currentToast = null;
        if (this.timeoutId) {
          clearTimeout(this.timeoutId);
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.toastSubscription) {
      this.toastSubscription.unsubscribe();
    }
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }

  closeToast(): void {
    this.currentToast = null;
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }
}
