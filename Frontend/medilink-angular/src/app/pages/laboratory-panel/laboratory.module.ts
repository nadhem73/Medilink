import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { LaboratoryRoutingModule } from './laboratory-routing.module';
import { LaboratoryDashboardComponent } from './laboratory-dashboard/laboratory-dashboard.component';


@NgModule({
  declarations: [
    LaboratoryDashboardComponent
  ],
  imports: [
    CommonModule,
    LaboratoryRoutingModule
  ]
})
export class LaboratoryModule { }
