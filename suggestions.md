# Code Review Suggestions

This document presents a comprehensive technical review of the **Policy Proposal Processing API** project. As a Senior Java Backend Engineer and Technical Reviewer, I have evaluated the codebase architecture, design choices, validation mechanisms, exception handling, data layer implementation, and testing strategies.

---

## Technical Issues and Code Review Findings

### 1. Potential NullPointerException in PAN Validation

**Severity**
- High

**Location**
- **File**: `src/main/java/com/policy/api/validation/Validation.java`
- **Class**: `Validation`
- **Method**: `validateProposal`

**Problem**
When validating proposal rules, the validation logic checks if the annual premium exceeds ₹50,000. If so, it verifies that a PAN number is provided and conforms to a regular expression. However, it calls `PAN.matches(...)` without a null check. Because `PAN` is a nullable field in `ProposalRequest` (it does not have any `@NotNull` or `@NotBlank` constraint), passing a null value for `PAN` with a premium > 50,000 triggers a `NullPointerException`.

**Impact**
If a client submits a proposal with an annual premium greater than 50,000 but omits the `PAN` field (or sends it as null), the application will crash internally, causing a `500 Internal Server Error` instead of returning a clean validation error message (such as a `400 Bad Request`).

**Recommendation**
Add a null/empty check for the `PAN` field inside the conditional block before invoking the `.matches()` method.

---

### 2. Manual Instantiation of Managed Spring Component (MaskPii)

**Severity**
- Medium

**Location**
- **File**: `src/main/java/com/policy/api/service/ProposalService.java`
- **Class**: `ProposalService`
- **Method**: Class field instantiation

**Problem**
The class `MaskPii` is defined as a Spring bean using `@Component`. In `CustomerService`, it is properly injected via constructor injection. However, in `ProposalService`, it is manually instantiated via `new MaskPii()` at the field level, bypassing Spring's dependency injection container.

**Impact**
This violates the dependency injection pattern and tightly couples `ProposalService` to a concrete instance of `MaskPii`. This prevents standard Mockito unit testing where `MaskPii` might need to be mocked or stubbed, and overrides Spring’s lifecycle management for that bean.

**Recommendation**
Modify the constructor of `ProposalService` to accept `MaskPii` as a parameter and let the Spring container inject the bean. Remove the direct field instantiation.

---

### 3. Missing Exception Handler for ProposalAlreadySubmittedException

**Severity**
- Medium

**Location**
- **File**: `src/main/java/com/policy/api/exception/GlobalExceptionHandler.java`
- **Class**: `GlobalExceptionHandler`
- **Method**: N/A (Missing handler)

**Problem**
The `ProposalAlreadySubmittedException` is thrown in `ProposalService` when a user attempts to submit a proposal that has already been accepted. While this exception extends `ApiException`, the global exception handler only handles specific subclass exceptions (`InvalidCustomerException` and `InvalidProposalException`) explicitly. It does not have a handler mapped for `ProposalAlreadySubmittedException` or the base `ApiException` class.

**Impact**
Since the exception is not explicitly mapped, it is caught by the fallback `Exception.class` handler. This returns an `HTTP 500 Internal Server Error` with a generic message "An unexpected error occurred" instead of an `HTTP 400 Bad Request` with the actual reason.

**Recommendation**
Add a handler for `ProposalAlreadySubmittedException` (or map the parent `ApiException` class) to the bad request handler method in `GlobalExceptionHandler` to ensure correct HTTP response mapping.

---

### 4. Leakage of ID Generation logic into DTO and Duplicate Generation Calls

**Severity**
- Medium

**Location**
- **File**: `src/main/java/com/policy/api/service/ProposalService.java` and `src/main/java/com/policy/api/service/AuditService.java`
- **Class**: `ProposalService`, `AuditService`
- **Method**: `submitProposal`, `createAudit`, `mapToModel`

**Problem**
In `ProposalService.submitProposal()`, when creating an audit trail, the service calls `generator.generateAuditId()` to pass it to the `AuditRequest` constructor. However, `AuditService.createAudit()` maps the request to the domain model by calling `generator.generateAuditId()` again. The value passed via the request DTO is completely discarded and overwritten.

**Impact**
Including a read-only generated ID (`auditId`) inside a request DTO violates DTO encapsulation principles (clients should not supply entity IDs during creation). Moreover, this double-invocation increments the underlying thread-safe atomic counter twice for a single record, wasting ID sequences.

**Recommendation**
Remove the `auditId` field from `AuditRequest` DTO and avoid generating the ID in `ProposalService`. The `AuditService` should internally handle ID generation during entity mapping.

---

### 5. Lack of Customer Soft-Delete Checks in Proposal Workflows

**Severity**
- Medium

**Location**
- **File**: `src/main/java/com/policy/api/service/ProposalService.java`
- **Class**: `ProposalService`
- **Method**: `getProposal`, `submitProposal`, `deleteProposal`

**Problem**
The application supports soft-deleting customers. However, once a customer is soft-deleted, their pre-existing proposals remain active. A client can still fetch, delete, or submit (transition to `ACCEPTED` status) proposals associated with a soft-deleted customer because `ProposalService` does not check if the customer is active.

**Impact**
This violates referential integrity and business domain rules. It allows insurance policies/proposals to be approved for deleted/inactive customers.

**Recommendation**
Add checks in `ProposalService`'s proposal retrieval and mutation methods to verify that the associated customer is present and not soft-deleted.

---

### 6. Non-Standard Starters and Build Configuration Issues

**Severity**
- Medium

**Location**
- **File**: `build.gradle`
- **Class**: N/A
- **Method**: Dependencies block

**Problem**
The build configuration imports non-standard Spring Boot starter dependencies:
- `spring-boot-starter-validation-test`
- `spring-boot-starter-webmvc-test`

Additionally, it uses `spring-boot-starter-webmvc` instead of the standard `spring-boot-starter-web`, and references an unreleased Spring Boot version `4.1.0`.

**Impact**
This can lead to build portability issues. Missing tomcat/server engines in `webmvc` might fail to start the embedded web container when launching as a standalone application. Standard testing libraries like JUnit/AssertJ/Mockito are not bundled properly due to the missing standard `spring-boot-starter-test`.

**Recommendation**
Replace the unreleased Spring Boot version with a stable release (e.g. `3.x.x`). Replace the non-standard testing and web starters with the official `spring-boot-starter-test` and `spring-boot-starter-web` dependencies.

---

### 7. Thread-Safety Concerns with In-Memory Object Mutations

**Severity**
- Medium

**Location**
- **File**: `src/main/java/com/policy/api/service/CustomerService.java`
- **Class**: `CustomerService`
- **Method**: `updateCustomer` / `deleteCustomer`

**Problem**
While the repository map uses `ConcurrentHashMap` for thread-safe map operations, the entities retrieved from the map (like `Customer`) are mutated directly in the service class via setters without locking or synchronization.

**Impact**
If multiple concurrent HTTP threads attempt to modify the same customer reference, a race condition could result in partially written/inconsistent state.

**Recommendation**
Consider implementing synchronization or defensive copying/cloning when retrieving and updating objects from the in-memory repository to guarantee thread-safe mutations.

---

### 8. Duplicate Import Statements in Exception Handler

**Severity**
- Low

**Location**
- **File**: `src/main/java/com/policy/api/exception/GlobalExceptionHandler.java`
- **Class**: N/A
- **Method**: N/A

**Problem**
Import statements for `jakarta.servlet.http.HttpServletRequest`, `org.springframework.http.HttpStatus`, and `org.springframework.http.ResponseEntity` are duplicated in the import section.

**Impact**
This is a minor code smell that degrades code cleaniness and readability.

**Recommendation**
Clean up the duplicate imports from the file header.

---

### 9. Redundant Validation Logic Between API and Service Layers

**Severity**
- Low

**Location**
- **File**: `src/main/java/com/policy/api/validation/Validation.java`
- **Class**: `Validation`
- **Method**: `validateCustomer`

**Problem**
The programmatic validation layer checks if a customer's age is between 18 and 65. However, this exact rule is already verified at the entry controller level via `@Min(18)` and `@Max(65)` annotations on the `CustomerRequest` DTO.

**Impact**
Duplicate validation logic leads to redundant checks and potential maintenance synchronization overhead if the age bounds ever change.

**Recommendation**
Consolidate the check. If the controller enforces syntactic limits, the service layer can rely on it and avoid duplicate manual checks, or keep a single source of truth.

---

### 10. Inconsistent Counter Declarations in IdGenerator

**Severity**
- Low

**Location**
- **File**: `src/main/java/com/policy/api/util/IdGenerator.java`
- **Class**: `IdGenerator`
- **Method**: N/A (Class fields)

**Problem**
The counter for policy numbers (`counter`) is declared as `static final`, while `customerCount`, `proposalCount`, and `auditCount` are declared as non-static instance variables. 

**Impact**
This causes inconsistent state sharing behavior. Since the bean is configured as a Spring singleton, this is not an active bug, but it represents poor practice. If the bean scope is ever changed, it will lead to unexpected differences in behavior.

**Recommendation**
Declare all counter variables consistently as instance fields of the singleton class.

---

## Overall Review

Below is the score evaluation of the project across various dimensions (scored out of 10):

| Area | Score | Notes / Rationale |
| :--- | :---: | :--- |
| **Architecture** | 8/10 | Well-separated layered architecture. Entities and relationships are correctly decoupled using IDs instead of circular references. |
| **Code Quality** | 7/10 | Code is generally clean, though some minor code smells (duplicate imports, redundant validations, inconsistent declarations) are present. |
| **Spring Boot Usage** | 6/10 | Manual instantiation of `MaskPii` and non-standard starters in `build.gradle` pull the score down. |
| **Java Best Practices** | 7/10 | Good use of `AtomicInteger` and `ConcurrentHashMap`, but mutable in-memory object modifications are not synchronized. |
| **Testing** | 8/10 | Comprehensive test cases covering most edge cases. Test cases compile and run successfully. |
| **Readability** | 9/10 | Variable names and class structures are intuitive and easy to understand. |
| **Maintainability** | 7/10 | High maintainability overall, but exception mappings and validation logic are slightly scattered. |
| **Project Structure** | 9/10 | Clear package layout distinguishing controllers, services, repositories, and models. |
| **Documentation** | 9/10 | Excellent documentation in `DESIGN_DECISIONS.md` explaining the workflow and architecture. |
| **Assignment Completeness** | 9/10 | Fulfills all functional requirements (Customer CRUD, Proposal creation/submission, auditing, and soft delete). |

**Overall Score**: **7.9 / 10**

---

## Graduate Engineering Trainee (GET) Assignment Review

### Would this project pass?
**Yes**, this project would very likely **PASS** a GET evaluation. 

The submission demonstrates a solid grasp of REST API development, layered architecture, unit testing, and DTO usage. The clean separation of concerns and thread-safe collections usage would put it in the upper bracket of typical trainee submissions.

### Strengths:
1. **Layered Structure**: Trainees often bundle business logic inside controllers or model classes. This project maintains a strict boundary separating Controller, Service, and Repository layers.
2. **DTO Mapping**: The correct implementation of separate request and response DTOs showcases an understanding of API contract versioning and payload encapsulation.
3. **Thread Safety**: Demonstrating knowledge of concurrent execution by using `ConcurrentHashMap` and `AtomicInteger` in an in-memory repository shows foresight.
4. **Comprehensive Test Coverage**: The existence of distinct unit tests for validation, controllers, and services is excellent.

### Weaknesses:
1. **NullPointer Risk in Validation**: The lack of defensive programming when validating nullable fields like `PAN` is a classic oversight.
2. **Bypassing Dependency Injection**: Manually instantiating `MaskPii` using the `new` keyword in a Spring service is a noticeable deviation from framework conventions.
3. **Exception Routing**: The oversight in mapping `ProposalAlreadySubmittedException` results in raw HTTP 500 error leaks.
4. **Cascading State Consistency**: A soft-deleted customer should not have active, submittable proposals, which is a business rule loophole.

### Things that would impress reviewers:
- Decoupling models using foreign keys (IDs) instead of full object reference mappings, avoiding recursive JSON serialization issues.
- Thorough use of JUnit 5 `assertAll()` for grouped assertions in test files.
- Documenting technical decisions in a clear `DESIGN_DECISIONS.md` markdown file.

### Things reviewers may question:
- Why `MaskPii` was manually instantiated in `ProposalService` but correctly injected in `CustomerService`.
- Why non-standard test starters (e.g. `spring-boot-starter-validation-test`) and unreleased Spring Boot versions are specified in the build configuration.
- Why the audit ID is passed inside the `AuditRequest` DTO if it's immediately regenerated by the mapping layer.
