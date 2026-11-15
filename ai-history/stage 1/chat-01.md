# Stage 1 AI history

**Tool:** Claude (claude.ai, model Opus 5.5)
**Date:** 3–4 November 2026

---

## Prompt 1

*Attached: `SC2002_Restaurant_Rush_Group_Assignment1.docx`, `stage1.svg` (our Stage 1 class diagram)*

Attached is the details of a group project and the class diagram we have made for stage 1. Check the class diagram for anything we missed out or anything that could be designed better. I am in charge of the game controller, what should I take note of?

### Reply

The diagram is solid, and the controller's job is mostly already defined by it. The controller shouldn't hold game rules. It runs the six turn steps in order and hands each step to the right object:

1. **Arrivals:** `arrivals.customerAt(turn)`, then `restaurant.enqueue(c)` if someone arrives.
2. **Show state:** reset each staff member for the turn, then `cli.showState(...)`.
3. **Waiter choice:** read the choice and process it. If it fails, show the reason and ask again without using up the task.
4. **Chef choice:** same pattern, after the Waiter, so the Chef can cook an order taken this turn.
5. **Eating and payment:** `restaurant.finishEating(turn)`, then the Cashier collects automatically.
6. **Penalties:** `restaurant.applyWaitingPenalties()`.

Gaps that affect your part:

- **End-of-turn display:** the brief asks for served count, departures, revenue, average satisfaction (or N/A) and turns used. Check which `GameCLI` method shows these.
- **Same-turn timing:** "a dish finished this turn can't be served" and "READY_TO_PAY this turn pays next turn" should be checked by `Order` and `Customer` using turn numbers, not by the controller.
- **Abandonment bookkeeping:** agree on who frees the table and records the departure when a customer reaches 0. It belongs in `applyWaitingPenalties()`, not the controller.
- **Reproducible runs:** if `GameCLI` can read from an input stream, you can replay a script of moves for the test notes.
- **Stage 2/3:** keep `runTurn` split into small step methods so the Host and Happy Hour can slot in later. A `switch` on the player's action is fine; never a `switch` on customer type.

---



## Prompt 2

for stage 1, I have implemented up to the GameController, test my current implementation with a GameCLI.java according to our class diagram and tell me if there are any defects or break cases

### Reply

I ran the `stage-1` code in my own sandbox with a temporary test `GameCLI` (the diagram's methods, playing scripted moves). Your files weren't changed.

- **No defects in the game rules.** The README victory route gives VICTORY with $51.00 and avg 80.0; waiting every turn gives DEFEAT.
- The brief's §3.3 Pasta example matches step by step (92 → 84 → 76, served the turn after READY, paid one turn later).
- Invalid choices are rejected with a reason, and the task isn't used up.
- A customer who reaches 0 while holding an order leaves, the order is cancelled, the table is freed, and no revenue is added.

**Break cases and weak spots:** successful actions and payments aren't shown; a `null` choice from the CLI would crash the controller; running out of input ends with a stack trace; arrival turns are hard-coded in `GameController`, not `GameConfig`; `ArrivalSchedule.reset()` is unused; "oldest first" payment can't be triggered in Stage 1.

---

## Prompt 3

Ok now test the entire stage 1 with test cases, including edge cases and state the important cases tested and results to me, identify any errors raised

### Reply

I built the `stage-1` tag as submitted, with the real `GameCLI`, and drove it with typed input.

| Area | Cases | Result |
|---|---|---|
| Build | Compile; no Stage 2/3 features | Pass |
| Whole games | README victory/defeat; best case (5 paid, $66.00); slow service (avg 56.0); only 3 served | Pass |
| Win limits | Exactly 4 paid / $45.00 / avg 60 → VICTORY; $44.99 → DEFEAT; avg 59.75 → DEFEAT | Pass |
| Rules | Pasta example, leaving with an order, acting on a customer who left, invalid choices | Pass |
| Typed input | `abc`, empty, `1.5`, huge number, menu `0`/`9`/`-1`, spaces, negative ID | Pass |
| Screens | Start-of-turn (§3.1) and final result (§3.5) | Pass |

**Error raised:** when input ends mid-game, the game crashes with `IllegalStateException: Input ended before the game was complete.` (exit code 1).

---

## Prompt 4

how do we solve this *(the arrival turns being hard-coded in `GameController`, not in `GameConfig`)*

### Reply

- **Option 1 (recommended near the deadline):** update the comment in `GameConfig.base()` so it only describes what the class stores. No behaviour, diagram or test changes. Do it on `stage-03` without moving the earlier tags.
- **Option 2:** move the arrivals (turn → customer type) into `GameConfig`. It's cleaner, but the diagram, Stage 3 tests and README comparison would all need updating.

It also works as the "alternative design / trade-off" example in the report.
