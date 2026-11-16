import { AfterViewInit, Component, DestroyRef, ElementRef, inject, signal, viewChild } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { Product } from './product.model';
import { ProductService } from './product.service';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-product-list',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductList implements AfterViewInit {
  private readonly productService = inject(ProductService);
  private readonly sentinel = viewChild.required<ElementRef<HTMLElement>>('sentinel');

  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(false);
  protected readonly hasNext = signal(true);
  protected readonly error = signal<string | null>(null);

  private nextPage = 0;
  private observer?: IntersectionObserver;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.observer?.disconnect());
  }

  ngAfterViewInit(): void {
    // Charge la page suivante dès que le bas de liste approche de l'écran
    this.observer = new IntersectionObserver(
      entries => {
        if (entries.some(e => e.isIntersecting)) {
          this.loadMore();
        }
      },
      { rootMargin: '300px' }
    );
    this.observer.observe(this.sentinel().nativeElement);
  }

  protected loadMore(): void {
    if (this.loading() || !this.hasNext()) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    this.productService.getPage(this.nextPage, PAGE_SIZE)
      .pipe(finalize(() => {
        this.loading.set(false);
        this.reobserve();
      }))
      .subscribe({
        next: page => {
          this.products.update(list => [...list, ...page.content]);
          this.hasNext.set(page.hasNext);
          this.nextPage++;
        },
        error: () => this.error.set('Impossible de charger les produits.')
      });
  }

  // L'observer ne se redéclenche que sur un changement d'intersection : si la sentinelle
  // est toujours visible après un chargement (grand écran), on force une nouvelle vérification.
  private reobserve(): void {
    if (!this.observer || this.error()) {
      return;
    }
    const el = this.sentinel().nativeElement;
    this.observer.unobserve(el);
    this.observer.observe(el);
  }
}
