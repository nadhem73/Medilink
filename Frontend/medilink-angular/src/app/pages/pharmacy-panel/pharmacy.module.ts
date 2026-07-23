import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ScrollingModule } from '@angular/cdk/scrolling';

import { PharmacyRoutingModule } from './pharmacy-routing.module';
import { PharmacyDashboardComponent } from './pharmacy-dashboard/pharmacy-dashboard.component';
import { PharmacySectionComponent } from './pharmacy-section/pharmacy-section.component';
import { PharmacyPrescriptionsComponent } from './pharmacy-prescriptions/pharmacy-prescriptions.component';
import { PharmacyStockComponent } from './pharmacy-stock/pharmacy-stock.component';
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
    PharmacyPrescriptionsComponent,
    PharmacyStockComponent,
    AlertsComponent,
    ForecastComponent,
    MessagesComponent,
    NotificationsComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    PharmacyRoutingModule
  ]
})
export class PharmacyModule { }
