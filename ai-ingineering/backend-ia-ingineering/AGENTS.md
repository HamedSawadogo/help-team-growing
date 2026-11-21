# AGENTS.md

## Project Overview
This is a Java 21 / Spring Boot backend application.

Read the following documents before making significant changes:

- .ai/context/business-context.md
- .ai/context/architecture-overview.md
- .ai/context/domain-glossary.md
- .ai/instructions/architecture.md
- .ai/instructions/java.md

## Architecture Rules

- Follow Hexagonal Architecture.
- Keep business logic independent from infrastructure.
- Respect bounded contexts and module boundaries.
- Avoid unnecessary abstractions.
- Do not introduce microservices without justification.
- Use dependency inversion.

## Java Rules

- Use constructor injection.
- Prefer immutable objects.
- Avoid exposing JPA entities through REST APIs.
- Use explicit transaction boundaries.
- Avoid business logic inside controllers.

## Testing Rules

- Every business rule must be tested.
- Write unit tests for domain logic.
- Write integration tests for persistence.
- Use Testcontainers when appropriate.
- Never remove tests simply to make the build pass.

## Security Rules

- Never hardcode secrets.
- Validate external inputs.
- Never log sensitive information.
- Check authorization at the appropriate boundary.

## Development Workflow

Before implementation:

1. Read the relevant specification.
2. Analyze the existing code.
3. Identify affected modules.
4. Propose an implementation plan.

After implementation:

1. Execute relevant tests.
2. Execute Maven verification.
3. Review the Git diff.
4. Report modified files and remaining risks.

## Restrictions

- Never modify unrelated modules.
- Never invent business requirements.
- Never introduce dependencies without justification.
- Ask for clarification when requirements are ambiguous.