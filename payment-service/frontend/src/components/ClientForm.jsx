import { useState } from "react";
import { clientsApi } from "../api/api.js";

const EMPTY_FORM = { firstName: "", lastName: "", email: "", phone: "" };

export default function ClientForm({ onCreated }) {
  const [form, setForm] = useState(EMPTY_FORM);
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
      const created = await clientsApi.create(form);
      setForm(EMPTY_FORM);
      onCreated(created);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card" onSubmit={handleSubmit}>
      <h2>Nouveau client</h2>
      <div className="form-row">
        <input name="firstName" placeholder="Prénom" value={form.firstName} onChange={handleChange} required />
        <input name="lastName" placeholder="Nom" value={form.lastName} onChange={handleChange} required />
      </div>
      <div className="form-row">
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
        <input name="phone" placeholder="Téléphone" value={form.phone} onChange={handleChange} />
      </div>
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? "Création..." : "Créer le client"}
      </button>
    </form>
  );
}
