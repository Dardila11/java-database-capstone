// render.js

function selectRole(role) {
  setRole(role);
  const token = localStorage.getItem('token');
  const routes = {
    admin: token ? `/adminDashboard?token=${token}` : null,
    doctor: token ? `/doctorDashboard?token=${token}` : null,
    patient: "/pages/patientDashboard.html",
    loggedPatient: token ? `/loggedPatientDashboard?token=${token}`  : null
  };

  const target = routes[role];
  if (target) {
    window.location.href = target;
  }
}


function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/";
  }
}
