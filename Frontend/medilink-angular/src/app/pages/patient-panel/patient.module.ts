import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PatientRoutingModule } from './patient-routing.module';
import { PatientDashboardComponent } from './patient-dashboard/patient-dashboard.component';
import { PatientSectionComponent } from './patient-section/patient-section.component';
import { MedicalRecordsComponent } from './medical-records/medical-records.component';
import { TeleconsultationComponent } from './teleconsultation/teleconsultation.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { BillingComponent } from './billing/billing.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';


@NgModule({
  declarations: [
    PatientDashboardComponent,
    PatientSectionComponent,
    MedicalRecordsComponent,
    TeleconsultationComponent,
    MessagesComponent,
    NotificationsComponent,
    BillingComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    PatientRoutingModule
  ]
})
export class PatientModule { }
