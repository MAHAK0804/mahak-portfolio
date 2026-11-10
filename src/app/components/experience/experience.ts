import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PortfolioService, Experience } from '../../services/portfolio';

@Component({
  selector: 'app-experience',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './experience.html',
  styleUrl: './experience.css',
})
export class ExperienceComponent {
  experiences = computed<Experience[]>(() => {
    this.portfolioService.contentRevision();
    return this.portfolioService.getExperiences().slice();
  });

  constructor(private portfolioService: PortfolioService) {}
}
