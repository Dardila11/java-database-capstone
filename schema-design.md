## MySQL Database Design

### Table: patients

- id: INT, Primary Key, Auto Increment
- name: VARCHAR(), Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(20), null
- gender: VARCHAR(20), Null
- blood_group: VARCHAR(10), Null
- allergies: VARCHAR(1000), Null
- address: VARCHAR(255), Null
- is_active: BOOLEAN, Not Null, Default = TRUE
- created_at: DATETIME, Not Null, Default = CURRENT_TIMESTAMP

### Table: doctors

- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- dob: DATETIME, Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone_number: VARCHAR(20), null
- speciality: VARCHAR(100), Not Null
- license_number: VARCHAR(100), Not Null, Unique
- created_at: DATETIME, Not Null, Default = CURRENT_TIMESTAMP

### Table: admin

- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- dob: DATETIME, Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone_number: VARCHAR(20), null
- role: VARCHAR(20), Not Null, Default = 'admin'
- is_active: Boolean, Not Null, Default = TRUE
- created_at: DATETIME, Not Null, Default = CURRENT_TIMESTAMP

### Table: appointments

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key -> doctors(id)
- patient_id: INT, Foreign Key -> patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- created_at: DATETIME, Not Null, Default = CURRENT_TIMESTAMP

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```
