const STATUS_CLASS = {
  PENDING: "badge badge-pending",
  COMPLETED: "badge badge-completed",
  FAILED: "badge badge-failed",
  REFUNDED: "badge badge-refunded",
};

export default function PaymentList({ payments, onProcess, onRefund }) {
  if (payments.length === 0) {
    return <p className="empty">Aucun paiement pour ce client.</p>;
  }

  return (
    <table className="payment-table">
      <thead>
        <tr>
          <th>#</th>
          <th>Montant</th>
          <th>Moyen</th>
          <th>Description</th>
          <th>Statut</th>
          <th>Créé le</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {payments.map((payment) => (
          <tr key={payment.id}>
            <td>{payment.id}</td>
            <td>{payment.amount} {payment.currency}</td>
            <td>{payment.method}</td>
            <td>{payment.description || "—"}</td>
            <td><span className={STATUS_CLASS[payment.status]}>{payment.status}</span></td>
            <td>{new Date(payment.createdAt).toLocaleString("fr-FR")}</td>
            <td className="actions">
              {payment.status === "PENDING" && (
                <button onClick={() => onProcess(payment.id)}>Traiter</button>
              )}
              {payment.status === "COMPLETED" && (
                <button className="link-danger" onClick={() => onRefund(payment.id)}>Rembourser</button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
