# Stage 2 AI history: chat-01

**Tool:** Claude (Claude Code, model Sonnet 5.5)
**Date:** 14 November 2026

---

## Prompt 1

Compare the `stage-1` and `stage-2` tags and list the new files, the changed files with the reason for each change, and any deleted files. Summarize the effect on the design, not only the line counts, and state what existing behavior was preserved.

### Reply

**New files (3):**
- `VIPCustomer.java` extends `Customer`. It overrides `waitingPenalty()` to return 5 and `billFor()` to apply a 10% discount.
- `CriticCustomer.java` extends `Customer`. It overrides `waitingPenalty()` to return 12 and `chooseItem()` to pick the most expensive item on the menu. It keeps the first item in menu order if two prices tie.
- `CustomerType.java` is an enum (`REGULAR`, `VIP`, `CRITIC`) that tells the arrival schedule which customer to create.

**Changed files (2):**
- `ArrivalSchedule.java` now takes a map of arrival turn to customer type instead of a set of turns. `customerAt(turn)` looks up the type and creates the matching customer. Reason: it is the one place that decides which concrete class to create, so the change had to go there.
- `GameController.java` now passes the customer type for each of the six arrival turns. Reason: the type of each arrival is setup data. No game rule or turn step changed.

**Deleted files:** none. The diff is 5 files, 86 insertions and 9 deletions.

**Effect on the design.** The line count understates the result. `Customer`, `Restaurant`, `Order`, `Table`, `ScoreBoard`, the three staff classes and `GameCLI` are unchanged. This is because Stage 1 already had `waitingPenalty()` as an abstract method and `chooseItem()` and `billFor()` as overridable methods, and `Restaurant` calls them without checking the customer's type. The new behavior is added by new subclasses, so only the creation point changes.

**Preserved behavior, with the changes that are expected:**
- The Stage 1 defeat run gives the same result (0 paid, 2 unhappy departures).
- The Stage 1 victory input still wins, but the numbers change from $51.00 and 80.0 to $49.50 and 78.5, because customer 2 is a VIP and customer 3 is a Critic. The VIP pays $13.50 instead of $15.00 for the Burger, and its satisfaction when paying is 90 instead of 84. The Critic's satisfaction when paying is 64 instead of 76, because it loses 12 per turn instead of 8.
- All turn steps, timing rules and win conditions are unchanged.

---

## Prompt 2

Compare my Stage 2 code with `stage2-class-diagram.mmd` and list every discrepancy in attributes, operations, visibility, relationships and annotations. Confirm that the polymorphism note on `Customer` accurately describes the code.

### Reply

**Method.** I extracted the 27 classes from `stage2-class-diagram.mmd` and checked that every attribute and operation name exists in the matching Java file. I also listed the public and protected methods in the code that the diagram does not show, and I read the relationships and notes against the code.

**Result: no missing members.** All 27 diagram classes have a matching source file. The three new classes, the `CustomerType` enum and the changed `ArrivalSchedule` constructor taking `Map<Integer, CustomerType>` are all in the code as drawn. The relationships `ArrivalSchedule ..> VIPCustomer : creates` and `ArrivalSchedule ..> CriticCustomer : creates` match the `switch` in `ArrivalSchedule.customerAt`.

**Remaining differences (all minor):**
- `toString()` overrides in `Table`, `Customer`, `Order` and `Dish` are not in the diagram.
- `Main` (the entry point) is not in the diagram.
- Private helper methods are not drawn.

**Limits of this check.** The script compared names only. Visibility and types were checked by reading the code against the diagram, not by a tool.

**The polymorphism note is accurate.** It says `waitingPenalty()` is abstract, that `chooseItem()` and `billFor()` can be overridden, and that `Restaurant` and `GameController` call them without type checks. That matches the code: `Restaurant.applyWaitingPenalties` calls `c.waitingPenalty()`, `Restaurant.placeOrder` calls `chooseItem` and `billFor`, and neither class refers to `VIPCustomer`, `CriticCustomer` or `CustomerType`. The notes on `VIPCustomer` (5 per turn, 10% off) and `CriticCustomer` (12 per turn, most expensive item) also match the code.

---

## Prompt 3

Section 4.2 of the brief says different customer behavior must be dispatched through the customer objects, not through type checks in the turn controller. Review my Stage 2 code for any type checks or `switch` statements on customer type, and assess whether `ArrivalSchedule` complies.

### Reply

I searched the Stage 2 source for `instanceof`, `getClass()`, `switch` and `case`.

**Switches on customer type: one, in `ArrivalSchedule.customerAt`.** It switches on `CustomerType` to create the right subclass (`VIPCustomer`, `CriticCustomer`, or `RegularCustomer` by default). This is creation, not behavior. After the object exists, nothing branches on its type.

**Other matches, none on customer type:**
- `GameController.processWaiterChoice` switches on `WaiterAction` (the manager's menu choice).
- `GameCLI` switches on the number the player typed.
- `GameCLI.describeCustomer` and `Customer.toString` call `getClass().getSimpleName()` to print the name. That is display only.

**Assessment: `ArrivalSchedule` complies with the brief.** The rule it protects is "no chain of type checks in the turn controller", and `GameController` has no check on customer type. It only passes the type for each arrival into `ArrivalSchedule`. The behavior differences (penalty, discount, food choice) all go through virtual calls on `Customer`.

**One caveat for the report.** The `switch` in `ArrivalSchedule` is a place that must be edited for each new customer type. That is an OCP cost, but it is limited to one method and it is creation code. Prompt 5 covers alternatives.

---

## Prompt 4

Verify that the Stage 1 turn rules still hold in Stage 2 for VIP and Critic customers, covering waiting penalties, discount calculation, food choice, abandonment and payment timing. Then check my Stage 2 test notes against the Stage 2 requirements in the brief.

### Reply

**Checks I ran on the `stage-2` tag.** I compiled it (no warnings), reran both scripted README runs, and wrote a small throwaway test that calls the customer classes directly.

**Rule checks:**
- **Waiting penalties:** Regular, VIP and Critic return 8, 5 and 12, as the brief specifies.
- **Abandonment turns:** starting at 100 satisfaction, a Regular leaves after 13 waiting turns, a VIP after 20 and a Critic after 9. Satisfaction never goes below 0.
- **Discount:** the VIP bill is 810 for Salad, 1350 for Burger and 1620 for Pasta, which is exactly 90% of 900, 1500 and 1800. The Critic and Regular pay full price. The prices divide evenly, so the integer arithmetic does not round.
- **Food choice:** Regular and VIP customers choose by ID (1 Salad, 2 Burger, 3 Pasta, repeating). The Critic chooses Pasta for every ID, because Pasta is the most expensive item.
- **Payment timing:** the READY then SERVED, eating and payment turn rules are in `Order` and `Customer`, which Stage 2 did not change. In the victory run, customers 1 to 4 pay on turns 5, 8, 12 and 15, as they do in the README table.

**Scripted runs:**
- Victory ends with 4 paid, $49.50 and average satisfaction 78.5.
- Defeat ends with 0 paid and 2 unhappy departures (the Regular on turn 13 and the Critic on turn 15).

**Stage 2 test notes against the brief.** Section 4.2 requires a reproducible base-setting run that includes at least one VIP and one Critic, six arrivals, the same win and lose criteria, and the same order and service flow for all customer types. The README covers each point: the arrivals list names the VIP (turn 4) and the Critic (turn 7), and the victory run serves and pays both. The Critic's rule is documented in the notes.

**Limitation to be aware of.** In the base schedule the Critic is customer 3, who would also get Pasta under the ID rule. The run therefore does not show the Critic's preference differing from a Regular's. The Critic's other difference does show: it pays with satisfaction 64 where a Regular in the same position would have 76. The brief asks for a distinct preference that you define and document, not for the run to show it differing, so the notes are compliant.

---

## Prompt 5

I create customers in `ArrivalSchedule` using a `CustomerType` enum and a `switch`. Describe one realistic alternative, such as a factory or a supplier of customers per arrival, and explain its trade-offs for adding new customer types in a later stage.

### Reply

**Current design.** `ArrivalSchedule` holds `Map<Integer, CustomerType>` and a `switch` that maps each type to a constructor call. Adding a type means changing three places: the enum, the `switch`, and the arrival setup.

**Alternative: put the creation in the enum.** Give `CustomerType` an abstract method `create(int id)`, implemented per constant (for example `VIP { Customer create(int id) { return new VIPCustomer(id); } }`). `ArrivalSchedule` then calls `type.create(nextCustomerId++)` with no `switch`.
- **Gain:** a new customer type touches only the enum and the new subclass. `ArrivalSchedule` becomes closed to change, which is the clearest OCP improvement.
- **Cost:** the enum in the `model` package now depends on every concrete customer class, so it knows the subclasses instead of `ArrivalSchedule`. The knowledge moves, it doesn't disappear. The enum also becomes harder to read as a simple list of labels.

**Second alternative: a supplier map.** Store `Map<Integer, Function<Integer, Customer>>` in the schedule, so each arrival carries its own constructor (`VIPCustomer::new`).
- **Gain:** no enum and no `switch`, so adding a type needs only the new class and a line of setup.
- **Cost:** the schedule is no longer plain data. Printing or testing which type arrives on which turn is harder, and the diagram would show lambdas instead of a named type, which communicates less.

**Alternative to inheritance itself.** The penalty and discount could be strategy objects held by a single `Customer` class instead of subclasses. That would remove the subclasses, but the Critic's food rule and the brief's wording ("different behavior dispatched through the customer objects") fit subclasses with overridden methods more naturally. With three customer types the extra objects would add structure without a payoff.

**Recommendation for the report.** Keep the enum and `switch` for Stage 2: it is small, readable and keeps the diagram clear. State the OCP cost honestly, and name the enum with an abstract `create` method as the improvement you would make if more customer types were expected.
