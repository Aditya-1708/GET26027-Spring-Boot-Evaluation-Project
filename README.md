# Policy Proposal Processing API

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 4.1.0](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit_5-5.x-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

---

## Project Overview

The **Policy Proposal Processing API** is a Spring Boot REST API that simulates a simplified insurance policy proposal processing system. It enables managing customer profiles, creating and validating policy proposals, submitting proposals to generate official policies, and logging audit events for submissions. 

To ensure lightweight execution, the application uses thread-safe, in-memory data structures for storage, completely removing the dependency on external databases.

---

## Features

- **Customer Management**: Register, retrieve, and update customer profiles.
- **Proposal Management**: Create policy proposals associated with registered customers, and retrieve proposals by ID.
- **Proposal Submission**: Submit proposals, triggering secondary validations, status transition, and policy number generation.
- **Audit Management**: Track successful submissions through system-generated audit logs.
- **Reference Master APIs**: Fetch allowed values dynamically for fields like policy terms and payment frequencies.
- **Validation Framework**: Utilizes both standard Java Bean Validation (`jakarta.validation`) and a programmatic validator component for complex business rules.
- **Global Exception Handling**: Centrally handles domain and request errors to return consistent, user-friendly JSON error payloads.
- **DTO Architecture**: Separation of concerns using request and response DTO mapping patterns to decouple the controller contract from domain models.
- **Thread-safe In-Memory Storage**: Employs `ConcurrentHashMap` inside repositories for state management, simulating data persistence.
- **JUnit 5 Unit Tests**: High-coverage unit tests for validation rules, services, and mappings.

---

## Technology Stack

- **Java**: 21 (OpenJDK)
- **Framework**: Spring Boot 4.1.0
- **Build Tool**: Gradle
- **Utilities**: Lombok
- **Testing**: JUnit 5, Mockito

---

## Project Structure

```text
GET26027 -  Spring Boot Evaluation Project/
├── src/
│   ├── main/
│   │   ├── java/com/policy/api/
│   │   │   ├── PolicyApiApplication.java       # Spring Boot Application Entry Point
│   │   │   ├── config/                         # Application configuration classes
│   │   │   ├── constants/                      # Policy enums (PaymentFrequency, PolicyStatus, PolicyTerm, ReferenceCategory)
│   │   │   ├── controller/                     # REST API Controllers
│   │   │   ├── dto/                            # DTOs (Request, Response, ErrorResponse)
│   │   │   ├── exception/                      # Custom Exceptions & Global Exception Handler
│   │   │   ├── model/                          # Core Domain Entities (Customer, Proposal, Audit)
│   │   │   ├── repository/                     # In-Memory Repositories using ConcurrentHashMap
│   │   │   ├── service/                        # Service layer implementing business workflows
│   │   │   ├── util/                           # Utilities (Thread-safe Atomic ID Generators)
│   │   │   └── validation/                     # Programmatic validation components
│   │   └── resources/
│   │       └── application.properties          # Server and application configuration parameters
│   └── test/
│       └── java/com/policy/api/
│           ├── PolicyApiApplicationTests.java  # Smoke test class
│           ├── service/                        # Service layer unit tests
│           └── validation/                     # Validation rules unit tests
```

---

## Setup Instructions

### Prerequisites
- JDK 21 installed and configured on your system path.

### 1. Clone the Repository
```bash
git clone <repository-url>
cd "GET26027 -  Spring Boot Evaluation Project"
```

### 2. Build the Project
* **Windows**:
  ```cmd
  gradlew.bat build
  ```
* **Linux/macOS**:
  ```bash
  ./gradlew build
  ```

### 3. Run the Application
* **Windows**:
  ```cmd
  gradlew.bat bootRun
  ```
* **Linux/macOS**:
  ```bash
  ./gradlew bootRun
  ```
The server will start running locally at: `http://localhost:8080`

---

## Running Tests

Execute the unit test suite containing JUnit 5 and Mockito tests:

* **Windows**:
  ```cmd
  gradlew.bat test
  ```
* **Linux/macOS**:
  ```bash
  ./gradlew test
  ```

---

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/customers` | Register a new customer. |
| **GET** | `/customers/` | Retrieve a list of all registered customers. |
| **GET** | `/customers/{customerID}` | Retrieve details of a specific customer by ID. |
| **PUT** | `/customers/{customerID}` | Update details of an existing customer by ID. |
| **POST** | `/proposals` | Create a new policy proposal (Status: `PENDING`). |
| **GET** | `/proposals/{proposalId}` | Retrieve details of a specific proposal by ID. |
| **POST** | `/proposals/{proposalId}/submit` | Submit a proposal (generates policy number, updates Status to `ACCEPTED`, logs audit). |
| **GET** | `/audits` | Retrieve all audit log entries. |
| **GET** | `/reference-master/{category}` | Fetch allowed reference data values by category (`POLICY_TERM` or `PAYMENT_FREQUENCY`). |

---

## Sample Requests

### Create Customer (`POST /customers`)
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "gender": "Male",
  "mobileNumber": "9876543210",
  "email": "john.doe@example.com",
  "address": "123 Ring Road, New Delhi"
}
```

### Create Proposal (`POST /proposals`)
```json
{
  "customerId": "CUS001",
  "policyTerm": 20,
  "sumAssured": 500000,
  "premium": 12000,
  "PAN": "ABCDE1234F",
  "nominee": "Jane Doe",
  "paymentFrequency": "YEARLY"
}
```

---

## Sample Success Responses

### Customer Registration Response
```json
{
  "customerId": "CUS001",
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "gender": "Male",
  "mobileNumber": "9876543210",
  "email": "john.doe@example.com",
  "address": "123 Ring Road, New Delhi"
}
```

### Proposal Creation Response (Pending)
```json
{
  "proposalId": "PRO001",
  "customerId": "CUS001",
  "policyTerm": "TERM_20",
  "sumAssured": 500000,
  "PAN": "ABCDE1234F",
  "nominee": "Jane Doe",
  "paymentFrequency": "YEARLY",
  "policyUid": 0,
  "policyStatus": "PENDING"
}
```

### Proposal Submission Response (Accepted)
```json
{
  "proposalId": "PRO001",
  "customerId": "CUS001",
  "policyTerm": "TERM_20",
  "sumAssured": 500000,
  "PAN": "ABCDE1234F",
  "nominee": "Jane Doe",
  "paymentFrequency": "YEARLY",
  "policyUid": 100001,
  "policyStatus": "ACCEPTED"
}
```

### Audit Retrieval Response (`GET /audits`)
```json
[
  {
    "auditId": "AUD001",
    "proposalId": "PRO001",
    "action": "Proposal submitted successfully",
    "timestamp": "2026-07-17T04:15:30.123"
  }
]
```

### Reference Master Response (`GET /reference-master/POLICY_TERM`)
```json
{
  "category": "POLICY_TERM",
  "values": [
    10,
    15,
    20,
    25,
    30
  ]
}
```

---

## Error Response Format

The REST API catches custom domain exceptions and outputs standardized JSON payloads. Here is an example of a validation failure response:

```json
{
  "timestamp": "2026-07-17T04:20:15.584",
  "status": 400,
  "error": "Bad Request",
  "message": "Customer age must be between 18 and 65 years.",
  "path": "/customers"
}
```

---

## Validation Rules

### Customer Constraints
- **Age limit**: Must be between **18** and **65** years (inclusive).
- **Mandatory fields**: First name, Last name, Gender, Email, Address, and Mobile Number.
- **Mobile number pattern**: Must be a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9 (`^[6-9]\d{9}$`).
- **Email validation**: Must follow standard email format constraints.

### Proposal Constraints
- **Policy term**: Must strictly match one of the predefined term lengths (in years): `10`, `15`, `20`, `25`, or `30`.
- **Sum assured range**: Must be between **₹100,000** and **₹50,000,000** (inclusive).
- **Minimum premium**: Must be a minimum of **₹5,000** annually.
- **PAN requirement**: Mandatory if the annual premium is greater than **₹50,000** and must match the format `^[A-Z]{5}\d{4}[A-Z]$`.
- **Nominee rule**: A nominee is required and must **not** match the Customer's full name (first name + last name, check is case-insensitive).
- **Policy Number**: Assigned strictly upon submission; prior to submission, `policyUid` defaults to `0`.

---

## Testing

The project has a robust testing suite implemented using **JUnit 5** and **Mockito**. Tests are separated into service unit tests and validation tests:
- **Service Tests**: Mock the dependencies (repositories, generators) using `@ExtendWith(MockitoExtension.class)` to verify logical paths in `CustomerService`, `ProposalService`, `AuditService`, and `ReferenceMasterService`.
- **Validation Tests**: Test both valid and boundary/edge inputs against the custom business constraints defined in `Validation.java`.

---
