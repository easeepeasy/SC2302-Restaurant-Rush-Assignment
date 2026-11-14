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

## Build and run

Replace `stage-1` with `stage-2` or `stage-3` to run another stage.

```bash
git clone https://github.com/easeepeasy/SC2302-Restaurant-Rush-Assignment.git
cd SC2302-Restaurant-Rush-Assignment
git checkout stage-1
javac -d out $(find src -name "*.java")
java -cp out restaurant.Main
```

## How to play

- **Waiter** (one task per turn): `1` seat a waiting customer (asks for customer ID and table ID), `2` take a seated customer's order (customer ID), `3` serve a READY order (order ID), `4` wait.
- **Chef** (one preparation unit per turn): `1` prepare an order (order ID), `2` wait.
- **Cashier** acts automatically: it collects at most one eligible payment per turn, oldest first.

An invalid choice shows the reason and asks again, and the staff task is not used up. Order ID equals the ID of the customer who placed it. Customers choose their own food by a fixed rule: customer ID 1, 2, 3, 4, 5, 6 orders Salad, Burger, Pasta, Salad, Burger, Pasta.

The game always runs 18 turns. Customers arrive at the start of turns 1, 4, 7, 10, 13 and 16, and there are 2 tables. You win if, at the end of turn 18, at least 4 customers have paid, revenue is at least $45.00, and the average satisfaction of paid customers is at least 60.

## Stage 1 test notes

Paste each command into the terminal, in the folder where you compiled `out`. It plays all 18 turns automatically, so you don't need to type any choices.

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

## Stage 3 test notes

## Stage comparisons

For each transition: the new files, the existing files changed or deleted and why, what still works, and a link to the commit comparison.

### Stage 1 to Stage 2



### Stage 2 to Stage 3


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

## Members and AI history

| Member | Branch | AI history |
|---|---|---|
| | | |
| | | |
| | | |
| | | |
| | | |

