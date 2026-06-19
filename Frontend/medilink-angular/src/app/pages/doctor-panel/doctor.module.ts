import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DoctorRoutingModule } from './doctor-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { DoctorDashboardComponent } from './doctor-dashboard/doctor-dashboard.component';
import { DoctorSectionComponent } from './doctor-section/doctor-section.component';
import { MedicalRecordsComponent } from './medical-records/medical-records.component';
import { TeleconsultationComponent } from './teleconsultation/teleconsultation.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';


@NgModule({
  declarations: [
    DoctorDashboardComponent,
    DoctorSectionComponent,
    MedicalRecordsComponent,
    TeleconsultationComponent,
    MessagesComponent,
    NotificationsComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    DoctorRoutingModule
  ]
})
export class DoctorModule { }
