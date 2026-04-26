import { useState } from "react";
import { Avatar } from "@/features/doctors/Avatar";
import { getInitials, todayISO } from "@/lib/utils";
import { useBookAppointment } from "./hooks";
import { useAuth } from "@/features/auth/AuthContext";
import type { Doctor } from "@/types/api";

const COLORS = ["#0d9488","#7c3aed","#0369a1","#b45309","#be185d","#065f46"];

interface Props {
  doctor: Doctor;
  onClose: () => void;
}

export function BookingModal({ doctor, onClose }: Props) {
  const { token, patientId } = useAuth();
  const { mutateAsync: book } = useBookAppointment(token);
  const [date, setDate] = useState(todayISO());
  const [slot, setSlot] = useState(doctor.availableTimes[0] ?? "");
  const [note, setNote] = useState("");
  const [confirmed, setConfirmed] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const color = COLORS[doctor.id % COLORS.length];

  const confirm = async () => {
    if (!patientId) { setError("Could not identify patient. Please re-login."); return; }
    setLoading(true);
    try {
      const [startTime] = slot.split("-");
      const appointmentTime = `${date}T${startTime}:00`;
      await book({ doctor: { id: doctor.id }, patient: { id: patientId }, appointmentTime, status: 0 });
      setConfirmed(true);
      setTimeout(onClose, 1800);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Booking failed");
      setLoading(false);
    }
  };

  return (
    <div
      style={{ position: "fixed", inset: 0, zIndex: 1000, background: "oklch(0% 0 0 / 0.45)", backdropFilter: "blur(6px)", display: "flex", alignItems: "center", justifyContent: "center", padding: 16, animation: "fadeIn 0.2s ease" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{ background: "white", borderRadius: 20, width: "100%", maxWidth: 420, boxShadow: "var(--shadow-lg)", animation: "slideUp 0.25s cubic-bezier(0.16,1,0.3,1)", overflow: "hidden" }}>
        {confirmed ? (
          <div style={{ padding: "48px 32px", textAlign: "center" }}>
            <div style={{ fontSize: 56, marginBottom: 16 }}>✅</div>
            <div style={{ fontSize: 20, fontWeight: 800 }}>Appointment Booked!</div>
            <div style={{ fontSize: 14, color: "var(--ink-3)", marginTop: 8 }}>Your appointment with {doctor.name} is confirmed.</div>
          </div>
        ) : (
          <>
            <div style={{ background: "var(--bg)", padding: "20px 24px 16px", borderBottom: "1px solid var(--border)", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div style={{ fontWeight: 700, fontSize: 17 }}>Book Appointment</div>
              <button onClick={onClose} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--ink-3)", fontSize: 20 }}>×</button>
            </div>
            <div style={{ padding: "18px 24px 24px", display: "flex", flexDirection: "column", gap: 14 }}>
              <div style={{ display: "flex", gap: 12, alignItems: "center", background: "var(--teal-light)", borderRadius: 12, padding: "12px" }}>
                <Avatar initials={getInitials(doctor.name)} color={color} size={44} />
                <div>
                  <div style={{ fontWeight: 700, fontSize: 15 }}>{doctor.name}</div>
                  <div style={{ fontSize: 13, color: "var(--teal)", fontWeight: 600 }}>{doctor.specialty}</div>
                </div>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Preferred Date</label>
                <input type="date" value={date} min={todayISO()} onChange={(e) => setDate(e.target.value)} style={{ padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%" }} />
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Available Time Slot</label>
                <select value={slot} onChange={(e) => setSlot(e.target.value)} style={{ padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%" }}>
                  {doctor.availableTimes.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Notes (optional)</label>
                <textarea value={note} onChange={(e) => setNote(e.target.value)} rows={3} placeholder="Describe your symptoms…" style={{ padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", resize: "none", width: "100%" }} />
              </div>

              {error && <div style={{ fontSize: 13, color: "#ef4444", background: "#fef2f2", borderRadius: 8, padding: "8px 12px" }}>{error}</div>}

              <button onClick={confirm} disabled={loading || !slot} style={{ width: "100%", padding: "13px", borderRadius: 10, background: "var(--teal)", border: "none", color: "white", fontSize: 15, fontWeight: 700, cursor: loading ? "default" : "pointer", opacity: !slot ? 0.6 : 1 }}>
                {loading ? "Booking…" : "Confirm Booking"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
