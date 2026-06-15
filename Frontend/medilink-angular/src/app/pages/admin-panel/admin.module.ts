import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminSectionComponent } from './admin-section/admin-section.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminSectionComponent,
    NotificationsComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
