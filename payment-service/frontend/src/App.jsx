import { useCallback, useEffect, useState } from "react";
import { clientsApi, paymentsApi } from "./api/api.js";
import ClientList from "./components/ClientList.jsx";
import ClientForm from "./components/ClientForm.jsx";
import PaymentForm from "./components/PaymentForm.jsx";
import PaymentList from "./components/PaymentList.jsx";

export default function App() {
  const [clients, setClients] = useState([]);
  const [selectedClientId, setSelectedClientId] = useState(null);
  const [payments, setPayments] = useState([]);
  const [error, setError] = useState(null);

  const loadClients = useCallback(async () => {
    try {
      const data = await clientsApi.list();
      setClients(data);
      if (data.length > 0 && !data.some((c) => c.id === selectedClientId)) {
        setSelectedClientId(data[0].id);
      }
      if (data.length === 0) {
        setSelectedClientId(null);
      }
    } catch (err) {
      setError(err.message);
    }
  }, [selectedClientId]);

  const loadPayments = useCallback(async (clientId) => {
    if (!clientId) {
      setPayments([]);
      return;
    }
    try {
      const data = await paymentsApi.listByClient(clientId);
      setPayments(data);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => {
    loadClients();
  }, []);

  useEffect(() => {
    loadPayments(selectedClientId);
  }, [selectedClientId, loadPayments]);

  async function handleDeleteClient(id) {
    try {
      await clientsApi.remove(id);
      await loadClients();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handlePaymentAction(action, id) {
    try {
      await action(id);
      await loadPayments(selectedClientId);
    } catch (err) {
      setError(err.message);
    }
  }

  const selectedClient = clients.find((c) => c.id === selectedClientId) ?? null;

  return (
    <div className="app">
      <header>
        <h1>Payment Service</h1>
        <p className="muted">Gestion basique des clients et des paiements</p>
      </header>

      {error && (
        <div className="error banner">
          {error} <button onClick={() => setError(null)}>×</button>
        </div>
      )}

      <div className="layout">
        <section className="column">
          <ClientForm onCreated={loadClients} />
          <div className="card">
            <h2>Clients</h2>
            <ClientList
              clients={clients}
              selectedClientId={selectedClientId}
              onSelect={setSelectedClientId}
              onDelete={handleDeleteClient}
            />
          </div>
        </section>

        <section className="column column-wide">
          {selectedClient ? (
            <>
              <h2 className="section-title">
                Paiements de {selectedClient.firstName} {selectedClient.lastName}
              </h2>
              <PaymentForm clientId={selectedClient.id} onCreated={() => loadPayments(selectedClientId)} />
              <div className="card">
                <PaymentList
                  payments={payments}
                  onProcess={(id) => handlePaymentAction(paymentsApi.process, id)}
                  onRefund={(id) => handlePaymentAction(paymentsApi.refund, id)}
                />
              </div>
            </>
          ) : (
            <p className="empty">Sélectionnez ou créez un client pour voir ses paiements.</p>
          )}
        </section>
      </div>
    </div>
  );
}
