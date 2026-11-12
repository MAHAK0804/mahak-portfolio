import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PortfolioService } from '../../services/portfolio';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.css',
})
export class ContactComponent {
  formData = {
    name: '',
    email: '',
    subject: '',
    message: '',
    website: ''
  };
  isSending = false;
  successMessage = '';
  errorMessage = '';

  constructor(private portfolioService: PortfolioService) {}

  async submitForm(): Promise<void> {
    if (this.isSending) return;
    this.isSending = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      await this.portfolioService.submitContactMessage({
        fullName: this.formData.name.trim(),
        email: this.formData.email.trim(),
        subject: this.formData.subject.trim(),
        message: this.formData.message.trim(),
        website: this.formData.website
      });
      this.resetForm();
      this.successMessage = 'Thanks for reaching out. Your message has been sent.';
    } catch (error) {
      this.errorMessage = error instanceof Error ? error.message : 'Your message could not be sent. Please try again.';
    } finally {
      this.isSending = false;
    }
  }

  resetForm() {
    this.formData = {
      name: '',
      email: '',
      subject: '',
      message: '',
      website: ''
    };
  }
}
