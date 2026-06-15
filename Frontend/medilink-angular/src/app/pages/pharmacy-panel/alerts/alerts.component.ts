import { Component } from '@angular/core';

@Component({
  selector: 'app-pharmacy-alerts',
  templateUrl: './alerts.component.html',
  styleUrls: ['./alerts.component.scss']
})
export class AlertsComponent {
  // Alertes automatiques de stock (cahier des charges, module 6.4)
  summary = [
    { label: 'Ruptures critiques', value: '2' },
    { label: 'Sous le seuil', value: '5' },
    { label: 'Peremptions < 30 j', value: '3' }
  ];

  ruptures = [
    { name: 'Insuline Lantus', detail: 'Stock : 4 stylos - seuil : 15', status: 'Critique' },
    { name: 'Amoxicilline 1 g', detail: 'Stock : 12 boites - seuil : 30', status: 'Critique' }
  ];

  thresholds = [
    { name: 'Paracetamol 1 g', detail: 'Stock : 8 boites - seuil : 25', status: 'A commander' },
    { name: 'Ventoline', detail: 'Stock : 18 unites - seuil : 25', status: 'A commander' },
    { name: 'Smecta', detail: 'Stock : 20 boites - seuil : 30', status: 'A surveiller' }
  ];

  expirations = [
    { name: 'Augmentin sirop', meta: 'Lot 2024-A18 - peremption 28 juin 2026' },
    { name: 'Doliprane pediatrique', meta: 'Lot 2024-C07 - peremption 02 juillet 2026' },
    { name: 'Spasfon', meta: 'Lot 2023-F12 - peremption 10 juillet 2026' }
  ];
}
