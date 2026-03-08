# Kiro PR Workflow Guide

## Overview

This guide shows how to use Kiro to streamline your Pull Request process, from creation to merge.

---

## PR Creation Workflow

### Step 1: Pre-PR Checklist

Before creating a PR, ask Kiro to verify your changes:

```
You: "Check if my changes are ready for PR"

Kiro will verify:
- ✓ All tests pass
- ✓ Code follows steering file standards
- ✓ No linting errors
- ✓ Documentation updated
- ✓ Spec files updated (if feature work)
- ✓ No debug code (console.log, System.out.println)
```

**Example:**
```
You: "Check if my login-service changes are ready for PR"

Kiro:
✓ All tests passing (15/15)
✓ Code follows architecture-standards.md
✓ Code follows spring-boot-standards.md
⚠️ Missing: JavaDoc for LoginController.handleExpiredAccount()
⚠️ Missing: Integration test for expired account scenario
❌ Found: System.out.println in LoginDomainService.java:45

Recommendation: Fix the issues above before creating PR
```

---

### Step 2: Generate PR Description

Ask Kiro to create a comprehensive PR description:

```
You: "Generate a PR description for my changes"

Kiro analyzes:
- Git diff
- Commit messages
- Modified files
- Spec files (if applicable)
```

**Example Output:**

```markdown
## Summary
Add support for expired account handling in login flow

## Changes
- Added `AccountExpiredException` to handle expired accounts
- Updated `LoginDomainService` to check account expiration
- Added `isExpired()` method to `User` entity
- Updated `LoginController` to return 403 for expired accounts

## Files Modified
- `src/main/java/com/banking/loginservice/domain/model/User.java`
- `src/main/java/com/banking/loginservice/domain/service/LoginDomainService.java`
- `src/main/java/com/banking/loginservice/infrastructure/adapters/in/LoginController.java`
- `src/test/java/com/banking/loginservice/domain/service/LoginDomainServiceTest.java`

## Breaking Changes
None

## Testing
- Added unit tests for expired account scenario
- Tested manually with expired test user
- All existing tests still pass

## Related Issues
- Fixes #123: Handle expired accounts in login

## Checklist
- [x] Tests added/updated
- [x] Documentation updated
- [x] Follows coding standards
- [x] No breaking changes
- [x] Spec files updated

## How to Test
1. Start login-service
2. Use test user: `expired@test.com` / `password123`
3. Verify 403 response with message "Account expired"
```

---

### Step 3: Create PR with Generated Description

```bash
# Copy Kiro's generated description
# Create PR on GitHub/GitLab/Bitbucket
# Paste the description
```

---

## PR Review Workflow

### As PR Author: Responding to Review Comments

#### Scenario 1: Reviewer Asks for Changes

**Reviewer Comment:**
> "Can you add null check for the user parameter in LoginController?"

**Your Workflow:**
```
You to Kiro: "Add null check for user parameter in LoginController.handleLogin()"

Kiro:
- Adds null validation
- Updates tests
- Commits the change

You: "Reply to the review comment with the commit hash"

Kiro generates:
"✅ Done in commit abc123f - Added null check and test case"
```

#### Scenario 2: Reviewer Questions Design Decision

**Reviewer Comment:**
> "Why did you use synchronous calls instead of async?"

**Your Workflow:**
```
You to Kiro: "Explain why I used synchronous calls in LoginDomainService"

Kiro analyzes design.md and code:
"The design uses synchronous calls because:
1. Login operations require immediate response
2. Average response time is <100ms (acceptable)
3. Async would add complexity without performance benefit
4. Aligns with architecture-standards.md requirement for simple operations

Reference: design.md Section 5.2 - Performance Considerations"
```

#### Scenario 3: Reviewer Finds Bug

**Reviewer Comment:**
> "This will throw NullPointerException if user.getEmail() is null"

**Your Workflow:**
```
You to Kiro: "Fix potential NPE in LoginDomainService where user.getEmail() is called"

Kiro:
- Identifies the issue
- Adds null check
- Updates tests to cover the case
- Commits the fix

You: "Thanks! Fixed in commit def456g"
```

---

### As Reviewer: Reviewing Someone's PR

#### Step 1: Understand the Changes

```
You to Kiro: "Summarize the changes in this PR"
(Paste the PR diff or provide the PR URL)

Kiro provides:
- High-level summary
- Impact analysis
- Files changed breakdown
- Potential risks
```

**Example:**
```
You: "Summarize the changes in PR #456"

Kiro:
Summary: Adds rate limiting to login endpoint

Impact:
- High: Changes authentication flow
- Medium: Adds Redis dependency
- Low: Updates configuration

Files Changed:
- LoginController.java (modified) - Added rate limit check
- RateLimitService.java (new) - Implements rate limiting logic
- pom.xml (modified) - Added Redis dependency
- application.yml (modified) - Added Redis configuration

Potential Risks:
⚠️ Redis is now a required dependency (service won't start without it)
⚠️ No fallback if Redis is down
⚠️ Rate limit configuration is hardcoded (should be in config)

Recommendations:
1. Add fallback mechanism if Redis is unavailable
2. Move rate limit values to application.yml
3. Add integration tests for rate limiting
```

#### Step 2: Check Against Standards

```
You to Kiro: "Check if this PR follows our coding standards"

Kiro verifies against steering files:
- architecture-standards.md
- spring-boot-standards.md
- security-standards.md
- testing-standards.md
```

**Example:**
```
You: "Check if PR #456 follows our standards"

Kiro:
✓ Follows Hexagonal Architecture (architecture-standards.md)
✓ Uses proper REST conventions (spring-boot-standards.md)
⚠️ Missing: Rate limit should be configurable (spring-boot-standards.md §3.2)
❌ Violation: Hardcoded Redis host (security-standards.md §2.1)
❌ Missing: Integration tests (testing-standards.md §4.1)

Recommendation: Request changes for the issues above
```

#### Step 3: Provide Constructive Feedback

```
You to Kiro: "Generate review comments for the issues found"

Kiro creates:
- Specific, actionable comments
- References to standards
- Code suggestions
```

**Example Output:**

```markdown
## Review Comments

### RateLimitService.java:15
❌ **Hardcoded Redis host**

```java
// Current
private static final String REDIS_HOST = "localhost";

// Suggested
@Value("${redis.host}")
private String redisHost;
```

**Reason:** Violates security-standards.md §2.1 - All configuration must be externalized

**Reference:** spring-boot-standards.md §3.2

---

### LoginController.java:45
⚠️ **Rate limit values should be configurable**

```java
// Current
if (attempts > 5) {
    throw new TooManyRequestsException();
}

// Suggested
@Value("${rate-limit.max-attempts}")
private int maxAttempts;

if (attempts > maxAttempts) {
    throw new TooManyRequestsException();
}
```

**Reason:** Allows different limits for dev/test/prod environments

---

### Missing: Integration Tests
❌ **No integration tests for rate limiting**

Please add integration tests that verify:
1. Rate limit is enforced after N attempts
2. Rate limit resets after timeout
3. Different IPs are tracked separately

**Reference:** testing-standards.md §4.1
```

---

## Advanced PR Workflows

### 1. Automated PR Validation (CI/CD Integration)

Create a GitHub Action / GitLab CI job that uses Kiro:

**.github/workflows/pr-validation.yml**
```yaml
name: PR Validation with Kiro

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check PR against standards
        run: |
          # Ask Kiro to validate
          kiro validate-pr --standards .kiro/steering/
          
      - name: Generate PR review
        run: |
          kiro review-pr --output review.md
          
      - name: Post review as comment
        uses: actions/github-script@v6
        with:
          script: |
            const fs = require('fs');
            const review = fs.readFileSync('review.md', 'utf8');
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: review
            });
```

---

### 2. Spec-Driven PR Validation

For PRs that implement spec tasks, Kiro can verify completeness:

```
You: "Verify this PR implements Task 3.2 from the spec"

Kiro checks:
- Task 3.2 requirements from tasks.md
- Acceptance criteria from requirements.md
- Design specifications from design.md
- Test coverage
```

**Example:**
```
You: "Verify PR #456 implements Task 3.2: Add rate limiting"

Kiro:
Task 3.2: Add rate limiting to login endpoint

Requirements Check:
✓ Rate limit implemented (5 attempts per minute)
✓ Returns 429 status code
✓ Includes Retry-After header
⚠️ Missing: Rate limit should reset after 1 minute (currently 5 minutes)

Design Check:
✓ Uses Redis as specified in design.md
✓ Follows key naming convention: rate_limit:login:{ip}
❌ Missing: Circuit breaker for Redis failures (design.md §5.3)

Test Coverage:
✓ Unit tests added
⚠️ Integration tests incomplete (missing timeout reset test)
❌ Property-based tests not added (requirements.md §6.2)

Overall: 60% complete - Request changes
```

---

### 3. Breaking Change Detection

```
You: "Check if this PR has breaking changes"

Kiro analyzes:
- API changes (endpoints, request/response schemas)
- Database schema changes
- Configuration changes
- Dependency updates
```

**Example:**
```
You: "Check PR #456 for breaking changes"

Kiro:
⚠️ BREAKING CHANGES DETECTED

1. API Change (HIGH IMPACT)
   - Endpoint: POST /api/v1/login
   - Change: Now returns 429 (new status code)
   - Impact: Clients must handle 429 status
   - Recommendation: Update API documentation

2. Configuration Change (MEDIUM IMPACT)
   - New required property: redis.host
   - Impact: Deployment will fail without this config
   - Recommendation: Update deployment docs

3. Dependency Change (LOW IMPACT)
   - Added: spring-boot-starter-data-redis
   - Impact: Increased memory footprint (~50MB)
   - Recommendation: Update resource limits

Recommendation: Mark this PR as "breaking change" and update CHANGELOG.md
```

---

### 4. PR Merge Checklist

Before merging, ask Kiro to do a final check:

```
You: "Final check before merging PR #456"

Kiro verifies:
- All review comments addressed
- All tests passing
- No merge conflicts
- Spec files updated (if applicable)
- Documentation updated
- CHANGELOG updated (if breaking changes)
```

---

## PR Templates

### Template 1: Feature PR

```markdown
## Feature: [Feature Name]

### Summary
[Brief description]

### Spec Reference
- Requirements: `.kiro/specs/requirements.md` (Section X)
- Design: `.kiro/specs/design.md` (Section Y)
- Tasks: `.kiro/specs/tasks.md` (Task Z)

### Changes
- [List of changes]

### Testing
- [How to test]

### Checklist
- [ ] Implements all requirements from spec
- [ ] Follows design.md specifications
- [ ] All tasks completed
- [ ] Tests added (unit + integration)
- [ ] Documentation updated
- [ ] No breaking changes
```

### Template 2: Bugfix PR

```markdown
## Bugfix: [Bug Description]

### Problem
[What was broken]

### Root Cause
[Why it was broken]

### Solution
[How it's fixed]

### Testing
- [ ] Bug is reproducible before fix
- [ ] Bug is fixed after changes
- [ ] Regression tests added
- [ ] No new bugs introduced

### Checklist
- [ ] Spec updated (if applicable)
- [ ] Tests added
- [ ] Documentation updated
```

---

## Best Practices

### 1. Use Kiro Before Creating PR
```bash
# Always check before creating PR
kiro check-pr-readiness
```

### 2. Keep PRs Small
```
You: "Is this PR too large?"

Kiro: "Yes, this PR changes 45 files. Consider splitting into:
1. PR 1: Add rate limiting infrastructure
2. PR 2: Implement rate limiting logic
3. PR 3: Add rate limiting tests"
```

### 3. Link to Spec Files
Always reference spec files in PR description:
- Requirements: Which user stories does this implement?
- Design: Which design decisions does this follow?
- Tasks: Which tasks does this complete?

### 4. Use Kiro for Review Responses
Instead of manually addressing each comment, ask Kiro to:
- Implement the requested changes
- Generate response comments
- Update tests

---

## Troubleshooting

### Issue: Kiro Suggests Changes That Conflict with Review

**Solution:**
```
You: "The reviewer wants X but you suggested Y. Which is correct?"

Kiro: Analyzes both suggestions against steering files and provides recommendation
```

### Issue: PR Fails Standards Check

**Solution:**
```
You: "Fix all standards violations in my PR"

Kiro: Automatically fixes issues that can be automated, lists manual fixes needed
```

### Issue: Reviewer Doesn't Understand Spec-Driven Approach

**Solution:**
```
You: "Generate an explanation of spec-driven development for my reviewer"

Kiro: Creates a summary with links to relevant documentation
```

---

## Summary

Kiro helps with PR process by:

1. **Pre-PR**: Validate changes, generate descriptions
2. **During Review**: Respond to comments, implement fixes
3. **As Reviewer**: Understand changes, check standards, provide feedback
4. **Post-Review**: Final checks, merge validation

**Key Benefits:**
- ✅ Faster PR creation (auto-generated descriptions)
- ✅ Higher quality (standards validation)
- ✅ Faster reviews (automated checks)
- ✅ Better collaboration (clear, actionable feedback)
- ✅ Traceability (links to spec files)

**Next Steps:**
1. Try: "Check if my changes are ready for PR"
2. Try: "Generate a PR description for my changes"
3. Try: "Review this PR against our standards"
