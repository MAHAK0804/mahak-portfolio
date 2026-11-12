import { Component, OnDestroy, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { PortfolioService } from '../../services/portfolio';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.css',
})
export class ContactComponent implements OnDestroy {
  @ViewChild('contactNgForm') private contactNgForm?: NgForm;

  formData = {
    name: '',
    email: '',
    subject: '',
    message: '',
    website: ''
  };
  isSending = signal(false);
  successToast = signal('');
  errorMessage = signal('');
  private successToastTimer?: ReturnType<typeof setTimeout>;

  constructor(private portfolioService: PortfolioService) {}

  ngOnDestroy(): void {
    if (this.successToastTimer) clearTimeout(this.successToastTimer);
  }

  async submitForm(): Promise<void> {
    if (this.isSending()) return;
    this.isSending.set(true);
    this.errorMessage.set('');

    try {
      await this.portfolioService.submitContactMessage({
        fullName: this.formData.name.trim(),
        email: this.formData.email.trim(),
        subject: this.formData.subject.trim(),
        message: this.formData.message.trim(),
        website: this.formData.website
      });
      this.resetForm();
      this.showSuccessToast();
    } catch (error) {
      this.errorMessage.set(error instanceof Error ? error.message : 'Your message could not be sent. Please try again.');
    } finally {
      this.isSending.set(false);
    }
  }

  private showSuccessToast(): void {
    if (this.successToastTimer) clearTimeout(this.successToastTimer);
    this.successToast.set('Thank you for reaching out. Your message has been received. I’ll get back to you as soon as possible.');
    this.successToastTimer = setTimeout(() => {
      this.successToast.set('');
      this.successToastTimer = undefined;
    }, 6000);
  }

  private resetForm(): void {
    this.formData = {
      name: '',
      email: '',
      subject: '',
      message: '',
      website: ''
    };
    this.contactNgForm?.resetForm(this.formData);
  }
}
