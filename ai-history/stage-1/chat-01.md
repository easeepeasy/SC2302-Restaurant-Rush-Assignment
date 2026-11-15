# Stage 1 AI history: chat-01

**Tool:** Claude (claude.ai chat, model Opus 5.5)
**Date:** 14 November 2026

---

## Prompt 1

*Attached: `SC2002_Restaurant_Rush_Group_Assignment1.docx`*

I'm responsible for `Table` and the waiting queue. Before I write them, suggest what each one should be responsible for, and whether the queue should be its own class or a list inside `Restaurant`. Explain the trade-offs. Don't write code.

### Reply

I'd make `Table` a thin class that only tracks who is sitting there. I'd make the waiting queue its own class rather than a list inside `Restaurant`. Here's what each should handle, and why.

**Table**

A table is a resource that is either free or occupied. It should handle:

- **Its identity:** a table number, so the manager can pick "Table 1" or "Table 2" and the display can show it.
- **Who is sitting there:** a reference to the seated customer, or none.
- **Keeping itself consistent:** seating someone at an occupied table should fail with a reason, and freeing it clears the occupant. The brief says "a table stays occupied until its customer pays or leaves". `Table` should enforce that itself instead of trusting every caller to check first.
- **Answering questions for the display:** is it free, and who is seated.

It should not handle:

- **Satisfaction, orders or billing.** Those belong to `Customer` and `Order`. If `Table` holds the order too, it ends up acting as a second customer object.
- **Choosing who gets seated.** That is the manager's choice, carried out by a staff action.
- **Knowing whether a Waiter or a Host seated the customer.** This matters in Stage 3. The Host also seats people, so seating should be one shared operation (on `Restaurant` or your turn controller) that both staff roles call. If seating logic sits inside `Waiter`, you'll have to copy it or rewrite it for `Host`. That's exactly the "modified earlier class" change you'd then have to justify in the README.

Decide one thing early: which direction the link goes. Two separate events free a table: payment (step 4) and abandonment (step 5). Each needs to find the right table from a customer.

- **Option 1: only `Table` points to its customer.** `Restaurant` finds the table by searching. With two tables that costs nothing, and there's only one link to keep in sync.
- **Option 2: `Customer` and `Table` point to each other.** Lookups are easy, but every seat and free has to update both sides, and a mismatch is a quiet bug.

I'd go with option 1, and route both events through a single "release the table for this customer" operation.

**The waiting queue**

It should handle:

- **Arrival order.** New arrivals join at the back (step 1), and the start-of-turn display shows them in order.
- **Removing a chosen customer, not just the front one.** The brief says the player sets seating priority, so the manager can seat any queued customer. Your "queue" is really an ordered waiting list. Say that clearly in the design discussion, or a marker may ask why you didn't use a strict first-in-first-out queue.
- **Checking the manager's choice.** If the position or customer is invalid, it should report why, so the UI can show the reason and re-prompt without using up the Waiter's task.
- **Removing customers who leave.** In step 5, a waiting customer who drops to 0 satisfaction leaves.
- **A read-only view for the display.** The UI should never get the real collection to change.

It should not apply the satisfaction loss. Step 5 applies to waiting and seated-but-unserved customers. In Stage 2 the amount depends on the customer type (8, 5 or 12 points per turn). So the turn controller should go through all the affected customers, and each customer should apply its own loss. Stage 3's Happy Hour recovery works across both groups the same way, so it should follow the same path.

**Own class or a list inside `Restaurant`?**

| | List inside `Restaurant` | Separate `WaitingQueue` class |
|---|---|---|
| Simplicity | Fewer classes, nothing to wire up | One more class |
| Encapsulation | `Restaurant` must hide the list itself; it's easy to leak it through a getter | The collection is private, and only valid operations are offered |
| Cohesion | `Restaurant` grows into a "god class" holding tables, queue, revenue and turn state | Queue rules live in one place |
| Stage 3 (Host) | Host and Waiter both reach into `Restaurant`'s list | Both use the same queue methods; nothing changes |
| Class diagram | Shows up as just an attribute; nothing to annotate | A clear composition (`Restaurant` has exactly one `WaitingQueue`) and a good spot to annotate encapsulation |
| Risk | Rules spread across `Restaurant` methods | Could become a pointless wrapper if it only passes calls through to a list |

**My recommendation:** make it a separate class, because it has real rules: manager selection with validation, removal on abandonment, an ordered view for display, and two staff roles using it from Stage 3. That's enough behaviour to justify a class.

The danger to avoid is the one in the brief's warning about checklist interfaces. If your `WaitingQueue` only has add, remove and get that pass straight through to a list, a marker may see it as an empty extra layer. Don't add an interface for it either. One concrete class is enough.

You can use the list-inside-`Restaurant` option as the alternative design the report asks you to discuss. You considered it, it's simpler, and you rejected it for cohesion and the Stage 3 Host.

**One thing to agree with your group**

Two places could say whether a customer is waiting: being in the queue, and the customer's own status (something like `WAITING`, `SEATED` or `ORDERED`). If both exist, they can disagree. Make "seat a customer" one operation that does three things together: remove them from the queue, seat them at the table, and update their status. Do the same for "customer leaves". That keeps the queue, the tables and the customers in sync, and gives you a clean sequence diagram for the seating step.

---

## Prompt 2

*Attached: `Table.java`*

Review my `Table` class. A table must stay occupied until its customer pays or leaves. Check that it can't be released twice, can't be given a second customer while occupied, and is freed in both the payment path and the leaving path. List any gaps.

### Reply

Your class correctly blocks a second customer while the table is occupied. It doesn't stop a table being released twice, and this file alone can't show that both the payment path and the leaving path free the table.

**1. Can it be given a second customer while occupied? Yes, it's blocked**

`occupy` checks `isFree()` first and throws with a clear message, so a second customer can't overwrite the first. There are two smaller gaps:

- **`occupy(null)` is allowed.** It sets the occupant to null, so the table stays free and nothing reports a problem. A missing customer from a bug elsewhere would pass silently. It should be rejected.
- **The same customer can be seated at two different tables.** Table 1 and Table 2 each only check themselves, so neither can catch this. The shared seating operation in `Restaurant` or your controller has to check it, for example by making sure the customer comes out of the queue first.

**2. Can it be released twice? No, there's no guard**

`release()` sets the occupant to null without checking anything. Releasing a free table silently succeeds. This matters because two different events free tables, payment (step 4) and abandonment (step 5). If both ever ran for the same customer, or a bug released the wrong table, you'd get no error.

- **Gap:** a second release should fail the same way a second occupy does, by throwing `IllegalStateException` with a message.
- **Related gap:** `release()` doesn't say who is leaving. Any code can free any table at any time, even if the person seated there hasn't paid or left. A safer version takes the departing customer and checks it matches the current occupant. That catches "freed the wrong table" bugs as well as double releases.

**3. Is it freed in both the payment and leaving paths? Not checkable from this file**

`Table` doesn't contain those paths. They'll be in your Cashier or payment logic and your end-of-turn abandonment logic. Check those files for:

- **Payment:** after the bill is added to revenue and the customer and order are marked `PAID`, the customer's table is released.
- **Leaving while seated:** when a seated customer hits 0 satisfaction, their unfinished order is cancelled and their table is released.
- **Leaving while still queued:** a customer who never got a table is only removed from the waiting queue. No table release should happen. If the leaving code always releases "the customer's table", this case must be handled or it may fail or release the wrong table.
- **Finding the table:** both paths need to get from the customer to the right table. Ideally they share one "release the table for this customer" operation. Then the occupant check from gap 2 sits in one place and both paths get it automatically.

**4. "Stays occupied until the customer pays or leaves" isn't enforced here**

Even with the fixes above, `Table` only knows occupied or free, not why it's being freed. You have two options:

- **Option A: `Table` checks the reason.** On release it checks that the customer's status is `PAID` or `LEFT`. This is the strictest guarantee, but it makes `Table` depend on the customer's status values.
- **Option B: `Table` checks only the occupant.** The release-with-occupant-check is all `Table` does. The paid-or-left rule is enforced at the single release point in `Restaurant`.

I'd pick option B. It keeps `Table` simple and doesn't tie it to customer statuses. Mention it in the design discussion as a deliberate choice about where the invariant lives.

**5. Smaller points**

- **Throwing exceptions vs. re-prompting the manager.** The brief says an invalid choice must explain why and let the manager choose again without using up the staff task. Throwing is fine as a programming safety net. Your controller should still check `isFree()` and report the reason before it calls `occupy`, so a manager's mistake never reaches the exception and never uses up the Waiter's task.
- **`toString` is display formatting.** That's acceptable for debugging. If the CLI uses it as the real start-of-turn display, that formatting arguably belongs in the UI layer. It's minor, but a marker checking the boundary between the UI and the game rules might notice.

**Summary of gaps**

- `release()` on a free table succeeds silently, so double release isn't caught.
- `release()` doesn't check which customer is leaving, so the wrong table can be freed.
- `occupy(null)` is accepted silently.
- Nothing at this level stops one customer being seated at two tables. Your shared seating operation needs to check that.
- Freeing on payment and on leaving can't be confirmed from this file. Check both paths, plus the queued-customer-leaves case, in the Cashier and abandonment code.

---

## Prompt 3

*Attached: `Restaurant.java`*

Check my seating logic against the brief. Customers should leave the queue in arrival order. Seating should fail when no table is free, and the failure should explain why without using up the Waiter's task. Point out any case where a customer could be seated twice or lost from the queue.

### Reply

Your seating logic is mostly sound. Every check runs before anything changes, so a failed attempt leaves the game untouched. There are four issues: arrival order isn't enforced, the "no free table" message is indirect, duplicate entries in the queue can lead to double seating, and the order in which you update the queue, table and customer leaves a small window where a customer can be lost.

**1. Arrival order: not enforced (check what your group intends)**

The queue keeps arrivals in order, so the display shows them correctly. But `seatCustomer` takes any `customerId` and pulls that customer out from anywhere in the queue. The manager can seat customer #3 ahead of customer #1. So, as written, customers do not have to leave in arrival order.

That may actually be correct. The brief says the player "sets seating, ordering and serving priority through the Waiter", and the Waiter's task is to "seat one queued customer". That reads as manager choice, not strict first-come-first-served. Your group needs to settle one rule:

- **If arrival order is the rule:** `seatCustomer` should only accept the customer at the front of the queue. Choosing anyone else should fail with a reason like "Customer #1 arrived first and must be seated first". At that point the manager is really only choosing the table.
- **If manager choice is the rule (closer to the brief, I think):** keep the code, and change your stated rule. Write in the report that the queue keeps arrival order for display, but the manager may seat any waiting customer. That's a real choice that changes outcomes, which the brief rewards.

Either is defensible. What matters is that the code and your write-up say the same thing.

**2. Failing when no table is free: works, but the message is indirect**

If both tables are occupied, the manager still has to name a table, and the failure only explains that that particular table is taken. It's correct and it gives a reason, but it doesn't say "no table is free". The manager might try the other table and get a second failure. Better:

- Check for "any free table at all" right after the empty-queue check, and fail with something like "All tables are occupied; no one can be seated this turn."
- Use the same check for the start-of-turn display of available staff tasks. Then seating isn't offered as an option when it can't succeed.

**3. Not using up the Waiter's task: fine here, check the controller**

Within `Restaurant`, every failure returns before anything is changed: the empty queue, the customer not being in the queue, a missing table and an occupied table are all checked first. That part is right.

But `Restaurant` doesn't decide whether the Waiter's task is used up. Your turn controller does. Check that it marks the Waiter as busy only when the result is ok, and loops back to re-prompt on a failure. Check the same for the Host's seating action in Stage 3.

**4. How a customer could be seated twice**

Duplicate queue entries are the main route. `enqueue` adds the customer to both `customers` and `waitingQueue` with no check for duplicates. If the same customer is ever enqueued twice, for example the arrival step running twice in one turn or a scenario replay bug:

- `waitingQueue.remove(customer)` removes only the first copy, so the customer is still in the queue after being seated.
- On a later turn, `findQueuedCustomer` finds them again, the other table is free, and they get seated at a second table. `Table` can't stop this, because each table only checks itself.
- The duplicate in `customers` also means `applyWaitingPenalties` applies the satisfaction loss twice per turn to that customer.

**Fix:** `enqueue` should refuse a customer who is already known or who isn't in the waiting state.

**Same customer, Waiter and Host in one turn (Stage 3):** this is safe. The first seating removes them from the queue, so the second attempt fails with "not in the waiting queue".

**Duplicate customer ids:** `findQueuedCustomer` matches on id. If your id generator ever gives two customers the same id, the manager's choice could seat the wrong person. This is unlikely, but make sure ids come from a single counter.

**5. How a customer could be lost from the queue**

The order of updates in `seatCustomer` is the risk. It removes the customer from the queue, then occupies the table, then calls `customer.seatAt(table)`. Two of those steps can throw:

- `table.occupy` can't throw in practice, because you checked `isFree()` just before.
- `customer.seatAt` can throw if `Customer` guards its own state, for example "can only be seated while `WAITING`". I can't see `Customer`, so check this. If it throws, the customer has already left the queue and is sitting in the table, but their state still says `WAITING`. They're no longer queued, not properly seated, and `placeOrder` will reject them as "not seated". That's a stuck, half-lost customer.

**Fix:** either check the customer's state among the up-front checks so nothing below can fail, or remove the customer from the queue as the last step, after the table and customer updates succeed.

Leaving from the queue is fine. `removeUnhappyCustomer` removes them from the queue, finds no table for a queued customer, records the abandonment and marks them as left. That's the intended removal, not a loss.

**6. Outside seating, but worth checking**

`applyWaitingPenalties` and `applyHappyHourRecovery` only act on customers who are `WAITING` or `SEATED`. If `attachOrder` moves the customer to a new state such as `ORDERED`, they stop losing satisfaction once they've ordered. The brief's example has satisfaction still dropping on turns 2 and 3, after the Pasta order and before it's served. They'd also miss Happy Hour recovery while waiting for food. If ordering changes the customer's state, both methods need to include it.

**Gaps**

- Any queued customer can be seated, not just the first to arrive. Decide which rule is yours and make the code and report agree.
- There's no direct "no table is free" failure; the manager only learns that the table they picked is taken.
- The Waiter's task not being used up on a failure depends on the controller, so check it there.
- `enqueue` allows duplicates, and a duplicate customer can be seated at a second table and penalised twice.
- If `customer.seatAt` can throw, a customer can be removed from the queue without being properly seated.
- If ordering changes the customer's state, seated customers who have ordered may skip the penalty and the Happy Hour recovery. This is outside seating but affects the turn result.

---