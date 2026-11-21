---
name: implement-feature
description: Implements features in the Java Spring Boot application.
---

# Feature Implementation

## Workflow

1. Read the approved specification.
2. Analyze the existing implementation.
3. Identify impacted modules.
4. Propose an implementation plan.
5. Wait for approval.
6. Implement the feature.
7. Write and execute tests.
8. Review the changes.

## Mandatory Rules

- Respect Hexagonal Architecture.
- Keep business logic out of controllers.
- Do not introduce unnecessary abstractions.
- Never modify approved business rules without authorization.
- Never skip relevant tests.

## References

Before implementation, consult:

- references/architecture-rules.md
- references/java-conventions.md
- references/testing-rules.md