# CSE202 Banking System

Object Oriented Analysis and Design with Java: JavaFX screens (boundary classes), controllers,
a domain model with a Bank facade, and MySQL storage through DAO classes.

    src/main/java/bank/view        JavaFX screens (no business logic)
    src/main/java/bank/controller  controllers: check the role, call the model, show the result
    src/main/java/bank/model       Bank, Customer, Account, Savings/Investment/Cheque, ...
    src/main/java/bank/dao         DAO interfaces, JDBC (MySQL) classes, PersistentBank
    src/main/java/bank/exception   exceptions
    src/main/java/bank/security    PasswordHasher
    src/test/java/bank/model       JUnit 5 tests
    database/schema.sql            the MySQL tables
    docs/                          Part A: requirements and UML diagrams (PDF)

## Run in GitHub Codespaces

1. On GitHub: **Code > Codespaces > Create codespace on main**. The first start takes a few
   minutes: it installs Java 21, Maven, a desktop and a MySQL 8.4 server (password `bank123`).
2. In the terminal: `mvn javafx:run`
3. Open the **Ports** tab, click the globe next to port **6080** and log in with password `bank123`.
   The Banking System window appears in that browser desktop.
4. Tests: `mvn test`

The login screen says "Connected to the MySQL database." when MySQL is working.

## Demo logins

| Username | Password | Role |
|---|---|---|
| teller1 | teller123 | Teller: customers, accounts, transactions view, monthly interest |
| customer1 | customer123 | Kabo Molefe: own accounts, deposit, withdraw, history |
| customer2 | customer123 | Kalahari Traders (company) |

## Run on your own computer

Needs JDK 17+, Maven and MySQL. Put your MySQL password in `db.properties`, then `mvn javafx:run`.
If MySQL is not reachable the program still starts with sample data in memory.

## Run in jGRASP (no Maven)

Do not compile `MainApp.java` on its own: jGRASP compiles one file at a time and the classes depend on
each other. Open **`RunBankingSystem.java`** (in the project folder, next to `pom.xml`), press **Compile**,
then **Run**. It compiles everything together and starts the GUI.

- JavaFX SDK: it looks for the `javafx-sdk-...` folder in Documents or Downloads, or edit `DEFAULT_JAVAFX`
  near the top of `RunBankingSystem.java`.
- MySQL: copy `mysql-connector-j-X.Y.Z.jar` into the `lib` folder and put your password in `db.properties`.
  Without them the program still starts with sample data in memory.
