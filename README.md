# Restaurant Rush

A Java command-line, turn-based restaurant management game for SC2302. You are the manager: staff time is limited, so you decide which customers and orders come first.

**Repository:** https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment

**Group:** TAIS1 Group 2

## Stages
| Stage | Adds | Tag | Branch |
|---|---|---|---|
| 1 | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, victory and defeat | [`stage-1`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/stage-1) | `stage-01` |
| 2 | VIP and Critic customers | [`stage-2`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/stage-2) | `stage-02` |
| 3 | Host, ComboMeal, Happy Hour | [`stage-3`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/stage-3) | `stage-03` |

The tags identify the finalised, independently runnable versions of each stage. The `main` branch contains the final Stage 3 Java implementation together with the README and diagrams.

## Build and run

Replace `stage-1` with `stage-2` or `stage-3` to run another stage.

```bash
git clone https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment.git
cd SC2302-Restaurant-Rush-Assignment
git checkout stage-1
javac -d out $(find src -name "*.java")
java -cp out restaurant.Main
```

When switching between stage tags, delete the previous compiled classes and rebuild to prevent stale classes from another stage being used:

```bash
git checkout stage-2      # or stage-1 / stage-3
rm -rf out
javac -d out $(find src -name "*.java")
java -cp out restaurant.Main
```

## How to play

- **Waiter** (one task per turn): `1` seat a waiting customer (asks for customer ID and table ID), `2` take a seated customer's order (customer ID), `3` serve a READY order (order ID), `4` wait.
- **Chef** (one preparation unit per turn): `1` prepare an order (order ID), `2` wait.
- **Cashier** acts automatically: it collects at most one eligible payment per turn, oldest first.

An invalid choice shows the reason and asks again, and the staff task is not used up. The updated CLI shows eligible customers, tables and orders for the relevant actions, and announces successful actions and payments. Order ID equals the ID of the customer who placed it. Customers choose their own food by a fixed rule: customer ID 1, 2, 3, 4, 5, 6 orders Salad, Burger, Pasta, Salad, Burger, Pasta in Stages 1 and 2; see Stage 3 notes for the expanded menu.

The game always runs 18 turns. Customers arrive at the start of turns 1, 4, 7, 10, 13 and 16, and there are 2 tables. You win if, at the end of turn 18, at least 4 customers have paid, revenue is at least $45.00, and the average satisfaction of paid customers is at least 60.

## Stage 1 test notes

Check out the final `stage-1` tag and compile it using the instructions above. Paste each command into the terminal in the repository folder. It supplies the choices for all 18 turns automatically, so you do not need to type them manually. The updated CLI prints eligible actions and successful-action messages, but the numeric choices remain the same.

### Victory run

```bash
printf '%s\n' 1 1 1 2 2 1 1 1 3 1 2 1 2 2 2 2 2 1 2 3 2 2 1 3 1 2 2 3 1 3 4 1 3 3 3 2 1 4 2 2 2 4 1 4 3 4 2 4 2 4 2 4 2 4 2 4 2 | java -cp out restaurant.Main
```

| Turn | Waiter | Chef |
|---|---|---|
| 1 | Seat customer 1 at table 1 | Wait |
| 2 | Take order from customer 1 (Salad) | Prepare order 1 |
| 3 | Serve order 1 | Wait |
| 4 | Seat customer 2 at table 2 | Wait |
| 5 | Take order from customer 2 (Burger) | Prepare order 2 |
| 6 | Serve order 2 | Wait |
| 7 | Seat customer 3 at table 1 | Wait |
| 8 | Take order from customer 3 (Pasta) | Prepare order 3 (unit 1 of 2) |
| 9 | Wait | Prepare order 3 (unit 2 of 2) |
| 10 | Serve order 3 | Wait |
| 11 | Seat customer 4 at table 2 | Wait |
| 12 | Take order from customer 4 (Salad) | Prepare order 4 |
| 13 | Serve order 4 | Wait |
| 14 to 18 | Wait | Wait |

Expected final result:

```
VICTORY!
Served customers: 4
Paid customers: 4
Unhappy departures: 0
Revenue: $51.00
Average paid satisfaction: 80.0
Turns used: 18
```

Customers 1, 2, 3 and 4 pay on turns 5, 8, 12 and 15. Revenue is $9.00 + $15.00 + $18.00 + $9.00 = $51.00. Their satisfaction when they pay is 84, 84, 76 and 76, so the average is 80.0.

### Defeat run

```bash
for i in $(seq 18); do printf '4\n2\n'; done | java -cp out restaurant.Main
```

Expected final result:

```
DEFEAT!
Served customers: 0
Paid customers: 0
Unhappy departures: 2
Revenue: $0.00
Average paid satisfaction: N/A
Turns used: 18
```

Customers 1 and 2 wait until their satisfaction reaches 0 and leave on turns 13 and 16. Customer 3 is still waiting at the end of turn 18, so it is not counted as a departure.

### Rule checks

- A dish that becomes READY on turn t cannot be served until turn t+1.
- A customer served on turn t becomes READY_TO_PAY on turn t+1, and pays on turn t+2 at the earliest. The brief's Pasta example (ready on turn 3, served on 4, ready to pay on 5, paid on 6) reproduces exactly.
- An invalid choice (unknown order, unknown table, customer not waiting, customer not seated) shows a reason and leaves the staff task unused.
- Non-numeric input is rejected with a message and asked again.

## Stage 2 test notes

Check out the final `stage-2` tag, delete the previous `out` folder, and compile it. Paste each command in the repository folder. As with Stage 1, the revised CLI displays eligible targets and action feedback without changing the numeric menu options.

The six arrivals are customer 1 (Regular, turn 1), customer 2 (VIP, turn 4), customer 3 (Critic, turn 7), and customers 4, 5 and 6 (Regular, turns 10, 13 and 16). Regular and VIP customers choose food by customer ID. A Critic always chooses the most expensive menu item, which is Pasta in the base menu.

### Victory run

Paste into terminal:

```bash
printf '%s\n' 1 1 1 2 2 1 1 1 3 1 2 1 2 2 2 2 2 1 2 3 2 2 1 3 1 2 2 3 1 3 4 1 3 3 3 2 1 4 2 2 2 4 1 4 3 4 2 4 2 4 2 4 2 4 2 4 2 | java -cp out restaurant.Main
```

| Turn | Waiter | Chef |
|---|---|---|
| 1 | Seat Regular customer 1 at table 1 | Wait |
| 2 | Take order from customer 1 (Salad) | Prepare order 1 |
| 3 | Serve order 1 | Wait |
| 4 | Seat VIP customer 2 at table 2 | Wait |
| 5 | Take order from customer 2 (Burger, 10% off) | Prepare order 2 |
| 6 | Serve order 2 | Wait |
| 7 | Seat Critic customer 3 at table 1 | Wait |
| 8 | Take order from customer 3 (Pasta, most expensive item) | Prepare order 3 (unit 1 of 2) |
| 9 | Wait | Prepare order 3 (unit 2 of 2) |
| 10 | Serve order 3 | Wait |
| 11 | Seat Regular customer 4 at table 2 | Wait |
| 12 | Take order from customer 4 (Salad) | Prepare order 4 |
| 13 | Serve order 4 | Wait |
| 14 to 18 | Wait | Wait |

Expected final result:

```
VICTORY!
Served customers: 4
Paid customers: 4
Unhappy departures: 0
Revenue: $49.50
Average paid satisfaction: 78.5
Turns used: 18
```

Customers 1, 2, 3 and 4 pay automatically through the Cashier on turns 5, 8, 12 and 15. Revenue is $9.00 + $13.50 + $18.00 + $9.00 = $49.50. The VIP pays 90% of the Burger's $15.00 price. Their satisfaction when they pay is 84, 90, 64 and 76, so the average is 78.5.

### Defeat run

Paste into terminal:

```bash
for i in $(seq 18); do printf '4\n2\n'; done | java -cp out restaurant.Main
```

Expected final result:

```
DEFEAT!
Served customers: 0
Paid customers: 0
Unhappy departures: 2
Revenue: $0.00
Average paid satisfaction: N/A
Turns used: 18
```

Regular customer 1 leaves on turn 13 and Critic customer 3 leaves on turn 15. No unpaid or abandoned order contributes to revenue.

### Rule checks

- Unserved waiting or seated Regular, VIP and Critic customers lose 8, 5 and 12 satisfaction per turn respectively, with a minimum of 0.
- A VIP receives 10% off the chosen item's price and keeps the ID-based food choice. A Critic pays full price and always chooses the most expensive menu item, keeping the first one in menu order if prices tie.
- All other rules are the same as Stage 1.

## Stage 3 test notes

Check out the final `stage-3` tag, delete the previous `out` folder, and compile it. Paste each command in the repository folder. Stage 3 asks for a Host choice every turn, and for a Happy Hour answer at the start of each turn until Happy Hour has been used, so the inputs differ from Stages 1 and 2. Each turn's input order is: Happy Hour (1 for yes, 2 for no, only while unused), Host, Waiter, Chef. The arrivals are the same as in Stage 2. The Salad and Pasta combo ($24.30, 3 preparation units) is the fourth menu item, so the ID rule gives customers 1 to 6 Salad, Burger, Pasta, Combo, Salad, Burger. The Critic always chooses the most expensive item, which is now the combo.

### Victory run

Paste into terminal:

```bash
printf '%s\n' 2 1 1 1 2 1 1 1 2 2 3 1 2 2 2 4 2 1 1 2 2 2 2 1 2 2 3 2 2 2 4 2 1 3 1 2 3 1 3 2 4 1 3 2 4 1 3 1 4 2 3 3 2 2 2 4 1 4 2 4 1 4 1 5 1 2 5 1 4 2 3 4 1 5 2 3 5 2 2 4 2 2 4 2 2 4 2 | java -cp out restaurant.Main
```

| Turn | Happy Hour | Host | Waiter | Chef |
|---|---|---|---|---|
| 1 | No | Seat customer 1 at table 1 | Take order from customer 1 (Salad) | Prepare order 1 |
| 2 | No | Wait | Serve order 1 | Wait |
| 3 | No | Wait | Wait | Wait |
| 4 | Yes | Seat VIP customer 2 at table 2 | Take order from customer 2 (Burger, $10.80) | Prepare order 2 |
| 5 | Not asked | Wait | Serve order 2 | Wait |
| 6 | Not asked | Wait | Wait | Wait |
| 7 | Not asked | Seat Critic customer 3 at table 1 | Take order from customer 3 (Combo) | Prepare order 3 (unit 1 of 3) |
| 8 | Not asked | Wait | Wait | Prepare order 3 (unit 2 of 3) |
| 9 | Not asked | Wait | Wait | Prepare order 3 (unit 3 of 3) |
| 10 | Not asked | Seat customer 4 at table 2 | Serve order 3 | Wait |
| 11 | Not asked | Wait | Take order from customer 4 (Combo) | Prepare order 4 (unit 1 of 3) |
| 12 | Not asked | Wait | Wait | Prepare order 4 (unit 2 of 3) |
| 13 | Not asked | Seat customer 5 at table 1 | Take order from customer 5 (Salad) | Prepare order 4 (unit 3 of 3) |
| 14 | Not asked | Wait | Serve order 4 | Prepare order 5 |
| 15 | Not asked | Wait | Serve order 5 | Wait |
| 16 to 18 | Not asked | Wait | Wait | Wait |

Expected final result:

```
VICTORY!
Served customers: 5
Paid customers: 5
Unhappy departures: 0
Revenue: $77.40
Average paid satisfaction: 81.6
Turns used: 18
```

Customers 1 to 5 pay automatically through the Cashier on turns 4, 7, 12, 16 and 17. Revenue is $9.00 + $10.80 + $24.30 + $24.30 + $9.00 = $77.40. The Host and the Waiter both act on turns 1, 4, 7 and 13: the Host seats the arriving customer and the Waiter takes that customer's order in the same turn. Customer 2's Burger costs $10.80: the VIP discount takes $15.00 to $13.50, then Happy Hour takes it to $10.80. Happy Hour also gives customer 2 15 satisfaction at the start of turns 4 and 5, so it pays with 100. The satisfaction recorded for the five paying customers is 92, 100, 64, 68 and 84, so the average is 81.6.

### Defeat run

Paste into terminal:

```bash
for i in $(seq 18); do printf '2\n2\n4\n2\n'; done | java -cp out restaurant.Main
```

The answers each turn are No to Happy Hour, then Wait for the Host, the Waiter and the Chef.

Expected final result:

```
DEFEAT!
Served customers: 0
Paid customers: 0
Unhappy departures: 2
Revenue: $0.00
Average paid satisfaction: N/A
Turns used: 18
```

Regular customer 1 leaves on turn 13 and Critic customer 3 leaves on turn 15. No unpaid or abandoned order contributes to revenue.

### Rule checks

- The Host seats at most one queued customer per turn in addition to the Waiter's task. A failed seating leaves the Host's task unused.
- Happy Hour can be activated once per game and lasts the activation turn and the next. Its remaining duration is shown at the start of each turn.
- The customer's own discount (the VIP's 10%) is applied first, then Happy Hour's 20%. The price is fixed when the order is taken, so earlier orders keep their bills.
- While Happy Hour is active, customers still waiting for a table or food recover 15 satisfaction at the start of the turn, up to 100.
- The combo costs 90% of its two items' combined price, rounded to the nearest cent, and needs the sum of their preparation units.
- All other rules are the same as Stage 1.

## Stage comparisons

Each comparison explains what changed between stages, why those changes were needed, and how existing game behavior was preserved.

**Reading the GitHub comparisons:** Each stage was finalised on its own branch before the final tags were updated. Some shared CLI and choice-handling improvements were applied separately to all three stage branches. As a result, GitHub's three-dot compare pages show changes since a common ancestor, including changes to `GameCLI.java` and `WaiterChoice.java` that are already present in both final tagged versions. To compare the *actual final source snapshots*, use `git diff stage-1 stage-2 -- src/` or `git diff stage-2 stage-3 -- src/`. The final tagged versions are the authoritative stage snapshots.

### Stage 1 to Stage 2

[Compare `stage-1` and `stage-2`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/compare/stage-1...stage-2)

**New files**
- `VIPCustomer.java`: loses 5 satisfaction per waiting turn and gets 10% off by overriding `waitingPenalty()` and `billFor()`.
- `CriticCustomer.java`: loses 12 satisfaction per waiting turn and always orders the most expensive item by overriding `waitingPenalty()` and `chooseItem()`.
- `CustomerType.java`: enum (`REGULAR`, `VIP`, `CRITIC`) that tells the arrival schedule which customer to create.

**Changed files**
- `ArrivalSchedule.java`: takes a map of arrival turn to customer type instead of a set of turns, and creates the matching customer. It centralizes the creation of Regular, VIP and Critic customers.
- `GameController.java`: the arrival setup now passes the customer type for each of the six turns. No game rules changed.

**Deleted files:** none.

**What still works**
- `Customer`, `Restaurant`, `Order`, the staff classes and `GameCLI` are unchanged between the final tagged snapshots, because VIP and Critic only add subclasses. The shared CLI and `WaiterChoice` improvements are present in both tags.
- The Stage 1 defeat run gives the same result. The Stage 1 victory input still wins, with revenue of $49.50 and average satisfaction of 78.5 instead of $51.00 and 80.0. The VIP discount reduces revenue, while the VIP and Critic waiting penalties change the recorded satisfaction.

### Stage 2 to Stage 3

[Compare `stage-2` and `stage-3`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/compare/stage-2...stage-3)

**New files**
- `Host.java`: a staff role that seats one queued customer per turn, in addition to the Waiter, by reusing `Restaurant.seatCustomer()`.
- `HostChoice.java`: carries the manager's Host decision (seat a customer at a table, or wait) from the CLI to the controller, like `WaiterChoice` and `ChefChoice`.
- `HappyHour.java`: holds the pricing policy. It can be used once per game, lasts two turns, takes 20% off a bill and gives waiting customers 15 satisfaction.
- `ComboMeal.java`: a `MenuItem` made of two other items. Its preparation units are their sum and its price is 90% of their combined price.

**Changed files**
- `Restaurant.java`: owns a `HappyHour`. `placeOrder()` applies the Happy Hour discount after the customer's own `billFor()`. It adds `activateHappyHour()`, `applyHappyHourRecovery()` and `getHappyHour()`, and its constructor now requires at least one table.
- `Customer.java`: adds `recoverSatisfaction()` (capped at 100), so Happy Hour raises satisfaction through the customer's own operation. A customer also clears its table reference when it pays or leaves.
- `GameController.java`: owns the Host, asks about Happy Hour at the start of each turn until it is used, lets the Host act before the Waiter, and adds the combo to the menu.
- `GameCLI.java`: new prompts for the Host choice and for Happy Hour, and the display shows Happy Hour's status and the Host's availability.

**Deleted files:** none.

**What still works**
- `Order`, `Menu`, `Dish`, the customer subclasses, `ScoreBoard`, `Waiter`, `Chef`, `Cashier` and `WaiterChoice` are unchanged between the final tagged snapshots, because the combo plugs into the `MenuItem` interface and the Host reuses the existing seating operation.
- The Stage 2 customer behaviours still apply (waiting penalties of 8, 5 and 12 and the VIP discount), and the all-wait defeat run still gives 0 paid and 2 unhappy departures.
- The Stage 1 and Stage 2 victory inputs no longer work as written, because every turn now asks for a Host choice and, until it is used, a Happy Hour answer. The combo is also the fourth menu item, so the ID rule gives customer 4 the combo and customer 5 Salad, and the Critic chooses the combo as the most expensive item.

## Diagrams

- Stage 1 class diagram: [`docs/stage1.png`](docs/stage1.png)
- Stage 2 class diagram: [`docs/stage2.png`](docs/stage2.png)
- Stage 3 class diagram: [`docs/stage3.png`](docs/stage3.png)
- Sequence diagram: [`docs/sequencediagram.png`](docs/sequencediagram.png)

## Video timestamps

| Segment | Speaker | Timestamp |
|---|---|---|
| | | |
| | | |
| | | |
| | | |
| | | |
| | | |
| | | |
| | | |



## Members and AI history

| Member | Branch | AI history |
|---|---|---|
| Levine | [`levine`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/levine) | [`ai-history`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/levine/ai-history) |
| Yi Xi | [`YiXi`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/YiXi) | [`ai-history`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/YiXi/Stage1) |
| | | |
| | | |
| Nicole | [`nicole`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/nicole) | [`ai-history`](https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment/tree/nicole/ai-history) |

