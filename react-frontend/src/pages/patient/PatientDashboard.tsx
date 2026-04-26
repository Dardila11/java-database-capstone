import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { DoctorCard } from "@/features/doctors/DoctorCard";
import { BookingModal } from "@/features/appointments/BookingModal";
import { AppointmentRow } from "@/features/appointments/AppointmentRow";
import { useAllDoctors } from "@/features/doctors/hooks";
import { usePatientByToken, usePatientAppointments } from "@/features/patients/hooks";
import { useCancelAppointment } from "@/features/appointments/hooks";
import type { Doctor, AppointmentDTO } from "@/types/api";

export function PatientDashboard() {
  const { token, logout, patientId, setPatientId } = useAuth();
  const navigate = useNavigate();
  const [bookingDoctor, setBookingDoctor] = useState<Doctor | null>(null);

  const { data: patientData } = usePatientByToken(token);
  const { data: doctors = [] } = useAllDoctors();
  const { data: appointments = [], isLoading: loadingAppts } = usePatientAppointments(patientId, token);
  const { mutateAsync: cancel } = useCancelAppointment(token);

  useEffect(() => {
    if (patientData?.patient && !patientId) {
      setPatientId(patientData.patient.id);
    }
  }, [patientData, patientId, setPatientId]);

  const handleLogout = () => { logout(); navigate("/"); };
  const upcomingAppts = appointments.filter((a: AppointmentDTO) => a.status === 0);

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "32px 24px" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 32 }}>
        <div>
          <div style={{ fontSize: 12, color: "var(--ink-3)" }}>Hello,</div>
          <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-0.4px" }}>
            {patientData?.patient.name ?? "Patient"}{" "}
            <span style={{ fontSize: 14, color: "#be185d", background: "#fce7f3", borderRadius: 6, padding: "2px 8px" }}>Patient</span>
          </div>
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          <button onClick={() => navigate("/patient/appointments")} style={{ display: "flex", alignItems: "center", gap: 6, padding: "9px 18px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
            📅 My Appointments
          </button>
          <button onClick={handleLogout} style={{ display: "flex", alignItems: "center", gap: 6, padding: "9px 16px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
            🚪 Sign out
          </button>
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
        {/* Appointments list */}
        <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 24, boxShadow: "var(--shadow-sm)" }}>
          <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>Your Appointments</div>
          {loadingAppts ? (
            <div style={{ fontSize: 14, color: "var(--ink-3)" }}>Loading…</div>
          ) : upcomingAppts.length === 0 ? (
            <div style={{ marginTop: 16, padding: "12px", background: "var(--bg)", borderRadius: 10, fontSize: 13, color: "var(--ink-3)", textAlign: "center" }}>
              No upcoming appointments.{" "}
              <button onClick={() => doctors.length > 0 && setBookingDoctor(doctors[0])} style={{ color: "var(--teal)", background: "none", border: "none", cursor: "pointer", fontWeight: 600, fontSize: 13, fontFamily: "inherit" }}>
                Book one →
              </button>
            </div>
          ) : (
            upcomingAppts.slice(0, 4).map((a: AppointmentDTO) => (
              <AppointmentRow
                key={a.id}
                appt={a}
                perspective="patient"
                onEdit={(appt) => navigate(`/patient/appointments/${appt.id}/edit`)}
                onCancel={(id) => cancel(id)}
              />
            ))
          )}
        </div>

        {/* Right panel */}
        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {/* Health summary */}
          <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 22, boxShadow: "var(--shadow-sm)" }}>
            <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Patient Info</div>
            {[
              ["Name", patientData?.patient.name ?? "—"],
              ["Email", patientData?.patient.email ?? "—"],
              ["Phone", patientData?.patient.phone ?? "—"],
              ["Address", patientData?.patient.address ?? "—"],
            ].map(([k, v]) => (
              <div key={k} style={{ display: "flex", justifyContent: "space-between", padding: "7px 0", borderBottom: "1px solid var(--border)", fontSize: 13 }}>
                <span style={{ color: "var(--ink-3)" }}>{k}</span>
                <span style={{ fontWeight: 600 }}>{v}</span>
              </div>
            ))}
          </div>

          {/* Browse CTA */}
          <div style={{ background: "linear-gradient(135deg,var(--teal),var(--indigo))", borderRadius: "var(--radius)", padding: 22, color: "white" }}>
            <div style={{ fontSize: 15, fontWeight: 700 }}>Browse Doctors</div>
            <div style={{ fontSize: 13, opacity: 0.8, marginTop: 4, marginBottom: 14 }}>{doctors.length} specialists available now</div>
            <button onClick={() => navigate("/patient")} style={{ background: "rgba(255,255,255,0.2)", border: "1.5px solid rgba(255,255,255,0.4)", borderRadius: 99, padding: "8px 18px", color: "white", fontSize: 13, fontWeight: 700, cursor: "pointer" }}>
              Find a Doctor →
            </button>
          </div>
        </div>
      </div>

      {/* Available doctors */}
      <div style={{ marginTop: 32 }}>
        <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>Book an Appointment</div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(280px,1fr))", gap: 14 }}>
          {doctors.slice(0, 6).map((doc: Doctor) => (
            <DoctorCard
              key={doc.id}
              doc={doc}
              onBook={() => setBookingDoctor(doc)}
            />
          ))}
        </div>
      </div>

      {bookingDoctor && <BookingModal doctor={bookingDoctor} onClose={() => setBookingDoctor(null)} />}
    </div>
  );
}
