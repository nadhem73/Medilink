import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PharmacyDashboardComponent } from './pharmacy-dashboard/pharmacy-dashboard.component';
import { PharmacySectionComponent } from './pharmacy-section/pharmacy-section.component';
import { AlertsComponent } from './alerts/alerts.component';
import { ForecastComponent } from './forecast/forecast.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

const routes: Routes = [
  { path: '', component: PharmacyDashboardComponent },
  {
    path: 'prescriptions',
    component: PharmacySectionComponent,
    data: { section: 'prescriptions', title: 'Ordonnances recues' }
  },
  {
    path: 'stock',
    component: PharmacySectionComponent,
    data: { section: 'stock', title: 'Stock des medicaments' }
  },
  {
    path: 'orders',
    component: PharmacySectionComponent,
    data: { section: 'orders', title: 'Commandes fournisseurs' }
  },
  {
    path: 'sales',
    component: PharmacySectionComponent,
    data: { section: 'sales', title: 'Ventes et dispensation' }
  },
  { path: 'alerts', component: AlertsComponent },
  { path: 'forecast', component: ForecastComponent },
  { path: 'messages', component: MessagesComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'help', component: HelpComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PharmacyRoutingModule { }
