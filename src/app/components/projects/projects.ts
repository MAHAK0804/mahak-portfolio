import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PortfolioService, Project } from '../../services/portfolio';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './projects.html',
  styleUrl: './projects.css',
})
export class ProjectsComponent {
  projects = computed<Project[]>(() => {
    this.portfolioService.contentRevision();
    return this.portfolioService.getProjects().slice();
  });

  constructor(private portfolioService: PortfolioService) {}
}
