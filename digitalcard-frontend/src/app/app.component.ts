// src/app/app.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // app-toast bileşeni *ngIf kullandığı için gerekli
import { RouterOutlet } from '@angular/router'; // RouterOutlet gerekli
import { ToastComponent } from './components/toast/toast.component'; // ToastComponent import edildi

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, // ToastComponent'in çalışması için gerekli
    RouterOutlet,
    ToastComponent // Toast bileşeni buraya eklendi
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'digitalcard-frontend';
}
