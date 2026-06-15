import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AdminSectionComponent } from './admin-section/admin-section.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { SettingsComponent } from './settings/settings.component';
import { HelpComponent } from './help/help.component';

const routes: Routes = [
  { path: '', component: AdminDashboardComponent },
  {
    path: 'approvals',
    component: AdminSectionComponent,
    data: { section: 'approvals', title: 'Validation des comptes medicaux' }
  },
  {
    path: 'users',
    component: AdminSectionComponent,
    data: { section: 'users', title: 'Gestion des utilisateurs' }
  },
  {
    path: 'monitoring',
    component: AdminSectionComponent,
    data: { section: 'monitoring', title: 'Monitoring systeme' }
  },
  {
    path: 'security',
    component: AdminSectionComponent,
    data: { section: 'security', title: 'Securite & politiques d acces' }
  },
  {
    path: 'logs',
    component: AdminSectionComponent,
    data: { section: 'logs', title: "Logs d'activite" }
  },
  {
    path: 'analytics',
    component: AdminSectionComponent,
    data: { section: 'analytics', title: 'Rapports analytics' }
  },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'help', component: HelpComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
