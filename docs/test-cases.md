# Manual Test Cases
Fill in **Actual result** and **Pass/Fail** only after executing each test.

| ID | Scenario | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|
| TC-01 | Valid login | Dashboard opens | | |
| TC-02 | Invalid password | "Invalid username or password." | | |
| TC-03 | Empty passenger name | Validation message | | |
| TC-04 | Unknown train number (e.g. 99999) | Booking rejected | | |
| TC-05 | Valid booking | Record saved, PNR displayed | | |
| TC-06 | Duplicate PNR (insert same PNR in MySQL) | DB rejects (`uk_pnr`) | | |
| TC-07 | Search existing PNR | Correct booking shown | | |
| TC-08 | Search unknown PNR | Not-found message | | |
| TC-09 | Confirm cancellation | Row deleted, archived in `cancelled_reservations` | | |
| TC-10 | Decline cancellation | Booking unchanged | | |
| TC-11 | Search/cancel same PNR again | "already been cancelled", no false success | | |
| TC-12 | Stop MySQL, launch app | Clear connection error dialog | | |
| TC-13 | Normal user opens another user's PNR | Not found | | |
| TC-14 | Past journey date | Booking rejected | | |
| TC-15 | Inactive train 19019 | "not available for booking" | | |
| TC-16 | Alphabetic train number | Validation message | | |
| TC-17 | Same source and destination | Validation message | | |
| TC-18 | Duplicate username on register | "already taken" | | |
| TC-19 | Admin adds / deactivates train | Table updates; train disappears from booking picker | | |
