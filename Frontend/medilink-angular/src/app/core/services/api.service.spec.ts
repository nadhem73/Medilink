import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('get<T>()', () => {
    it('should make a GET request with the correct URL', () => {
      const mockResponse = { id: 1, name: 'Test' };

      service.get<any>('users/1').subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/users/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should convert params object to HttpParams', () => {
      const params = { page: '1', limit: '10' };

      service.get<any>('users', params).subscribe();

      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/users');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('limit')).toBe('10');
      req.flush({});
    });

    it('should filter out null and undefined params', () => {
      const params = { page: '1', limit: null, filter: undefined, name: 'test' };

      service.get<any>('users', params).subscribe();

      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/users');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('limit')).toBeNull();
      expect(req.request.params.get('filter')).toBeNull();
      expect(req.request.params.get('name')).toBe('test');
      req.flush({});
    });
  });

  describe('post<T>()', () => {
    it('should make a POST request with the correct URL and body', () => {
      const body = { name: 'New User' };
      const mockResponse = { id: 1, ...body };

      service.post<any>('users', body).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/users');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });

  describe('put<T>()', () => {
    it('should make a PUT request with the correct URL and body', () => {
      const body = { name: 'Updated User' };
      const mockResponse = { id: 1, ...body };

      service.put<any>('users/1', body).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/users/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });

  describe('patch<T>()', () => {
    it('should make a PATCH request with the correct URL and body', () => {
      const body = { name: 'Patched User' };
      const mockResponse = { id: 1, ...body };

      service.patch<any>('users/1', body).subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/users/1');
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });

  describe('delete<T>()', () => {
    it('should make a DELETE request with the correct URL', () => {
      const mockResponse = { success: true };

      service.delete<any>('users/1').subscribe(res => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8765/api/users/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });
});
