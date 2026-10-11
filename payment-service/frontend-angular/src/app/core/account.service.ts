import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AccountBalance } from './models/account.model';

const API_BASE = 'http://localhost:9090/api';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private readonly http: HttpClient) {}

  getBalance(): Observable<AccountBalance> {
    return this.http.get<AccountBalance>(`${API_BASE}/accounts/balance`);
  }
}
