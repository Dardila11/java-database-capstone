import { useState } from "react";
import { SPECIALTIES, HOURS, slotToTimeRange } from "@/lib/constants";
import type { DoctorCreateRequest } from "@/types/api";

const EMPTY_FORM = {
  name: "", specialty: SPECIALTIES[0], exp: "", email: "", phone: "",
  availability: HOURS.reduce<Record<string, boolean>>((a, h) => ({ ...a, [h]: true }), {}),
  status: "Active",
};

interface Props {
  onClose: () => void;
  onAdd: (doctor: DoctorCreateRequest) => Promise<void>;
}

export function AddDoctorModal({ onClose, onAdd }: Props) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const set = (k: string, v: unknown) => setForm((f) => ({ ...f, [k]: v }));

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.name.trim()) e.name = "Name is required";
    if (!form.exp.trim()) e.exp = "Experience is required";
    if (!form.email.trim()) e.email = "Email is required";
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = "Invalid email";
    if (!form.phone.trim()) e.phone = "Phone is required";
    return e;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      const availableTimes = Object.entries(form.availability)
        .filter(([, on]) => on)
        .map(([label]) => slotToTimeRange(label));
      await onAdd({ name: form.name, specialty: form.specialty, email: form.email, phone: form.phone, password: form.password, availableTimes });
      setSuccess(true);
      setTimeout(onClose, 1400);
    } catch (err) {
      setErrors({ submit: err instanceof Error ? err.message : "Failed to add doctor" });
      setLoading(false);
    }
  };

  const inputStyle: React.CSSProperties = {
    padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)",
    fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%",
  };
  const focus = (e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => (e.target.style.borderColor = "var(--teal)");
  const blur = (e: React.FocusEvent<HTMLInputElement | HTMLSelectElement>) => (e.target.style.borderColor = "var(--border)");

  if (success) {
    return (
      <div style={{ position: "fixed", inset: 0, zIndex: 1100, background: "oklch(0% 0 0 / 0.45)", backdropFilter: "blur(6px)", display: "flex", alignItems: "center", justifyContent: "center", padding: 16, animation: "fadeIn 0.2s ease" }}>
        <div style={{ background: "white", borderRadius: 20, maxWidth: 520, width: "100%", padding: "56px 32px", textAlign: "center", boxShadow: "var(--shadow-lg)" }}>
          <div style={{ fontSize: 60, marginBottom: 16 }}>🎉</div>
          <div style={{ fontSize: 20, fontWeight: 800 }}>Doctor Added!</div>
          <div style={{ fontSize: 14, color: "var(--ink-3)", marginTop: 8 }}>{form.name} has been added to the roster.</div>
        </div>
      </div>
    );
  }

  return (
    <div
      style={{ position: "fixed", inset: 0, zIndex: 1100, background: "oklch(0% 0 0 / 0.45)", backdropFilter: "blur(6px)", display: "flex", alignItems: "center", justifyContent: "center", padding: 16, animation: "fadeIn 0.2s ease" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{ background: "white", borderRadius: 20, width: "100%", maxWidth: 520, boxShadow: "var(--shadow-lg)", overflow: "hidden", animation: "popIn 0.25s cubic-bezier(0.16,1,0.3,1)", maxHeight: "90vh", display: "flex", flexDirection: "column" }}>
        <div style={{ padding: "22px 24px 18px", borderBottom: "1px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "space-between", flexShrink: 0 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 36, height: 36, borderRadius: 10, background: "var(--teal-light)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18 }}>➕</div>
            <div>
              <div style={{ fontWeight: 800, fontSize: 17 }}>Add New Doctor</div>
              <div style={{ fontSize: 12, color: "var(--ink-3)" }}>Fill in the details to register a new physician</div>
            </div>
          </div>
          <button onClick={onClose} style={{ background: "var(--bg)", border: "none", borderRadius: 8, width: 32, height: 32, cursor: "pointer", fontSize: 16, color: "var(--ink-3)" }}>×</button>
        </div>

        <form onSubmit={handleSubmit} style={{ padding: "20px 24px 24px", display: "flex", flexDirection: "column", gap: 14, overflowY: "auto" }}>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Full Name *</label>
              <input value={form.name} onChange={(e) => set("name", e.target.value)} placeholder="Dr. Jane Smith" style={{ ...inputStyle, borderColor: errors.name ? "#ef4444" : "var(--border)" }} onFocus={focus} onBlur={blur} />
              {errors.name && <span style={{ fontSize: 11, color: "#ef4444" }}>{errors.name}</span>}
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Specialty *</label>
              <select value={form.specialty} onChange={(e) => set("specialty", e.target.value)} style={{ ...inputStyle, cursor: "pointer" }} onFocus={focus} onBlur={blur}>
                {SPECIALTIES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Experience *</label>
            <input value={form.exp} onChange={(e) => set("exp", e.target.value)} placeholder="e.g. 8 yrs" style={{ ...inputStyle, borderColor: errors.exp ? "#ef4444" : "var(--border)" }} onFocus={focus} onBlur={blur} />
            {errors.exp && <span style={{ fontSize: 11, color: "#ef4444" }}>{errors.exp}</span>}
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Availability — click slots to toggle (8 AM – 6 PM)</label>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 6 }}>
              {HOURS.map((h) => {
                const on = form.availability[h];
                return (
                  <button
                    key={h}
                    type="button"
                    onClick={() => set("availability", { ...form.availability, [h]: !on })}
                    style={{ padding: "7px 4px", borderRadius: 8, cursor: "pointer", fontSize: 12, fontWeight: 600, border: "1.5px solid " + (on ? "var(--teal)" : "var(--border)"), background: on ? "var(--teal-light)" : "var(--bg)", color: on ? "var(--teal)" : "var(--ink-3)", transition: "all 0.12s", textAlign: "center" }}
                  >
                    {h}
                  </button>
                );
              })}
            </div>
            <div style={{ fontSize: 12, color: "var(--ink-3)" }}>
              <span style={{ color: "var(--teal)", fontWeight: 700 }}>{Object.values(form.availability).filter(Boolean).length}</span> of {HOURS.length} slots selected
            </div>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Email Address *</label>
            <input value={form.email} onChange={(e) => set("email", e.target.value)} type="email" placeholder="doctor@medicore.io" style={{ ...inputStyle, borderColor: errors.email ? "#ef4444" : "var(--border)" }} onFocus={focus} onBlur={blur} />
            {errors.email && <span style={{ fontSize: 11, color: "#ef4444" }}>{errors.email}</span>}
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Phone Number *</label>
            <input value={form.phone} onChange={(e) => set("phone", e.target.value)} placeholder="5551234567" style={{ ...inputStyle, borderColor: errors.phone ? "#ef4444" : "var(--border)" }} onFocus={focus} onBlur={blur} />
            {errors.phone && <span style={{ fontSize: 11, color: "#ef4444" }}>{errors.phone}</span>}
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Password *</label>
            <input type="password" onChange={(e) => set("password", e.target.value)} placeholder="Minimum 6 characters" style={inputStyle} onFocus={focus} onBlur={blur} />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Status</label>
            <div style={{ display: "flex", gap: 8 }}>
              {["Active", "On Leave", "Inactive"].map((s) => (
                <button key={s} type="button" onClick={() => set("status", s)} style={{ flex: 1, padding: "8px", borderRadius: 8, cursor: "pointer", fontSize: 13, fontWeight: 600, border: "1.5px solid " + (form.status === s ? "var(--teal)" : "var(--border)"), background: form.status === s ? "var(--teal-light)" : "white", color: form.status === s ? "var(--teal)" : "var(--ink-3)", transition: "all 0.15s" }}>{s}</button>
              ))}
            </div>
          </div>

          {errors.submit && <div style={{ fontSize: 13, color: "#ef4444", background: "#fef2f2", borderRadius: 8, padding: "8px 12px" }}>{errors.submit}</div>}

          <div style={{ display: "flex", gap: 10, marginTop: 4 }}>
            <button type="button" onClick={onClose} style={{ flex: 1, padding: "12px", borderRadius: 10, border: "1.5px solid var(--border)", background: "white", fontSize: 14, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>Cancel</button>
            <button type="submit" disabled={loading} style={{ flex: 2, padding: "12px", borderRadius: 10, border: "none", background: "var(--teal)", color: "white", fontSize: 14, fontWeight: 700, cursor: "pointer" }}>
              {loading ? "Adding…" : "➕ Add Doctor"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
