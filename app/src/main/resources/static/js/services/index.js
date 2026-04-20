import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = `${API_BASE_URL}/admin/login`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;
const PATIENT_API =  `${API_BASE_URL}/patient`;

window.onload = () => {
  const adminLoginBtn = document.getElementById("adminLogin");
  const doctorLoginBtn = document.getElementById("doctorLogin");
  const patientLoginBtn = document.getElementById("patientLogin");

  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", () => openModal("patientLogin"));
  }

  if (adminLoginBtn) {
    adminLoginBtn.addEventListener("click", () => openModal("adminLogin"));
  }

  if (doctorLoginBtn) {
    doctorLoginBtn.addEventListener("click", () => openModal("doctorLogin"));
  }
};

window.patientLoginHandler = async  () => {
  const email = document.getElementById("email").value
  const password = document.getElementById("password").value
  const data = {email, password,}

  try {
    const response =  await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    if(response.ok){
      const data = await response.json()
      console.log(data)
      localStorage.setItem("token", data.token)
      selectRole("loggedPatient")
    } else {
      alert("Invalid patient credentials. Please try again.");
    }
  } catch (error) {
    alert("An error occurred while logging in. Please try again later.");
  }
}

window.adminLoginHandler = async () => {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const admin = { username, password };

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(admin),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      selectRole("admin");
    } else {
      alert("Invalid admin credentials. Please try again.");
    }
  } catch (error) {
    alert("An error occurred while logging in. Please try again later.");
  }
};

window.doctorLoginHandler = async () => {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  const doctor = { email, password };

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);

      console.log(data.token)
      selectRole("doctor");
    } else {
      alert("Invalid doctor credentials. Please try again.");
    }
  } catch (error) {
    console.error(error);
    alert("An error occurred while logging in. Please try again later.");
  }
};
