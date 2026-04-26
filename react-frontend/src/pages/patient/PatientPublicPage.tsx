import { useState } from "react";
import { LoginModal } from "@/features/auth/LoginModal";
import { Navbar } from "@/features/auth/Navbar";
import { DoctorCard } from "@/features/doctors/DoctorCard";
import { useAllDoctors } from "@/features/doctors/hooks";
import type { Doctor } from "@/types/api";

export function PatientPublicPage() {
  const [showLogin, setShowLogin] = useState(false);
  const { data: doctors = [], isLoading } = useAllDoctors();

  return (
    <>
      <Navbar onLoginClick={() => setShowLogin(true)} />
      <div style={{ maxWidth: 1100, margin: "0 auto", padding: "40px 24px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: 32, flexWrap: "wrap", gap: 12 }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 600, color: "var(--teal)", letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 8 }}>Browse</div>
            <h2 style={{ fontSize: "clamp(24px,4vw,36px)", fontWeight: 800, letterSpacing: "-0.5px" }}>Our Specialists</h2>
            <p style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 8 }}>Log in to book an appointment with any of our doctors.</p>
          </div>
          <button onClick={() => setShowLogin(true)} style={{ padding: "11px 24px", borderRadius: 99, border: "none", background: "var(--teal)", color: "white", fontSize: 14, fontWeight: 700, cursor: "pointer" }}>
            🔐 Login to Book
          </button>
        </div>

        {isLoading ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(290px,1fr))", gap: 16 }}>
            {[1,2,3,4,5,6].map((i) => (
              <div key={i} style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", height: 220 }} />
            ))}
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(290px,1fr))", gap: 16 }}>
            {doctors.map((doc: Doctor) => (
              <DoctorCard key={doc.id} doc={doc} onBook={() => setShowLogin(true)} />
            ))}
          </div>
        )}
      </div>
      {showLogin && <LoginModal onClose={() => setShowLogin(false)} />}
    </>
  );
}
