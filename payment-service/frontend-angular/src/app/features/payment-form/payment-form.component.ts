import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { PaymentFeedService } from '../../core/payment-feed.service';
import { PaymentService } from '../../core/payment.service';
import { PaymentMethod } from '../../core/models/payment.model';

interface MethodOption {
  value: PaymentMethod;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './payment-form.component.html',
  styleUrl: './payment-form.component.scss',
})
export class PaymentFormComponent {
  readonly methods: MethodOption[] = [
    { value: 'WAVE', label: 'Wave', icon: '🌊' },
    { value: 'ORANGE_MONEY', label: 'Orange Money', icon: '🟠' },
  ];

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successPing = signal(false);

  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly paymentService: PaymentService,
    private readonly paymentFeed: PaymentFeedService
  ) {
    this.form = this.fb.group({
      clientId: [null as number | null, [Validators.min(1)]],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      method: ['WAVE' as PaymentMethod, Validators.required],
      description: [''],
    });
  }

  selectMethod(method: PaymentMethod): void {
    this.form.controls.method.setValue(method);
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const { clientId, amount, method, description } = this.form.getRawValue();

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.paymentService
      .create({
        clientId: clientId ?? null,
        amount: amount as number,
        currency: 'XOF',
        method: method as PaymentMethod,
        description: description || null,
        idempotencyKey: crypto.randomUUID(),
      })
      .subscribe({
        next: (payment) => {
          this.paymentFeed.notifyCreated(payment);
          this.submitting.set(false);
          this.successPing.set(true);
          setTimeout(() => this.successPing.set(false), 1800);
          this.form.reset({ clientId: null, amount: null, method: 'WAVE', description: '' });
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.errorMessage.set(err.error?.message ?? 'Le paiement a échoué. Réessaie.');
        },
      });
  }
}
