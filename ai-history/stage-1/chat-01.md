# Stage 1 AI history: chat-01

**Tool:** Claude (Claude Code, model Sonnet 5.5)
**Date:** 14 November 2026

---

## Prompt 1

Analyze my Stage 1 code against the SOLID principles. For each principle, identify the classes that demonstrate it, and state where a principle is not applicable or not reflected.

### Reply

**Single Responsibility: mostly reflected, with one weaker class.**
- `Order` owns the timing rules for its own life cycle (`advanceOneUnit`, `canServe`, `markServed`, `markPaid`, `cancel`).
- `ScoreBoard` only records results and evaluates victory or defeat.
- `ActionResult` only carries success or failure with a message.
- `GameCLI` only reads input and prints. It has no game rules.
- `Waiter`, `Chef` and `Cashier` only decide whether the staff member may act and then delegate to `Restaurant`.
- Not fully reflected: `Restaurant` has several responsibilities. It holds the queue, the tables and the active orders. It also seats customers, places, prepares and serves orders, processes payment and applies waiting penalties. It also formats a dollar amount inline in `payOldestEligible`. That is a cohesion trade-off, because keeping these in one coordinator avoids passing the same lists around, but it is the first class that would be split if the design grew.

**Open/Closed: reflected in `Customer`.**
- `waitingPenalty()` is abstract, and `chooseItem()` and `billFor()` can be overridden. A new customer type extends `Customer` and does not edit `Restaurant`, `Order` or the turn loop.
- `MenuItem` is an interface, so `Order`, `Menu` and `Customer` work with any new menu item without changes.
- Not fully reflected: `ArrivalSchedule` creates `RegularCustomer` directly, so a new type would modify that class. It is the only place that knows concrete customer classes.

**Liskov Substitution: reflected, with little to test.**
- `RegularCustomer` overrides `waitingPenalty()` and `chooseItem()` without changing the state rules defined in `Customer`. Code that works with a `Customer` works with a `RegularCustomer`.
- `Waiter`, `Chef` and `Cashier` extend `StaffMember` and do not weaken its contract (one task per turn).
- With a single customer subclass, LSP is satisfied but has not been put under real pressure yet.

**Interface Segregation: reflected in `MenuItem`, otherwise not applicable.**
- `MenuItem` is a small interface (`getName`, `getPriceCents`, `getPrepUnits`), and every implementer needs all three.
- There are no other interfaces. `StaffMember` is an abstract class, not an interface, so ISP does not apply to the staff.

**Dependency Inversion: partly reflected.**
- `Order`, `Menu` and `Customer` depend on the `MenuItem` abstraction, not on `Dish`.
- Not reflected at the top level: `GameController` creates `Restaurant`, `Waiter`, `Chef`, `Cashier` and `GameCLI` itself with `new`, and `Restaurant` creates its own `ScoreBoard`. These are concrete dependencies. For this assignment that is reasonable, but it should not be described as DIP in the report.

**Suggested wording for the report:** claim SRP, OCP and the `MenuItem` part of ISP and DIP. State LSP as satisfied but lightly exercised, and state that `Restaurant` is the main SRP trade-off.

---

## Prompt 2

Compare my Stage 1 code with the Stage 1 class diagram and list every discrepancy in attributes, operations, visibility and relationships. Then assess whether Stage 1 is ready for submission against the assignment brief.

### Reply

**Comparison method.** I extracted the 24 classes from `stage1-class-diagram.mmd` and checked that every attribute and operation name exists in the matching Java file. I also listed the public and protected methods in the code that the diagram does not show.

**Result: no missing members.** All 24 diagram classes have a matching source file, and every attribute and operation in the diagram exists in the code. The earlier mismatches have been resolved:
- `ChefChoice.prepare(orderId)` replaced `cook(orderId)`.
- `WaiterChoice` now has the public four-argument constructor shown in the diagram.
- `GameConfig` has instance `final` fields, with `base()` as the static factory, and `GameController` and `ScoreBoard.evaluate` read from the config object.
- `GameCLI.showResult(result, score, turnsUsed)` matches the diagram, and the controller passes `config.MAX_TURNS`.

**Remaining differences (all minor):**
- `toString()` overrides in `Table`, `Customer`, `Order` and `Dish` are not in the diagram. They are display helpers.
- `Main` (the entry point) is not in the diagram.
- Private helper methods in `Restaurant` and `GameCLI` are not drawn, which is normal for a class diagram.

**Limits of this check.** The script compared names only. Visibility and types were reviewed by reading the code against the diagram, not machine-checked.

**Readiness against the brief:**
- The code compiles with no warnings under `javac -Xlint:all`.
- The scripted victory run ends with 4 paid, $51.00 revenue and average satisfaction 80.0. The scripted defeat run ends with 0 paid and 2 unhappy departures. Both are in the README.
- The turn order, READY then SERVED timing, payment timing, abandonment and the 18-turn ending follow Section 3 of the brief.
- The source has no Stage 2 or Stage 3 features.
- What is left is outside the code: the PDF report, the video, the declarations, and the Stage 2 and Stage 3 deliverables.

---

## Prompt 3

Review `Restaurant.java` and the `Customer` state methods for defects in the turn rules. Pay particular attention to customers who abandon on the same turn they are served, tables released on payment, and multiple customers becoming ready to pay on the same turn.

### Reply

I read the code and ran the scripted runs. I found no rule-breaking defects. The three scenarios you named behave as the brief requires:

- **Abandon and served on the same turn:** serving happens in step 3 and the waiting penalty in step 5. A served customer is already `EATING`, so `applyWaitingPenalties` skips them. A customer with low satisfaction who is served in time therefore does not leave.
- **Payment and abandonment on the same turn:** payment is step 4 and penalties are step 5. A customer who pays that turn is `PAID` and is never penalised, and the table is released on payment.
- **Table release:** `payOldestEligible` calls `table.release()` after `markPaid()`. `removeUnhappyCustomer` releases the table and removes the cancelled order from the active orders.
- **Several customers ready to pay together:** `payOldestEligible` picks the customer with the lowest `readyToPayTurn` and breaks ties by lower ID. Only one payment happens per turn. The others stay `READY_TO_PAY`, keep their table, and lose no satisfaction while they wait.
- **Timing:** `Order.canServe` requires `readyTurn < turn`, `Customer.startEating` sets `readyToPayTurn = turn + 1`, and `canPay` requires `turn > readyToPayTurn`. This reproduces the brief's Pasta example (ready on turn 3, served on 4, ready to pay on 5, paid on 6).

**Weaknesses found (none break a rule):**
1. **Success messages are discarded.** `GameController` only shows messages when an action fails. After a successful seat, order, prepare or serve, nothing is printed at once. The food the customer chose appears in the state display at the start of the next turn, not immediately after the order is placed. Section 2.2 of the brief says to show the choice once the order is placed, so this is a visible risk. Printing the `ActionResult` message on success would remove it, at the cost of a small `GameCLI` change.
2. **Misleading failure text.** `Restaurant.placeOrder` reports "is not seated" for any customer whose state is not `SEATED`, including one who is already eating.
3. **No guard for an empty menu.** The `Menu` constructor accepts an empty list, and `Customer.chooseItem` would then fail with a divide-by-zero. The game never builds an empty menu, so this cannot happen in play.
4. **Input ending early.** If standard input ends before turn 18, the program stops with an `IllegalStateException`. That only happens with truncated piped input.
5. **`customers` is never pruned.** Customers who paid or left stay in the list. At six customers this has no effect.

---

## Prompt 4

Compare the Stage 1 test notes in my README with Sections 3 and 4.1 of the assignment brief. Identify any requirements that are not yet covered.

### Reply

**Covered:**
- Section 4.1 requires one reproducible victory run and one reproducible defeat run in the test notes. The README has both, each with a one-line command, a turn-by-turn table for the victory run, and the expected final result.
- The expected results match what the code prints: victory with 4 paid, $51.00 and 80.0, and defeat with 0 paid, 2 unhappy departures and N/A average.
- Section 3.3's Pasta example is reproduced by customer 3 in the victory run (satisfaction 92, 84, 76, served, paid with 76).
- Section 3.4's rules (invalid choices explain themselves and do not use up the task, tables stay occupied until payment or leaving) are listed under "Rule checks".
- Section 3.5's end-of-game figures (served, unhappy departures, revenue, average, turns used) are shown in the final result block.
- The start-of-turn display includes the turn number, revenue, paid count and average satisfaction, the waiting queue, tables with each customer's satisfaction and order status, and the staff tasks.
- Build and run instructions, and the food-choice rule, are documented.

**Not covered or worth stating:**
1. The README does not mention that order choices appear in the next state display and not immediately after the order is placed (see Prompt 3, weakness 1). A one-line note would remove any doubt about Section 2.2.
2. The "Cashier" line in the display always says "available" because the staff are reset before it is shown. The README says the Cashier collects payments automatically, which explains it.
3. Section 3.4 says the turn 16 customer cannot finish paying within 18 turns and that at most five customers can pay. The README does not mention this. It is not required, but it helps a reader understand why only some arrivals pay.

No requirement in Sections 3 or 4.1 is missing from the test notes.

---

## Prompt 5

Based only on the Stage 1 code, identify the extension points for adding new customer types or staff roles. Point out any place where a new type would require modifying an existing stable class.

### Reply

**Extension points that already work:**
- **New customer types.** Extend `Customer` and override `waitingPenalty()`. `chooseItem()` and `billFor()` can be overridden for a different food rule or a discount. `Restaurant.placeOrder` calls `customer.chooseItem(menu)` and `customer.billFor(item)` without checking the type, and `applyWaitingPenalties` calls `waitingPenalty()` the same way. `Order`, `Table`, `ScoreBoard` and the turn loop need no change.
- **New menu items.** `MenuItem` is an interface, and `Order`, `Menu` and `Customer` use only that interface, so a new kind of item (for example a combined dish) can be added by implementing it.
- **New staff behaviour on the same pattern.** A new `StaffMember` subclass gets the one-task-per-turn rule (`isAvailable`, `consumeTask`, `resetForTurn`) for free.

**Places where a new type would force an edit to an existing class:**
1. **`ArrivalSchedule`** creates `RegularCustomer` directly. A new customer type needs this class to change, either by picking the type or by taking a map of arrival turn to customer type. This is the single expected change point for customers.
2. **`GameController`** holds `waiter`, `chef` and `cashier` as separate fields, resets each one every turn, and passes them to the display. A new staff role needs a new field, a reset call, and a new choice type in `processWaiterChoice` or a new processing method.
3. **`GameCLI.availability(staff, index)`** reads the staff list by position (0 for Waiter, 1 for Chef, 2 for Cashier). Adding or reordering staff would silently show the wrong role. Looking up each role by type or name would be safer.
4. **`GameCLI.readWaiterChoice`** has a fixed list of four options. A new Waiter-style task needs a menu entry and a `WaiterAction` value.
5. **`Restaurant`** already holds the seating logic, so a role that also seats customers could reuse `seatCustomer`. A role with new rules would add a method there, which grows the class discussed in Prompt 1.

**Summary:** customer types are well isolated, and the one cost is `ArrivalSchedule`. Staff roles are less isolated, because the controller and CLI know the three roles by position and field.
