# Handover: Person A (Stage 1)

Owner: Person A. Scope: `Customer`, `MenuItem`, `Order`, `Menu` and their states; money stored as cents.
Branch: `stage-01`. Updated to follow the Stage 1 class diagram exactly.

## 1. What is delivered

| File | Purpose |
|---|---|
| `Customer.java` | Abstract customer: satisfaction, state, order, table, per-turn rules |
| `RegularCustomer.java` | Loses 8 satisfaction per waiting turn, full price, picks food by id |
| `CustomerState.java` | `WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT` |
| `Order.java` | One order: prep progress and the READY/SERVED timing rules |
| `OrderStatus.java` | `PLACED, PREPARING, READY, SERVED, PAID, CANCELLED` |
| `MenuItem.java` | Interface: `getName()`, `getPriceCents()`, `getPrepUnits()` |
| `Dish.java` | A single dish (implements `MenuItem`) |
| `Menu.java` | List of items: `Menu(List<MenuItem>)`, `listItems()`, `itemAt(index)` |
| `Money.java` | `Money.format(1830)` -> `"$18.30"` (helper, not in the diagram) |

Not included on purpose: VIP/Critic, `ComboMeal` (Stage 2/3), `ActionResult`, `ScoreBoard`, `GameConfig`, `GameResult` (other owners).

`Menu` no longer has `baseMenu()`. Whoever builds the game (`GameController`) creates the default menu:
Salad $9.00 / 1 unit, Burger $15.00 / 1 unit, Pasta $18.00 / 2 units, e.g.
`new Menu(List.of(new Dish("Salad", 900, 1), new Dish("Burger", 1500, 1), new Dish("Pasta", 1800, 2)))`.

## 2. Design rules that live in these classes

- **Money is integer cents.** Use `Money.format(cents)` for display.
- **Timing is enforced by turn numbers**, so the controller does not recompute it:
  - A dish that became READY on turn *t* is servable from turn *t+1* (`order.canServe`).
  - `customer.startEating(t)` (called when served on turn *t*) sets `readyToPayTurn = t+1`.
  - `customer.finishEating(turn)` moves EATING to READY_TO_PAY once `turn >= readyToPayTurn`. It does nothing otherwise, so it is safe to call for every customer.
  - `customer.canPay(turn)` is true when READY_TO_PAY and `turn > readyToPayTurn`, so payment is one turn after eating finishes.
- **Polymorphism:** `waitingPenalty()`, `chooseItem(menu)` are abstract in `Customer`; `billFor(item)` returns the item price by default. Stage 2 subclasses only override these, so the controller never checks customer types.
- **Food choice rule (documented):** `RegularCustomer.chooseItem` rotates through the menu by customer id (id 1 -> Salad, 2 -> Burger, 3 -> Pasta, 4 -> Salad, ...). Deterministic, so runs are reproducible.
- **Order id** is passed in by whoever creates the order (`Restaurant.placeOrder`); use the customer id so there is one id per customer.
- State-changing methods throw `IllegalStateException` when misused. Check the guard first (`order.canServe`, `customer.canPay`) and report the reason to the manager instead of catching the exception.

## 3. API by teammate

### Person B (Restaurant, Table, queue, satisfaction loss, leaving)
| Need | Call |
|---|---|
| Move customer from queue to a table | `table.occupy(customer)` then `customer.seatAt(table)` (customer must be WAITING) |
| Who loses satisfaction at turn end? | `customer.getState()` is `WAITING` or `SEATED` (there is no `isUnserved()` any more) |
| Amount to lose | `customer.waitingPenalty()` (protected, same package only) then `customer.loseSatisfaction(points)` (clamped at 0) |
| Did they hit 0? | `!customer.isSatisfied()` then `customer.leave()` (cancels the order) |

After `leave()` you must free the table and remove them from the queue yourself.

`Restaurant` changes needed because of the diagram: `markSeated()` -> `seatAt(table)`, and `isUnserved()` -> the state check above.

### Person C (Waiter, Chef, Cashier) and `Restaurant` order methods
| Action | Guard | Call |
|---|---|---|
| Take order | customer state `SEATED` and `getOrder() == null` | `item = customer.chooseItem(menu)`, `order = new Order(customer.getId(), customer, item, customer.billFor(item), turn)`, `customer.attachOrder(order)` |
| Serve | `order.canServe(turn)` | `order.markServed(turn)` then `customer.startEating(turn)` |
| Chef: advance | order status `PLACED` or `PREPARING` | `order.advanceCookUnit(turn)` (becomes READY on its last unit) |
| Eating | every turn, before the Cashier acts | `customer.finishEating(turn)` |
| Cashier: collect | `customer.canPay(turn)` | `customer.markPaid()`, then add `order.getBillCents()` to revenue |

For "oldest first", sort eligible customers by `customer.getReadyToPayTurn()`, then by `customer.getId()`. The getter was added to `Customer` and to the class diagram.

### Person D (GameController)
Per turn, step 4 (eating and payment): call `finishEating(turn)` for every seated customer (via `Restaurant.finishEating(turn)`) **before** the Cashier acts.

Step 5 loop (inside `Restaurant.applyWaitingPenalties()`):

```java
if (state == WAITING || state == SEATED) {
    customer.loseSatisfaction(customer.waitingPenalty());
    if (!customer.isSatisfied()) {
        customer.leave();   // then free the table / drop from queue
    }
}
```

Record each paid customer's `getSatisfaction()` at payment for the average.

### Person E (GameCLI)
Read-only getters for the start-of-turn display: `getId()`, `getSatisfaction()`, `getState()`, `getOrder()` (null before ordering), `order.getItem().getName()`, `order.getStatus()`, `order.getUnitsDone()`, `order.toString()` (e.g. `Pasta [PREPARING, 1/2]`). `customer.toString()` shows the class name, e.g. `RegularCustomer #3 (satisfaction 76, SEATED)`. There is no `getTypeName()` any more.

## 4. Verified behaviour

The Person A files compile together with `Table.java` (`javac` on all files except `Restaurant.java`). `Restaurant.java` and the missing classes (`ActionResult`, `ScoreBoard`) still need their owners' changes before the whole project builds. The refactored timing was not re-run against the Section 3.3 Pasta example; with the new rules it should give READY on turn 3, served turn 4, READY_TO_PAY turn 5, payable turn 6.

## 5. Known limitations and open items

- `Order.markPaid()` and `Customer.markPaid()` do not take a turn; they rely on the Cashier calling `canPay(turn)` first.
- `Customer.seatAt(table)` only records the table; `Restaurant` still calls `table.occupy(customer)` itself.
- `RegularCustomer.chooseItem` assumes a non-empty menu.
- Files are in `src/restaurant/model/` (package `restaurant.model`). Confirm one layout for the whole team before integration.
- Branch rules: commit to `stage-01`. Do not tag yet. Tag `stage-1` on the final integrated, runnable commit.

## 6. Build and quick check

```bash
javac -d out $(find src -name "*.java")
```

(Once everyone's classes exist, the same command compiles the whole game.)
