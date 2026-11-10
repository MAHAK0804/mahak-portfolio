import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  siCss,
  siExpo,
  siExpress,
  siFirebase,
  siHtml5,
  siJavascript,
  siMongodb,
  siNextdotjs,
  siNodedotjs,
  siPostgresql,
  siReact,
  siRedux,
  siSocketdotio,
  siTailwindcss,
  siTypescript,
  type SimpleIcon
} from 'simple-icons';
import { PortfolioService } from '../../services/portfolio';

const technologyIcons: Record<string, SimpleIcon> = {
  javascript: siJavascript,
  typescript: siTypescript,
  react: siReact,
  reactjs: siReact,
  reactnative: siReact,
  nextjs: siNextdotjs,
  nodejs: siNodedotjs,
  expressjs: siExpress,
  expo: siExpo,
  socketio: siSocketdotio,
  mongodb: siMongodb,
  postgresql: siPostgresql,
  firebase: siFirebase,
  redux: siRedux,
  tailwindcss: siTailwindcss,
  html: siHtml5,
  css: siCss
};

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hero.html',
  styleUrl: './hero.css',
})
export class HeroComponent {
  pointerX = 50;
  pointerY = 50;
  skills = computed<string[]>(() => {
    this.portfolioService.contentRevision();
    return this.portfolioService.getSkills().slice();
  });

  constructor(private portfolioService: PortfolioService) {}

  getTechnologyIcon(technology: string): SimpleIcon | undefined {
    const key = technology.toLowerCase().replace(/[^a-z0-9]/g, '');
    return technologyIcons[key];
  }

  handlePointerMove(event: MouseEvent) {
    const target = event.currentTarget as HTMLElement;
    const bounds = target.getBoundingClientRect();
    this.pointerX = ((event.clientX - bounds.left) / bounds.width) * 100;
    this.pointerY = ((event.clientY - bounds.top) / bounds.height) * 100;
  }

  resetPointer() {
    this.pointerX = 50;
    this.pointerY = 50;
  }

  scrollToSection(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
