import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

interface Props {
  onLoginClick: () => void;
}

export function Navbar({ onLoginClick }: Props) {
  const { token, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav style={{ position: "sticky", top: 0, zIndex: 100, background: "rgba(255,255,255,0.92)", backdropFilter: "blur(12px)", borderBottom: "1px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 32px", height: 60 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }} onClick={() => navigate("/")}>
        <div style={{ width: 28, height: 28, borderRadius: 8, background: "var(--teal)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16 }}>⚕️</div>
        <span style={{ fontSize: 18, fontWeight: 800, letterSpacing: "-0.3px", color: "var(--ink)" }}>MediCore</span>
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
        {!token ? (
          <>
            {["Services", "Doctors", "About"].map((l) => (
              <span key={l} style={{ fontSize: 14, color: "var(--ink-2)", fontWeight: 500, cursor: "pointer" }}>{l}</span>
            ))}
            <button onClick={onLoginClick} style={{ padding: "8px 20px", borderRadius: 99, border: "none", background: "var(--teal)", color: "white", fontSize: 14, fontWeight: 700, cursor: "pointer" }}>Login</button>
          </>
        ) : (
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <span style={{ fontSize: 18, color: "var(--ink-3)" }}>🔔</span>
            <div style={{ width: 32, height: 32, borderRadius: "50%", background: "var(--teal-light)", border: "2px solid var(--teal)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 700, color: "var(--teal)" }}>
              {role === "admin" ? "AD" : role === "doctor" ? "DR" : "PT"}
            </div>
            <span style={{ fontSize: 14, fontWeight: 600, color: "var(--ink)", textTransform: "capitalize" }}>{role}</span>
            <button onClick={handleLogout} style={{ padding: "6px 14px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>Sign out</button>
          </div>
        )}
      </div>
    </nav>
  );
}
