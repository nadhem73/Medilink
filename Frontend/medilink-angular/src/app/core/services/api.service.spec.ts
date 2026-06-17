import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

function testHttpMethod(
  methodName: string,
  httpMethod: string,
  endpoint: string,
  name: string,
  getService: () => ApiService,
  getHttpMock: () => HttpTestingController
) {
  describe(`${methodName}()`, () => {
    it(`should make a ${httpMethod} request`, () => {
      const service = getService();
      const httpMock = getHttpMock();
      const body = { name };
      const mockResponse = { id: 1, ...body };
      (service as any)[methodName](endpoint, body).subscribe((res: any) => {
        expect(res).toEqual(mockResponse);
      });
      const req = httpMock.expectOne(`http://localhost:8765/api/${endpoint}`);
      expect(req.request.method).toBe(httpMethod);
      expect(req.request.body).toEqual(body);
      req.flush(mockResponse);
    });
  });
}

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

  describe('get()', () => {
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
      service.get<any>('users', { page: '1', limit: '10' }).subscribe();
      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/users');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('limit')).toBe('10');
      req.flush({});
    });

    it('should filter out null and undefined params', () => {
      service.get<any>('users', { page: '1', limit: null, filter: undefined, name: 'test' }).subscribe();
      const req = httpMock.expectOne(r => r.url === 'http://localhost:8765/api/users');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('limit')).toBeNull();
      expect(req.request.params.get('filter')).toBeNull();
      expect(req.request.params.get('name')).toBe('test');
      req.flush({});
    });
  });

  testHttpMethod('post', 'POST', 'users', 'New User', () => service, () => httpMock);
  testHttpMethod('put', 'PUT', 'users/1', 'Updated User', () => service, () => httpMock);
  testHttpMethod('patch', 'PATCH', 'users/1', 'Patched User', () => service, () => httpMock);

  describe('delete()', () => {
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
