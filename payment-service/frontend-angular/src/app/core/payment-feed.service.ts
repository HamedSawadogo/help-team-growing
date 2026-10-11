import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Payment } from './models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentFeedService {
  private readonly createdSubject = new Subject<Payment>();

  readonly created$ = this.createdSubject.asObservable();

  notifyCreated(payment: Payment): void {
    this.createdSubject.next(payment);
  }
}
