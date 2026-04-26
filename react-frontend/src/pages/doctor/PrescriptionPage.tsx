import { useState, useEffect } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { usePrescription, useSavePrescription } from "@/features/prescriptions/hooks";

export function PrescriptionPage() {
  const { appointmentId } = useParams<{ appointmentId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { token } = useAuth();

  const mode = searchParams.get("mode") ?? "view";
  const patientName = searchParams.get("patientName") ?? "";
  const apptId = appointmentId ? parseInt(appointmentId) : null;

  const { data: prescriptions = [], isLoading } = usePrescription(apptId, token);
  const { mutateAsync: save } = useSavePrescription(token);

  const [form, setForm] = useState({ patientName, medication: "", dosage: "", doctorNotes: "" });
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (prescriptions.length > 0) {
      const p = prescriptions[0];
      setForm({ patientName: p.patientName, medication: p.medication, dosage: p.dosage, doctorNotes: p.doctorNotes });
    }
  }, [prescriptions]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!apptId) return;
    try {
      await save({ ...form, appointmentId: apptId });
      setSuccess(true);
      setTimeout(() => navigate("/doctor"), 1500);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save prescription");
    }
  };

  const inputStyle: React.CSSProperties = {
    padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)",
    fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%",
  };
  const readOnlyStyle: React.CSSProperties = { ...inputStyle, background: "#f8fafc", color: "var(--ink-3)" };
  const isView = mode === "view" || prescriptions.length > 0;

  if (success) {
    return (
      <div style={{ maxWidth: 600, margin: "60px auto", textAlign: "center", padding: "48px 32px", background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)" }}>
        <div style={{ fontSize: 56, marginBottom: 16 }}>✅</div>
        <div style={{ fontSize: 20, fontWeight: 800 }}>Prescription Saved!</div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 600, margin: "40px auto", padding: "0 24px" }}>
      <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 28, boxShadow: "var(--shadow-sm)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
          <button onClick={() => navigate("/doctor")} style={{ background: "none", border: "none", cursor: "pointer", fontSize: 20, color: "var(--ink-3)" }}>←</button>
          <div>
            <div style={{ fontSize: 18, fontWeight: 800 }}>{isView ? "View" : "Add"} Prescription</div>
            <div style={{ fontSize: 13, color: "var(--ink-3)" }}>Appointment #{apptId}</div>
          </div>
        </div>

        {isLoading ? (
          <div style={{ fontSize: 14, color: "var(--ink-3)", textAlign: "center", padding: "40px 0" }}>Loading…</div>
        ) : (
          <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Patient Name</label>
              <input value={form.patientName} readOnly style={readOnlyStyle} />
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Medication *</label>
              <input value={form.medication} onChange={(e) => setForm((f) => ({ ...f, medication: e.target.value }))} required readOnly={isView} style={isView ? readOnlyStyle : inputStyle} placeholder="e.g. Amoxicillin 500mg" />
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Dosage *</label>
              <textarea value={form.dosage} onChange={(e) => setForm((f) => ({ ...f, dosage: e.target.value }))} required readOnly={isView} rows={3} style={{ ...isView ? readOnlyStyle : inputStyle, resize: "none" }} placeholder="e.g. 1 tablet 3x daily with food" />
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Doctor Notes</label>
              <textarea value={form.doctorNotes} onChange={(e) => setForm((f) => ({ ...f, doctorNotes: e.target.value }))} readOnly={isView} rows={3} style={{ ...isView ? readOnlyStyle : inputStyle, resize: "none" }} placeholder="Additional notes…" />
            </div>

            {error && <div style={{ fontSize: 13, color: "#ef4444", background: "#fef2f2", borderRadius: 8, padding: "8px 12px" }}>{error}</div>}

            <div style={{ display: "flex", gap: 10 }}>
              <button type="button" onClick={() => navigate("/doctor")} style={{ flex: 1, padding: "12px", borderRadius: 10, border: "1.5px solid var(--border)", background: "white", fontSize: 14, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
                Back
              </button>
              {!isView && (
                <button type="submit" style={{ flex: 2, padding: "12px", borderRadius: 10, border: "none", background: "var(--teal)", color: "white", fontSize: 14, fontWeight: 700, cursor: "pointer" }}>
                  Save Prescription
                </button>
              )}
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
