import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

type PharmacySectionKey = 'prescriptions' | 'stock' | 'orders' | 'sales';

@Component({
  selector: 'app-pharmacy-section',
  templateUrl: './pharmacy-section.component.html',
  styleUrls: ['./pharmacy-section.component.scss']
})
export class PharmacySectionComponent implements OnInit {
  section: PharmacySectionKey = 'prescriptions';
  title = '';

  prescriptions = [
    {
      patient: 'Mohamed Khelifi',
      doctor: 'Dr. Yasmine Ben Salem',
      date: 'Aujourd hui 08:45',
      items: 'Amlodipine, Aspirine, Omeprazole',
      status: 'A preparer'
    },
    {
      patient: 'Salma Aouini',
      doctor: 'Dr. Mehdi Trabelsi',
      date: 'Aujourd hui 09:20',
      items: 'Creme dermique',
      status: 'En attente'
    },
    {
      patient: 'Karim Bouzid',
      doctor: 'Dr. Ines Gharbi',
      date: 'Aujourd hui 10:05',
      items: 'Amoxicilline, Paracetamol',
      status: 'Verification stock'
    }
  ];

  stock = [
    {
      medication: 'Amoxicilline 1 g',
      category: 'Antibiotique',
      quantity: '12 boites',
      threshold: 'Seuil : 30 boites',
      status: 'Critique'
    },
    {
      medication: 'Paracetamol 1 g',
      category: 'Antalgique',
      quantity: '8 boites',
      threshold: 'Seuil : 25 boites',
      status: 'A commander'
    },
    {
      medication: 'Doliprane 500 mg',
      category: 'Antalgique',
      quantity: '210 boites',
      threshold: 'Seuil : 50 boites',
      status: 'Suffisant'
    }
  ];

  orders = [
    {
      reference: 'CMD-2026-031',
      supplier: 'MediPharm Tunisie',
      date: 'Passee le 09 juin 2026',
      items: '40 references',
      status: 'En cours'
    },
    {
      reference: 'CMD-2026-030',
      supplier: 'Laboratoires Galiens',
      date: 'Passee le 06 juin 2026',
      items: '15 references',
      status: 'Livree'
    },
    {
      reference: 'CMD-2026-029',
      supplier: 'Distri-Sante Lac 2',
      date: 'Passee le 04 juin 2026',
      items: '8 references',
      status: 'A valider'
    }
  ];

  sales = [
    {
      reference: 'VTE-2026-187',
      detail: 'Dispensation ordonnance - Mohamed Khelifi',
      date: 'Aujourd hui 11:10',
      amount: '34 DT',
      status: 'Delivree'
    },
    {
      reference: 'VTE-2026-186',
      detail: 'Vente libre - Parapharmacie',
      date: 'Aujourd hui 10:32',
      amount: '18 DT',
      status: 'Delivree'
    },
    {
      reference: 'VTE-2026-185',
      detail: 'Dispensation ordonnance - Salma Aouini',
      date: 'Hier 17:48',
      amount: '52 DT',
      status: 'Delivree'
    }
  ];

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] as PharmacySectionKey;
      this.title = data['title'] as string;
    });
  }
}
