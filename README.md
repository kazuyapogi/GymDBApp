# Gym Class and Attendance Management System

CCINFOM S40F Group 5: Avelino, Manzano, Niki, Ojeda

A desktop DB application for a martial arts gym (boxing, Muay Thai, BJJ, taekwondo, and more), where members train only by attending scheduled classes.
It is built with **Java Swing** (GUI), **JDBC** (database access) and **MySQL 8**.

## What you need

| Software | Notes |
|---|---|
| JDK 11 or newer | `javac -version` must work in Command Prompt |
| MySQL Server 8 + MySQL Workbench | Remember the root password you set |
| MySQL Connector/J | One `.jar` file; see step 2 |

## Setup (Windows)

1. **Create the database.** Open MySQL Workbench, connect to your local server, open `sql/gym_db.sql`, and run the whole script (lightning bolt icon).
   It creates `gym_db` with the 5 tables and sample data (12 trainers, 25 members, 13 classes, 130 sessions, 420 attendance records).
   Running it again resets the database to the original sample data.
2. **Add the JDBC driver.** Download Connector/J from https://dev.mysql.com/downloads/connector/j/ (choose *Platform Independent*), unzip it, and copy `mysql-connector-j-9.x.x.jar` into the `lib` folder.
3. **Set your password.** Open `config/db.properties` and put your MySQL root password after `db.password=`.
4. **Build.** Double-click `build.bat`. It compiles everything into the `out` folder.
5. **Run.** Double-click `run.bat`.

On Mac or Linux, use `./build.sh` and `./run.sh` instead.

If the app cannot connect, it shows a message explaining what to check (MySQL running, script run, password, driver jar).

## Project layout (three tiers)

```
src/gym/
  Main.java            starts the app and tests the connection
  model/               plain data classes, one per table (Member, Trainer, GymClass, ClassSession, Attendance)
  data/                data tier: SQL with PreparedStatements (one DAO per table + ReportDAO)
  logic/               logic tier: business rules and validation (one Service per record + ReportService)
  ui/                  presentation tier: Swing screens (one Panel per record + ReportsPanel)
sql/gym_db.sql         creates the database and sample data
config/db.properties   connection settings
```

| Member | Record (screen / service / DAO) | Report (ReportDAO method) |
|---|---|---|
| Kazuya Niki | MemberPanel, MemberService, MemberDAO | memberActivity |
| Jan Carlo Avelino | TrainerPanel, TrainerService, TrainerDAO | trainerWorkload |
| Charlotte Mae Ojeda | ClassPanel, ClassService, ClassDAO | classAttendanceSummary |
| Matthew Mari Manzano | AttendancePanel, AttendanceService, AttendanceDAO | membershipEngagement |

Shared: Main, MainFrame, UI, RecordPanel, ReportsPanel, ReportService, DBConnection, Sql, SessionPanel/Service/DAO, sql/gym_db.sql.

## Rules enforced (from the approved proposal)

- A member's status becomes **Expired** once the membership end date has passed.
- Members with attendance, trainers with classes, classes with sessions, and sessions with attendance **cannot be deleted** (set them to Expired / Inactive / Cancelled instead).
- A class can only be given an **Active** trainer.
- A session must fall on the class's day of the week, only one per class per date, and only for Active classes.
- Attendance can only be recorded for a **Held** session of an **Active** class, on a date within the member's membership period.
- Reports count only **Held** sessions.

See `DEMO_GUIDE.md` for a step-by-step demo script.
