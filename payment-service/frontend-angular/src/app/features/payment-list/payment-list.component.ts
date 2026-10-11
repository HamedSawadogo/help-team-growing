import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { PaymentFeedService } from '../../core/payment-feed.service';
import { PaymentService } from '../../core/payment.service';
import { Payment } from '../../core/models/payment.model';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-payment-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-list.component.html',
  styleUrl: './payment-list.component.scss',
})
export class PaymentListComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scrollContainer') scrollContainerRef!: ElementRef<HTMLDivElement>;
  @ViewChild('sentinel') sentinelRef!: ElementRef<HTMLDivElement>;

  readonly payments = signal<Payment[]>([]);
  readonly totalElements = signal(0);
  readonly loadingInitial = signal(true);
  readonly loadingMore = signal(false);
  readonly hasMore = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly newlyAddedIds = signal<Set<number>>(new Set());

  private page = 0;
  private observer?: IntersectionObserver;
  private feedSubscription?: Subscription;

  constructor(
    private readonly paymentService: PaymentService,
    private readonly paymentFeed: PaymentFeedService
  ) {}

  ngOnInit(): void {
    this.loadPage(0);

    this.feedSubscription = this.paymentFeed.created$.subscribe((payment) => {
      this.payments.update((current) => [payment, ...current]);
      this.totalElements.update((count) => count + 1);
      this.flagAsNew(payment.id);
    });
  }

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        const visible = entries[0]?.isIntersecting;
        if (visible && this.hasMore() && !this.loadingMore() && !this.loadingInitial()) {
          this.loadPage(this.page + 1);
        }
      },
      { root: this.scrollContainerRef.nativeElement, threshold: 0.1 }
    );
    this.observer.observe(this.sentinelRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.feedSubscription?.unsubscribe();
  }

  retry(): void {
    this.loadPage(this.page === 0 ? 0 : this.page);
  }

  trackById(_index: number, payment: Payment): number {
    return payment.id;
  }

  isNew(id: number): boolean {
    return this.newlyAddedIds().has(id);
  }

  private loadPage(page: number): void {
    const isFirstPage = page === 0;
    isFirstPage ? this.loadingInitial.set(true) : this.loadingMore.set(true);
    this.errorMessage.set(null);

    this.paymentService.list(page, PAGE_SIZE).subscribe({
      next: (response) => {
        this.page = response.number;
        this.hasMore.set(!response.last);
        this.totalElements.set(response.totalElements);
        this.payments.update((current) =>
          isFirstPage ? response.content : [...current, ...response.content]
        );
        this.loadingInitial.set(false);
        this.loadingMore.set(false);
      },
      error: () => {
        this.loadingInitial.set(false);
        this.loadingMore.set(false);
        this.errorMessage.set('Impossible de charger les paiements. Vérifie que le serveur tourne.');
      },
    });
  }

  private flagAsNew(id: number): void {
    const next = new Set(this.newlyAddedIds());
    next.add(id);
    this.newlyAddedIds.set(next);
    setTimeout(() => {
      const after = new Set(this.newlyAddedIds());
      after.delete(id);
      this.newlyAddedIds.set(after);
    }, 2200);
  }
}
