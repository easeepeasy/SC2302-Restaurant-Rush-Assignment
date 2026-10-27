# Handover: Person A (Stage 1)

Owner: Person A. Scope: `Customer`, `MenuItem`, `Order` and their states; money stored as cents.
Branch: `stage-01`. Commit: `4b79fa4`.

## 1. What is delivered

| File | Purpose |
|---|---|
| `Customer.java` | Abstract customer: satisfaction, state, order, per-turn rules |
| `RegularCustomer.java` | Loses 8 satisfaction per waiting turn, full price |
| `CustomerState.java` | `WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT` |
| `Order.java` | One order: prep progress and the READY/SERVED/pay timing rules |
| `OrderStatus.java` | `PLACED, PREPARING, READY, SERVED, PAID, CANCELLED` |
| `MenuItem.java` | Interface: `getName()`, `getPriceCents()`, `getPrepUnits()` |
| `Dish.java` | A single dish (implements `MenuItem`) |
| `Menu.java` | List of items; `Menu.baseMenu()` = Salad $9.00/1, Burger $15.00/1, Pasta $18.00/2 |
| `Money.java` | `Money.format(1830)` -> `"$18.30"` |

Not included on purpose: VIP/Critic, `customerDiscount`, `recover`, `ComboMeal` (Stage 2/3), and `ActionResult` (Person C).

## 2. Design rules that live in these classes

- **Money is integer cents.** Use `Money.format(cents)` for display.
- **Timing is enforced by turn numbers**, so the controller does not recompute it:
  - A dish that became READY on turn *t* is servable from turn *t+1* (`canServe`).
  - A customer served on turn *t* becomes READY_TO_PAY on turn *t+1* (`finishEating`).
  - A customer READY_TO_PAY on turn *t* can pay from turn *t+1* (`canPay`).
- **Polymorphism:** `waitingPenalty()` is abstract; Stage 2 adds subclasses, so the controller never checks customer types.
- **Food choice rule (documented):** `Customer.chooseItem` rotates through the menu by customer id (id 1 -> Salad, 2 -> Burger, 3 -> Pasta, 4 -> Salad, ...). Deterministic, so runs are reproducible. Customers 1-6 order Salad, Burger, Pasta, Salad, Burger, Pasta.
- State-changing methods throw `IllegalStateException` when misused. Always check the `can...` guard first and report the reason to the manager instead of catching the exception.

## 3. API by teammate

### Person B (Table, queue, satisfaction loss, leaving)
| Need | Call |
|---|---|
| Move customer from queue to a table | `customer.markSeated()` (must be WAITING) |
| Who loses satisfaction at turn end? | `customer.isUnserved()` (WAITING or SEATED) |
| Amount to lose | `customer.waitingPenalty()` then `customer.loseSatisfaction(points)` (clamped at 0) |
| Did they hit 0? | `!customer.isSatisfied()` then `customer.leave()` (cancels the order) |

After `leave()` you must free the table and remove them from the queue yourself.

### Person C (Waiter, Chef, Cashier)
| Action | Guard | Call |
|---|---|---|
| Waiter: take order | `customer.getState() == SEATED && !customer.hasOrdered()` | `customer.placeOrder(menu)` returns the `Order` (customer picks the dish; show it to the manager) |
| Waiter: serve | `customer.canBeServed(turn)` | `customer.markServed(turn)` |
| Chef: advance | `order.canAdvance()` | `order.advance(turn)` (order becomes READY on its last unit) |
| Cashier: collect | `customer.canPay(turn)` | `customer.markPaid()`, then add `order.getBillCents()` to revenue |

Cashier "oldest first": sort ready customers by `order.getReadyToPayTurn()`, then `order.getId()`.
Order id equals customer id (one order per customer).

### Person D (GameController)
Per turn, step 4 (eating and payment): call `customer.finishEating(turn)` for every seated customer **before** the Cashier acts. Doing it before payment is what makes "pays next turn at the earliest" work, because `canPay` compares against the turn `finishEating` recorded.

Step 5 loop:

```java
if (customer.isUnserved()) {
    customer.loseSatisfaction(customer.waitingPenalty());
    if (!customer.isSatisfied()) {
        customer.leave();   // then free the table / drop from queue
    }
}
```

Record each paid customer's `getSatisfaction()` at payment for the average.

### Person E (ConsoleUI)
Read-only getters for the start-of-turn display: `getId()`, `getTypeName()`, `getSatisfaction()`, `getState()`, `getOrder()` (null before ordering), `order.getItem().getName()`, `order.getStatus()`, `order.getUnitsDone()`, `order.toString()` (e.g. `Pasta [PREPARING, 1/2]`).

## 4. Verified behaviour

A scratch test reproduced the brief's Pasta example (Section 3.3): READY on turn 3 and not servable that turn; served turn 4; READY_TO_PAY turn 5; payable turn 6; satisfaction 76 and a bill of $18.00 at payment. It also checked that `leave()` cancels the order and that satisfaction clamps at 0. The test is not in the repo.

## 5. Known limitations and open items

- `Order.markPaid()` and `Customer.markPaid()` do not take a turn; they rely on the Cashier calling `canPay(turn)` first. A stricter `markPaid(turn)` is possible if the group wants it, but it changes the signature the Cashier uses.
- `chooseItem` assumes a non-empty menu (always true for `Menu.baseMenu()`).
- `Menu` was added by Person A because `chooseItem(menu)` needs it. Tell the group so nobody writes a duplicate.
- Files are in `src/restaurant/model/` (package `restaurant.model`). Confirm one layout for the whole team before integration.
- Branch rules: commit to `stage-01`. Do not tag yet. Tag `stage-1` on the final integrated, runnable commit.

## 6. Build and quick check

```bash
javac -d out $(find src -name "*.java")
```

(Once other people's classes exist, the same command compiles the whole game.)
