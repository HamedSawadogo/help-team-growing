import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProductPage } from './product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);

  getPage(page: number, size: number): Observable<ProductPage> {

    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<ProductPage>('/api/products', { params });
  }
}
