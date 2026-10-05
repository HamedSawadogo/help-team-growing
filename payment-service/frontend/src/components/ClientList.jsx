export default function ClientList({ clients, selectedClientId, onSelect, onDelete }) {
  if (clients.length === 0) {
    return <p className="empty">Aucun client pour le moment.</p>;
  }

  return (
    <ul className="client-list">
      {clients.map((client) => (
        <li
          key={client.id}
          className={client.id === selectedClientId ? "selected" : ""}
          onClick={() => onSelect(client.id)}
        >
          <div>
            <strong>{client.firstName} {client.lastName}</strong>
            <span className="muted">{client.email}</span>
          </div>
          <button
            className="link-danger"
            onClick={(event) => {
              event.stopPropagation();
              onDelete(client.id);
            }}
          >
            Supprimer
          </button>
        </li>
      ))}
    </ul>
  );
}
