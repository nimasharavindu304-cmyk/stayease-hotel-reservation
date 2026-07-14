# StayEase - Hotel Reservation & Booking Management System

Java Swing enterprise application built for **EAD1 Coursework 1** (Diploma in Software Engineering,
Batch DSE 25.2) in the **Hospitality and Tourism** specified area.

The scenario: a small hotel's front desk uses StayEase to register guests, manage rooms, take
bookings (the major transaction), and print booking invoices / revenue reports.

## 1. Requirement-to-feature map

| Coursework requirement | Where it lives |
|---|---|
| Transaction UI (major functionality) | `ui.BookingPanel` + `service.BookingService` — create a booking, check availability, calculate total, record payment |
| Input UIs | `ui.RoomManagementPanel`, `ui.GuestManagementPanel` (CRUD forms) |
| Dashboard | `ui.DashboardPanel` — occupancy & revenue stats, live-refreshed via Observer |
| Design patterns | Singleton (`db.DBConnection`), DAO (`dao.*`), Observer (`observer.*`), Factory (`report.ReportFactory`) |
| Jasper report, 2+ tables | `reports/BookingInvoiceReport.jrxml` (bookings+guests+rooms+payments+users) and `reports/RevenueSummaryReport.jrxml` |
| Input validation & exception handling | `util/ValidationUtil`, `exception/InvalidBookingException`, `exception/RoomNotAvailableException`, `exception/DuplicateGuestException` (all user-defined, checked) |
| MySQL CRUD | `dao/*DAOImpl.java` via JDBC |
| Git version control | `git init` already run in this folder — see §5 |
| Optional API integration | `util/EmailUtil` — SMTP booking-confirmation email (off by default, toggle in `config.properties`) |

## 2. Prerequisites

- JDK 11+
- Apache Maven 3.6+
- MySQL 8.x running locally
- NetBeans (recommended editor — File → Open Project → select the `StayEase` folder; NetBeans
  reads `pom.xml` natively as a Maven project)

## 3. Setup

1. **Create the database:**
   ```
   mysql -u root -p < sql/schema.sql
   ```
   This creates `stayease_db` with sample rooms/guests/one booking, and two staff logins.

2. **Configure the connection** in `src/main/resources/config.properties` — update
   `db.user` / `db.password` to match your MySQL install.

3. **Build:**
   ```
   mvn clean package
   ```
   This downloads `mysql-connector-j` and `jasperreports` from Maven Central and produces
   `target/StayEase.jar` (a runnable "fat jar" with all dependencies bundled, via the
   `maven-shade-plugin`).

4. **Run:**
   ```
   java -jar target/StayEase.jar
   ```
   Default login: **admin / admin123**

## 4. Producing the .exe deliverable

`jpackage` (bundled with JDK 14+) and `javac`/Maven only produce native installers for the OS
they run on, so the `.exe` must be built **on Windows**, using the `StayEase.jar` produced above:

**Option A — jpackage (JDK 17+, simplest):**
```
jpackage --input target --name StayEase --main-jar StayEase.jar ^
  --main-class com.stayease.Main --type exe --win-console
```

**Option B — Launch4j:** wrap `target/StayEase.jar` with [Launch4j](https://launch4j.sourceforge.net/),
pointing the "Jar" field at `StayEase.jar` and Main class at `com.stayease.Main`.

Deliverables to submit: the `.exe`, `StayEase.jar` (the required "JAR file"), and your GitHub
repository link.

## 5. Git

This project folder is already an initialised Git repository with an initial commit (see
`git log`). To publish it:
```
git remote add origin <your-empty-github-repo-url>
git branch -M main
git push -u origin main
```

## 6. Design pattern notes (for the VIVA)

- **Singleton — `db.DBConnection`.** One JDBC `Connection` is shared app-wide instead of every
  DAO opening its own; lazily created with double-checked locking. Ask yourself: what would break
  if each DAO opened its own connection? (Connection-pool exhaustion, no single place to manage
  transactions.)
- **DAO — `dao.*DAO` / `dao.*DAOImpl`.** Every table has an interface (contract) and a JDBC
  implementation. UI and service code depend on the interface, never on raw SQL — swapping MySQL
  for another RDBMS would only mean rewriting the `*Impl` classes.
- **Observer — `observer.BookingSubject` / `observer.DashboardObserver`.** `BookingPanel` and
  `BookingService` never talk to `DashboardPanel` directly. `BookingService.notifyObservers()` is
  called after every booking/check-in/check-out/cancel, and `DashboardPanel` (the only registered
  observer here) reacts by re-querying and repainting its stat cards. This decouples "something
  changed" from "who needs to know."
- **Factory — `report.ReportFactory`.** `ReportPanel` asks for `ReportFactory.getGenerator(type)`
  without knowing whether it gets a `BookingInvoiceReportGenerator` or a
  `RevenueSummaryReportGenerator`, or which `.jrxml` backs it. Adding a third report later means
  one new class + one new `case`, not touching the UI.

Be ready to explain **why** each pattern was chosen over the naive alternative (e.g. "why not just
`new Connection()` everywhere?") — that's usually the actual VIVA question, not just "name the
pattern."

## 7. Business logic worth knowing cold

- `BookingService.createBooking()`: validates dates/capacity → checks the room isn't under
  maintenance → checks for date-overlap with `BookingDAO.hasOverlap()` (throws
  `RoomNotAvailableException` if taken) → computes `nights * rate` → inserts the `Booking` and its
  `Payment` **in one JDBC transaction** (`conn.setAutoCommit(false)` / `commit()` / `rollback()`)
  so you never get a booking with no payment row or vice versa.
- `RoomDAOImpl.findAvailableForDates()`: a room is available if it isn't `MAINTENANCE` and has no
  `CONFIRMED`/`CHECKED_IN` booking whose range overlaps the requested `[checkIn, checkOut)` —
  the classic `existing.start < newEnd AND existing.end > newStart` overlap test.

## 8. Project layout

```
StayEase/
├── pom.xml
├── sql/schema.sql
├── src/main/resources/
│   ├── config.properties
│   └── reports/*.jrxml
└── src/main/java/com/stayease/
    ├── Main.java
    ├── model/       (Guest, Room, Booking, Payment, User, enums)
    ├── exception/   (custom checked exceptions)
    ├── db/          (DBConnection singleton)
    ├── dao/         (interfaces + JDBC implementations)
    ├── service/     (BookingService, GuestService, AuthService)
    ├── observer/    (Observer pattern)
    ├── report/      (Factory pattern + Jasper generators)
    ├── util/        (PasswordUtil, ValidationUtil, EmailUtil)
    └── ui/          (Swing screens: Login, MainFrame, Dashboard, Booking, Room, Guest, Report)
```
