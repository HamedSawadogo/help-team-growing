import { useState } from "react";
import { paymentsApi } from "../api/api.js";

const METHODS = ["CARD", "BANK_TRANSFER", "PAYPAL", "CASH"];

export default function PaymentForm({ clientId, onCreated }) {
  const [form, setForm] = useState({ amount: "", currency: "EUR", method: "CARD", description: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const created = await paymentsApi.create({
        clientId,
        amount: Number(form.amount),
        currency: form.currency,
        method: form.method,
        description: form.description,
      });
      setForm({ amount: "", currency: "EUR", method: "CARD", description: "" });
      onCreated(created);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card" onSubmit={handleSubmit}>
      <h2>Nouveau paiement</h2>
      <div className="form-row">
        <input
          name="amount"
          type="number"
          step="0.01"
          min="0.01"
          placeholder="Montant"
          value={form.amount}
          onChange={handleChange}
          required
        />
        <input name="currency" placeholder="Devise" value={form.currency} onChange={handleChange} maxLength={3} />
      </div>
      <div className="form-row">
        <select name="method" value={form.method} onChange={handleChange}>
          {METHODS.map((method) => (
            <option key={method} value={method}>{method}</option>
          ))}
        </select>
        <input name="description" placeholder="Description" value={form.description} onChange={handleChange} />
      </div>
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? "Création..." : "Créer le paiement"}
      </button>
    </form>
  );
}
