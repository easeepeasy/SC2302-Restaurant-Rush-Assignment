# Stage 3 AI history: chat-01

**Tool:** Claude (claude.ai chat, model Opus 5.5)
**Date:** 14 November 2026

---

## Prompt 1

*Attached: `Restaurant.java`, `SC2002_Restaurant_Rush_Group_Assignment1.docx`*

I'm adding `ComboMeal`, made of two existing menu items. Should it implement `MenuItem` directly and hold two `MenuItem` references? Or is there a better structure? Explain how either choice affects `Order` and `Menu`. Don't write the class.

### Reply

Yes, `ComboMeal` should implement `MenuItem` and hold two `MenuItem` references, provided `MenuItem` is an abstraction (an interface or abstract class). This is the Composite pattern. A combo is a thing a customer can order, and it has two component items. I only had `Restaurant.java` and the brief, so I'm inferring `MenuItem`, `Order` and `Menu` from how `Restaurant` uses them.

**How it works**

`MenuItem` exposes what the rest of the game needs: a name, a price in cents and a number of preparation units. Salad, Burger and Pasta give fixed values. `ComboMeal` works out its values from its two components:

- **Preparation units:** the sum of the two components' units.
- **Price:** 90% of the combined price, rounded to the nearest cent once, in integer cents.

So Salad + Pasta comes to 2430 cents and 3 units, matching the brief.

**Effect on `Order`**

`Order` doesn't change. It holds one `MenuItem`, reads its prep units to decide when `advanceOneUnit` makes it `READY`, and stores the bill it was given. A 3-unit combo just takes the Chef three turns, like any 3-unit item would.

`Restaurant.placeOrder` doesn't change either. `customer.billFor(item)` applies the VIP discount to the combo's price, then `happyHour.discount(...)` applies Happy Hour, and the result is locked into the `Order`. For example, a VIP ordering Salad + Pasta during Happy Hour pays $24.30 → $21.87 → $17.50. This is real polymorphism (Liskov substitution): every caller treats a combo exactly like a single dish.

**Effect on `Menu`**

If `Menu` already stores a list of `MenuItem`, the only change is registering one or more combos at Stage 3 setup. That's the "small registration change" the brief says to expect.

The real new logic belongs in `Customer.chooseItem(menu)`, because the brief requires a documented rule for when a customer picks a combo. Since a combo knows its components, the rule can ask things like "does this combo contain Pasta?". That matters for the Critic: you need to decide whether a Critic who prefers a dish counts a combo containing it.

**Two catches**

- **If `MenuItem` is an enum** (common for three fixed dishes), `ComboMeal` can't implement it. Extract an interface (e.g. `Orderable` or `Dish`) that the enum implements and `ComboMeal` also implements. Then change the parameter types in `Order`, `Customer.chooseItem`/`billFor` and `Menu` from the enum to the interface. That's a Stage 2 → 3 change to existing classes, so give the reason in the README. Better still, introduce the interface at Stage 1 so Stage 3 only adds a class.
- **Typing the components as `MenuItem` allows a combo of combos.** The brief says "two existing menu items", so decide whether to allow nesting. If not, type the components as the single-dish type or reject combos at construction.

**Alternatives and why they're weaker**

- **`Order` holds a list of items.** `Order` then has to sum prep units, know the 90% combo rule and handle rounding. That spreads pricing logic into a class that should only track status (breaks single responsibility). It also changes a stable Stage 1 class, which you'd have to justify. Every other consumer of the order's item (display, Critic preference, bill) also has to handle a list. This makes a good "alternative design considered" for the report.
- **A combo factory that returns a plain `MenuItem`** with the computed name, price and units. `Order` and `Menu` stay untouched, but the object forgets its components. You can't apply a rule like "Critic prefers combos containing Pasta" or list the components on screen. You also lose the composition example the class diagram is supposed to show.

The composite version is also the one that gives you genuine evidence for the open/closed principle (adding combos needed no change to `Order` or `Restaurant`) and for composition in the report.

---

## Prompt 2

Our Critic always orders the most expensive item, and a Salad and Pasta combo costs more than a Burger. Explain how adding combos to the menu changes what each customer type orders, and whether this breaks any Stage 2 behaviour or test run.

### Reply

Adding combos changes what the Critic orders, and may change what Regulars and VIPs order depending on how their choice rule works. It doesn't break the Stage 2 version you submit, because that's a separate tagged version, but your Stage 2 test runs won't reproduce on the Stage 3 build. I haven't seen your Regular or VIP choice rules, so that part is conditional.

**The Critic**

The Critic's rule is unchanged ("order the most expensive item"), but the result changes because the menu changed. The comparison that matters is with Pasta, not Burger: before combos the Critic always ordered Pasta ($18.00, 2 units). Salad + Pasta costs $24.30 and takes 3 units, so the Critic now orders the combo. If you register other combos, the Critic orders whichever is dearest: Burger + Pasta would be $29.70.

Knock-on effects, with the Critic getting the manager's full attention (like the brief's 3.3 example, but losing 12 per turn):

| Turn | Pasta (Stage 2) | Salad + Pasta (Stage 3) |
|---|---|---|
| 1 seated | 88 | 88 |
| 2 order, unit 1 | 76 | 76 |
| 3 unit 2 | 64, `READY` | 64 |
| 4 | served at 64 | unit 3, `READY`, 52 |
| 5 | ready to pay | served at 52 |
| 6 | pays $18.00 at 64 | ready to pay |
| 7 | | pays $24.30 at 52 |

- Revenue goes up by $6.30, which makes the $45 target easier.
- The Critic's satisfaction drops by 12, one extra waiting turn, which makes the average-60 target harder.
- The Chef is busy one extra turn, so the next customer's food is delayed too.
- The Critic pays one turn later, which matters near the end of the 18 turns.

**Regulars and VIPs**

This depends on what kind of rule they use:

- **A rule that names a fixed item or attribute** ("always Burger", "cheapest item", "shortest preparation") isn't affected: Salad is still the cheapest and quickest, and no combo beats it.
- **A rule that depends on menu position or size** (customer id modulo the number of items, cycling through the list, seeded random over the menu) changes for everyone. The menu grows from 3 to 4 or more items, so the same customer id or the same random seed lands on a different item. Every Regular's and VIP's order can shift, not just the Critic's.

The brief also says a customer chooses a combo "according to a rule you document". If only the Critic's "most expensive" rule ever picks a combo, that's your documented rule. Write it down explicitly, including what happens on a price tie.

**Does it break Stage 2?**

- **The `stage-2` tag isn't affected.** It has no combos, so its behaviour and its reproducible VIP and Critic runs stay exactly as they are.
- **Your Stage 2 test runs won't reproduce on Stage 3 code:** the Critic's order, timing and satisfaction differ, and possibly everyone's orders. This isn't a design fault, since each stage is supposed to have its own runs, but under section 4.4 you should say in the README and report that the Critic's outcome changes because the menu grew, not because the Critic's rule was rewritten.
- **The design itself isn't broken.** The combo flows through `chooseItem`, `billFor` and `Order` without any change to the Critic class. That's a good open/closed example to cite.

**Your two options**

1. **Keep it (my recommendation).** Accept that the Critic orders the combo, document it, and record fresh Stage 3 victory and defeat runs. It also makes for a meaningful manager decision: the Critic now ties up the Chef for 3 turns, which is worth showing in the video.
2. **Restrict the Critic to single dishes,** if you want the Critic to behave as in Stage 2. Then "most expensive single dish" is still Pasta. Don't implement this as an `instanceof ComboMeal` check in the Critic, since the brief wants behaviour dispatched through objects, not type checks. Instead, give `MenuItem` a query (e.g. whether it's a combo, or how many components it has), or have `Menu` keep single dishes and combos as separate lists.

Whichever you pick, rerun your Stage 3 base-setting victory run to check it's still winnable with the Critic at 52 or lower. Lean on Happy Hour's +15 recovery, or seat the Critic early.

---

## Prompt 3

Suggest two or three rules for when a customer chooses a combo instead of a single dish. Which is easiest to document and test, and does it need changes outside `chooseItem()`?

### Reply

I'd go with Rule 1 (a fixed rule based on customer id). It's the easiest to document and test, and it needs no changes outside the choice logic apart from letting the customer find the combo on the menu.

Whatever rule you pick, document which combo is chosen if there's more than one, and how the rule interacts with the Critic. The simplest setup is to register one combo (Salad + Pasta) in the base setting.

**Rule 1: Fixed by customer id**

"Customers with an even id order the combo; odd ids order their usual single dish."

- **Documenting it:** one sentence, and anyone can predict every order from the arrival list.
- **Testing it:** fully deterministic. The same arrivals always produce the same orders, whatever the manager does, so your victory and defeat runs reproduce exactly.
- **Weakness:** it's arbitrary, and the manager can't influence it.

**Rule 2: Based on satisfaction when the order is taken**

"A customer with satisfaction of 80 or more when the Waiter takes the order chooses the combo; below 80, they want food quickly and order a single dish."

- **Documenting it:** still short, but you must say exactly when satisfaction is read: during the Waiter's task in step 2, after any Happy Hour recovery at turn start and before the end-of-turn penalty.
- **Testing it:** deterministic for a given sequence of manager inputs, but a different seating order changes the menu choices. You need a scripted input sequence, and test cases either side of the threshold (79, 80, 81).
- **Strength:** it ties player choices to the result. Taking an order quickly gets a combo, which brings more revenue but means a longer Chef queue. That's strong material for "how the player's choices change a result" in the report and video.

**Rule 3: During Happy Hour**

"Customers choose the combo when Happy Hour is active."

- **Documenting it:** easy to state, but the outcome depends on when the manager activates Happy Hour.
- **Testing it:** the hardest of the three, because it couples two Stage 3 features, so you'd need runs with Happy Hour on, off and at different times.
- **Design cost:** `chooseItem(menu)` currently only receives the menu, and the customer can't see Happy Hour. You'd have to pass in extra information, which changes `Restaurant.placeOrder` and every `Customer` subclass that overrides `chooseItem`.

**Changes outside `chooseItem()`**

- **All rules:** the customer needs a way to find the combo on the menu without an `instanceof` check. Either `Menu` keeps single dishes and combos as separate lists, or `MenuItem` offers a query such as whether it's a combo. This is the same small registration change as before.
- **Rules 1 and 2:** nothing else. The id and satisfaction are the customer's own state.
- **Rule 3:** a signature change, as above.

**Structure inside the `Customer` classes**

If VIP and Critic override `chooseItem`, a combo rule written only in the base `Customer` gets bypassed by those overrides. The clean option is a template method:

1. The base `Customer.chooseItem` decides combo versus single dish, using Rule 1 or 2.
2. It then calls a step that each subclass fills in to pick which single dish.

Every type then follows the combo rule, without type checks in the controller. The Critic is the exception: "most expensive item" already picks the combo by price. Decide whether the Critic follows the shared rule or keeps its own, and say which in the report.

---