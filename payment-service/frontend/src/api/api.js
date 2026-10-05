const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!response.ok) {
    let message = `Erreur ${response.status}`;
    try {
      const body = await response.json();
      message = body.message ?? message;
    } catch {
      // ignore body parsing errors
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const clientsApi = {
  list: () => request("/clients"),
  get: (id) => request(`/clients/${id}`),
  create: (data) => request("/clients", { method: "POST", body: JSON.stringify(data) }),
  remove: (id) => request(`/clients/${id}`, { method: "DELETE" }),
};

export const paymentsApi = {
  list: () => request("/payments"),
  listByClient: (clientId) => request(`/payments/client/${clientId}`),
  create: (data) => request("/payments", { method: "POST", body: JSON.stringify(data) }),
  process: (id) => request(`/payments/${id}/process`, { method: "POST" }),
  refund: (id) => request(`/payments/${id}/refund`, { method: "POST" }),
};
