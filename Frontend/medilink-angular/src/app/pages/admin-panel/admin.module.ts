import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminSectionComponent } from './admin-section/admin-section.component';
import { MonitoringOverviewComponent } from './monitoring-overview/monitoring-overview.component';
import { LogExplorerComponent } from './log-explorer/log-explorer.component';
import { SecurityDashboardComponent } from './security-dashboard/security-dashboard.component';
import { AnalyticsReportsComponent } from './analytics-reports/analytics-reports.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminSectionComponent,
    MonitoringOverviewComponent,
    LogExplorerComponent,
    SecurityDashboardComponent,
    AnalyticsReportsComponent,
    NotificationsComponent,
    SettingsComponent,
    HelpComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
