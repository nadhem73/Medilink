import { Component, OnInit } from '@angular/core';

interface Service {
  title: string;
  category: string;
  badge: string;
  image: string;
  sessions: number;
  users: number;
  rating: number;
  type: string;
}

interface Doctor {
  name: string;
  specialty: string;
  image: string;
}

interface Testimonial {
  name: string;
  role: string;
  avatar: string;
  text: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  selectedCategory = 'teleconsultation';

  services: Service[] = [
    {
      title: 'Consultation en ligne avec un médecin généraliste',
      category: 'Téléconsultation',
      badge: 'GRATUIT',
      image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=600&h=400&fit=crop',
      sessions: 24,
      users: 150,
      rating: 4.8,
      type: 'teleconsultation'
    },
    {
      title: 'Consultation spécialisée en cardiologie',
      category: 'Téléconsultation',
      badge: '30 DT',
      image: 'https://images.unsplash.com/photo-1628348068343-c6a848d2b6dd?w=600&h=400&fit=crop',
      sessions: 15,
      users: 89,
      rating: 4.9,
      type: 'teleconsultation'
    },
    {
      title: 'Suivi psychologique à distance',
      category: 'Téléconsultation',
      badge: '50 DT',
      image: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600&h=400&fit=crop',
      sessions: 12,
      users: 120,
      rating: 4.7,
      type: 'teleconsultation'
    },
    {
      title: 'Consultation pédiatrique en ligne',
      category: 'Téléconsultation',
      badge: '35 DT',
      image: 'https://images.unsplash.com/photo-1631217868264-e5b90bb7e133?w=600&h=400&fit=crop',
      sessions: 18,
      users: 95,
      rating: 4.8,
      type: 'teleconsultation'
    },
    {
      title: 'Commande de médicaments avec ordonnance',
      category: 'Pharmacie',
      badge: 'Livraison',
      image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?w=600&h=400&fit=crop',
      sessions: 0,
      users: 500,
      rating: 4.6,
      type: 'pharmacie'
    },
    {
      title: 'Médicaments sans ordonnance',
      category: 'Pharmacie',
      badge: '24/7',
      image: 'https://images.unsplash.com/photo-1587854692152-cbe660dbde88?w=600&h=400&fit=crop',
      sessions: 0,
      users: 350,
      rating: 4.5,
      type: 'pharmacie'
    },
    {
      title: 'Produits de parapharmacie',
      category: 'Pharmacie',
      badge: 'Promo',
      image: 'https://images.unsplash.com/photo-1556742502-ec7c0e9f34b1?w=600&h=400&fit=crop',
      sessions: 0,
      users: 280,
      rating: 4.4,
      type: 'pharmacie'
    },
    {
      title: 'Consultation pharmaceutique',
      category: 'Pharmacie',
      badge: 'GRATUIT',
      image: 'https://images.unsplash.com/photo-1631549916768-4119b2e5f926?w=600&h=400&fit=crop',
      sessions: 10,
      users: 120,
      rating: 4.7,
      type: 'pharmacie'
    },
    {
      title: 'Analyses sanguines complètes',
      category: 'Laboratoire',
      badge: '80 DT',
      image: 'https://images.unsplash.com/photo-1579154204845-3d33f2d70165?w=600&h=400&fit=crop',
      sessions: 0,
      users: 200,
      rating: 4.9,
      type: 'laboratoire'
    },
    {
      title: 'Test COVID-19 PCR',
      category: 'Laboratoire',
      badge: '120 DT',
      image: 'https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?w=600&h=400&fit=crop',
      sessions: 0,
      users: 450,
      rating: 4.8,
      type: 'laboratoire'
    },
    {
      title: 'Bilan hormonal',
      category: 'Laboratoire',
      badge: '150 DT',
      image: 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=600&h=400&fit=crop',
      sessions: 0,
      users: 180,
      rating: 4.7,
      type: 'laboratoire'
    },
    {
      title: 'Analyses d\'urine',
      category: 'Laboratoire',
      badge: '40 DT',
      image: 'https://images.unsplash.com/photo-1581093458791-9d42e3c2c1f1?w=600&h=400&fit=crop',
      sessions: 0,
      users: 220,
      rating: 4.6,
      type: 'laboratoire'
    },
    {
      title: 'Ambulance équipée 24/7',
      category: 'Urgence',
      badge: '24/7',
      image: 'https://images.unsplash.com/photo-1587745416684-47953f16f02f?w=600&h=400&fit=crop',
      sessions: 0,
      users: 1000,
      rating: 4.9,
      type: 'urgence'
    },
    {
      title: 'Transport médicalisé',
      category: 'Urgence',
      badge: 'Rapide',
      image: 'https://images.unsplash.com/photo-1504439904031-93ded9f93e4e?w=600&h=400&fit=crop',
      sessions: 0,
      users: 800,
      rating: 4.8,
      type: 'urgence'
    },
    {
      title: 'Intervention d\'urgence',
      category: 'Urgence',
      badge: 'Priorité',
      image: 'https://images.unsplash.com/photo-1614935151651-0bea6508db6b?w=600&h=400&fit=crop',
      sessions: 0,
      users: 950,
      rating: 4.9,
      type: 'urgence'
    },
    {
      title: 'Assistance médicale à domicile',
      category: 'Urgence',
      badge: 'Express',
      image: 'https://images.unsplash.com/photo-1516549655169-df83a0774514?w=600&h=400&fit=crop',
      sessions: 0,
      users: 600,
      rating: 4.7,
      type: 'urgence'
    }
  ];

  doctors: Doctor[] = [
    {
      name: 'Dr. Sami Ben Ahmed',
      specialty: 'Cardiologue',
      image: 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=500&h=600&fit=crop'
    },
    {
      name: 'Dr. Leila Trabelsi',
      specialty: 'Dermatologue',
      image: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=500&h=600&fit=crop'
    },
    {
      name: 'Dr. Mohamed Zarrouk',
      specialty: 'Pédiatre',
      image: 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=500&h=600&fit=crop'
    },
    {
      name: 'Dr. Amira Khiari',
      specialty: 'Gynécologue',
      image: 'https://images.unsplash.com/photo-1594824476967-48c8b964273f?w=500&h=600&fit=crop'
    },
    {
      name: 'Dr. Karim Mansour',
      specialty: 'Neurologue',
      image: 'https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=500&h=600&fit=crop'
    },
    {
      name: 'Dr. Salma Bouazizi',
      specialty: 'Médecin généraliste',
      image: 'https://images.unsplash.com/photo-1527613426441-4da17471b66d?w=500&h=600&fit=crop'
    }
  ];

  testimonials: Testimonial[] = [
    {
      name: 'Ahmed Benali',
      role: 'Patient',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=faces',
      text: 'Service exceptionnel! J\'ai pu consulter un médecin rapidement sans me déplacer. La téléconsultation est vraiment pratique.'
    },
    {
      name: 'Fatma Touati',
      role: 'Patiente',
      avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop&crop=faces',
      text: 'La plateforme est très facile à utiliser. J\'ai reçu mes médicaments le jour même. Excellent service de pharmacie en ligne!'
    },
    {
      name: 'Youssef Jebali',
      role: 'Patient',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=faces',
      text: 'Les résultats de mes analyses étaient disponibles en ligne rapidement. Le laboratoire est très professionnel.'
    }
  ];

  filteredServices: Service[] = [];

  ngOnInit(): void {
    this.filterServices();
  }

  selectCategory(category: string): void {
    this.selectedCategory = category;
    this.filterServices();
  }

  filterServices(): void {
    this.filteredServices = this.services.filter(
      service => service.type === this.selectedCategory
    );
  }
}

