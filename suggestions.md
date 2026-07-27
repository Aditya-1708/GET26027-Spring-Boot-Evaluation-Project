# Executive Summary

This document presents a comprehensive, senior-level pre-submission code review of the **Policy Proposal Processing API** project. As a Senior Java Backend Engineer, I have analyzed the entire codebase, including controllers, services, repositories, exception handling, data mapping, build configurations, and unit tests.

### Current State
The project is structured as a standard three-tier REST API utilizing Spring Boot and Java 21. It implements in-memory repositories using thread-safe collections (`ConcurrentHashMap`) and counters (`AtomicInteger`), and provides distinct request/response DTOs, custom exception wrappers, and a global exception interceptor. 

### GET Evaluation Recommendation: **FAIL (as-is) / High PASS (with suggested fixes)**
* **Why it would fail in its current state:** The project currently suffers from a broken test suite (`./gradlew test` fails out-of-the-box due to compilation and runtime test failures), and a critical logic bug in the validation layer that completely bypasses the mandatory PAN card check for high-premium policies. Automated evaluation pipelines would reject this submission immediately.
* **Why it will easily pass with fixes:** The architectural fundamentals are highly sound. The separation of concerns, package organization, use of DTOs, thread-safe constructs, and manual mapping logic show a level of care and structure that is well above typical graduate trainee submissions. Resolving the identified compilation, test, and logic errors will turn this into an excellent submission.

---

# High Priority Tasks

These are critical issues that must be resolved before submitting the assignment.

## Critical: Incorrect Logic and Bypassed PAN Validation

### Location
`src/main/java/com/policy/api/validation/Validation.java` → `Validation` → `validateProposal`

### Problem
The logic to enforce a mandatory PAN card check for annual premiums greater than ₹50,000 is written as:
```java
else if(annualPremium > 50000 && !(PAN == null || PAN.isBlank() || PAN.matches("^[A-Z]{5}\\d{4}[A-Z]$")))
```
If a proposal request is submitted with an annual premium > 50,000 but the PAN field is completely omitted or sent as null, the expression `PAN == null` short-circuits to `true`, making the parenthesized expression `true`. The negation operator `!` then turns this into `false`. As a result, the code skips this block, goes to the `else` branch, and returns `"true"` (validation passes). The validation error is only returned if a client provides a non-blank, invalid PAN string.

### Why it matters
This is a critical business rule violation. In practice, PAN is never validated as mandatory. Anyone can submit a high-premium policy with no PAN and successfully bypass tax and financial compliance checks.

### Suggested Fix
Invert the condition within the exclamation mark or rewrite it clearly. The service should fail validation if the premium is high AND the PAN is either null, blank, or doesn't match the regex pattern.
- DO NOT use the outer negation. Instead, write the condition to check if `PAN == null` OR `PAN.isBlank()` OR `!PAN.matches(...)`.

### Priority
Critical

---

## Critical: Broken Unit Test (NullPointerException)

### Location
`src/test/java/com/policy/api/service/CustomerServiceTest.java` → `CustomerServiceTest` → `shouldDeleteCustomerSuccessfully`

### Problem
The test method fails with a `NullPointerException` during execution. Inside `CustomerService.deleteCustomer(customerId)`, the service calls `hasActiveProposals(customerId)`, which queries `proposalRepository.getByCustomerId(customerId)`. Since `proposalRepository` is a Mockito mock and its `getByCustomerId` method is not stubbed for `"CUST001"` in this test, Mockito returns a default value of `null` instead of an empty list. The service then attempts to check `fetchedProposals.isEmpty()`, which throws the NPE.

### Why it matters
Broken unit tests fail the gradle test lifecycle and stop build pipelines, which would lead to an automated reject of the assignment submission.

### Suggested Fix
Add a stubbing in the test setup for `shouldDeleteCustomerSuccessfully` using Mockito's `when(proposalRepository.getByCustomerId(...))` to return an empty list or a list of soft-deleted proposals.

### Priority
Critical

---

## Critical: Broken Unit Test (UnnecessaryStubbingException)

### Location
`src/test/java/com/policy/api/service/ProposalServiceTest.java` → `ProposalServiceTest` → `shouldSubmitProposalSuccessfully`

### Problem
The test method stubs `when(generator.generateAuditId()).thenReturn("AUD001")`. However, the method under test (`ProposalService.submitProposal`) constructs an `AuditRequest` DTO which does not invoke `generateAuditId()`. The ID is generated downstream inside `AuditService.createAudit()`. Under Mockito's strict JUnit 5 extension, stubbing a mock method that is never invoked during the test execution throws an `UnnecessaryStubbingException`.

### Why it matters
Causes the unit test execution to fail, causing build pipeline failures.

### Suggested Fix
Remove the unused stubbing for `generator.generateAuditId()` from the test method setup.

### Priority
Critical

---

## High: Unhandled `InvalidCustomerException` leading to HTTP 500

### Location
`src/main/java/com/policy/api/exception/GlobalExceptionHandler.java` → `GlobalExceptionHandler`

### Problem
`InvalidCustomerException` is thrown by the service layer when a customer fails programmatic validation (such as the age bounds check). However, `InvalidCustomerException` extends `RuntimeException` directly (rather than the base custom `ApiException`), and the `GlobalExceptionHandler` does not define an explicit `@ExceptionHandler` for it.

### Why it matters
Because it is not handled, the exception falls back to the generic `Exception.class` handler. This returns an HTTP `500 Internal Server Error` with the message "An unexpected error occurred." instead of an HTTP `400 Bad Request` with the actual validation message (e.g., "Customer age must be between 18 and 65 years.").

### Suggested Fix
Refactor `InvalidCustomerException` to inherit from the custom `ApiException` class (similar to `InvalidProposalException`), or define a specific `@ExceptionHandler(InvalidCustomerException.class)` in the `GlobalExceptionHandler` that returns `HttpStatus.BAD_REQUEST`.

### Priority
High

---

# Medium Priority Tasks

These issues should be resolved to meet clean code standards, Spring framework guidelines, and proper domain integration.

## Medium: Soft-Deleted Customers with Active Proposals

### Location
`src/main/java/com/policy/api/service/ProposalService.java` → `ProposalService` → `getProposal` / `submitProposal` / `deleteProposal`

### Problem
The application supports soft-deleting customers. When a customer is soft-deleted, their existing proposals remain active in memory. The `ProposalService` does not check the status of the associated customer when performing operations. A user can still fetch, delete, or submit (approve and generate a policy number for) proposals belonging to a soft-deleted customer.

### Why it matters
This violates referential integrity and business logic. Insurance policies should not be approved or managed for inactive/soft-deleted customers.

### Suggested Fix
Add validation in the proposal service methods to look up the associated customer using `CustomerService.getCustomer()`. Since `CustomerService` throws `CustomerNotFoundException` for soft-deleted customers, this will automatically prevent mutations on proposals of inactive customers.

### Priority
Medium

---

## Medium: Spring Boot Dependency Injection Bypass

### Location
`src/main/java/com/policy/api/service/CustomerService.java` & `src/main/java/com/policy/api/service/ProposalService.java` → Fields

### Problem
The class `MaskPii` is annotated with `@Component` to be managed as a bean in the Spring application context. However, inside both `CustomerService` and `ProposalService`, it is manually instantiated as a field variable:
```java
private final MaskPii maskPii = new MaskPii();
```
This completely bypasses Spring's IoC container.

### Why it matters
This violates dependency injection design principles and tightly couples the service layers to a concrete class. It prevents configuring `MaskPii` as a mock or stub in unit testing or applying Spring AOP/lifecycle management.

### Suggested Fix
Remove the manual `new MaskPii()` instantiations. Declare `MaskPii` as a constructor argument in both `CustomerService` and `ProposalService` to allow Spring to inject the managed bean.

### Priority
Medium

---

## Medium: Non-standard Build Configuration and Invalid Versions

### Location
`build.gradle` → plugins & dependencies

### Problem
The project configuration contains several dependency issues:
1. It uses Spring Boot version `'4.1.0'`. Spring Boot 4.x is not a released version.
2. It includes non-existent testing dependencies: `'org.springframework.boot:spring-boot-starter-validation-test'` and `'org.springframework.boot:spring-boot-starter-webmvc-test'`.
3. It uses `spring-boot-starter-webmvc` instead of the standard `spring-boot-starter-web`.
4. It is missing the standard testing framework wrapper `'org.springframework.boot:spring-boot-starter-test'`.

### Why it matters
This configuration is brittle and non-standard. The lack of standard starters breaks test utility version management and might prevent test execution on clean environments without cached custom repositories.

### Suggested Fix
Set the Spring Boot version to a stable 3.x release (such as `'3.4.1'`). Clean up the dependencies by replacing the invalid validation and webmvc test starters with the single standard starter:
- `testImplementation 'org.springframework.boot:spring-boot-starter-test'`
- Replace `spring-boot-starter-webmvc` with `spring-boot-starter-web`.

### Priority
Medium

---

# Low Priority Tasks

These are code quality improvements, naming standards, and optimization suggestions.

## Low: Thread-Safety Risks on In-Memory Object Mutations

### Location
`src/main/java/com/policy/api/service/CustomerService.java` → `CustomerService` → `updateCustomer`

### Problem
Although the repository uses `ConcurrentHashMap` for thread safety, the entity instances stored inside the map are shared references. In `updateCustomer()`, the service retrieves the customer and calls setters directly on the mutable entity reference:
```java
existingCustomer.setFirstName(customerRequest.getFirstName());
// ...
```

### Why it matters
In a concurrent environment (e.g., multiple HTTP threads making requests), two threads modifying the same customer reference concurrently can cause race conditions, resulting in an inconsistent or partially updated customer state.

### Suggested Fix
Use defensive copying. Retrieve the entity, create a copy, modify the fields on the copied instance, and save/replace it back in the repository.

### Priority
Low

---

## Low: Redundant and Dead Code in Customer/Proposal Deletion Check

### Location
`src/main/java/com/policy/api/service/ProposalService.java` → `ProposalService` → `canDeleteCustomer`

### Problem
The method `canDeleteCustomer` in `ProposalService` implements the exact same logic as `hasActiveProposals` in `CustomerService`. Furthermore, `ProposalService.canDeleteCustomer` is never used anywhere in the codebase.

### Why it matters
Dead code increases cognitive overhead, and duplicate code violates the DRY (Don't Repeat Yourself) principle.

### Suggested Fix
Remove the unused `canDeleteCustomer` method from `ProposalService`.

### Priority
Low

---

## Low: Misleading Helper Method Name

### Location
`src/main/java/com/policy/api/service/CustomerService.java` → `CustomerService` → `hasActiveProposals`

### Problem
The helper method checks if a customer has active proposals. However, the boolean logic is:
```java
if (fetchedProposals.isEmpty()) { return true; }
return fetchedProposals.stream().allMatch(Proposal::isDeleted);
```
If a customer has NO proposals (empty list), it returns `true`. If all proposals are soft-deleted, it returns `true`. If the customer has an active proposal, it returns `false`.

### Why it matters
The method returns `true` when there are **no** active proposals, and `false` when there **are** active proposals. This completely contradicts its name, making the code highly confusing to read and maintain.

### Suggested Fix
Rename the method to `hasNoActiveProposals` or invert the return logic to match the name.

### Priority
Low

---

## Low: Inconsistent Counter Field Scope

### Location
`src/main/java/com/policy/api/util/IdGenerator.java` → `IdGenerator`

### Problem
The policy number counter is declared as a static variable:
```java
private static final AtomicInteger counter = new AtomicInteger(100000);
```
Meanwhile, the customer, proposal, and audit counters are instance fields (`private final AtomicInteger`).

### Why it matters
Since `IdGenerator` is registered as a Spring singleton bean, there is no need for `counter` to be static. Mixing static and instance states is inconsistent and could cause state synchronization mismatches if the bean scope is ever changed.

### Suggested Fix
Declare `counter` as a standard instance variable: `private final AtomicInteger counter = ...`.

### Priority
Low

---

## Low: Java Naming Convention Violation in Model

### Location
`src/main/java/com/policy/api/model/Proposal.java` → `Proposal`

### Problem
The fields `Nominee` and `PolicyUid` start with uppercase letters.

### Why it matters
This violates standard Java camelCase naming conventions. It also causes Lombok to generate capital-letter getters/setters (`getNominee`, `getPolicyUid`) which does not align with standard bean specifications and can cause mapping issues in serialization libraries.

### Suggested Fix
Rename the fields to `nominee` and `policyUid`.

### Priority
Low

---

## Low: Redundant Customer Age Validation

### Location
`src/main/java/com/policy/api/validation/Validation.java` → `Validation` → `validateCustomer`

### Problem
The programmatic check inside the service layer verifies if age is between 18 and 65 years. However, this rule is already validated at the controller binding level via JSR-380 annotations `@Min(18)` and `@Max(65)` inside `CustomerRequest`.

### Why it matters
Duplicate validation logic violates the single source of truth principle, leading to maintenance issues if boundaries are modified.

### Suggested Fix
Rely on the JSR-380 DTO annotations and remove the redundant manual check from `Validation`.

### Priority
Low

---

## Low: Return Type `Object` in Reference Controller

### Location
`src/main/java/com/policy/api/controller/ReferenceMasterController.java` → `ReferenceMasterController` → `getReferenceData`

### Problem
The controller method is declared to return `Object` instead of a strongly typed class structure.

### Why it matters
It reduces compile-time type safety and prevents API documentation tools (like Swagger/OpenAPI) from discovering the response schema.

### Suggested Fix
Change the controller return type to `ReferenceDataResponse<?>`.

### Priority
Low

---

## Low: Non-Standard Trailing Slash in Get Mapping

### Location
`src/main/java/com/policy/api/controller/CustomerController.java` → `CustomerController` → `getAllCustomers`

### Problem
The endpoint is mapped as `@GetMapping("/")` instead of `@GetMapping` or `@GetMapping("")`.

### Why it matters
This introduces path matching inconsistencies. Clients are forced to use `/customers/` instead of `/customers` to retrieve customer listings (depending on Spring path matching configuration).

### Suggested Fix
Remove the trailing slash from the mapping.

### Priority
Low

---

# Test Review

### Correctness
* **Issue:** The test suite does not compile and run out-of-the-box. The `CustomerServiceTest` throws a `NullPointerException` due to an unstubbed repository call, and `ProposalServiceTest` throws an `UnnecessaryStubbingException` due to an unused Mockito stub.
* **Good Tests:** Tests such as `shouldCreateCustomerSuccessfully`, `shouldThrowInvalidCustomerExceptionWhenValidationFails`, and `ReferenceMasterServiceTest` are well-structured, use correct Mockito annotations, and utilize JUnit 5 `assertAll` for strong validations.

### Unnecessary Tests
* There are no unnecessary test files, but some stubbing setups are unused and cause test suite crashes under strict mock settings.

### Missing Tests
1. **Validation edge case:** There is no test verifying that proposal validation fails when `PAN` is null or empty and the premium exceeds 50,000. Writing this test would have immediately caught the PAN logic bug.
2. **Cascading State Check:** There are no tests checking whether the proposal service blocks proposal creation or submission when the associated customer is soft-deleted.
3. **Negative deletion flow:** There is no unit test in `CustomerServiceTest` to verify that `deleteCustomer` successfully throws `InvalidCustomerException` when a customer attempts to delete but has active proposals.

### Weak Assertions
In `AuditServiceTest.shouldCreateAuditSuccessfully`, the test asserts that `response.getAuditId()` equals `"AUD001"`. However, the mock `IdGenerator.generateAuditId()` is never stubbed in the setup, which means it returned `null` inside the service's `mapToModel` method. The test passes only because the mock repository is hard-configured to return a stubbed `Audit` object with `"AUD001"`. This assertion is weak because it masks the fact that the ID generation component returned null during the actual system execution.

### Mocking and Readability
* Mocking boundaries are mostly correct using `@ExtendWith(MockitoExtension.class)`.
* Readability is high, with clean naming conventions like `shouldReturnOnlyActiveCustomers` and structured AAA (Arrange-Act-Assert) blocks.

---

# Architecture Review

### Layered Architecture & Separation of Concerns
* **Strengths:** Excellent boundaries. Controller handles HTTP mapping, Service handles business validation, and Repository manages the database simulation.
* **Weaknesses:** Bypassing Dependency Injection by manually calling `new MaskPii()` in service classes violates architectural isolation.

### DTO Usage & Mapping
* **Strengths:** Excellent separation between request inputs and response payloads. Internal database flags (`deleted`, `deletedAt`) are protected from direct external modification.
* **Weaknesses:** Programmatic mapping code is written inline inside service classes (`mapToModel`, `mapToResponse`). While acceptable for small assignments, using a mapping framework or dedicated converter classes keeps services cleaner.

### Validation & Exception Handling
* **Strengths:** Good split between DTO annotation validation (for null/empty/ranges) and business validation (for policy values). Centralized exception advice handles most errors cleanly.
* **Weaknesses:** Incorrect inheritance of `InvalidCustomerException` (extends `RuntimeException` directly instead of `ApiException`) breaks global exception routing, resulting in raw HTTP 500 error leakages.

### Repository Design
* **Strengths:** Clean in-memory simulation using thread-safe collections.
* **Weaknesses:** Mutable objects are modified directly without deep cloning or locking, risking concurrent write corruptions.

---

# Submission Checklist

- [ ] Fix PAN validation condition in `Validation.java` to fail when PAN is null/empty for premium > 50,000.
- [ ] Stub `proposalRepository.getByCustomerId` in `CustomerServiceTest.shouldDeleteCustomerSuccessfully` to fix `NullPointerException`.
- [ ] Remove unused `generator.generateAuditId()` stub in `ProposalServiceTest.shouldSubmitProposalSuccessfully` to fix `UnnecessaryStubbingException`.
- [ ] Change `InvalidCustomerException` to extend `ApiException` (or add an explicit handler) to return HTTP 400 instead of HTTP 500.
- [ ] Inject `MaskPii` into `CustomerService` and `ProposalService` using constructor injection rather than manual instantiation.
- [ ] Fix `build.gradle` to use a stable Spring Boot 3.x release, replace invalid test starters, and add `spring-boot-starter-test`.
- [ ] Add active/soft-deleted checks for customers when fetching or mutating proposals in `ProposalService`.
- [ ] Rename `Nominee` and `PolicyUid` fields in `Proposal.java` to start with lowercase letters to adhere to standard camelCase naming.
- [ ] Remove unused duplicate method `canDeleteCustomer` from `ProposalService`.
- [ ] Rename or invert the logic of `hasActiveProposals` in `CustomerService` to match its actual behavior.

---

# Overall Assessment

### Scores (Out of 10)
* **Architecture:** 8 / 10 (Solid layered structure; decoupled references)
* **Code Quality:** 6 / 10 (Inconsistent counter scopes, reversed naming, manual instantiation, and naming violations)
* **Java:** 7 / 10 (Good concurrent collections, but references mutated without defensive copying)
* **Spring Boot:** 6 / 10 (Bypassed DI context; invalid build configuration)
* **Testing:** 5 / 10 (Fails to build out-of-the-box due to compile and runtime test failures)
* **Readability:** 8 / 10 (Indentation, imports, and variables are generally clean, except for reversed naming semantics)
* **Maintainability:** 7 / 10 (Well-organized package layout; modular validation rules)
* **Assignment Completeness:** 8 / 10 (Implements CRUD, soft-deletes, and audits, but PAN compliance checks are broken)

---

### Interview Guidance

#### 1. Would you pass this assignment?
As it currently stands, **NO**. A broken test suite and failing Gradle build will result in an immediate rejection in any automated or manual grading pipeline. However, once the two broken tests and the PAN validation logic bug are resolved, this would receive a **High Pass** with top marks for its layered separation, custom validation structure, and DTO implementation.

#### 2. What would impress the interviewer?
* The clean decoupling of domain models: using customer ID foreign-keys in the `Proposal` class rather than direct entity object mappings, which prevents circular reference mapping crashes in REST serialization.
* Thread-safe memory design using `ConcurrentHashMap` and `AtomicInteger` to represent data in a realistic concurrent web container environment.
* High readability of tests using grouped assertions (`assertAll`).
* Clear architectural design documentation inside `DESIGN_DECISIONS.md`.

#### 3. What questions might the interviewer ask?
* *"Why did you manually instantiate `MaskPii` using the `new` keyword if it's annotated as a Spring `@Component` bean?"*
* *"In your `CustomerService.updateCustomer` method, what would happen if two separate HTTP requests updated the same customer concurrently? Is your thread-safe collection enough to prevent inconsistent state?"*
* *"Explain why `CustomerService.hasActiveProposals` returns true when a customer has no proposals. Does the method name match the behavior?"*
* *"How would you transition this in-memory repository structure to a relational database using Spring Data JPA and Hibernate?"*

#### 4. What parts of the code should the student be able to explain?
* **Concurrency Mechanics:** The difference between `HashMap` and `ConcurrentHashMap`, and how `AtomicInteger.incrementAndGet()` guarantees thread-safe ID increments.
* **Global Exception Handling:** How `@RestControllerAdvice` intercepts service exceptions and serializes them into custom `ErrorResponse` payloads.
* **DTO Pattern:** Why separating models and DTOs is necessary for api contract versioning and information security (e.g. hiding audit metadata fields).
* **Validation lifecycle:** The differences between JSR-380 controller-level validation and service-layer programmatic validation.
