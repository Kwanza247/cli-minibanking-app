# CLI Mini Banking App (Pure Java)

A console banking app written in plain Java: no framework, no Maven/Gradle, no `pom.xml`.
Full requirements are in [docs/PRD.md](docs/PRD.md).

## Features
- Register a customer (full name, email optional, phone, NIN, BVN, address, PIN)
- Savings, Current and Loan accounts, with a generated account number
- Deposit, withdraw, transfer, check balance, transaction history, find transaction by ID
- Switch between your accounts
- Input validation (PIN, email, phone number, NIN, BVN)
- Daily withdrawal/transfer limits
- Loans: up to NGN 5,000,000, up to 12 months, 3% per month

## Requirements
Java JDK 11 or newer. Check with:
```
java -version
javac -version
```

## Project structure
```
cli-minibanking-app/
├── docs/PRD.md
├── src/main/java/com/minibank/
│   ├── Main.java
│   ├── config/       settings: limits, loan rules
│   ├── exception/    custom error type
│   ├── model/        Customer, Account types, Transaction
│   ├── repository/   in-memory storage
│   ├── service/      business rules
│   ├── ui/           menus and screens
│   └── util/         validation, IDs, input helpers
└── README.md
```

## Build and run

**Windows (PowerShell)**
```powershell
Remove-Item -Recurse -Force out
mkdir out
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName } | Out-File -Encoding ascii sources.txt
javac -d out "@sources.txt"
java -cp out com.minibank.Main
```

**macOS / Linux**
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.minibank.Main
```

## Default limits and rules
| Item | Value |
|---|---|
| Savings daily limit | NGN 500,000 |
| Current daily limit | NGN 1,000,000 |
| Maximum loan | NGN 5,000,000 |
| Maximum loan tenure | 12 months |
| Loan interest | 3% per month (simple interest) |

These can be changed in `src/main/java/com/minibank/config/BankConfig.java`.

## Notes
- Data is stored in memory only and resets when the app closes.
- PINs are stored as SHA-256 hashes, never as plain text.