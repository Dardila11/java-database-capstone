import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

await loadDoctorCards();


async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    const content = document.getElementById("content");
    content.innerHTML = "";
    doctors.forEach((doctor) => {
      content.appendChild(createDoctorCard(doctor));
    });
  } catch (error) {
    console.error("Error loading doctor cards:", error);
  }

  const searchBar = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  searchBar.addEventListener("input", filterDoctorsOnChange);
  timeFilter.addEventListener("change", filterDoctorsOnChange);
  specialtyFilter.addEventListener("change", filterDoctorsOnChange);
}

async function filterDoctorsOnChange() {
  try {
    const name = document.getElementById("searchBar").value.trim() || null;
    console.log(name)
    const time = document.getElementById("timeFilter").value || null;
    const specialty = document.getElementById("specialtyFilter").value || null;

    const data = await filterDoctors(name, time, specialty);
    const doctors = data.doctors;
    const content = document.getElementById("content");

    if (doctors && doctors.length > 0) {
      renderDoctorCards(doctors);
    } else {
      content.innerHTML = "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    alert("An error occurred while filtering doctors. Please try again.");
  }
}

function renderDoctorCards(doctors) {
  const content = document.getElementById("content");
  content.innerHTML = "";
  doctors.forEach((doctor) => {
    content.appendChild(createDoctorCard(doctor));
  });
}

window.adminAddDoctor = async function () {
  const name = document.getElementById("doctorName").value.trim();
  console.log(name)
  const email = document.getElementById("doctorEmail").value.trim();
  const phone = document.getElementById("doctorPhone").value.trim();
  const password = document.getElementById("doctorPassword").value.trim();
  const specialty = document.getElementById("specialization").value;
  const availableTimes = Array.from(
    document.querySelectorAll("input[name='availability']:checked")
  ).map((cb) => cb.value);

  const token = localStorage.getItem("token");
  if (!token) {
    alert("Authentication token not found. Please log in again.");
    return;
  }

  const doctor = { name, email, phone, password, specialty, availableTimes };

  const { success, message } = await saveDoctor(doctor, token);

  if (success) {
    alert(message);
    document.getElementById("modal").style.display = "none";
    location.reload();
  } else
  {
    alert(message);
  }
};
