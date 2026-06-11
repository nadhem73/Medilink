import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AmbulanceRoutingModule } from './ambulance-routing.module';
import { AmbulanceDashboardComponent } from './ambulance-dashboard/ambulance-dashboard.component';


@NgModule({
  declarations: [
    AmbulanceDashboardComponent
  ],
  imports: [
    CommonModule,
    AmbulanceRoutingModule
  ]
})
export class AmbulanceModule { }
