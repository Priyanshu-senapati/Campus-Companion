# 🤝 Contributing Guide

Thank you for contributing to Campus Companion! This guide covers everything you need to know to get your PR merged smoothly.

---

## Before You Start

1. Read [ARCHITECTURE.md](../architecture/ARCHITECTURE.md) to understand the project structure
2. Check the [Issues tab](https://github.com/Priyanshu-senapati/Campus-Companion/issues) — pick an existing issue or open one before starting work
3. Comment on the issue to claim it so two people don't work on the same thing

---

## Setting Up Locally

```bash
# 1. Fork the repo on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/Campus-Companion.git
cd Campus-Companion

# 2. Add upstream remote
git remote add upstream https://github.com/Priyanshu-senapati/Campus-Companion.git

# 3. Follow Firebase setup in docs/firebase/FIREBASE_SETUP.md
# 4. Build and run
./gradlew assembleDebug
```

---

## Branch Naming

```
feat/short-description       # new feature
fix/what-was-broken          # bug fix
docs/what-you-documented     # documentation
refactor/what-you-cleaned    # code cleanup, no behaviour change
test/what-you-tested         # new or updated tests
chore/what-you-did           # build config, dependency updates
```

**Examples:**
```
feat/biometric-login
fix/attendance-percent-zero-total-crash
docs/gemini-integration
refactor/attendanceviewmodel-extract-logic
```

---

## Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]
[optional footer: closes #issue]
```

**Examples:**
```
feat(attendance): add color-coded warning banner below 65%

fix(auth): prevent crash when Google Sign-In is cancelled by user
Closes #42

docs(firebase): add Firestore index setup instructions

refactor(canteen): move menu fetch logic from Fragment to Repository
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `style`, `perf`

---

## Code Style

**Kotlin style:**
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Max line length: 120 characters
- Use `val` over `var` wherever possible
- Prefer `when` over chained `if/else`
- No `!!` (non-null assertion) — use `?.let {}` or `?: return` instead

**Architecture rules (strictly enforced):**
- ❌ **No business logic in Fragments** — Fragments only observe and dispatch
- ❌ **No Firestore/Room calls in Fragments or ViewModels** — only in Repositories
- ❌ **No `GlobalScope`** — use `viewModelScope` or `lifecycleScope`
- ✅ **Every async operation must return `Resource<T>`**
- ✅ **Use `ViewBinding`** — no `findViewById`

**Naming conventions:**
```kotlin
// Layouts
fragment_attendance.xml       // Fragments
item_event_card.xml           // RecyclerView items
dialog_mark_attendance.xml    // Dialogs
sheet_add_class.xml           // Bottom sheets

// IDs
tvSubjectName                 // TextView (tv prefix)
rvEventsList                  // RecyclerView (rv prefix)
btnMarkPresent                // Button (btn prefix)
ivProfilePhoto                // ImageView (iv prefix)
etEmailInput                  // EditText (et prefix)
```

---

## Pull Request Checklist

Before opening a PR, confirm:

- [ ] Code builds without errors: `./gradlew assembleDebug`
- [ ] No lint warnings introduced: `./gradlew lint`
- [ ] I tested the feature manually on a device or emulator
- [ ] I did not add business logic to a Fragment
- [ ] I did not hardcode any API keys, UIDs, or secrets
- [ ] I updated relevant docs if I changed architecture or added a new feature
- [ ] My branch is up to date with `master`

```bash
git fetch upstream
git rebase upstream/master
```

---

## PR Description Template

```markdown
## What does this PR do?
<!-- One sentence summary -->

## Why?
<!-- Link the issue: Closes #XX -->

## How was it tested?
<!-- Device, API level, test steps -->

## Screenshots (if UI change)
<!-- Before / After -->

## Checklist
- [ ] Builds without errors
- [ ] No business logic in Fragments
- [ ] No hardcoded secrets
- [ ] Docs updated if needed
```

---

## Getting Help

- Open a GitHub Discussion for questions
- Tag `@Priyanshu-senapati` on your issue if you're stuck
- For Firebase/Gemini setup questions, check the relevant docs first

---

## What NOT to Contribute

- Direct Firestore calls in Fragments or ViewModels
- New features without a linked issue
- Large refactors without discussing first
- Dependency updates without testing for conflicts
- Any changes to `local.properties` or `google-services.json` (these are git-ignored for a reason)
