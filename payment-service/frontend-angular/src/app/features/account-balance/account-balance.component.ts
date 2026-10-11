import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { AccountService } from '../../core/account.service';
import { PaymentFeedService } from '../../core/payment-feed.service';

@Component({
  selector: 'app-account-balance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-balance.component.html',
  styleUrl: './account-balance.component.scss',
})
export class AccountBalanceComponent implements OnInit, OnDestroy {
  readonly balance = signal<number | null>(null);
  readonly currency = signal('XOF');
  readonly justUpdated = signal(false);
  readonly loading = signal(true);

  private feedSubscription?: Subscription;

  constructor(
    private readonly accountService: AccountService,
    private readonly paymentFeed: PaymentFeedService
  ) {}

  ngOnInit(): void {
    this.accountService.getBalance().subscribe({
      next: (account) => {
        this.balance.set(account.balance);
        this.currency.set(account.currency);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.feedSubscription = this.paymentFeed.created$.subscribe((payment) => {
      if (payment.balanceAfter != null) {
        this.balance.set(payment.balanceAfter);
        this.justUpdated.set(true);
        setTimeout(() => this.justUpdated.set(false), 1200);
      }
    });
  }

  ngOnDestroy(): void {
    this.feedSubscription?.unsubscribe();
  }
}
