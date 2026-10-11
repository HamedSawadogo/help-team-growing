import { Component } from '@angular/core';
import { AccountBalanceComponent } from './features/account-balance/account-balance.component';
import { PaymentFormComponent } from './features/payment-form/payment-form.component';
import { PaymentListComponent } from './features/payment-list/payment-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AccountBalanceComponent, PaymentFormComponent, PaymentListComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
