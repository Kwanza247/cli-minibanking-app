# Product Requirements Document (PRD)
## CLI Mini Banking App

| | |
|---|---|
| **Product** | CLI Mini Banking App |
| **Version** | 1.0 |
| **Tech** | Pure Java (11+), no framework, no Maven/Gradle (no `pom.xml`), compiled with plain `javac` |
| **Interface** | Command-line (text menus) |
| **Storage** | In-memory (data resets when the app closes) |

---

## 1. Overview
A console banking application that lets a customer register, open savings, current and loan accounts, and perform everyday banking operations (deposit, withdraw, transfer, balance check, account switching) with PIN protection, input validation and daily limits.

## 2. Goals and non-goals
**Goals**
- Register customers with validated details.
- Support three account types: Savings, Current, Loan.
- Generate a unique account number for every account.
- Let a logged-in customer operate on, and switch between, their accounts.
- Give every money movement a unique transaction ID.
- Enforce daily withdrawal/transfer limits and loan rules.
- Use only the Java standard library.

**Non-goals (v1)**
- No database or file persistence, no GUI, web or API.
- No real NIN/BVN verification against NIMC/NIBSS.
- No interest on savings, no overdraft, no admin or staff role.

## 3. User
**Customer**: a person registering and using their own accounts through the terminal.

## 4. Functional requirements

### FR-1 Customer registration
| Field | Required | Rule |
|---|---|---|
| Full name | Yes | At least first and last name, letters only |
| Email | **No** | If supplied, must be a valid email format |
| Phone number | Yes | 11 digits, starts with 07 / 08 / 09, unique |
| NIN | Yes | Exactly 11 digits, numbers only, unique |
| BVN | Yes | Exactly 11 digits, numbers only, unique |
| Address | Yes | Minimum 5 characters |
| PIN | Yes | Exactly 4 digits, entered twice to confirm |

On success the customer picks a first account (Savings or Current) and the system **displays the generated account number**.

### FR-2 Account types and generation
- **Savings** and **Current**: a customer may hold several; each gets a unique 10-digit account number.
- **Loan**: created only through a loan application (see FR-8).
- New account details (number, type, balance) are displayed immediately.

### FR-3 Login and greeting
- Login with phone number and PIN (maximum 3 attempts, then back to the main menu).
- After login, and after every action, the screen shows:
  `Hello <Full Name>!`, `Account Number : <number> (<type>)`, `Balance : NGN <amount>` (for a loan account: `Outstanding Loan`).

### FR-4 Deposit
Credits the active account. On a **loan account**, a deposit is a cash repayment and cannot exceed the outstanding amount.

### FR-5 Withdraw
- Requires PIN confirmation.
- Blocked if the balance is insufficient or the daily limit would be exceeded.
- Not allowed on a loan account.

### FR-6 Transfer
- The destination is a 10-digit account number. The app shows the account holder's name and asks for confirmation.
- Requires PIN confirmation, with the same balance and daily-limit checks as withdrawal.
- Cannot transfer to the same account or into a loan account (use *Repay loan* instead).
- Both sides of a transfer record the **same transaction ID**.

### FR-7 Balance, history and transaction ID
- **Check balance / account details**: balance, daily limit, used today, remaining today (for a loan: principal, interest, total, tenure, installment, outstanding).
- **Transaction history**: newest first, each entry with ID, type, amount, balance after and narration.
- **Find transaction by ID**.
- Every deposit, withdrawal, transfer, loan disbursement and repayment returns a receipt with a transaction ID (format `TXN<yyyyMMddHHmmss>-<sequence>`).

### FR-8 Loan account
- Maximum loan: **NGN 5,000,000**. Maximum tenure: **12 months**.
- Interest: **3% per month**, simple interest (`principal x 3% x months`), added to the amount owed.
- The customer must already have a savings or current account to receive the money.
- One active loan at a time. A new loan is allowed once the previous one is fully repaid.
- A loan summary (principal, interest, total, monthly installment) is shown before the customer accepts, and a PIN is required.
- Repayment: *Repay loan* (from a savings or current account) or a cash deposit into the loan account.

### FR-9 Switch accounts
The customer can list all their accounts (number, type, balance) and choose which one is active. Opening a new account switches to it automatically.

### FR-10 Daily limits
The limit applies to **withdrawals plus transfers out**, per account, per calendar day.

| Account | Daily limit |
|---|---|
| Savings | NGN 500,000 |
| Current | NGN 1,000,000 |
| Loan | No outflows allowed |

Loan repayments do not count toward the limit.

## 5. Validation summary
| Input | Rule |
|---|---|
| Phone number | 11 digits, starts with 07, 08 or 09 |
| NIN | Exactly 11 digits, numbers only |
| BVN | Exactly 11 digits, numbers only |
| PIN | Exactly 4 digits |
| Email | Optional; valid format only if provided |
| Amount | Greater than 0, maximum 2 decimal places |
| Account number | Exactly 10 digits |

## 6. Non-functional requirements
- **Language and tooling**: Java 11+, standard library only, no `pom.xml` or build tool.
- **Money**: `BigDecimal` everywhere (no floating-point errors).
- **Security**: PINs stored as SHA-256 hashes, never in plain text.
- **Usability**: clear error messages; invalid input re-prompts instead of crashing.
- **Maintainability**: layered packages (model / service / repository / ui / util / config). Limits and rates live in one config class.

## 7. Architecture
```
cli-minibanking-app/
├── src/main/java/com/minibank/
│   ├── Main.java
│   ├── config/BankConfig.java
│   ├── exception/BankException.java
│   ├── model/        Account, SavingsAccount, CurrentAccount, LoanAccount,
│   │                 Customer, Transaction, AccountType, TransactionType
│   ├── repository/   BankRepository
│   ├── service/      CustomerService, AccountService, LoanService
│   ├── ui/           ConsoleApp, DashboardMenu
│   └── util/         Validator, IdGenerator, PinHasher, Money, ConsoleInput
├── docs/             PRD.md
├── README.md
└── .gitignore
```

## 8. Acceptance criteria (samples)
1. Registering with a 10-digit NIN is rejected; an 11-digit numeric NIN is accepted.
2. Leaving the email blank still registers the customer.
3. After registration, the generated 10-digit account number is displayed.
4. Savings: withdrawing NGN 300,000 and then NGN 300,000 again on the same day fails on the second attempt (limit NGN 500,000).
5. A loan of NGN 100,000 for 6 months shows interest of NGN 18,000 and a total of NGN 118,000.
6. A loan above NGN 5,000,000, or a tenure above 12 months, is rejected.
7. A transfer's transaction ID can be found from the receiver's side.
8. After login, the header shows the greeting, account number and balance.

## 9. Assumptions and future scope
- The phone number is the login identifier. There is one customer per phone, NIN and BVN.
- Loan interest is simple (not compounding) and is not recalculated on early repayment.
- Future: file or database persistence, statement export, savings interest, an admin role, loan installment scheduling, unit tests.