# Stage 1 AI history: chat-01

**Tool:** Claude (Claude Code, model Opus 5.5)
**Date:** 15 November 2026

---

## Prompt 1

Review my Stage 1 staff classes (`ActionResult`, `StaffMember`, `Waiter`, `Chef` and `Cashier`) against the brief. Do they cover every staff rule, and are there any cases I have missed?

### Reply

**The staff rules in the brief are all covered.**
- **One task per turn:** every action checks `isAvailable()` first and fails with a reason if the task is already used. `GameController` calls `resetForTurn()` on every staff member at the start of each turn.
- **An invalid choice does not use up the task:** each action calls `Restaurant`, and `consumeTask()` runs only when the returned `ActionResult` is a success. A failed seat, order or serve leaves the Waiter free to choose again.
- **The Waiter's three tasks:** `seat()`, `takeOrder()` and `serve()` match the brief, and `waitTurn()` covers the fourth option.
- **The Chef prepares one unit per turn:** `prepare()` advances one unit, and the order decides when it becomes READY. The Chef does not need to know which dish it is cooking.
- **The Cashier collects at most one payment per turn:** `collectOldest()` uses its task only when a payment succeeds, and `Restaurant` chooses the customer who has been ready to pay the longest.

**Cases worth checking:**
- `waitTurn()` does not check `isAvailable()`, and it returns nothing. This is safe in the current game, because the controller only offers Wait while the staff member is free. It is still the one action that does not follow the same pattern as the others.
- The Cashier inherits `waitTurn()`, but nothing ever calls it, because the Cashier acts automatically. This is harmless, but see Prompt 3.

---

## Prompt 2

Is my use of encapsulation correct? Have I chosen the right access level for each field and method?

### Reply

**`ActionResult` is well encapsulated.**
- Both fields are `private final`, so a result cannot change after it is created.
- The constructor is `private`, so results can only be made through `ok()` and `fail()`. These names say what the result means where it is used.
- There are getters and no setters, so `ActionResult` is immutable.
- One small improvement: it could be declared `final`, like `HostChoice` is in Stage 3, so that no subclass can change its behaviour.

**`StaffMember` has one weak spot.**
- `consumeTask()` is `protected`, which is right. Only staff subclasses should be able to use up a task.
- `taskUsed` is also `protected`. That means a subclass could set it directly and skip the rule in `consumeTask()`. Because `isAvailable()`, `resetForTurn()` and `consumeTask()` already cover every use, `taskUsed` could be `private`. This is the clearest encapsulation improvement in the staff classes.

**The staff classes do not hold a `Restaurant`.** It is passed in to each action, so the staff classes have no state except whether their task is used.

---

## Prompt 3

Which design principles from the lectures do my staff classes follow, and where could the design be better?

### Reply

**Single Responsibility: reflected.** The staff classes only decide whether they may act this turn. `Restaurant` checks the game rules, such as whether the table is free or the dish is READY. `ActionResult` only carries the outcome and its reason. `GameCLI` only reads input and prints.

**Open/Closed: reflected.** `StaffMember` holds the one-task-per-turn rule once. A new staff role can extend it without changing the existing staff classes.

**Liskov Substitution: mostly reflected.** `Waiter`, `Chef` and `Cashier` keep the `StaffMember` contract: they are available until a successful action. The Cashier inheriting `waitTurn()`, which it never uses, is a small sign that not every staff member needs every method. For a game this size, that is acceptable.

**Loose coupling and high cohesion:** each staff class depends only on `Restaurant`, `Menu` and `ActionResult`, not on the UI. Each class is small and has one purpose.

**Where it could be better:**
- **Repeated code.** The pattern "check available, call `Restaurant`, use the task if it succeeded" appears in `Waiter` (through `completeIfSuccessful()`), `Chef` and `Cashier`. Moving `completeIfSuccessful()` into `StaffMember` as a `protected` method would put the rule in one place. That is the same reason `StaffMember` exists in the first place.
- **Hardcoded role names.** The messages "Waiter has already acted this turn" and "Chef has already acted this turn" are written out in each class. A `protected` method in `StaffMember` that builds this message from a role name would remove the repetition.
- **Dependency Inversion is not applied.** The staff classes depend on the concrete `Restaurant` class. That is reasonable here, because there is only one restaurant, but it should not be described as DIP in the report.

---

## Prompt 4

Compare my design to what we learnt in the lectures about inheritance and abstract classes. Was it right to make `StaffMember` abstract?

### Reply

**Yes.** An abstract class fits when subclasses share both state and behaviour, which is the case here. Every staff member has a "task used" state and the same rules for checking, resetting and consuming it. An interface could only describe the methods, so each class would have to repeat the same field and logic.

**It is abstract for a good reason.** A plain "staff member" with no role should never exist in the game, so preventing `new StaffMember()` matches the domain.

**It is justified by Stage 1 alone.** `Waiter`, `Chef` and `Cashier` already share the per-turn rule, so the abstraction is useful now, not only for a possible future role. This matches the lecture point that abstractions should come from real shared behaviour, not from guessing what might be needed later.

**Composition was the alternative.** Each staff class could hold a small "turn task" object instead of inheriting one. That would also work, and it would avoid the Cashier inheriting `waitTurn()`. With three similar roles that are all kinds of staff member, inheritance is the simpler and clearer choice.
