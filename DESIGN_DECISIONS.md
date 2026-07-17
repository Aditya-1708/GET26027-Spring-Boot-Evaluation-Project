# Design Decisions

This document explains the choices I made when designing and building this API project.

---

# Design Decision: Layered Architecture

## Decision
I separated the code into three layers: Controller, Service, and Repository.
- Controllers handle HTTP routing and API input validation.
- Services implement the business rules and orchestrate operations.
- Repositories deal with data storage operations.

## Why I chose this
I separated the controller and service because I wanted controllers to only handle HTTP requests while business logic stays inside services. This makes the code much cleaner and easier to read. The repository layer manages how the data is stored in memory, so the service doesn't have to know about hash maps.

## Trade-offs
It requires writing more boilerplate classes, such as mapping methods and extra DTO classes, even for simple read operations.

---

# Design Decision: In-Memory Storage using ConcurrentHashMap

## Decision
Instead of setting up a database (like MySQL or H2), I stored all data in-memory inside the repositories using `ConcurrentHashMap`.

## Why I chose this
The assignment guidelines did not require a database. Using a standard `HashMap` could lead to thread safety issues when multiple API requests try to write at the same time. I chose `ConcurrentHashMap` because it is thread-safe and built into Java, which makes the setup simple and fast to run.

## Trade-offs
All data is lost whenever the application is restarted. Also, we cannot perform complex SQL queries or joins easily.

---

# Design Decision: DTOs for Requests and Responses

## Decision
I created separate classes for request payloads (like `CustomerRequest`, `ProposalRequest`) and response payloads (like `CustomerResponse`, `ProposalResponse`).

## Why I chose this
I did not want to expose the internal model entities directly to the API client. For example, the `Customer` model has internal fields like `deleted` and `deletedAt` which the client should not be able to modify during a POST request. Using DTOs allows me to customize what data is sent and received.

## Trade-offs
I had to write mapping logic to convert models to DTOs and vice-versa, which adds more code.

---

# Design Decision: Centralized Exception Handling using ControllerAdvice

## Decision
I created a `GlobalExceptionHandler` class using Spring Boot's `@RestControllerAdvice` to catch custom exceptions globally.

## Why I chose this
Instead of writing try-catch blocks in every controller method or returning custom status codes manually, I wanted a single place to catch errors. When a service throws `CustomerNotFoundException`, the handler catches it and returns a clean `ErrorResponse` with a 404 status.

## Trade-offs
All exception responses follow a single format, which might be hard to customize if one specific endpoint needs a totally different error payload.

---

# Design Decision: AtomicInteger for ID and Policy Number Generation

## Decision
I created an `IdGenerator` component using `AtomicInteger` to generate customer IDs, proposal IDs, audit IDs, and policy numbers.

## Why I chose this
Since we are not using a database with auto-increment columns, I needed a custom way to generate unique IDs. I used `AtomicInteger` to avoid duplicates if two requests try to generate an ID at the same time.

## Trade-offs
The IDs start from 1 again every time the application is restarted, which can cause duplicate IDs if we were saving data permanently.

---

# Design Decision: Separated Validation Logic (Bean Validation + Custom Validation Component)

## Decision
I split the validation into two parts: basic syntactic checks in DTOs (using annotations like `@NotBlank` and `@Pattern`) and complex business checks in a standalone `Validation` class.

## Why I chose this
Using Jakarta Bean annotations in DTOs is very easy for checking basic things like empty fields or email formats. For more complex business checks, like range limits and PAN card regex checks, I wrote a separate `Validation` class that runs programmatically in the service layer.

## Trade-offs
The validation rules are split across different places (annotations and Java code), which might make it harder to see all validations at once.

---

# Design Decision: Soft Delete for Customers

## Decision
Instead of permanently deleting a customer from the map when `DELETE` is called, I added `deleted` (boolean) and `deletedAt` (timestamp) fields to mark them as deleted.

## Why I chose this
In real applications, permanently deleting customer records is bad because we might lose transaction history or active policies. Soft deleting keeps the customer record in the map but hides them from active listings.

## Trade-offs
The soft-deleted records still consume memory in the in-memory map, and we have to manually filter out deleted records in service methods.

---

# Design Decision: PII Masking in Responses

## Decision
I wrote a `MaskPii` utility class to mask sensitive customer details like email, mobile number, and PAN card before returning them in the response DTO.

## Why I chose this
Personal Identifiable Information (PII) like full email addresses and phone numbers should not be shown publicly to prevent misuse. I mask the first few characters/digits of these fields in the response mapper.

## Trade-offs
Once masked in the response DTO, the client cannot view the original value anymore, which might require a separate decrypted API if needed in the future.

---

# Design Decision: Audit Trail Logging

## Decision
I implemented an `Audit` entity and an `AuditRepository` to track actions, specifically when a proposal is submitted.

## Why I chose this
Submitting a proposal is a critical action. Having an audit trail helps track when the proposal was submitted and what action took place, which is useful for debugging.

## Trade-offs
We are logging audit entries in memory, which means the audit history is lost on application restart just like other data.

---

# Design Decision: Enums for Terms, Statuses, and Frequencies

## Decision
I used Java enums like `PolicyTerm`, `PolicyStatus`, and `PaymentFrequency` to define allowed terms, policy statuses, and payment frequencies.

## Why I chose this
Using enums prevents typos and restricts input values to allowed options. For example, `PolicyTerm` only permits values like 10, 15, 20, 25, 30.

## Trade-offs
If we need to support new term durations or statuses, we must modify the Java code and recompile, rather than updating a database table.

---

# Design Decision: Unit Testing with JUnit and Mockito

## Decision
I wrote unit tests for service layers and validation logic using Mockito to mock repository classes.

## Why I chose this
I wanted to test the service business logic (like updating fields or submitting proposals) without launching a full Spring context or calling actual repository storage, which keeps tests fast.

## Trade-offs
Mocking configuration can be tedious and doesn't verify if everything works together end-to-end.

---

# Overall Design

The overall architecture is a simple three-tier Spring Boot REST API. It uses in-memory collections instead of a database to store data, which fits the assignment scope perfectly and makes the code very fast to compile and test. While this setup is not suitable for a production system since all data is lost on restart, it demonstrates how to structure controllers, service business logic, and repositories cleanly. I also implemented basic features like soft deletes, DTO mapping, and input validation to show how clean code principles can be applied in a simplified project. Overall, the design focuses on meeting the basic requirements using simple Java structures without over-complicating the setup.
