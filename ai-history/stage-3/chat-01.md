# Stage 3 AI history: chat-01

**Tool:** Claude (Claude Code, model Opus 5.5)
**Date:** 15 November 2026

---

## Prompt 1

Review my `Host` class and `HostChoice` against the Stage 3 brief. Does the Host follow every rule, and is anything missing?

### Reply

**The Host rules are covered.**
- **It seats at most one customer per turn:** `seat()` checks `isAvailable()` first, and the task is used only when seating succeeds.
- **It acts in addition to the Waiter:** the Host has its own task, separate from the Waiter's. `GameController` asks for the Host's choice first, then the Waiter's, so the Host can seat a customer and the Waiter can take that customer's order in the same turn.
- **A failed seating leaves the task unused:** if the customer is not queued, the table does not exist, or the table is occupied, `Restaurant.seatCustomer()` returns a failure, and the Host can choose again.
- **It is reset each turn:** `GameController` calls `host.resetForTurn()` with the other staff.

**`HostChoice` is well designed.**
- It is a `final` class with `private final` fields and a `private` constructor, so choices can only be made through `seat()` and `waitTurn()`.
- It matches the pattern of `WaiterChoice` and `ChefChoice`, so the controller handles every staff choice the same way.
- The `NONE` constant for a waiting choice's IDs is clearer than a bare `-1`.

---

## Prompt 2

Did adding the Host follow the Open/Closed Principle? Did any of my Stage 1 staff classes have to change?

### Reply

**Yes, it followed OCP.** Comparing the `stage-1` and `stage-3` tags shows that `ActionResult`, `StaffMember`, `Waiter`, `Chef` and `Cashier` are identical. The Host was added as a new class, not by editing the existing ones.

**Why it worked:**
- `Host` extends `StaffMember`, so it inherits the one-task-per-turn rule without rewriting it.
- `Host.seat()` calls the existing `Restaurant.seatCustomer()`. The Host and the Waiter therefore follow exactly the same seating rules, and those rules live in one place.

**What did change:** `GameController` creates the Host and asks for its choice, and `GameCLI` shows the new prompt. These changes are expected. They are where the turn order and the input live, and adding a role has to touch them.

---

## Prompt 3

Comparing my Host to what we learnt in the lectures, is there anything I could improve in the design?

### Reply

**Repeated code is the main improvement.** `Host.seat()` repeats the same steps as `Waiter.seat()`: check the task, call `seatCustomer()`, and use the task only if it succeeded. The same pattern appears in `Chef` and `Cashier`. Moving a `protected completeIfSuccessful()` method into `StaffMember` would make the Host even shorter, and the rule would be written once.

**The hardcoded message.** "Host has already acted this turn." follows the same hardcoded pattern as the other staff. A shared message built from a role name in `StaffMember` would remove this.

**Encapsulation in `StaffMember`.** As in Stage 1, `taskUsed` is `protected`. Making it `private` would ensure the Host, and any future role, can only change it through `consumeTask()`.

**Reuse instead of duplication is a strength.** Calling `seatCustomer()` instead of copying the seating checks into the Host is the right choice. It means that any future change to seating rules automatically applies to both the Host and the Waiter.
