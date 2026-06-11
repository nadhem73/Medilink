import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PharmacyRoutingModule } from './pharmacy-routing.module';
import { PharmacyDashboardComponent } from './pharmacy-dashboard/pharmacy-dashboard.component';


@NgModule({
  declarations: [
    PharmacyDashboardComponent
  ],
  imports: [
    CommonModule,
    PharmacyRoutingModule
  ]
})
export class PharmacyModule { }
