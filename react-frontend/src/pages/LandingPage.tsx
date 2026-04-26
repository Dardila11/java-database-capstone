import { useState } from "react";
import { LoginModal } from "@/features/auth/LoginModal";
import { Navbar } from "@/features/auth/Navbar";
import { DoctorCard } from "@/features/doctors/DoctorCard";
import { useAllDoctors } from "@/features/doctors/hooks";
import { STATS_DATA } from "@/lib/constants";
import type { Doctor } from "@/types/api";

export function LandingPage() {
  const [showLogin, setShowLogin] = useState(false);
  const { data: doctors = [], isLoading } = useAllDoctors();

  return (
    <>
      <Navbar onLoginClick={() => setShowLogin(true)} />

      {/* Hero */}
      <section style={{ background: "linear-gradient(160deg,oklch(20% 0.04 240) 0%,oklch(28% 0.06 220) 100%)", padding: "80px 24px 100px", textAlign: "center", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", inset: 0, backgroundImage: "linear-gradient(oklch(100% 0 0 / 0.04) 1px,transparent 1px),linear-gradient(90deg,oklch(100% 0 0 / 0.04) 1px,transparent 1px)", backgroundSize: "48px 48px", pointerEvents: "none" }} />
        <div style={{ position: "relative", maxWidth: 720, margin: "0 auto" }}>
          <div style={{ display: "inline-flex", alignItems: "center", gap: 8, background: "oklch(52% 0.17 192 / 0.15)", border: "1px solid oklch(52% 0.17 192 / 0.4)", borderRadius: 99, padding: "6px 14px", marginBottom: 24 }}>
            <span style={{ width: 7, height: 7, borderRadius: "50%", background: "oklch(52% 0.17 192)", display: "inline-block", boxShadow: "0 0 8px oklch(52% 0.17 192)" }} />
            <span style={{ color: "oklch(75% 0.12 192)", fontSize: 13, fontWeight: 600 }}>Now serving 12,400+ patients</span>
          </div>
          <h1 style={{ fontSize: "clamp(36px,6vw,64px)", fontWeight: 800, color: "white", lineHeight: 1.1, letterSpacing: "-1.5px", marginBottom: 20 }}>
            Smart Care,<br /><span style={{ color: "oklch(72% 0.15 192)" }}>Seamlessly Managed.</span>
          </h1>
          <p style={{ fontSize: "clamp(15px,2vw,18px)", color: "oklch(70% 0.02 240)", lineHeight: 1.7, maxWidth: 520, margin: "0 auto 36px" }}>
            MediCore connects patients, doctors, and administrators in one intelligent platform.
          </p>
          <div style={{ display: "flex", gap: 12, justifyContent: "center", flexWrap: "wrap" }}>
            <button onClick={() => setShowLogin(true)} style={{ padding: "14px 28px", borderRadius: 99, border: "none", background: "oklch(52% 0.17 192)", color: "white", fontSize: 16, fontWeight: 700, cursor: "pointer" }}>
              Get Started →
            </button>
            <button style={{ padding: "14px 28px", borderRadius: 99, border: "1.5px solid oklch(100% 0 0 / 0.2)", background: "transparent", color: "white", fontSize: 16, fontWeight: 600, cursor: "pointer" }}>
              Watch Demo
            </button>
          </div>
        </div>
      </section>

      {/* Stats bar */}
      <div style={{ background: "white", borderBottom: "1px solid var(--border)", display: "flex", justifyContent: "center", flexWrap: "wrap" }}>
        {STATS_DATA.map((s, i) => (
          <div key={i} style={{ padding: "20px 40px", textAlign: "center", borderRight: i < STATS_DATA.length - 1 ? "1px solid var(--border)" : "none" }}>
            <div style={{ fontSize: 24, fontWeight: 800, color: "var(--teal)", letterSpacing: "-0.5px" }}>{s.value}</div>
            <div style={{ fontSize: 13, color: "var(--ink-3)", marginTop: 2 }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Specialists section */}
      <section style={{ maxWidth: 1100, margin: "0 auto", padding: "64px 24px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: 32, flexWrap: "wrap", gap: 12 }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 600, color: "var(--teal)", letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 8 }}>Meet Our Team</div>
            <h2 style={{ fontSize: "clamp(24px,4vw,36px)", fontWeight: 800, letterSpacing: "-0.5px" }}>Our Specialists</h2>
            <p style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 8 }}>World-class physicians across 24 departments.</p>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8, background: "oklch(52% 0.17 192 / 0.08)", border: "1.5px solid oklch(52% 0.17 192 / 0.25)", borderRadius: 10, padding: "10px 16px" }}>
            <span style={{ fontSize: 14 }}>🔒</span>
            <span style={{ fontSize: 13, color: "var(--teal)", fontWeight: 600 }}>Login required to book appointments</span>
          </div>
        </div>

        {isLoading ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(290px,1fr))", gap: 16 }}>
            {[1,2,3,4,5,6].map((i) => (
              <div key={i} style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 20, height: 200, animation: "pulse 2s infinite" }} />
            ))}
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(290px,1fr))", gap: 16 }}>
            {doctors.map((doc: Doctor) => (
              <DoctorCard key={doc.id} doc={doc} onBook={() => setShowLogin(true)} />
            ))}
          </div>
        )}
      </section>

      {/* CTA */}
      <section style={{ background: "linear-gradient(135deg,oklch(52% 0.17 192),oklch(52% 0.17 262))", padding: "60px 24px", textAlign: "center" }}>
        <h2 style={{ fontSize: "clamp(22px,4vw,36px)", fontWeight: 800, color: "white", marginBottom: 12, letterSpacing: "-0.5px" }}>Ready to take control of your health?</h2>
        <p style={{ fontSize: 16, color: "rgba(255,255,255,0.75)", marginBottom: 28 }}>Join thousands of patients, doctors, and clinics on MediCore.</p>
        <button onClick={() => setShowLogin(true)} style={{ padding: "14px 32px", borderRadius: 99, border: "2px solid white", background: "white", color: "var(--teal)", fontSize: 16, fontWeight: 800, cursor: "pointer" }}>
          Sign In Now
        </button>
      </section>

      {/* Footer */}
      <footer style={{ background: "oklch(18% 0.02 240)", padding: "28px 24px", textAlign: "center" }}>
        <div style={{ fontSize: 20, fontWeight: 800, color: "white", marginBottom: 6 }}>MediCore</div>
        <div style={{ fontSize: 13, color: "oklch(50% 0.01 240)" }}>© 2026 MediCore Clinic Systems. All rights reserved.</div>
      </footer>

      {showLogin && <LoginModal onClose={() => setShowLogin(false)} />}
    </>
  );
}
