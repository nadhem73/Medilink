import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { DashboardLayoutComponent } from './layouts/dashboard-layout/dashboard-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

const routes: Routes = [
  // Routes publiques avec MainLayout
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./pages/home/home.module').then(m => m.HomeModule)
      }
    ]
  },
  
  // Routes d'authentification avec AuthLayout
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./pages/auth-pages/auth.module').then(m => m.AuthModule)
      }
    ]
  },
  
  // Routes des panels avec DashboardLayout (protégées)
  {
    path: 'dashboard',
    children: [
      {
        path: '',
        component: DashboardLayoutComponent,
        children: [
          {
            path: 'patient',
            canActivate: [authGuard],
            loadChildren: () => import('./pages/patient-panel/patient.module').then(m => m.PatientModule)
          },
          {
            path: 'admin',
            canActivate: [adminGuard],
            loadChildren: () => import('./pages/admin-panel/admin.module').then(m => m.AdminModule)
          },
          {
            path: 'doctor',
            canActivate: [authGuard],
            loadChildren: () => import('./pages/doctor-panel/doctor.module').then(m => m.DoctorModule)
          },
          {
            path: 'pharmacy',
            loadChildren: () => import('./pages/pharmacy-panel/pharmacy.module').then(m => m.PharmacyModule)
          },
          {
            path: 'laboratory',
            loadChildren: () => import('./pages/laboratory-panel/laboratory.module').then(m => m.LaboratoryModule)
          },
        ]
      }
    ]
  },
  
  // Redirect vers home si route non trouvée
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      anchorScrolling: 'enabled',
      scrollPositionRestoration: 'enabled'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
