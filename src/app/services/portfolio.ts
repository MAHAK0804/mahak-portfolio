import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import type { SupabaseClient } from '@supabase/supabase-js';
import { SUPABASE_ADMIN_USER_ID, SUPABASE_ANON_KEY, SUPABASE_URL } from './supabase.config';

export interface Project {
  id: number;
  title: string;
  description: string;
  role: string;
  technologies: string[];
  teamSize: string;
}

export interface Experience {
  id: number;
  position: string;
  company: string;
  location: string;
  description: string;
}

export interface Service {
  id: number;
  name: string;
  description: string;
  icon: string;
}

export interface PortfolioContent {
  projects: Project[];
  experiences: Experience[];
  services: Service[];
  skills: string[];
}

export interface ContactReply {
  id: string;
  body: string;
  created_at: string;
  email_sent_at: string | null;
}

export interface ContactMessage {
  id: string;
  full_name: string;
  email: string;
  subject: string;
  message: string;
  created_at: string;
  is_read: boolean;
  contact_message_replies: ContactReply[];
}

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  readonly contentRevision = signal(0);
  private clientPromise?: Promise<SupabaseClient>;
  private readonly defaultContent: PortfolioContent;

  projects: Project[] = [
    {
      id: 1,
      title: 'Game Onn',
      description: 'A venue booking platform that allows players to book sports venues while enabling venue hosts to list, manage, and schedule availability.',
      role: 'Full Stack Developer',
      technologies: ['NextJS', 'Redux', 'Regex', 'MongoDB', 'Tailwind CSS', 'TypeScript'],
      teamSize: '5–6'
    },
    {
      id: 2,
      title: 'Music Application',
      description: 'A mobile app for streaming music, featuring categorized browsing, searching, audio playback, and a smooth cross-platform UI.',
      role: 'Full Stack Developer',
      technologies: ['React Native', 'NextJS', 'Redux', 'MongoDB', 'Tailwind CSS', 'TypeScript'],
      teamSize: '10–12'
    },
    {
      id: 3,
      title: 'Apna Pump',
      description: 'An on-demand fuel delivery app allowing drivers to complete and manage deliveries in real time via an intuitive mobile interface.',
      role: 'Full Stack Developer',
      technologies: ['NextJS', 'Redux', 'Regex', 'MongoDB', 'Tailwind CSS', 'TypeScript'],
      teamSize: '10–12'
    },
    {
      id: 4,
      title: 'Matka App',
      description: 'A mobile gaming app for number-based satta betting. Users can bet, check results, and manage their history with a secure interface.',
      role: 'Full Stack Developer',
      technologies: ['React Native', 'NextJS', 'PostgreSQL', 'Redux', 'Tailwind CSS', 'TypeScript'],
      teamSize: '8–9'
    },
    {
      id: 5,
      title: 'Food Recipe App',
      description: 'A cross-platform mobile app featuring detailed recipes, smart grocery list, cooking timer with notifications, nutrition info, multi-language support, subscriptions, and premium features.',
      role: 'React Native Developer',
      technologies: ['React Native', 'Expo', 'Firebase', 'Redux Toolkit', 'AsyncStorage'],
      teamSize: 'SOLO'
    },
    {
      id: 6,
      title: 'Hindi Shayari App',
      description: 'A mobile app offering a collection of categorized Hindi Shayari with options to mark favorites, share content, multi-language support, and a custom settings section.',
      role: 'React Native Developer',
      technologies: ['React Native', 'Expo', 'AsyncStorage', 'React Navigation'],
      teamSize: 'SOLO'
    },
    {
      id: 7,
      title: 'Education Management System',
      description: 'A full-stack web application designed for managing student records, attendance, assignments, and exam results. Includes search, filter, sort, pagination, and CRUD functionality.',
      role: 'Full Stack Developer',
      technologies: ['React', 'Node.js', 'Express.js', 'MongoDB', 'Axios'],
      teamSize: 'SOLO'
    },
    {
      id: 8,
      title: 'Chat Application',
      description: 'A simple real-time chat application that allows users to send and receive messages with authentication and basic UI.',
      role: 'Web Developer',
      technologies: ['React', 'Node.js', 'Socket.io', 'MongoDB'],
      teamSize: 'SOLO'
    }
  ];

  experiences: Experience[] = [
    {
      id: 1,
      position: 'Mobile Application Developer',
      company: 'Staveo Tech Pvt. Ltd.',
      location: 'New Delhi',
      description: 'Working as a full-time Mobile App Developer, building high-quality Android and iOS applications using React Native and Expo. Responsible for implementing core features, integrating Firebase, managing state with Redux Toolkit, and optimizing performance. Also involved in publishing apps to the Play Store and maintaining production builds.'
    },
    {
      id: 2,
      position: 'Associate Software Development Engineer Intern',
      company: 'Geeks2Connect Infotech Pvt Ltd',
      location: 'Jaipur',
      description: 'Joined as an ASDE Intern and contributed as a Full Stack Developer and App Developer. Worked on various web and mobile application projects using React, React Native, Expo, Express.js, MongoDB, and Firebase. Gained experience in individually handling projects, designing UI, managing APIs, integrating third-party services like payment gateways, and deploying apps.'
    },
    {
      id: 3,
      position: 'Node.js Developer Intern',
      company: 'Celebal Technologies',
      location: 'Jaipur',
      description: 'Worked as a backend developer using Node.js and Express. Built REST APIs and collaborated with frontend teams to integrate features. Gained hands-on experience with MongoDB, authentication mechanisms, and project structure.'
    },
    {
      id: 4,
      position: 'MERN Stack Developer',
      company: 'Learn and Build',
      location: 'Remote',
      description: 'Developed full-stack web applications using the MERN stack. Built interactive UIs with React, implemented APIs in Node.js/Express, and used MongoDB for database operations. Enhanced skills in deployment, project architecture, and version control.'
    }
  ];

  services: Service[] = [
    {
      id: 1,
      name: 'JavaScript Development',
      description: 'Creating dynamic and interactive web applications using JavaScript.',
      icon: '⚙️'
    },
    {
      id: 2,
      name: 'React.js Development',
      description: 'Building modern and responsive user interfaces with React.js.',
      icon: '⚛️'
    },
    {
      id: 3,
      name: 'Node.js Backend',
      description: 'Developing scalable server-side applications using Node.js.',
      icon: '🚀'
    },
    {
      id: 4,
      name: 'Next.js Development',
      description: 'Creating server-rendered React applications with Next.js.',
      icon: '▲'
    },
    {
      id: 5,
      name: 'TypeScript Development',
      description: 'Ensuring robust and maintainable code with TypeScript.',
      icon: '📘'
    },
    {
      id: 6,
      name: 'Tailwind CSS Styling',
      description: 'Designing beautiful and responsive interfaces with Tailwind CSS.',
      icon: '🎨'
    },
    {
      id: 7,
      name: 'Mobile App Development',
      description: 'Designing beautiful and responsive interfaces for mobile view with React Native.',
      icon: '📱'
    },
    {
      id: 8,
      name: 'Backend Development',
      description: 'Designing and implementing database schemas, queries, and integrations using MongoDB.',
      icon: '🗄️'
    }
  ];

  skills = [
    'JavaScript', 'TypeScript', 'React.js', 'Next.js', 'Node.js',
    'Express.js', 'React Native', 'Expo', 'Socket.io', 'MongoDB',
    'PostgreSQL', 'Firebase', 'Redux', 'Tailwind CSS', 'HTML', 'CSS'
  ];

  constructor(@Inject(PLATFORM_ID) private platformId: object) {
    this.defaultContent = structuredClone(this.getContent());
  }

  getProjects(): Project[] {
    return this.projects;
  }

  getExperiences(): Experience[] {
    return this.experiences;
  }

  getServices(): Service[] {
    return this.services;
  }

  getSkills(): string[] {
    return this.skills;
  }

  isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  isSharedStorageConfigured(): boolean {
    return SUPABASE_URL.startsWith('https://')
      && !SUPABASE_URL.includes('YOUR_')
      && !SUPABASE_ANON_KEY.includes('YOUR_');
  }

  isAdminConfigured(): boolean {
    return this.isSharedStorageConfigured()
      && SUPABASE_ADMIN_USER_ID.length > 0
      && !SUPABASE_ADMIN_USER_ID.includes('YOUR_');
  }

  async loadSharedContent(): Promise<void> {
    if (!this.isBrowser() || !this.isSharedStorageConfigured()) return;

    const client = await this.getClient();
    const { data, error } = await client
      .from('portfolio_content')
      .select('content')
      .eq('id', 1)
      .maybeSingle();

    if (error) throw error;
    if (data?.content) this.applyContent(data.content as PortfolioContent);
  }

  async getAdminSession(): Promise<boolean> {
    if (!this.isBrowser() || !this.isAdminConfigured()) return false;

    const client = await this.getClient();
    const { data, error } = await client.auth.getUser();
    if (error || !data.user) return false;
    if (data.user.id === SUPABASE_ADMIN_USER_ID) return true;

    await client.auth.signOut();
    return false;
  }

  async signIn(email: string, password: string): Promise<void> {
    if (!this.isAdminConfigured()) throw new Error('Supabase is not configured for owner access.');

    const client = await this.getClient();
    const { data, error } = await client.auth.signInWithPassword({ email, password });
    if (error) throw error;

    if (data.user?.id !== SUPABASE_ADMIN_USER_ID) {
      await client.auth.signOut();
      throw new Error('This account does not have portfolio editor access.');
    }
  }

  async signOut(): Promise<void> {
    const client = await this.getClient();
    const { error } = await client.auth.signOut();
    if (error) throw error;
  }

  async saveContent(): Promise<number> {
    if (!this.isAdminConfigured()) throw new Error('Supabase is not configured for owner access.');

    const client = await this.getClient();
    const { data, error: sessionError } = await client.auth.getUser();
    if (sessionError || data.user?.id !== SUPABASE_ADMIN_USER_ID) {
      throw new Error('Sign in with the portfolio owner account before saving.');
    }

    const content = structuredClone(this.getContent());
    const { data: savedRow, error } = await client
      .from('portfolio_content')
      .upsert({
        id: 1,
        owner_id: SUPABASE_ADMIN_USER_ID,
        content,
        updated_at: new Date().toISOString()
      }, { onConflict: 'id' })
      .select('content')
      .single();

    if (error) throw error;
    const savedExperiences = savedRow.content?.experiences;
    if (!Array.isArray(savedExperiences) || savedExperiences.length !== content.experiences.length) {
      throw new Error('Supabase did not confirm the full experience list. Check the table permissions and try again.');
    }

    return savedExperiences.length;
  }

  async resetContent(): Promise<void> {
    this.applyContent(structuredClone(this.defaultContent));
    await this.saveContent();
  }

  async submitContactMessage(input: {
    fullName: string;
    email: string;
    subject: string;
    message: string;
    website: string;
  }): Promise<void> {
    const client = await this.getClient();
    const { error } = await client.functions.invoke('contact-message', {
      body: input
    });
    if (error) throw error;
  }

  async loadContactMessages(): Promise<ContactMessage[]> {
    const client = await this.getClient();
    await this.requireAdminUser(client);

    const { data, error } = await client
      .from('contact_messages')
      .select('id, full_name, email, subject, message, created_at, is_read, contact_message_replies(id, body, created_at, email_sent_at)')
      .order('created_at', { ascending: false })
      .order('created_at', { ascending: true, referencedTable: 'contact_message_replies' });

    if (error) throw error;
    return data as ContactMessage[];
  }

  async markContactMessageRead(messageId: string): Promise<void> {
    const client = await this.getClient();
    await this.requireAdminUser(client);
    const { error } = await client
      .from('contact_messages')
      .update({ is_read: true })
      .eq('id', messageId);
    if (error) throw error;
  }

  async replyToContactMessage(messageId: string, body: string): Promise<void> {
    const client = await this.getClient();
    await this.requireAdminUser(client);

    const { data, error } = await client
      .from('contact_message_replies')
      .insert({ message_id: messageId, body: body.trim() })
      .select('id')
      .single();
    if (error) throw error;

    const { error: sendError } = await client.functions.invoke('send-contact-reply', {
      body: { replyId: data.id }
    });
    if (sendError) {
      throw new Error(`Reply saved, but its email could not be sent: ${sendError.message}`);
    }
  }

  async retryContactReplyEmail(replyId: string): Promise<void> {
    const client = await this.getClient();
    await this.requireAdminUser(client);
    const { error } = await client.functions.invoke('send-contact-reply', {
      body: { replyId }
    });
    if (error) throw error;
  }

  private async requireAdminUser(client: SupabaseClient): Promise<void> {
    const { data, error } = await client.auth.getUser();
    if (error || data.user?.id !== SUPABASE_ADMIN_USER_ID) {
      throw new Error('Sign in with the portfolio owner account to access messages.');
    }
  }

  private getContent(): PortfolioContent {
    return {
      projects: this.projects,
      experiences: this.experiences,
      services: this.services,
      skills: this.skills
    };
  }

  private applyContent(content: PortfolioContent): void {
    this.projects.splice(0, this.projects.length, ...(content.projects ?? []));
    this.experiences.splice(0, this.experiences.length, ...(content.experiences ?? []));
    this.services.splice(0, this.services.length, ...(content.services ?? []));
    this.skills.splice(0, this.skills.length, ...(content.skills ?? []));
    this.contentRevision.update((revision) => revision + 1);
  }

  private getClient(): Promise<SupabaseClient> {
    if (!this.clientPromise) {
      const browser = this.isBrowser();
      this.clientPromise = import('@supabase/supabase-js').then(({ createClient }) =>
        createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
          auth: {
            autoRefreshToken: browser,
            persistSession: browser,
            detectSessionInUrl: browser
          }
        })
      );
    }

    return this.clientPromise;
  }
}
