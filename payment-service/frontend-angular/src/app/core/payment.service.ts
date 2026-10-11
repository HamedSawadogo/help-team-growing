import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Payment, PageResponse, PaymentRequest } from './models/payment.model';

const API_BASE = 'http://localhost:9090/api';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  constructor(private readonly http: HttpClient) {}

  list(page: number, size = 20): Observable<PageResponse<Payment>> {
    return this.http.get<PageResponse<Payment>>(`${API_BASE}/payments`, {
      params: { page, size },
    });
  }

  create(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${API_BASE}/payments`, request);
  }
}
