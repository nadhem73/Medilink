import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';

import { AdminSectionComponent } from './admin-section.component';
import { AuthService, AdminUserDto } from '../../../core/services/auth.service';

describe('AdminSectionComponent', () => {
  let component: AdminSectionComponent;
  let fixture: ComponentFixture<AdminSectionComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockRoute = {
    data: of({ section: 'users', title: 'Gestion des utilisateurs' })
  };

  const users: AdminUserDto[] = [
    { id: 1, email: 'sami@med.tn', firstName: 'Sami', lastName: 'Khelifi', phone: '111',
      role: 'DOCTOR', status: 'ACTIVE', createdAt: '2025-01-03T10:00:00', specialty: 'Cardio' },
    { id: 2, email: 'manar@pharma.tn', firstName: 'M', lastName: 'B', phone: '222',
      role: 'PHARMACY', status: 'SUSPENDED', createdAt: '2025-01-01T10:00:00', pharmacyName: 'Pharmacie El Manar' },
    { id: 3, email: 'ali@patient.tn', firstName: 'Ali', lastName: 'Ben', phone: '333',
      role: 'PATIENT', status: 'INACTIVE', createdAt: '2025-01-02T10:00:00' }
  ];

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AuthService', ['getAllUsers', 'updateUserStatus']);
    spy.getAllUsers.and.returnValue(of(users));
    spy.updateUserStatus.and.returnValue(of({ message: 'ok', success: true }));

    await TestBed.configureTestingModule({
      declarations: [AdminSectionComponent],
      imports: [CommonModule, FormsModule],
      providers: [
        { provide: AuthService, useValue: spy },
        { provide: ActivatedRoute, useValue: mockRoute }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture = TestBed.createComponent(AdminSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // déclenche ngOnInit -> loadUsers()
  });

  it('should create and load users for the users section', () => {
    expect(component).toBeTruthy();
    expect(authServiceSpy.getAllUsers).toHaveBeenCalled();
    expect(component.users.length).toBe(3);
  });

  // ---- Filtre par rôle ----
  it('should return all users when filter is ALL', () => {
    component.userFilter = 'ALL';
    expect(component.filteredUsers.length).toBe(3);
  });

  it('should filter by DOCTOR', () => {
    component.userFilter = 'DOCTOR';
    expect(component.filteredUsers.every(u => u.role === 'DOCTOR')).toBeTrue();
    expect(component.filteredUsers.length).toBe(1);
  });

  it('should filter by PATIENT', () => {
    component.userFilter = 'PATIENT';
    expect(component.filteredUsers.length).toBe(1);
    expect(component.filteredUsers[0].role).toBe('PATIENT');
  });

  // ---- Recherche ----
  it('should search by name / email / specialty', () => {
    component.userFilter = 'ALL';
    component.userSearch = 'manar';
    expect(component.filteredUsers.length).toBe(1);
    expect(component.filteredUsers[0].role).toBe('PHARMACY');

    component.userSearch = 'cardio';
    expect(component.filteredUsers[0].role).toBe('DOCTOR');
  });

  // ---- Tri ----
  it('should sort by name ascending and descending', () => {
    component.userFilter = 'ALL';
    component.userSearch = '';
    component.userSortBy = 'name';

    component.userSortDir = 'asc';
    const asc = component.filteredUsers.map(u => component.getUserDisplayName(u));
    const sorted = [...asc].sort((a, b) => a.localeCompare(b));
    expect(asc).toEqual(sorted);

    component.toggleSortDir();
    expect(component.userSortDir).toBe('desc');
    const desc = component.filteredUsers.map(u => component.getUserDisplayName(u));
    expect(desc).toEqual([...sorted].reverse());
  });

  it('should sort by creation date', () => {
    component.userSortBy = 'date';
    component.userSortDir = 'asc';
    const ids = component.filteredUsers.map(u => u.id);
    expect(ids[0]).toBe(2); // 2025-01-01 le plus ancien
  });

  // ---- Libellés / classes ----
  it('should build display name per role', () => {
    expect(component.getUserDisplayName(users[0])).toBe('Dr. Sami Khelifi');
    expect(component.getUserDisplayName(users[1])).toBe('Pharmacie El Manar');
    expect(component.getUserDisplayName(users[2])).toBe('Ali Ben');
  });

  it('should map status label and class', () => {
    expect(component.getUserStatusLabel('ACTIVE')).toBe('Actif');
    expect(component.getUserStatusClass('SUSPENDED')).toBe('st-suspended');
  });

  // ---- Actions ----
  it('activateUser should call service and update status', () => {
    const target = { ...users[2] } as AdminUserDto;
    component.activateUser(target);
    expect(authServiceSpy.updateUserStatus).toHaveBeenCalledWith(3, { status: 'ACTIVE' });
    expect(target.status).toBe('ACTIVE');
  });

  it('deactivateUser should call service with INACTIVE', () => {
    const target = { ...users[0] } as AdminUserDto;
    component.deactivateUser(target);
    expect(authServiceSpy.updateUserStatus).toHaveBeenCalledWith(1, { status: 'INACTIVE' });
    expect(target.status).toBe('INACTIVE');
  });

  it('confirmSuspend should send SUSPENDED with a future suspendUntil', () => {
    const target = { ...users[0] } as AdminUserDto;
    component.openSuspendModal(target);
    component.suspendDuration = 3;
    component.suspendUnit = 'DAYS';
    component.confirmSuspend();

    const call = authServiceSpy.updateUserStatus.calls.mostRecent().args;
    expect(call[0]).toBe(1);
    expect(call[1].status).toBe('SUSPENDED');
    expect(new Date(call[1].suspendUntil!).getTime()).toBeGreaterThan(Date.now());
    expect(component.showSuspendModal).toBeFalse();
  });

  it('getSuspendLabel should show remaining time or expired', () => {
    const future = { ...users[1], suspendUntil: new Date(Date.now() + 2 * 86400000).toISOString() } as AdminUserDto;
    expect(component.getSuspendLabel(future)).toContain('restant');

    const past = { ...users[1], suspendUntil: new Date(Date.now() - 1000).toISOString() } as AdminUserDto;
    expect(component.getSuspendLabel(past)).toContain('expiré');
  });
});
