import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DoctorDashboardComponent } from './doctor-dashboard/doctor-dashboard.component';
import { DoctorSectionComponent } from './doctor-section/doctor-section.component';
import { MedicalRecordsComponent } from './medical-records/medical-records.component';
import { TeleconsultationComponent } from './teleconsultation/teleconsultation.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

const routes: Routes = [
  { path: '', component: DoctorDashboardComponent },
  {
    path: 'patients',
    component: DoctorSectionComponent,
    data: { section: 'patients', title: 'Mes patients' }
  },
  {
    path: 'appointments',
    component: DoctorSectionComponent,
    data: { section: 'appointments', title: 'Mon agenda' }
  },
  {
    path: 'consultations',
    component: DoctorSectionComponent,
    data: { section: 'consultations', title: 'Mes consultations' }
  },
  {
    path: 'prescriptions',
    component: DoctorSectionComponent,
    data: { section: 'prescriptions', title: 'Ordonnances' }
  },
  {
    path: 'labs',
    component: DoctorSectionComponent,
    data: { section: 'labs', title: "Resultats d'analyses" }
  },
  {
    path: 'profile',
    component: DoctorSectionComponent,
    data: { section: 'profile', title: 'Mon profil' }
  },
  { path: 'medical-records', component: MedicalRecordsComponent },
  { path: 'teleconsultation', component: TeleconsultationComponent },
  { path: 'messages', component: MessagesComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'help', component: HelpComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DoctorRoutingModule { }
