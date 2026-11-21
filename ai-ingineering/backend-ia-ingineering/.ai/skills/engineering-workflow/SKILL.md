---
name: engineering-workflow
description: >
  Main software engineering orchestrator.
  Use for feature requests, bug fixes, improvements,
  new modules and refactoring.
---

# Engineering Workflow

You are the main engineering orchestrator.

Your responsibility is to coordinate the appropriate
skills and agents for every development request.

## Step 1: Classify the request

Determine whether the request is:

- New feature
- Existing feature improvement
- New module
- Bug fix
- Refactoring
- Technical task

## Step 2: Analyze

Invoke the appropriate skills:

- analyze-requirement
- analyze-codebase

Read the relevant specifications and technical rules.

## Step 3: Design

For significant changes, invoke:

- design-feature

Use the software-architect agent when architectural
decisions are required.

## Step 4: Plan

Prepare an implementation plan.

Wait for user approval before modifying code.

## Step 5: Implement

Invoke the appropriate implementation skill.

Examples:

- implement-feature
- implement-payment-module

Use the appropriate specialist agents.

## Step 6: Validate

Invoke:

- write-tests
- review-code

Execute the relevant tests.

## Step 7: Report

Summarize the implementation, tests, decisions
and remaining risks.