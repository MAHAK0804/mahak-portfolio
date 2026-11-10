import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { ContactComponent } from './components/contact/contact';
import { ExperienceComponent } from './components/experience/experience';
import { FooterComponent } from './components/footer/footer';
import { HeaderComponent } from './components/header/header';
import { HeroComponent } from './components/hero/hero';
import { ProjectsComponent } from './components/projects/projects';
import { ServicesComponent } from './components/services/services';
import { PortfolioService } from './services/portfolio';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    HeaderComponent,
    HeroComponent,
    ProjectsComponent,
    ServicesComponent,
    ExperienceComponent,
    ContactComponent,
    FooterComponent
  ],
  templateUrl: './home.html',
})
export class HomeComponent implements AfterViewInit, OnDestroy {
  private observer?: IntersectionObserver;

  constructor(private portfolioService: PortfolioService) {}

  ngAfterViewInit(): void {
    if (this.portfolioService.isBrowser()) {
      void this.portfolioService.loadSharedContent().catch((error: unknown) => {
        console.error('Unable to load shared portfolio content.', error);
      });
    }

    if (typeof document === 'undefined') return;

    const revealItems = document.querySelectorAll<HTMLElement>('[data-reveal]');
    if (typeof IntersectionObserver === 'undefined') {
      revealItems.forEach((item) => item.classList.add('scroll-reveal', 'is-visible'));
      return;
    }

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        entry.target.classList.toggle('is-visible', entry.isIntersecting);
      });
    }, { threshold: 0.14, rootMargin: '0px 0px -8% 0px' });

    revealItems.forEach((item) => {
      item.classList.add('scroll-reveal');
      this.observer?.observe(item);
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
