import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-billing',
  templateUrl: './billing.component.html',
  styleUrls: ['./billing.component.scss']
})
export class BillingComponent {
  summary = [
    { label: 'Total paye (2026)', value: '420 DT' },
    { label: 'En attente', value: '85 DT' },
    { label: 'Factures', value: '6' }
  ];

  invoices = [
    { ref: 'FACT-2026-014', label: 'Consultation cardiologie', date: '02 juin 2026', amount: '60 DT', status: 'Payee' },
    { ref: 'FACT-2026-013', label: 'Analyses - Bilan lipidique', date: '08 mai 2026', amount: '45 DT', status: 'Payee' },
    { ref: 'FACT-2026-012', label: 'Teleconsultation generale', date: '21 mai 2026', amount: '40 DT', status: 'En attente' },
    { ref: 'FACT-2026-011', label: 'Consultation dermatologie', date: '02 mai 2026', amount: '55 DT', status: 'Payee' }
  ];
}
