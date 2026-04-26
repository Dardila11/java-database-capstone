import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { adminLogin, patientLogin, doctorLogin } from "./api";
import { getPatientByToken } from "@/features/patients/api";

const ROLES = [
  { key: "admin" as const, label: "Admin", icon: "🛡️" },
  { key: "doctor" as const, label: "Doctor", icon: "🩺" },
  { key: "patient" as const, label: "Patient", icon: "🧑" },
];

interface Props {
  onClose: () => void;
}

export function LoginModal({ onClose }: Props) {
  const { login, setPatientId } = useAuth();
  const navigate = useNavigate();
  const [role, setRole] = useState<"admin" | "doctor" | "patient">("patient");
  const [credential, setCredential] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const switchRole = (r: typeof role) => {
    setRole(r);
    setCredential("");
    setPassword("");
    setError("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      let token: string;
      if (role === "admin") {
        const res = await adminLogin(credential, password);
        token = res.token;
        login(token, "admin");
        onClose();
        navigate("/admin");
      } else if (role === "doctor") {
        const res = await doctorLogin(credential, password);
        token = res.token;
        login(token, "doctor");
        onClose();
        navigate("/doctor");
      } else {
        const res = await patientLogin(credential, password);
        token = res.token;
        login(token, "loggedPatient");
        const patientData = await getPatientByToken(token);
        setPatientId(patientData.patient.id);
        onClose();
        navigate("/patient/home");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{ position: "fixed", inset: 0, zIndex: 1000, background: "oklch(0% 0 0 / 0.45)", backdropFilter: "blur(6px)", display: "flex", alignItems: "center", justifyContent: "center", padding: 16, animation: "fadeIn 0.2s ease" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{ background: "white", borderRadius: 20, width: "100%", maxWidth: 440, boxShadow: "var(--shadow-lg)", overflow: "hidden", animation: "slideUp 0.25s cubic-bezier(0.16,1,0.3,1)" }}>
        {/* Gradient header */}
        <div style={{ background: "linear-gradient(135deg,oklch(52% 0.17 192),oklch(52% 0.17 240))", padding: "28px 28px 24px" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            <div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "white", letterSpacing: "-0.3px" }}>Welcome back</div>
              <div style={{ fontSize: 14, color: "rgba(255,255,255,0.75)", marginTop: 4 }}>Sign in to access MediCore</div>
            </div>
            <button onClick={onClose} style={{ background: "rgba(255,255,255,0.15)", border: "none", borderRadius: "50%", width: 32, height: 32, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", color: "white", fontSize: 18 }}>×</button>
          </div>
          <div style={{ display: "flex", gap: 8, marginTop: 20 }}>
            {ROLES.map((r) => (
              <button
                key={r.key}
                onClick={() => switchRole(r.key)}
                style={{
                  flex: 1, padding: "10px 6px", borderRadius: 10, cursor: "pointer",
                  border: "2px solid " + (role === r.key ? "white" : "rgba(255,255,255,0.25)"),
                  background: role === r.key ? "rgba(255,255,255,0.22)" : "transparent",
                  color: "white", fontSize: 13, fontWeight: role === r.key ? 700 : 400,
                  transition: "all 0.15s", display: "flex", flexDirection: "column", alignItems: "center", gap: 4,
                }}
              >
                <span style={{ fontSize: 20 }}>{r.icon}</span>{r.label}
              </button>
            ))}
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ padding: "22px 28px 28px", display: "flex", flexDirection: "column", gap: 14 }}>
          <div style={{ fontSize: 13, color: "var(--ink-3)", background: "var(--teal-light)", borderRadius: 8, padding: "8px 12px" }}>
            💡 Enter your {role === "admin" ? "username" : "email"} and password
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>
              {role === "admin" ? "Username" : "Email address"}
            </label>
            <input
              value={credential}
              onChange={(e) => setCredential(e.target.value)}
              type={role === "admin" ? "text" : "email"}
              required
              placeholder={role === "admin" ? "admin username" : "doctor@clinic.com"}
              style={{ padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%" }}
              onFocus={(e) => (e.target.style.borderColor = "var(--teal)")}
              onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
            />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Password</label>
            <div style={{ position: "relative" }}>
              <input
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                type={showPw ? "text" : "password"}
                required
                style={{ padding: "10px 40px 10px 13px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%" }}
                onFocus={(e) => (e.target.style.borderColor = "var(--teal)")}
                onBlur={(e) => (e.target.style.borderColor = "var(--border)")}
              />
              <button type="button" onClick={() => setShowPw(!showPw)} style={{ position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", cursor: "pointer", color: "var(--ink-3)", fontSize: 14 }}>
                {showPw ? "🙈" : "👁️"}
              </button>
            </div>
          </div>

          {error && (
            <div style={{ fontSize: 13, color: "#ef4444", background: "#fef2f2", borderRadius: 8, padding: "8px 12px" }}>{error}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{ width: "100%", padding: "13px", borderRadius: 10, background: loading ? "var(--teal-mid)" : "var(--teal)", border: "none", color: "white", fontSize: 15, fontWeight: 700, cursor: loading ? "default" : "pointer", display: "flex", alignItems: "center", justifyContent: "center", gap: 8 }}
          >
            {loading ? "Signing in…" : `Sign in as ${ROLES.find((r) => r.key === role)?.label}`}
          </button>
        </form>
      </div>
    </div>
  );
}
