import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminSectionComponent } from './admin-section/admin-section.component';
import { MonitoringOverviewComponent } from './monitoring-overview/monitoring-overview.component';
import { LogExplorerComponent } from './log-explorer/log-explorer.component';
import { SecurityDashboardComponent } from './security-dashboard/security-dashboard.component';
import { AnalyticsReportsComponent } from './analytics-reports/analytics-reports.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

const routes: Routes = [
  { path: '', component: AdminDashboardComponent },
  {
    path: 'users',
    data: { section: 'users', title: 'Gestion des utilisateurs' },
    children: [
      { path: '', redirectTo: 'patients', pathMatch: 'full' },
      { path: 'patients', component: AdminSectionComponent, data: { section: 'users', title: 'Gestion des utilisateurs — Patients', filter: 'PATIENT' } },
      { path: 'doctors', component: AdminSectionComponent, data: { section: 'users', title: 'Gestion des utilisateurs — Medecins', filter: 'DOCTOR' } },
      { path: 'pharmacies', component: AdminSectionComponent, data: { section: 'users', title: 'Gestion des utilisateurs — Pharmaciens', filter: 'PHARMACY' } }
    ]
  },
  { path: 'monitoring', component: MonitoringOverviewComponent },
  { path: 'security', component: SecurityDashboardComponent },
  { path: 'logs', component: LogExplorerComponent },
  { path: 'analytics', component: AnalyticsReportsComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'help', component: HelpComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
