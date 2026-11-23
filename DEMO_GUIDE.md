# Demo Guide

**Before the defense:** run `sql/gym_db.sql` in MySQL Workbench so the record IDs below match, then start the app with `run.bat`.
The sample data covers September 1 to November 13, 2026. Use **October 2026** for every report.

For each function, do it in the app, then run the Workbench query under it to show what is stored.
Every blocked action shows a **"Not allowed"** dialog naming the rule; nothing is saved, and the form reopens with what you typed.

---

## Setup (5 to 10 minutes, one shared screen)

1. Download the submission from AnimoSpace and unzip it.
2. MySQL Workbench: open `sql/gym_db.sql`, run the whole script.
3. Put the MySQL password in `config/db.properties` (`db.password=`). Make sure the Connector/J jar is in `lib`.
4. Double-click `build.bat`, then `run.bat`.

```sql
USE gym_db;
SELECT (SELECT COUNT(*) FROM member) AS members, (SELECT COUNT(*) FROM trainer) AS trainers,
       (SELECT COUNT(*) FROM gym_class) AS classes, (SELECT COUNT(*) FROM class_session) AS sessions,
       (SELECT COUNT(*) FROM class_attendance) AS attendance;   -- 25, 12, 13, 130, 420
```

---

## 1. Kazuya Niki: Member records and Member Activity Report

| Step | In the app (Members tab) | Workbench |
|---|---|---|
| List/Filter | Membership type = Annual, Status = Active, Apply filter. Then Show all. | `SELECT * FROM member WHERE membership_type='Annual' AND status='Active';` |
| Add | Add a member, type Monthly. The end date fills in (start + 1 month − 1 day). Change type to Annual: the end date updates. Save. | `SELECT * FROM member ORDER BY member_id DESC LIMIT 1;` |
| Blocked | Add again with the end date before the start date → "Membership end date must be later than the start date." | (nothing new saved) |
| Update | Select the new member, Update, change the contact number. | same query as Add |
| View | Select member 1 and View (or double-click). Shows all-time attendance count. | `SELECT * FROM member WHERE member_id = 1;` |
| Delete blocked | Delete member 1 (Dela Cruz) → blocked, she has attendance records. | `SELECT COUNT(*) FROM class_attendance WHERE member_id = 1;` |
| Delete | Delete the member you added. | `SELECT * FROM member ORDER BY member_id DESC LIMIT 1;` |
| Report | Reports → Member Activity, October 2026, Generate. **Dela Cruz, Ana: 15.** All 25 members listed. | see below |

```sql
SELECT COUNT(*) FROM class_attendance a JOIN class_session s ON s.session_id = a.session_id
WHERE a.member_id = 1 AND s.status = 'Held' AND s.session_date BETWEEN '2026-10-01' AND '2026-10-31';  -- 15
```

## 2. Jan Carlo Avelino: Trainer records and Trainer Workload Report

| Step | In the app (Trainers tab) | Workbench |
|---|---|---|
| List/Filter | Specialization = Muay Thai (Lim, Marco). Then Status = Inactive (Santos, Torres). | `SELECT * FROM trainer WHERE status='Inactive';` |
| Add / Update | Add a trainer (e.g. Judo), then Update their contact number. | `SELECT * FROM trainer ORDER BY trainer_id DESC LIMIT 1;` |
| View | View trainer 3: shows the classes assigned to him (MTH101, MTH201). | `SELECT class_code, class_name FROM gym_class WHERE trainer_id = 3;` |
| Delete blocked | Delete trainer 1 (Ramos) → blocked, assigned to BOX101. | `SELECT * FROM gym_class WHERE trainer_id = 1;` |
| Delete | Delete the trainer you added. | |
| Report | Trainer Workload, October 2026. **Lim, Marco: 2 classes, 9 sessions.** Switch to September 2026: inactive Santos, Rey appears because KAR101 ran until Sep 20. | see below |

```sql
SELECT COUNT(*) FROM class_session s JOIN gym_class c ON c.class_code = s.class_code
WHERE c.trainer_id = 3 AND s.status = 'Held' AND s.session_date BETWEEN '2026-10-01' AND '2026-10-31';  -- 9
```

## 3. Charlotte Mae Ojeda: Class records and Monthly Class Attendance Summary

| Step | In the app (Classes tab) | Workbench |
|---|---|---|
| List/Filter | Day = Wednesday (TKD101, KAR101, MMA101). Then Trainer = Lim, Marco. | `SELECT * FROM gym_class WHERE day_of_week='Wednesday';` |
| Blocked | Add a class with trainer "Santos, Rey (Inactive)" → "Santos, Rey is Inactive and cannot be assigned to a class." | |
| Blocked | Same form, an Active trainer but max capacity 0 → "Maximum capacity must be greater than zero." | |
| Add / Update | Add BOX201 (Friday, an Active trainer, capacity 10). Update its end time. | `SELECT * FROM gym_class WHERE class_code='BOX201';` |
| View | View BJJ101: schedule, trainer and number of session records. | |
| Delete blocked | Delete BJJ101 → cancelled, it has session records. | `SELECT COUNT(*) FROM class_session WHERE class_code='BJJ101';` |
| Delete | Delete BOX201. | |
| Report | Monthly Class Attendance Summary, October 2026. **MTH201: 5 sessions, 23 attendance, 57.50%** = 23 / (5 × 8) × 100. KAR101 (Inactive, no October sessions) is not listed. | see below |

```sql
SELECT COUNT(DISTINCT s.session_id) AS sessions_held, COUNT(a.attendance_id) AS attendance
FROM class_session s LEFT JOIN class_attendance a ON a.session_id = s.session_id
WHERE s.class_code = 'MTH201' AND s.status = 'Held' AND s.session_date BETWEEN '2026-10-01' AND '2026-10-31';  -- 5, 23
```

## 4. Class Session records (presenter to be decided)

| Step | In the app (Class Sessions tab) | Workbench |
|---|---|---|
| List/Filter | Status = Cancelled (7). Then Class = BJJ201. Then tick Date range for October. | `SELECT * FROM class_session WHERE status='Cancelled';` |
| Blocked | Add BOX101 on a Tuesday → "BOX101 meets on Mondays, but ... is a Tuesday." (Picking a class jumps the date to its next class day; change it.) | |
| Blocked | Add BOX101 on 2026-11-09 → already has a session that day. | |
| Blocked | Add KAR101 (Inactive) → "KAR101 is Inactive, so no new sessions can be added." | |
| Add | Add BOX101 on its next Monday, status Held. **Keep it for Matthew.** | `SELECT * FROM class_session ORDER BY session_id DESC LIMIT 1;` |
| Update / View | Update session 117's status, or View session 130 (6 attendees). | |
| Delete blocked | Delete session 130 → it has attendance records. | `SELECT COUNT(*) FROM class_attendance WHERE session_id = 130;` |

## 5. Matthew Mari Manzano: Attendance records and Membership Engagement Report

| Step | In the app (Attendance tab) | Workbench |
|---|---|---|
| List/Filter | Date range Oct 1 to Oct 31, Class = MTH201. | |
| Add | Record member 1 in the BOX101 session added in step 4. | `SELECT * FROM class_attendance ORDER BY attendance_id DESC LIMIT 1;` |
| Update / View | Change the time in, then View. | same query |
| Blocked | Member 1 in session #117 (BJJ201, Nov 5, Cancelled). | `SELECT status FROM class_session WHERE session_id = 117;` |
| Blocked | Member 4 (Garcia, ended Sep 30) in session #130 → outside membership period. | `SELECT membership_start, membership_end FROM member WHERE member_id = 4;` |
| Blocked | Member 2 in session #30 (KAR101) → class is Inactive. | `SELECT status FROM gym_class WHERE class_code = 'KAR101';` |
| Delete | Delete the attendance you added. | |
| Report | Membership Engagement, October 2026, type Monthly. Aguilar (from Oct 16) and Villanueva (until Oct 14) are included because their memberships overlap October. | see below |

```sql
SELECT member_id, last_name, membership_start, membership_end FROM member
WHERE membership_type = 'Monthly' AND membership_start <= '2026-10-31' AND membership_end >= '2026-10-01';
```

---

To start over at any time, run `sql/gym_db.sql` again.
