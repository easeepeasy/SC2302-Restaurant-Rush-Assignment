# Handover: Person B (Stage 1)

Owner: Person B. Scope: `Table`, the waiting queue, satisfaction loss and customers leaving.
Branch: `stage-01`.

## 1. What is delivered

| File | Purpose |
|---|---|
| `Table.java` | One table: holds at most one customer until they pay or leave |
| `Restaurant.java` (B section) | Waiting queue, the 2 tables, seating, end-of-turn waiting penalties and departures |

`Restaurant.java` now also implements `placeOrder`, `prepareOrder`, `serveOrder`, `finishEating`, `payOldestEligible` and the getters (`getWaitingQueue`, `getTables`, `getActiveOrders`, `getScoreBoard`), so it matches the class diagram. The placeholders were replaced by Person A. The B section (seating, penalties, leaving) is unchanged apart from the two calls described in Section 5.

Not included on purpose: Happy Hour recovery (Stage 3), `ActionResult` (Person C), `ScoreBoard` (Person D).

## 2. Design rules that live in these classes

- **`Restaurant` coordinates changes to the queue, tables and orders.** Each class still validates its own state transitions. Nobody else calls `table.occupy()` / `table.release()` directly. `Restaurant.payOldestEligible` frees the table when a customer pays.
- **The queue keeps arrival order, but any queued customer can be seated.** The manager chooses who to seat by customer id, not just the front of the queue. `Queue<Customer>` (`ArrayDeque`) still works because `remove(Object)` and iteration are allowed.
- **Invalid seating never throws.** `seatCustomer` returns `ActionResult.fail(reason)` for: empty queue, customer not in queue, table does not exist, table occupied. The Waiter only uses up its task when `isSuccess()` is true.
- **Who loses satisfaction:** every customer whose state is `WAITING` or `SEATED` ( including seated customers who have ordered but not been served). Served, eating and ready-to-pay customers lose nothing.
- **Polymorphism:** the amount lost comes from `customer.waitingPenalty()`. Stage 2 VIP/Critic only override that method; `applyWaitingPenalties()` needs no change and has no type checks.
- **Leaving:** a customer who reaches 0 is removed from the queue, their table is freed, their order is removed from `activeOrders` and cancelled (`customer.leave()`), and `scoreBoard.recordAbandonment()` is called. All leavers are collected first, then removed, so nobody is skipped while looping.
- `Table.occupy()` throws `IllegalStateException` if the table is taken. The player can never trigger this because `seatCustomer` checks first; it only catches bugs.

## 3. API by teammate

### Person C (Waiter, Cashier)
| Action | Call |
|---|---|
| Waiter: seat | `restaurant.seatCustomer(customerId, tableId)` returns `ActionResult`; consume the task only if `isSuccess()` |
| Cashier: collect payment (also frees the table) | `restaurant.payOldestEligible(turn)` returns `ActionResult` |

I use `ActionResult.ok(String)` and `ActionResult.fail(String)` (static) plus `isSuccess()` / `getMessage()`, as in the class diagram. Please keep those signatures.

If the Chef works from `activeOrders`, a customer who leaves has their order removed from that list and set to `CANCELLED`, so it can no longer be advanced.

### Person D (GameController, ScoreBoard)
| Step | Call |
|---|---|
| Setup | `new Restaurant(GameConfig.TABLE_COUNT)` |
| 1 Arrivals | `restaurant.enqueue(customer)` |
| 5 Waiting and abandonment | `restaurant.applyWaitingPenalties()` (once, at turn end, after payment) |
| Results | `scoreBoard.getUnhappyDepartures()` (or whatever getter you use) |

I need from `ScoreBoard`: `recordAbandonment()`. The `Restaurant` constructor creates the `ScoreBoard`, as in the diagram.

`applyWaitingPenalties()` must run **after** step 4. A customer served this turn is already EATING by then, so they correctly lose nothing.

### Person E (GameCLI)
| Need | Call |
|---|---|
| Waiting queue in arrival order | `restaurant.getWaitingQueue()` (read-only `List<Customer>`) |
| Tables | `restaurant.getTables()`, then `table.getId()`, `table.isFree()`, `table.getOccupant()` |
| One-line table text | `table.toString()` e.g. `Table 1: Customer #2` / `Table 2: free` |

### What I use from Person A
`getId()`, `getState()`, `getOrder()`, `seatAt(table)`, `waitingPenalty()`, `loseSatisfaction(int)`, `isSatisfied()`, `leave()`. (`markSeated()` and `isUnserved()` no longer exist.)

## 4. Verified behaviour

An earlier scratch test, written against Person A's old API (before the diagram refactor) and not re-run, checked:
- The brief's Pasta example (Section 3.3): satisfaction 92, 84, 76 at the end of turns 1-3 (seated, ordered, READY but not served), then no loss once served.
- Seating a customer who is not at the front of the queue; the rest of the queue keeps its order.
- Each invalid seat returns a failure with a reason: empty queue, customer not in queue, table occupied, table does not exist.
- A waiting Regular customer is at 4 after 12 turn ends and leaves on the 13th. Three customers (two queued, one seated with an order) all leave on the same turn: order CANCELLED, table freed, queue empty, 3 abandonments recorded.
- Customers who have left are never penalised or counted again.

The test is not in the repo.

## 5. Known limitations and open items

- **Two `Restaurant` calls changed** after Person A's refactor to the class diagram: `customer.markSeated()` became `customer.seatAt(table)`, and the `isUnserved()` check became a `WAITING`/`SEATED` state check. `findTableOf` is now private because the diagram does not list it.
- `Restaurant.java` still needs `ActionResult` and `ScoreBoard` (not written yet) to compile.
- `applyWaitingPenalties()` returns `void`, so the CLI can show how many left but not who. If E wants "Customer #3 left unhappy", I can change it to return `List<Customer>`; the diagram must change too.
- Stage 3 (Happy Hour) will need one new `Restaurant` method that gives +15 (max 100) to every `WAITING` or `SEATED` customer. Not added in Stage 1 on purpose.
- Branch rules: commit to `stage-01`. Do not tag yet. Tag `stage-1` on the final integrated, runnable commit.

## 6. Build and quick check

```bash
javac -d out $(find src -name "*.java")
```

Files are in `src/restaurant/model/` (package `restaurant.model`).
