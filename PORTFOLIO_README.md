# 🎯 Mahak Garg - Angular Portfolio

A professional, responsive, and modern portfolio website built with **Angular 19** showcasing Mahak Garg's work as a Full Stack Developer.

## 📋 Project Features

✨ **Fully Responsive Design** - Works seamlessly on desktop, tablet, and mobile devices
🎨 **Modern UI/UX** - Beautiful gradient backgrounds and smooth animations
📱 **Mobile-Optimized Navigation** - Hamburger menu for mobile devices
🚀 **Component-Based Architecture** - Modular and maintainable code structure
📊 **Portfolio Showcase** - 8 featured projects with detailed descriptions
💼 **Experience Timeline** - Professional work history with timeline visualization
🔧 **Services Section** - Highlight your technical expertise and services
💬 **Contact Form** - Functional contact form with email integration
🎯 **Smooth Scrolling** - Navigate smoothly between sections
📈 **SEO-Friendly** - Optimized for search engines

## 🏗️ Project Structure

```
mahak-portfolio/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── header/          # Navigation header with mobile menu
│   │   │   ├── hero/            # Hero section with intro and skills
│   │   │   ├── projects/        # Projects showcase grid
│   │   │   ├── services/        # Services/expertise cards
│   │   │   ├── experience/      # Timeline of work experience
│   │   │   ├── contact/         # Contact form section
│   │   │   └── footer/          # Footer with links
│   │   ├── services/
│   │   │   └── portfolio.ts     # Data service with portfolio content
│   │   ├── app.ts              # Main app component
│   │   ├── app.html            # App template
│   │   ├── app.css             # App styles
│   │   └── app.routes.ts       # Route configuration
│   ├── styles.css              # Global styles
│   ├── index.html              # Main HTML file
│   └── main.ts                 # Application entry point
├── angular.json                # Angular configuration
├── package.json                # Dependencies
├── tsconfig.json               # TypeScript configuration
└── README.md                   # This file
```

## 🛠️ Tech Stack

- **Framework**: Angular 19 (Standalone Components)
- **Language**: TypeScript
- **Styling**: CSS3 with modern features
- **Layout**: CSS Grid & Flexbox
- **Build Tool**: Angular CLI
- **Runtime**: Node.js

## 📦 Installation

### Prerequisites
- Node.js (v18+ recommended)
- npm or yarn
- Angular CLI (v19+)

### Setup Steps

1. **Navigate to the project directory:**
   ```bash
   cd mahak-portfolio
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   ng serve
   ```

4. **Open in browser:**
   - Navigate to `http://localhost:4200`
   - The app will automatically reload when you make changes

## 🚀 Running the Application

### Development Server
```bash
ng serve
# or with open option
ng serve --open
```

The development server runs on `http://localhost:4200`

### Production Build
```bash
ng build
# or for production-optimized build
ng build --configuration production
```

Output will be in the `dist/` directory

### Build and Deploy
```bash
ng build --configuration production
```

## 📄 Content Sections

### 1. **Hero Section**
- Welcome message with name and title
- Call-to-action buttons
- Technology stack showcase

### 2. **Projects Section**
- Grid of 8 featured projects
- Project descriptions, roles, and tech stack
- Hover effects for interactivity

### 3. **Services Section**
- 8 service offerings
- Icon-based visual representation
- Professional service descriptions

### 4. **Experience Section**
- Timeline view of work history
- 4 professional positions
- Detailed job descriptions

### 5. **Contact Section**
- Contact form with fields for:
  - Full Name
  - Email Address
  - Subject
  - Message
- Direct email link (mailto)
- Social media links

### 6. **Footer**
- Copyright information
- Quick navigation links
- Responsive design

## 🎨 Customization Guide

### Update Portfolio Content

Edit `src/app/services/portfolio.ts` to update:
- Projects data
- Experience entries
- Services offered
- Skills list

Example:
```typescript
projects: Project[] = [
  {
    id: 1,
    title: 'Your Project Title',
    description: 'Project description...',
    role: 'Your Role',
    technologies: ['Tech1', 'Tech2'],
    teamSize: 'Team Size'
  },
  // ... more projects
];
```

### Modify Styling

Global styles: `src/styles.css`
Component styles: `src/app/components/[component]/[component].css`

Color scheme:
- Primary: `#667eea`
- Secondary: `#764ba2`
- Text: `#333`
- Background: `#ffffff`

### Update Contact Information

Edit `src/app/components/contact/contact.html`:
- Email address
- Social media links

Edit `src/app/services/portfolio.ts` for social media URLs

## 🔧 Available Commands

```bash
# Development
ng serve                    # Start dev server
ng serve --open            # Start dev server and open browser

# Building
ng build                   # Build for development
ng build --configuration production  # Production build

# Testing
ng test                    # Run unit tests
ng e2e                     # Run end-to-end tests

# Code Quality
ng lint                    # Run ESLint
```

## 📱 Responsive Breakpoints

- **Desktop**: 1200px and above
- **Tablet**: 768px - 1199px
- **Mobile**: Below 768px

The portfolio adapts beautifully to all screen sizes with optimized layouts for each device type.

## 🌐 Deployment Options

### Firebase Hosting
```bash
npm install -g firebase-tools
firebase init hosting
ng build --configuration production
firebase deploy
```

### Vercel
```bash
npm install -g vercel
ng build --configuration production
vercel
```

### Netlify
```bash
ng build --configuration production
# Connect to Netlify via CLI or drag-and-drop dist folder
```

### GitHub Pages
```bash
ng build --configuration production --base-href "/repository-name/"
# Push to gh-pages branch
```

## 🎯 Performance Optimization

The portfolio includes:
- Lazy-loaded components
- Optimized CSS animations
- Minimal JavaScript bundle
- Responsive image strategies

## 📞 Contact & Social Links

- **Email**: mahaksde@gmail.com
- **GitHub**: https://github.com/MAHAK0804
- **LinkedIn**: https://www.linkedin.com/in/mahak-garg-09197523b/
- **Twitter**: https://twitter.com/MahakGa
- **Instagram**: https://www.instagram.com/mahak_asde/

## 📝 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Feel free to fork, modify, and use this portfolio template for your own projects!

## 🔄 Version History

- **v1.0.0** - Initial release with full portfolio features

## ❓ Troubleshooting

**Port 4200 already in use:**
```bash
ng serve --port 4201
```

**Clear node_modules and reinstall:**
```bash
rm -rf node_modules package-lock.json
npm install
```

**Build errors:**
```bash
ng cache clean
ng build
```

## 📚 Resources

- [Angular Documentation](https://angular.dev)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [MDN Web Docs](https://developer.mozilla.org/)
- [CSS-Tricks](https://css-tricks.com/)

---

**Built with ❤️ by Mahak Garg**
