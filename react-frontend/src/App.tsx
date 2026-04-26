import { HashRouter, Routes, Route } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/features/auth/AuthContext";
import { ProtectedRoute } from "@/features/auth/ProtectedRoute";
import { queryClient } from "@/lib/queryClient";

import { LandingPage } from "@/pages/LandingPage";
import { AdminDashboard } from "@/pages/admin/AdminDashboard";
import { DoctorDashboard } from "@/pages/doctor/DoctorDashboard";
import { PrescriptionPage } from "@/pages/doctor/PrescriptionPage";
import { PatientRecordPage } from "@/pages/doctor/PatientRecordPage";
import { PatientPublicPage } from "@/pages/patient/PatientPublicPage";
import { PatientDashboard } from "@/pages/patient/PatientDashboard";
import { PatientAppointments } from "@/pages/patient/PatientAppointments";
import { UpdateAppointmentPage } from "@/pages/patient/UpdateAppointmentPage";

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <HashRouter>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/patient" element={<PatientPublicPage />} />

            <Route element={<ProtectedRoute allowedRoles={["admin"]} />}>
              <Route path="/admin" element={<AdminDashboard />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={["doctor"]} />}>
              <Route path="/doctor" element={<DoctorDashboard />} />
              <Route path="/prescription/:appointmentId" element={<PrescriptionPage />} />
              <Route path="/doctor/patient/:patientId" element={<PatientRecordPage />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={["loggedPatient"]} />}>
              <Route path="/patient/home" element={<PatientDashboard />} />
              <Route path="/patient/appointments" element={<PatientAppointments />} />
              <Route path="/patient/appointments/:id/edit" element={<UpdateAppointmentPage />} />
            </Route>
          </Routes>
        </HashRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
