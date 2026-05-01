// prescriptionServices.js
import { API_BASE_URL } from '../config/config.js'

const PRESCRIPTION_API = API_BASE_URL + "/prescription"
export async function savePrescription(prescription, token) {
  try {
    const response = await fetch(`${PRESCRIPTION_API}`, {
      method: "POST",
      headers: {
        "Content-type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(prescription)
    });
    const result = await response.json();
    return { success: response.ok, message: result.message }
  }
  catch (error) {
    console.error("Error :: savePrescription :: ", error)
    return { success: false, message: "Network error" }
  }
}

export async function getPrescription(appointmentId, token) {
  try {
    const response = await fetch(`${PRESCRIPTION_API}/${appointmentId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      }
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Failed to fetch prescription:", errorData);
      throw new Error(errorData.message || "Unable to fetch prescription");
    }

    const result = await response.json();
    console.log(result)
    console.log(result)
    return result; // This should be your prescription object
  } catch (error) {
    console.error("Error :: getPrescription ::", error);
    throw error;
  }
}
