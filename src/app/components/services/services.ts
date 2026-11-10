import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PortfolioService, Service } from '../../services/portfolio';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './services.html',
  styleUrl: './services.css',
})
export class ServicesComponent {
  services = computed<Service[]>(() => {
    this.portfolioService.contentRevision();
    return this.portfolioService.getServices().slice();
  });

  constructor(private portfolioService: PortfolioService) {}
}
