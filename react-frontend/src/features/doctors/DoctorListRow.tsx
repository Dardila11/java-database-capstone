import { Avatar } from "./Avatar";
import { StatusBadge } from "@/features/appointments/StatusBadge";
import { getInitials } from "@/lib/utils";
import type { Doctor } from "@/types/api";

const COLORS = ["#0d9488","#7c3aed","#0369a1","#b45309","#be185d","#065f46"];

interface Props {
  doc: Doctor & { status?: string; exp?: string; rating?: number; patients?: number; color?: string };
  showRatings?: boolean;
  onDelete?: (id: number) => void;
}

export function DoctorListRow({ doc, showRatings = true, onDelete }: Props) {
  const color = doc.color ?? COLORS[doc.id % COLORS.length];
  const slots = doc.availableTimes?.length ?? 0;

  return (
    <div
      style={{ display: "flex", alignItems: "center", gap: 14, padding: "14px 20px", borderBottom: "1px solid var(--border)", background: "white", transition: "background 0.12s", animation: "slideIn 0.2s ease" }}
      onMouseEnter={(e) => ((e.currentTarget as HTMLDivElement).style.background = "var(--bg)")}
      onMouseLeave={(e) => ((e.currentTarget as HTMLDivElement).style.background = "white")}
    >
      <Avatar initials={getInitials(doc.name)} color={color} size={40} />
      <div style={{ flex: "0 0 200px" }}>
        <div style={{ fontWeight: 700, fontSize: 14 }}>{doc.name}</div>
        <div style={{ fontSize: 12, color: "var(--ink-3)" }}>{doc.specialty}</div>
      </div>
      {doc.exp && <div style={{ flex: "0 0 90px", fontSize: 13, color: "var(--ink-2)" }}>{doc.exp}</div>}
      {doc.status && <div style={{ flex: "0 0 80px" }}><StatusBadge status={doc.status} /></div>}
      <div style={{ flex: "0 0 80px", fontSize: 13, fontWeight: 600, color: slots <= 2 ? "#ef4444" : "#22c55e" }}>{slots} slots</div>
      {showRatings && doc.rating !== undefined && (
        <div style={{ flex: "0 0 60px", fontSize: 13, fontWeight: 600, color: "#f59e0b" }}>{doc.rating}★</div>
      )}
      {doc.patients !== undefined && (
        <div style={{ flex: "0 0 70px", fontSize: 13, color: "var(--ink-3)" }}>{doc.patients} pts</div>
      )}
      <div style={{ flex: 1, display: "flex", justifyContent: "flex-end", gap: 6 }}>
        <button style={{ padding: "6px 12px", borderRadius: 7, border: "1.5px solid var(--border)", background: "white", fontSize: 12, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>✏️ Edit</button>
        <button onClick={() => onDelete?.(doc.id)} style={{ padding: "6px 8px", borderRadius: 7, border: "1.5px solid #fee2e2", background: "#fef2f2", cursor: "pointer", color: "#ef4444" }}>🗑️</button>
      </div>
    </div>
  );
}
