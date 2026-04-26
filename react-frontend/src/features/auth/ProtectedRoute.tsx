import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";
import type { UserRole } from "@/types/api";

interface Props {
  allowedRoles: UserRole[];
}

export function ProtectedRoute({ allowedRoles }: Props) {
  const { token, role } = useAuth();
  if (!token || !role || !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
