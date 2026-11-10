import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  socials = [
    { name: 'GitHub', url: 'https://github.com/MAHAK0804', icon: '🔗' },
    { name: 'LinkedIn', url: 'https://www.linkedin.com/in/mahak-garg-09197523b/', icon: '🔗' },
    { name: 'Twitter', url: 'https://twitter.com/MahakGa', icon: '🔗' },
    { name: 'Instagram', url: 'https://www.instagram.com/mahak_asde/', icon: '🔗' }
  ];
}
