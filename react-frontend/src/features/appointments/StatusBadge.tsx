interface Props {
  status: string;
}

const STATUS_COLORS: Record<string, { bg: string; color: string }> = {
  Active:   { bg: "#dcfce7", color: "#16a34a" },
  "On Leave": { bg: "#fef9c3", color: "#ca8a04" },
  Inactive: { bg: "#fee2e2", color: "#dc2626" },
  Pending:  { bg: "#fef9c3", color: "#ca8a04" },
  Scheduled: { bg: "#dbeafe", color: "#2563eb" },
  Completed: { bg: "#f1f5f9", color: "#64748b" },
};

export function StatusBadge({ status }: Props) {
  const cfg = STATUS_COLORS[status] ?? { bg: "#f1f5f9", color: "#64748b" };
  return (
    <span style={{ fontSize: 11, fontWeight: 700, padding: "3px 9px", borderRadius: 99, background: cfg.bg, color: cfg.color, whiteSpace: "nowrap" }}>
      {status}
    </span>
  );
}
