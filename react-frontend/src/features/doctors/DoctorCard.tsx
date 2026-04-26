import { Avatar } from "./Avatar";
import { StatusBadge } from "@/features/appointments/StatusBadge";
import { getInitials } from "@/lib/utils";
import type { Doctor } from "@/types/api";

interface Props {
  doc: Doctor & { status?: string; exp?: string; rating?: number; patients?: number; color?: string };
  compact?: boolean;
  showRatings?: boolean;
  onDelete?: (id: number) => void;
  onBook?: () => void;
  isAdmin?: boolean;
}

const COLORS = ["#0d9488","#7c3aed","#0369a1","#b45309","#be185d","#065f46","#9333ea","#0284c7"];

function getColor(id: number): string {
  return COLORS[id % COLORS.length];
}

export function DoctorCard({ doc, compact = false, showRatings = true, onDelete, onBook, isAdmin = false }: Props) {
  const color = doc.color ?? getColor(doc.id);
  const initials = getInitials(doc.name);
  const slots = doc.availableTimes?.length ?? 0;

  return (
    <div
      style={{
        background: "white", borderRadius: "var(--radius)",
        border: "1.5px solid var(--border)", padding: compact ? "14px 16px" : "20px",
        display: "flex", flexDirection: "column", gap: compact ? 10 : 14,
        boxShadow: "var(--shadow-sm)", transition: "box-shadow 0.2s, transform 0.2s",
        animation: "popIn 0.25s ease",
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLDivElement).style.boxShadow = "var(--shadow-md)"; (e.currentTarget as HTMLDivElement).style.transform = "translateY(-2px)"; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLDivElement).style.boxShadow = "var(--shadow-sm)"; (e.currentTarget as HTMLDivElement).style.transform = "none"; }}
    >
      <div style={{ display: "flex", gap: 12, alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
          <Avatar initials={initials} color={color} size={compact ? 40 : 52} />
          <div>
            <div style={{ fontWeight: 700, fontSize: compact ? 14 : 15 }}>{doc.name}</div>
            <div style={{ fontSize: 13, color, fontWeight: 600, marginTop: 2 }}>{doc.specialty}</div>
            {doc.exp && <div style={{ fontSize: 12, color: "var(--ink-3)", marginTop: 2 }}>{doc.exp} experience</div>}
          </div>
        </div>
        {doc.status && <StatusBadge status={doc.status} />}
      </div>

      {!compact && (
        <div style={{ display: "grid", gridTemplateColumns: showRatings ? "1fr 1fr 1fr" : "1fr 1fr", gap: 8 }}>
          {doc.patients !== undefined && (
            <div style={{ background: "var(--bg)", borderRadius: 8, padding: "8px 10px", textAlign: "center" }}>
              <div style={{ fontSize: 15, fontWeight: 800, color: "var(--ink)" }}>{doc.patients}</div>
              <div style={{ fontSize: 11, color: "var(--ink-3)", marginTop: 1 }}>Patients</div>
            </div>
          )}
          <div style={{ background: "var(--bg)", borderRadius: 8, padding: "8px 10px", textAlign: "center" }}>
            <div style={{ fontSize: 15, fontWeight: 800, color: slots <= 2 ? "#ef4444" : "#22c55e" }}>{slots} open</div>
            <div style={{ fontSize: 11, color: "var(--ink-3)", marginTop: 1 }}>Slots</div>
          </div>
          {showRatings && doc.rating !== undefined && (
            <div style={{ background: "var(--bg)", borderRadius: 8, padding: "8px 10px", textAlign: "center" }}>
              <div style={{ fontSize: 15, fontWeight: 800, color: "#f59e0b" }}>{doc.rating}★</div>
              <div style={{ fontSize: 11, color: "var(--ink-3)", marginTop: 1 }}>Rating</div>
            </div>
          )}
        </div>
      )}

      {isAdmin && (
        <div style={{ display: "flex", gap: 8 }}>
          <button style={{ flex: 1, padding: "8px", borderRadius: "var(--radius-sm)", border: "1.5px solid var(--border)", background: "white", fontSize: 12, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
            ✏️ Edit
          </button>
          <button
            onClick={() => onDelete?.(doc.id)}
            style={{ padding: "8px 10px", borderRadius: "var(--radius-sm)", border: "1.5px solid #fee2e2", background: "#fef2f2", fontSize: 12, cursor: "pointer", color: "#ef4444" }}
          >
            🗑️
          </button>
        </div>
      )}

      {!isAdmin && onBook && (
        <button
          onClick={onBook}
          style={{ width: "100%", padding: "9px", borderRadius: "var(--radius-sm)", border: "1.5px dashed var(--border)", background: "var(--bg)", color: "var(--ink-3)", fontSize: 13, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", gap: 6, transition: "all 0.15s" }}
          onMouseEnter={(e) => { const el = e.currentTarget; el.style.borderColor = "var(--teal)"; el.style.color = "var(--teal)"; el.style.background = "var(--teal-light)"; }}
          onMouseLeave={(e) => { const el = e.currentTarget; el.style.borderColor = "var(--border)"; el.style.color = "var(--ink-3)"; el.style.background = "var(--bg)"; }}
        >
          🔒 Login to Book
        </button>
      )}

      {!isAdmin && !onBook && (
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: 13 }}>
          <span style={{ color: "#f59e0b", fontWeight: 600 }}>{doc.rating ? `${doc.rating} ★` : ""}</span>
          <span style={{ color: slots <= 2 ? "#ef4444" : "#22c55e", fontWeight: 600 }}>{slots} slots available</span>
        </div>
      )}
    </div>
  );
}
