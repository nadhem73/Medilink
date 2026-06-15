import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PharmacyRoutingModule } from './pharmacy-routing.module';
import { PharmacyDashboardComponent } from './pharmacy-dashboard/pharmacy-dashboard.component';
import { PharmacySectionComponent } from './pharmacy-section/pharmacy-section.component';
import { AlertsComponent } from './alerts/alerts.component';
import { ForecastComponent } from './forecast/forecast.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';


@NgModule({
  declarations: [
    PharmacyDashboardComponent,
    PharmacySectionComponent,
    AlertsComponent,
    ForecastComponent,
    MessagesComponent,
    NotificationsComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    PharmacyRoutingModule
  ]
})
export class PharmacyModule { }
