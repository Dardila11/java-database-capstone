// header.js

import {openModal} from "./modals.js";

const headerContainer = document.getElementById("header");
function getLogoPath() {
  const isRoot = window.location.pathname === "/" || window.location.pathname.endsWith("/index.html");
  return isRoot ? "assets/images/logo/logo.png" : "../assets/images/logo/logo.png";
}

function logout() {
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.setItem("userRole", "patient");
  localStorage.removeItem("token");
  window.location.href = "/pages/patientDashboard.html";
}

function checkSession(role, token) {
  const sessionRoles = ["loggedPatient", "admin", "doctor"];
  if (sessionRoles.includes(role) && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return false; // indicate session is invalid
  }
  return true; // session is valid
}

function getHeaderNavContent(role) {
  switch (role) {
    case "admin":
      return `
        <button id="addDocBtn" class="adminBtn">Add Doctor</button>
        <a href="#" id="logoutBtn">Logout</a>`;
    case "doctor":
      return `
        <button id="doctorHomeBtn" class="adminBtn">Home</button>
        <a href="#" id="logoutBtn">Logout</a>`;
    case "patient":
      return `
        <button id="patientLoginBtn" class="adminBtn">Login</button>
        <button id="patientSignupBtn" class="adminBtn">Sign Up</button>`;
    case "loggedPatient":
      return `
        <button id="patientHomeBtn" class="adminBtn">Home</button>
        <button id="patientAppointmentsBtn" class="adminBtn">Appointments</button>
        <a href="#" id="logoutPatientBtn">Logout</a>`;
    default:
      return '';
  }
}

function attachEventListeners() {
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) addDocBtn.addEventListener("click", () => openModal('addDoctor'));

    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", logout);

    const doctorHomeBtn = document.getElementById("doctorHomeBtn");
    if (doctorHomeBtn) doctorHomeBtn.addEventListener("click", () => {
        if(typeof selectRole === 'function') {
            selectRole('doctor')
        }
    });

    const patientLoginBtn = document.getElementById("patientLoginBtn");
    if (patientLoginBtn) patientLoginBtn.addEventListener("click", () => openModal('patientLogin'));

    const patientSignupBtn = document.getElementById("patientSignupBtn");
    if (patientSignupBtn) patientSignupBtn.addEventListener("click", () => openModal('patientSignup'));

    const patientHomeBtn = document.getElementById("patientHomeBtn");
    const token = localStorage.getItem("token");
    if (patientHomeBtn) patientHomeBtn.addEventListener("click", () => window.location.href=`../../loggedPatientDashboard/${token}`);

    const patientAppointmentsBtn = document.getElementById("patientAppointmentsBtn");

    if (patientAppointmentsBtn) patientAppointmentsBtn.addEventListener("click", () => window.location.href='/pages/patientAppointments.html');

    const logoutPatientBtn = document.getElementById("logoutPatientBtn");
    if (logoutPatientBtn) logoutPatientBtn.addEventListener("click", logoutPatient);
}

function renderHeader() {
  if (!headerContainer) return;

  const isRoot = window.location.pathname.endsWith("/");
  const logoPath = getLogoPath();

  if (isRoot) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerContainer.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="${logoPath}" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  if (!checkSession(role, token)) {
    return; // Stop rendering if session is invalid and redirection is happening
  }

  // if a role is logged in
  const navContent = getHeaderNavContent(role);

  const headerHTML = `
    <header class="header">
      <div class="logo-section">
        <img src="${logoPath}" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav>${navContent}</nav>
    </header>`;

  headerContainer.innerHTML = headerHTML;
  attachEventListeners();
}

// Initial render
renderHeader();
