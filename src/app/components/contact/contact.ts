import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
    message: ''
  };

  submitForm() {
    if (this.formData.name && this.formData.email && this.formData.message) {
      const mailtoLink = `mailto:mahaksde@gmail.com?subject=${encodeURIComponent(this.formData.subject)}&body=${encodeURIComponent(`Name: ${this.formData.name}\nEmail: ${this.formData.email}\n\n${this.formData.message}`)}`;
      window.location.href = mailtoLink;
      this.resetForm();
    }
  }

  resetForm() {
    this.formData = {
      name: '',
      email: '',
      subject: '',
      message: ''
    };
  }
}
