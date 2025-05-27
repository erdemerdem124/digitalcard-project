// src/app/services/toast.service.ts
import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface ToastMessage {
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number; // milliseconds
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastSubject = new Subject<ToastMessage | null>();

  constructor() { }

  getToastMessages(): Observable<ToastMessage | null> {
    return this.toastSubject.asObservable();
  }

  show(message: string, type: 'success' | 'error' | 'info' = 'info', duration: number = 3000): void {
    this.toastSubject.next({ message, type, duration });
  }

  success(message: string, duration?: number): void {
    this.show(message, 'success', duration);
  }

  error(message: string, duration?: number): void {
    this.show(message, 'error', duration);
  }

  info(message: string, duration?: number): void {
    this.show(message, 'info', duration);
  }

  clear(): void {
    this.toastSubject.next(null);
  }
}
