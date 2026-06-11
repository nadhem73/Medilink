# 🏥 MediLink Tunisia - Frontend Angular

> Plateforme médicale complète pour la Tunisie

[![Angular](https://img.shields.io/badge/Angular-18-red)](https://angular.io)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)](https://www.typescriptlang.org/)
[![SCSS](https://img.shields.io/badge/SCSS-Styling-pink)](https://sass-lang.com/)

## 📖 Description

MediLink Tunisia est une plateforme médicale digitale permettant aux patients tunisiens d'accéder facilement aux services de santé : téléconsultations, pharmacie en ligne, analyses médicales et services d'urgence.

## ✨ Fonctionnalités

### ✅ Implémenté
- 🏠 **Home Page** moderne avec recherche de services
- 🔐 **Authentification** (Login/Register)
- 👨‍⚕️ **Liste des médecins** avec spécialités
- 💊 **Services médicaux** par catégorie
- ⭐ **Témoignages** de patients
- 📱 **Responsive** sur tous les appareils

### 🚧 En Développement
- 📊 Dashboards (Patient, Médecin, Pharmacie, etc.)
- 📅 Système de rendez-vous
- 💬 Téléconsultation vidéo
- 📋 Ordonnances électroniques
- 🔬 Résultats d'analyses en ligne

## 🚀 Démarrage Rapide

```bash
# Installation
npm install

# Développement
ng serve

# Ouvrir le navigateur
http://localhost:4200

# Build production
ng build --configuration production
```

📚 **Documentation complète :** [QUICK_START.md](./QUICK_START.md)

## 📁 Structure du Projet

```
src/app/
├── core/               # Services singleton (Auth, API, Storage)
├── shared/             # Composants réutilisables (Navbar, Footer)
├── layouts/            # Layouts (Main, Auth, Dashboard)
├── features/           # Modules fonctionnels (lazy loading)
│   ├── home/          # Page d'accueil ✅
│   ├── auth/          # Authentification ✅
│   ├── patient/       # Panel patient 🚧
│   ├── doctor/        # Panel médecin 🚧
│   ├── pharmacy/      # Panel pharmacie 🚧
│   ├── laboratory/    # Panel laboratoire 🚧
│   └── ambulance/     # Panel ambulance 🚧
└── assets/
    └── images/        # 41 images du template
```

## 🎨 Design System

### Couleurs
```scss
$primary: #611f69;      // Violet principal
$primary-dark: #4a1751; // Violet foncé  
$success: #6b9f36;      // Vert
$orange: #f9cd92;       // Orange accent
```

### Composants UI
- Cards avec hover effects
- Buttons avec transitions
- Badges colorés
- Avatars circulaires
- Grids responsive

## 🏗️ Architecture

### Lazy Loading
Chaque feature module est chargé à la demande pour optimiser les performances.

### State Management
- Services avec RxJS pour l'état global
- BehaviorSubjects pour les données réactives

### Sécurité
- **AuthGuard** : Protection des routes
- **RoleGuard** : Contrôle d'accès par rôle
- **AuthInterceptor** : Injection automatique du JWT
- **ErrorInterceptor** : Gestion centralisée des erreurs

## 🔌 Backend Integration

### API Gateway
```
http://localhost:8765/api
```

### Services Backend
- **Auth Service** (8081) - Authentification
- **Patient Service** (8082) - Gestion patients
- **Doctor Service** (8083) - Gestion médecins
- **Pharmacy Service** (8084) - Pharmacie
- **Laboratory Service** (8085) - Laboratoire
- **Ambulance Service** (8086) - Urgences
- **Teleconsultation Service** (8087) - Vidéo
- **Prescription Service** (8088) - Ordonnances

## 📱 Responsive Design

### Breakpoints
- **Mobile** : < 768px
- **Tablet** : 768px - 1024px
- **Desktop** : > 1024px

## 🌍 Internationalisation

Support prévu pour :
- 🇫🇷 Français (par défaut)
- 🇹🇳 Arabe (RTL)
- 🇬🇧 Anglais

## 🧪 Tests

```bash
# Tests unitaires
ng test

# Tests E2E
ng e2e

# Coverage
ng test --code-coverage
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Architecture détaillée |
| [QUICK_START.md](./QUICK_START.md) | Guide de démarrage |
| [TODO.md](./TODO.md) | Tâches à faire |
| [TEMPLATE_MIGRATION_COMPLETE.md](./TEMPLATE_MIGRATION_COMPLETE.md) | État de la migration |

## 🛠️ Technologies

- **Framework** : Angular 18
- **Language** : TypeScript 5
- **Styling** : SCSS
- **HTTP** : HttpClient
- **Routing** : Angular Router
- **Forms** : Reactive Forms
- **State** : RxJS

## 👥 Rôles Utilisateurs

- 👤 **PATIENT** - Accès aux services médicaux
- 👨‍⚕️ **DOCTOR** - Gestion des consultations
- 💊 **PHARMACIST** - Gestion pharmacie
- 🔬 **LABORATORY** - Gestion laboratoire
- 🚑 **AMBULANCE** - Gestion urgences
- 👑 **ADMIN** - Administration complète

## 🎯 Roadmap

### Phase 1 - MVP (En cours)
- [x] Home page
- [x] Authentication
- [ ] Patient dashboard
- [ ] Booking system

### Phase 2 - Core Features
- [ ] Teleconsultation
- [ ] Prescriptions
- [ ] Laboratory results
- [ ] Pharmacy orders

### Phase 3 - Advanced
- [ ] AI recommendations
- [ ] Mobile app (React Native)
- [ ] Analytics dashboard
- [ ] Multi-language

## 🤝 Contribution

Ce projet est privé et destiné à un usage interne.

## 📄 License

Propriétaire - Tous droits réservés © 2024 MediLink Tunisia

## 📞 Contact

Pour toute question concernant le projet, contactez l'équipe de développement.

---

**Fait avec ❤️ pour améliorer l'accès aux soins de santé en Tunisie**
