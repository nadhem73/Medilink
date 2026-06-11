import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PatientDashboardComponent } from './patient-dashboard/patient-dashboard.component';
import { PatientSectionComponent } from './patient-section/patient-section.component';
import { MedicalRecordsComponent } from './medical-records/medical-records.component';
import { TeleconsultationComponent } from './teleconsultation/teleconsultation.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { BillingComponent } from './billing/billing.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

const routes: Routes = [
  { path: '', component: PatientDashboardComponent },
  {
    path: 'appointments',
    component: PatientSectionComponent,
    data: { section: 'appointments', title: 'Mes rendez-vous' }
  },
  {
    path: 'prescriptions',
    component: PatientSectionComponent,
    data: { section: 'prescriptions', title: 'Mes ordonnances' }
  },
  {
    path: 'labs',
    component: PatientSectionComponent,
    data: { section: 'labs', title: 'Mes analyses' }
  },
  {
    path: 'profile',
    component: PatientSectionComponent,
    data: { section: 'profile', title: 'Mon profil' }
  },
  { path: 'medical-records', component: MedicalRecordsComponent },
  { path: 'teleconsultation', component: TeleconsultationComponent },
  { path: 'messages', component: MessagesComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'billing', component: BillingComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'help', component: HelpComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PatientRoutingModule { }
