import { useState } from "react";
import { DoctorCard } from "./DoctorCard";
import { DoctorListRow } from "./DoctorListRow";
import { AddDoctorModal } from "./AddDoctorModal";
import type { Doctor } from "@/types/api";

interface DoctorWithMeta extends Doctor {
  status?: string;
  exp?: string;
  rating?: number;
  patients?: number;
  color?: string;
}

interface Props {
  doctors: DoctorWithMeta[];
  isAdmin?: boolean;
  compact?: boolean;
  showRatings?: boolean;
  defaultView?: "grid" | "list";
  onDelete?: (id: number) => void;
  onAdd?: (doctor: Omit<Doctor, "id">) => Promise<void>;
  onBook?: (doc: DoctorWithMeta) => void;
}

const STATUSES = ["All", "Active", "On Leave", "Inactive"];

export function DoctorGrid({ doctors, isAdmin = false, compact = false, showRatings = true, defaultView = "grid", onDelete, onAdd, onBook }: Props) {
  const [view, setView] = useState<"grid" | "list">(defaultView);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("All");
  const [showAdd, setShowAdd] = useState(false);

  const filtered = doctors.filter((d) => {
    const matchSearch = d.name.toLowerCase().includes(search.toLowerCase()) || d.specialty.toLowerCase().includes(search.toLowerCase());
    const matchFilter = filter === "All" || d.status === filter;
    return matchSearch && matchFilter;
  });

  const inputStyle: React.CSSProperties = {
    padding: "9px 9px 9px 34px", borderRadius: 10, border: "1.5px solid var(--border)",
    fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%",
  };

  return (
    <div style={{ animation: "slideIn 0.2s ease" }}>
      {/* Toolbar */}
      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 20, flexWrap: "wrap" }}>
        <div style={{ position: "relative", flex: "1 1 200px" }}>
          <span style={{ position: "absolute", left: 11, top: "50%", transform: "translateY(-50%)", pointerEvents: "none", fontSize: 14, color: "var(--ink-3)" }}>🔍</span>
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search doctors…" style={inputStyle} onFocus={(e) => (e.target.style.borderColor = "var(--teal)")} onBlur={(e) => (e.target.style.borderColor = "var(--border)")} />
        </div>

        {isAdmin && (
          <div style={{ display: "flex", gap: 4, background: "var(--bg)", borderRadius: 99, padding: 4, border: "1px solid var(--border)" }}>
            {STATUSES.map((s) => (
              <button key={s} onClick={() => setFilter(s)} style={{ padding: "5px 12px", borderRadius: 99, border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600, background: filter === s ? "white" : "transparent", color: filter === s ? "var(--ink)" : "var(--ink-3)", boxShadow: filter === s ? "var(--shadow-sm)" : "none", transition: "all 0.15s" }}>{s}</button>
            ))}
          </div>
        )}

        <div style={{ display: "flex", background: "var(--bg)", borderRadius: 8, padding: 3, border: "1px solid var(--border)" }}>
          {([["grid", "Cards"], ["list", "List"]] as const).map(([v, label]) => (
            <button key={v} onClick={() => setView(v)} style={{ display: "flex", alignItems: "center", gap: 5, padding: "6px 12px", borderRadius: 6, border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600, background: view === v ? "white" : "transparent", color: view === v ? "var(--ink)" : "var(--ink-3)", boxShadow: view === v ? "var(--shadow-sm)" : "none", transition: "all 0.15s" }}>
              {v === "grid" ? "⊞" : "≡"} {label}
            </button>
          ))}
        </div>

        {isAdmin && onAdd && (
          <button onClick={() => setShowAdd(true)} style={{ display: "flex", alignItems: "center", gap: 6, padding: "9px 18px", borderRadius: 99, border: "none", background: "var(--teal)", color: "white", fontSize: 13, fontWeight: 700, cursor: "pointer", whiteSpace: "nowrap" }}
            onMouseEnter={(e) => ((e.currentTarget as HTMLButtonElement).style.opacity = "0.88")}
            onMouseLeave={(e) => ((e.currentTarget as HTMLButtonElement).style.opacity = "1")}
          >
            ➕ Add Doctor
          </button>
        )}
      </div>

      <div style={{ fontSize: 13, color: "var(--ink-3)", marginBottom: 14 }}>
        Showing <strong style={{ color: "var(--ink)" }}>{filtered.length}</strong> of {doctors.length} doctors
      </div>

      {view === "grid" ? (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 14 }}>
          {filtered.map((d) => (
            <DoctorCard
              key={d.id}
              doc={d}
              compact={compact}
              showRatings={showRatings}
              isAdmin={isAdmin}
              onDelete={onDelete}
              onBook={onBook ? () => onBook(d) : undefined}
            />
          ))}
          {isAdmin && onAdd && (
            <button onClick={() => setShowAdd(true)} style={{ borderRadius: "var(--radius)", border: "2px dashed var(--border)", background: "transparent", padding: "32px 20px", cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 10, color: "var(--ink-3)", transition: "all 0.15s", minHeight: 180 }}
              onMouseEnter={(e) => { const el = e.currentTarget; el.style.borderColor = "var(--teal)"; el.style.color = "var(--teal)"; el.style.background = "var(--teal-light)"; }}
              onMouseLeave={(e) => { const el = e.currentTarget; el.style.borderColor = "var(--border)"; el.style.color = "var(--ink-3)"; el.style.background = "transparent"; }}
            >
              <div style={{ width: 40, height: 40, borderRadius: "50%", border: "2px dashed currentColor", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20 }}>+</div>
              <span style={{ fontSize: 13, fontWeight: 600 }}>Add New Doctor</span>
            </button>
          )}
        </div>
      ) : (
        <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", overflow: "hidden", boxShadow: "var(--shadow-sm)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "11px 20px", background: "var(--bg)", borderBottom: "1px solid var(--border)" }}>
            {["Name", "Experience", "Status", "Slots", showRatings && "Rating", "Patients"].filter(Boolean).map((label) => (
              <div key={String(label)} style={{ flex: label === "Name" ? "0 0 200px" : "0 0 80px", fontSize: 11, fontWeight: 700, color: "var(--ink-3)", textTransform: "uppercase", letterSpacing: "0.5px" }}>{String(label)}</div>
            ))}
            <div style={{ flex: 1 }} />
          </div>
          {filtered.length === 0 ? (
            <div style={{ padding: "40px", textAlign: "center", color: "var(--ink-3)", fontSize: 14 }}>No doctors match your search.</div>
          ) : (
            filtered.map((d) => <DoctorListRow key={d.id} doc={d} showRatings={showRatings} onDelete={onDelete} />)
          )}
        </div>
      )}

      {showAdd && onAdd && (
        <AddDoctorModal onClose={() => setShowAdd(false)} onAdd={async (doc) => { await onAdd(doc); setShowAdd(false); }} />
      )}
    </div>
  );
}
