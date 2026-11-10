import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Experience, PortfolioService, Project, Service } from '../../services/portfolio';

@Component({
  selector: 'app-portfolio-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './portfolio-manager.html',
  styleUrl: './portfolio-manager.css'
})
export class PortfolioManagerComponent implements OnInit {
  projects: Project[];
  experiences: Experience[];
  services: Service[];
  skills: string[];
  skillsText: string;
  isReady = signal(false);
  isAuthenticated = signal(false);
  isBusy = signal(false);
  isConfigured: boolean;
  activeTab = signal<'projects' | 'experience' | 'services' | 'technologies'>('projects');
  hasLoadError = signal(false);
  errorMessage = signal('');
  saveMessage = signal('');
  experienceMessage = signal('');
  loginEmail = '';
  loginPassword = '';

  newProject: Project;
  newExperience: Experience;
  newService: Service;

  constructor(
    private portfolioService: PortfolioService,
    private router: Router
  ) {
    this.isConfigured = this.portfolioService.isAdminConfigured();
    this.projects = this.portfolioService.getProjects();
    this.experiences = this.portfolioService.getExperiences();
    this.services = this.portfolioService.getServices();
    this.skills = this.portfolioService.getSkills();
    this.skillsText = this.skills.join(', ');
    this.newProject = this.emptyProject();
    this.newExperience = this.emptyExperience();
    this.newService = this.emptyService();
  }

  async ngOnInit(): Promise<void> {
    if (!this.portfolioService.isBrowser()) return;
    await this.initialize();
  }

  async initialize(): Promise<void> {
    this.isReady.set(false);
    this.hasLoadError.set(false);
    this.errorMessage.set('');
    try {
      await this.portfolioService.loadSharedContent();
      this.newProject = this.emptyProject();
      this.newExperience = this.emptyExperience();
      this.newService = this.emptyService();
      this.isAuthenticated.set(await this.portfolioService.getAdminSession());
    } catch (error) {
      this.hasLoadError.set(true);
      this.errorMessage.set(this.errorText(error));
    } finally {
      this.isReady.set(true);
    }
  }

  async login(): Promise<void> {
    if (this.hasLoadError()) return;
    this.isBusy.set(true);
    this.errorMessage.set('');
    try {
      await this.portfolioService.signIn(this.loginEmail, this.loginPassword);
      this.loginPassword = '';
      this.isAuthenticated.set(true);
    } catch (error) {
      this.errorMessage.set(this.errorText(error));
    } finally {
      this.isBusy.set(false);
    }
  }

  async logout(): Promise<void> {
    try {
      await this.portfolioService.signOut();
      this.isAuthenticated.set(false);
    } catch (error) {
      this.errorMessage.set(this.errorText(error));
    }
  }

  async save(): Promise<void> {
    const updatedSkills = this.skillsText
      .split(',')
      .map((skill) => skill.trim())
      .filter(Boolean);
    this.skills.splice(0, this.skills.length, ...updatedSkills);
    this.skillsText = this.skills.join(', ');
    this.isBusy.set(true);
    this.errorMessage.set('');
    this.saveMessage.set('');
    try {
      const savedExperienceCount = await this.portfolioService.saveContent();
      this.saveMessage.set(`Changes saved and published (${savedExperienceCount} experience entries).`);
    } catch (error) {
      this.errorMessage.set(this.errorText(error));
    } finally {
      this.isBusy.set(false);
    }
  }

  async reset(): Promise<void> {
    if (!window.confirm('Reset all shared portfolio content to the original details?')) return;

    this.isBusy.set(true);
    this.errorMessage.set('');
    try {
      await this.portfolioService.resetContent();
      await this.router.navigateByUrl('/');
    } catch (error) {
      this.errorMessage.set(this.errorText(error));
    } finally {
      this.isBusy.set(false);
    }
  }

  addProject(): void {
    if (!this.newProject.title.trim()) return;
    this.projects.push({ ...this.newProject, id: this.nextId(this.projects), technologies: [...this.newProject.technologies] });
    this.newProject = this.emptyProject();
  }

  removeProject(index: number): void {
    this.projects.splice(index, 1);
  }

  addExperience(): void {
    this.errorMessage.set('');
    this.experienceMessage.set('');
    if (!this.newExperience.position.trim()) {
      this.errorMessage.set('Enter a position before adding the experience.');
      return;
    }

    this.experiences.push({ ...this.newExperience, id: this.nextId(this.experiences) });
    this.experienceMessage.set('Entry added. Click Save to publish it.');
    this.newExperience = this.emptyExperience();
  }

  removeExperience(index: number): void {
    this.experiences.splice(index, 1);
  }

  addService(): void {
    if (!this.newService.name.trim()) return;
    this.services.push({ ...this.newService, id: this.nextId(this.services) });
    this.newService = this.emptyService();
  }

  removeService(index: number): void {
    this.services.splice(index, 1);
  }

  projectTechnologies(project: Project): string {
    return project.technologies.join(', ');
  }

  updateProjectTechnologies(project: Project, value: string): void {
    project.technologies = value.split(',').map((technology) => technology.trim()).filter(Boolean);
  }

  private emptyProject(): Project {
    return { id: this.nextId(this.projects), title: '', description: '', role: '', technologies: [], teamSize: '' };
  }

  private emptyExperience(): Experience {
    return { id: this.nextId(this.experiences), position: '', company: '', location: '', description: '' };
  }

  private emptyService(): Service {
    return { id: this.nextId(this.services), name: '', description: '', icon: '◆' };
  }

  private nextId(items: Array<{ id: number }>): number {
    return items.reduce((highest, item) => Math.max(highest, item.id), 0) + 1;
  }

  private errorText(error: unknown): string {
    return error instanceof Error ? error.message : 'Something went wrong. Please try again.';
  }
}
