import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import type { UserRole } from "@/types/api";

interface AuthContextValue {
  token: string | null;
  role: UserRole | null;
  patientId: number | null;
  login: (token: string, role: UserRole) => void;
  logout: () => void;
  setPatientId: (id: number) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [role, setRole] = useState<UserRole | null>(null);
  const [patientId, setPatientId] = useState<number | null>(null);

  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedRole = localStorage.getItem("userRole") as UserRole | null;
    if (storedToken && storedRole) {
      setToken(storedToken);
      setRole(storedRole);
    }
  }, []);

  const login = (newToken: string, newRole: UserRole) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("userRole", newRole);
    setToken(newToken);
    setRole(newRole);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    setToken(null);
    setRole(null);
    setPatientId(null);
  };

  return (
    <AuthContext.Provider value={{ token, role, patientId, login, logout, setPatientId }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
