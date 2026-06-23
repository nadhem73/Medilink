import { TestBed } from '@angular/core/testing';

import { StorageService } from './storage.service';

describe('StorageService', () => {
  let service: StorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StorageService);

    spyOn(localStorage, 'setItem').and.callFake(() => {});
    spyOn(localStorage, 'getItem').and.returnValue(null);
    spyOn(localStorage, 'removeItem').and.callFake(() => {});
    spyOn(localStorage, 'clear').and.callFake(() => {});
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Token Management', () => {
    it('setToken() should store token in localStorage', () => {
      service.setToken('my-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('auth_token', 'my-token');
    });

    it('getToken() should retrieve token from localStorage', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue('stored-token');
      const result = service.getToken();
      expect(localStorage.getItem).toHaveBeenCalledWith('auth_token');
      expect(result).toBe('stored-token');
    });

    it('getToken() should return null when no token exists', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);
      const result = service.getToken();
      expect(result).toBeNull();
    });

    it('removeToken() should remove token from localStorage', () => {
      service.removeToken();
      expect(localStorage.removeItem).toHaveBeenCalledWith('auth_token');
    });
  });

  describe('Refresh Token Management', () => {
    it('setRefreshToken() should store refresh token in localStorage', () => {
      service.setRefreshToken('my-refresh');
      expect(localStorage.setItem).toHaveBeenCalledWith('refresh_token', 'my-refresh');
    });

    it('getRefreshToken() should retrieve refresh token from localStorage', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue('stored-refresh');
      const result = service.getRefreshToken();
      expect(localStorage.getItem).toHaveBeenCalledWith('refresh_token');
      expect(result).toBe('stored-refresh');
    });

    it('removeRefreshToken() should remove refresh token from localStorage', () => {
      service.removeRefreshToken();
      expect(localStorage.removeItem).toHaveBeenCalledWith('refresh_token');
    });
  });

  describe('Generic Storage Methods', () => {
    it('setItem() should store value in localStorage', () => {
      service.setItem('custom-key', 'custom-value');
      expect(localStorage.setItem).toHaveBeenCalledWith('custom-key', 'custom-value');
    });

    it('getItem() should retrieve value from localStorage', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue('stored-value');
      const result = service.getItem('custom-key');
      expect(localStorage.getItem).toHaveBeenCalledWith('custom-key');
      expect(result).toBe('stored-value');
    });

    it('removeItem() should remove value from localStorage', () => {
      service.removeItem('custom-key');
      expect(localStorage.removeItem).toHaveBeenCalledWith('custom-key');
    });
  });

  describe('clear()', () => {
    it('should clear all localStorage', () => {
      service.clear();
      expect(localStorage.clear).toHaveBeenCalled();
    });
  });
});
