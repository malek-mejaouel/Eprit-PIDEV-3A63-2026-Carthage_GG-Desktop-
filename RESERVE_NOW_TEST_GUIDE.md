# 🧪 "Reserve Now" Button - Step-by-Step Test Guide

## 🎯 Objective
Verify that clicking "Reserve Now" button:
1. ✅ Loads the reservation form dialog
2. ✅ Allows user input (name + seats)
3. ✅ Calculates price correctly
4. ✅ Saves data to MySQL database

---

## 📋 Pre-Test Requirements

### Software
- [ ] Java JDK 17+ installed
- [ ] MySQL server running
- [ ] Maven (via mvnw.cmd)
- [ ] Project compiled successfully

### Database
- [ ] Database `carthage_gg` exists
- [ ] Table `reservations` created
- [ ] At least one Event record in `event` table
- [ ] At least one Location record in `locations` table

### Verification
```bash
# Check database
mysql -u root -p carthage_gg

# Check reservations table
SHOW CREATE TABLE reservations;

# Check if events exist
SELECT * FROM event LIMIT 1;

# Check if locations exist
SELECT * FROM locations LIMIT 1;
```

---

## 🚀 Test Procedure

### STEP 1: Compile & Launch Application

```powershell
# Set Java home
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Navigate to project
cd "C:\Users\BOUZID\Desktop\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment"

# Compile
.\mvnw.cmd clean compile
# Expected: [INFO] BUILD SUCCESS ✅

# Run
.\mvnw.cmd javafx:run
# Expected: Application window opens ✅
```

**Verification**:
- [ ] No compilation errors
- [ ] Application window appears
- [ ] CarthageGG title visible

---

### STEP 2: Login to Application

```
Screen: Login Page

Actions:
1. Click "Email" field
2. Enter: admin@carthagegg.tn
3. Click "Password" field
4. Enter: admin123
5. Click "Login" button

Expected Result:
- [ ] ✅ Login succeeds
- [ ] ✅ Redirect to Dashboard/Home
- [ ] ✅ Sidebar menu visible
```

---

### STEP 3: Navigate to Events

```
Screen: Home/Dashboard

Actions:
1. Locate "Events" in sidebar menu
2. Click on "Events"

Expected Result:
- [ ] ✅ Events page loads
- [ ] ✅ Event list displayed
- [ ] ✅ Cards/tiles show event information
- [ ] ✅ Each event has a button (View Details OR event clickable)
```

---

### STEP 4: Open Event Details

```
Screen: Events List

Actions:
1. Find an event WITH location data
2. Click on event card OR "View Details" button

Expected Result:
- [ ] ✅ EventDetails page loads
- [ ] ✅ Event title displayed
- [ ] ✅ Event date/time shown
- [ ] ✅ Available seats shown
- [ ] ✅ Location information visible
- [ ] ✅ Map displayed (OpenStreetMap with marker)
- [ ] ✅ "Reserve Now" button visible
```

---

### STEP 5: Click "Reserve Now" Button ⭐ KEY TEST

```
Screen: Event Details

Actions:
1. Locate the "RESERVE NOW" button
   - Should be at bottom of details section
   - Should have ticket icon
   - Style: Blue/Primary color

2. Click the button

Expected Result:
- [ ] ✅ Dialog opens immediately (no delay)
- [ ] ✅ Dialog titled "Make a Reservation"
- [ ] ✅ Dialog has styled header with ticket icon
- [ ] ✅ Header text: "Make a Reservation"
- [ ] ✅ Subtext: "Reserve your spot for this event"

FAILURE SCENARIOS:
- ❌ Dialog doesn't open → Check logs for IOException
- ❌ Blank dialog → Check FXML is in resources
- ❌ Error message → FXML parsing error
```

---

### STEP 6: Verify Form Fields

```
Screen: Reservation Form Dialog

Expected Fields Present:
- [ ] ✅ "FULL NAME" label
- [ ] ✅ Full Name TextField (empty, with placeholder)
- [ ] ✅ "NUMBER OF SEATS" label
- [ ] ✅ Seats Spinner (default: 1, range 1-10)
- [ ] ✅ "PRICE PER SEAT" label
- [ ] ✅ Price TextField (read-only, shows 25.00)
- [ ] ✅ "TOTAL PRICE" label
- [ ] ✅ Total Price TextField (read-only, shows 25.00 initially)
- [ ] ✅ "EVENT DETAILS" section
- [ ] ✅ Event title displayed
- [ ] ✅ Event date displayed
- [ ] ✅ Available seats shown

Style Check:
- [ ] ✅ Title text is cyan (#00f0ff) or gold (#FFC107)
- [ ] ✅ Labels are styled (gold/orange color)
- [ ] ✅ Scrollable if content exceeds height
- [ ] ✅ Dialog has "Reserve Now" and "Cancel" buttons
```

---

### STEP 7: Fill in Full Name

```
Screen: Reservation Form

Actions:
1. Click on "FULL NAME" TextField
2. Type: Ahmed Ben Ali
3. Verify text appears

Expected Result:
- [ ] ✅ Text field accepts input
- [ ] ✅ Text displays as typed
- [ ] ✅ Cursor visible in field
- [ ] ✅ No errors or warnings
```

---

### STEP 8: Change Number of Seats

```
Screen: Reservation Form

Initial State:
- [ ] Number of Seats: 1
- [ ] Total Price: 25.00

Actions:
1. Click the Spinner up arrow OR in the field
2. Set value to: 3
3. Observe auto-calculation

Expected Result - AUTO CALCULATION:
- [ ] ✅ Spinner value changes to 3
- [ ] ✅ Total Price INSTANTLY updates to 75.00
- [ ] ✅ Calculation: 3 × 25.00 = 75.00 TND ✅
- [ ] ✅ No delay in calculation

VALIDATION:
- [ ] ✅ Spinner prevents <1 (minimum 1)
- [ ] ✅ Spinner prevents >10 (maximum 10)
- [ ] ✅ Trying to type 11 is prevented OR clipped to 10
- [ ] ✅ Decimal values not accepted (integers only)
```

---

### STEP 9: Verify Calculated Prices

```
Screen: Reservation Form (after setting 3 seats)

Price Verification:
- [ ] ✅ Price Per Seat: 25.00 TND (read-only)
- [ ] ✅ Total Price: 75.00 TND (read-only)

Try Different Amounts:
Action: Change seats to 5
- [ ] ✅ Total updates to 125.00
- [ ] ✅ Calculation: 5 × 25.00 = 125.00 ✅

Action: Change seats to 1
- [ ] ✅ Total updates to 25.00
- [ ] ✅ Calculation: 1 × 25.00 = 25.00 ✅

Action: Change seats to 10 (maximum)
- [ ] ✅ Total updates to 250.00
- [ ] ✅ Calculation: 10 × 25.00 = 250.00 ✅
```

---

### STEP 10: Submit the Reservation

```
Screen: Reservation Form (Ready to submit)

Current State:
- Full Name: Ahmed Ben Ali
- Seats: 3
- Total Price: 75.00 TND

Actions:
1. Locate the "Reserve Now" button at bottom of dialog
2. Click "Reserve Now" button

WAIT: Form should validate and process...

Expected Result:
- [ ] ✅ Dialog doesn't close immediately
- [ ] ✅ Form validates name (not empty)
- [ ] ✅ Creation object built with:
       * name: "Ahmed Ben Ali"
       * seats: 3
       * price: 75.00
       * eventId: [event id]
       * status: WAITING
       * reservation_date: NOW

THEN EITHER:

SUCCESS PATH:
- [ ] ✅ Form processes
- [ ] ✅ Database INSERT executed
- [ ] ✅ Dialog closes
- [ ] ✅ Success Alert appears: "Your reservation has been successfully created!"
- [ ] ✅ User clicks OK to close alert
- [ ] ✅ Back to Event Details page

FAILURE PATH:
- ❌ Database error appears
- ❌ "'SQLException: ..." message shown
- ❌ User can try again or cancel
- ❌ Dialog remains open (user can retry)
```

---

### STEP 11: Verify Success Alert

```
Screen: Success Message (AFTER clicking Reserve Now)

Alert Content:
- [ ] ✅ Title: "Reservation Created"
- [ ] ✅ Message: "Your reservation has been successfully created!"
- [ ] ✅ Type: Information (OK button, not error)
- [ ] ✅ Color: Green/blue theme

Actions:
1. Click OK button on alert

Expected Result:
- [ ] ✅ Alert closes
- [ ] ✅ Back to Event Details page
- [ ] ✅ Dialog is gone
- [ ] ✅ Can see event information again
```

---

### STEP 12: Verify Data Saved in MySQL ⭐ CRITICAL

```
Action: Open MySQL terminal/client

Command 1: Check reservation created
mysql> USE carthage_gg;
mysql> SELECT * FROM reservations ORDER BY id DESC LIMIT 1;

Expected Result:
- [ ] ✅ Record found
- [ ] ✅ name = 'Ahmed Ben Ali'
- [ ] ✅ seats = 3
- [ ] ✅ price = 75.00
- [ ] ✅ event_id = [matched event]
- [ ] ✅ status = 'WAITING'
- [ ] ✅ reservation_date = recent timestamp
- [ ] ✅ id = auto-incremented

MySQL OUTPUT FORMAT:
+----+------------------+--------+---------------------+----------+-------+----------+
| id | name             | price  | reservation_date    | event_id | seats | status   |
+----+------------------+--------+---------------------+----------+-------+----------+
|  1 | Ahmed Ben Ali    | 75.00  | 2026-04-29 14:30:20 |        1 |     3 | WAITING  |
+----+------------------+--------+---------------------+----------+-------+----------+

Command 2: Verify data types
mysql> DESC reservations;

Expected Result:
- id: int(11) ✅
- name: varchar(255) ✅
- price: decimal(10,2) ✅
- reservation_date: datetime ✅
- event_id: int(11) with FK ✅
- seats: int(11) ✅
- status: enum('WAITING','CONFIRMED','CANCELLED') ✅

Command 3: Count total reservations
mysql> SELECT COUNT(*) FROM reservations;

Expected Result:
- [ ] ✅ Count increased by 1
- [ ] ✅ New reservation is there

Command 4: Verify foreign key integrity
mysql> SELECT r.id, r.name, e.title 
       FROM reservations r 
       JOIN event e ON r.event_id = e.id 
       WHERE r.id = [newly created id];

Expected Result:
- [ ] ✅ Joint works (FK valid)
- [ ] ✅ Event title matches selected event
- [ ] ✅ Data fully consistent
```

---

## ✅ Complete Test Checklist

```
PRE-TEST
- [ ] MySQL server running
- [ ] Database carthage_gg created
- [ ] Table reservations exists
- [ ] Events and Locations have data
- [ ] Project compiles successfully

TEST EXECUTION
- [ ] Application launches
- [ ] Login successful
- [ ] Events page loads
- [ ] Event details page opens
- [ ] "Reserve Now" button visible
- [ ] Dialog opens with form
- [ ] Form fields all present
- [ ] Full name input works
- [ ] Seats spinner adjusts (1-10)
- [ ] Total price auto-calculates
- [ ] Submit button works
- [ ] Success alert appears
- [ ] Can see new reservation in MySQL

DATABASE VERIFICATION
- [ ] Reservation record saved
- [ ] All fields populated correctly
- [ ] Data types match schema
- [ ] Foreign key valid
- [ ] Timestamp recorded
- [ ] Status = WAITING

RESULT: ✅ ALL TESTS PASSED - SYSTEM OPERATIONAL
```

---

## 🔍 Debug Console Output (Expected)

During execution, you should see in IDE console:

```
14:30:15 INFO: Loading reservation form for event: 1
14:30:20 INFO: ReservationFormController initialized
14:30:25 INFO: Validating reservation form...
14:30:26 INFO: Creating reservation: Ahmed Ben Ali, 3 seats, 75.00 TND
14:30:27 INFO: Executing INSERT: INSERT INTO reservations (name, price, ...) VALUES (...)
14:30:28 INFO: ✅ Reservation saved with ID: 1
14:30:29 INFO: Dialog closing - returning to Event Details
```

**NO ERRORS** should appear in console.

---

## ❌ Common Issues & Solutions

### Issue 1: "No controller specified for fx:controller"
```
Error in FXML loading
✓ FIX: Ensure fx:controller path is correct
✓ FIX: Verify ReservationFormController.java exists
✓ FIX: Rebuild project with mvn clean compile
```

### Issue 2: "Cannot find resource"
```
Error: Cannot find /com/carthagegg/fxml/front/ReservationForm.fxml
✓ FIX: Verify file in src/main/resources/
✓ FIX: Check file path matches exactly
✓ FIX: Rebuild with mvn clean
```

### Issue 3: "SQLException" when saving
```
Error: Cannot insert - unknown column
✓ FIX: Verify reservations table structure
✓ FIX: Run database.sql again
✓ FIX: Check MySQL connection
```

### Issue 4: Form doesn't calculate total
```
Issue: Total Price stays at 25.00 even after changing seats
✓ FIX: Check ReservationFormController.initialize()
✓ FIX: Verify seatsSpinner listener is attached
✓ FIX: Check updateTotalPrice() method exists
```

### Issue 5: Dialog won't close after success
```
Issue: Form stays open after click Reserve Now
✓ FIX: Check buttonData in FXML: buttonData="OK_DONE"
✓ FIX: Verify controller.createReservation() returns non-null
✓ FIX: Check exception isn't silently caught
```

---

## 📊 Success Metrics

| Metric | Expected | Actual |
|--------|----------|--------|
| Form load time | <500ms | __ ms |
| Form validation | Instant | __ |
| Database insert | <1s | __ s |
| User feedback | Alert shown | ✓ |
| Data persistence | Record in MySQL | ✓ |
| UI responsiveness | No freezing | ✓ |

---

## 📝 Test Log Template

```
TEST DATE: [____/____/____]
TESTER: [______________]
BROWSER: Application v1.0

Test Case 1: Load Reservation Form
Result: ✓ PASS / ✗ FAIL
Notes: [_____________________________________]

Test Case 2: Fill Form
Result: ✓ PASS / ✗ FAIL
Notes: [_____________________________________]

Test Case 3: Calculate Price
Result: ✓ PASS / ✗ FAIL
Notes: [_____________________________________]

Test Case 4: Submit & Save
Result: ✓ PASS / ✗ FAIL
Notes: [_____________________________________]

Test Case 5: Verify MySQL Data
Result: ✓ PASS / ✗ FAIL
Notes: [_____________________________________]

OVERALL: ✓ ALL PASS / ✗ SOME FAILURES
```

---

## 🎉 Final Step: Admin Panel Verification

After successful test, verify in Admin:

```
Additional Test (Optional):
1. Login as Admin (if not already)
2. Navigate to "Events Management"
3. Click "Reservations" tab
4. Verify your test reservation appears:
   - ID: [should be visible]
   - Name: Ahmed Ben Ali
   - Price: 75.00
   - Event: [matching event]
   - Status: WAITING

Expected: ✅ Reservation visible in admin table
```

---

**✅ Test Complete!**

If all checks pass ✅, the "Reserve Now" system is **FULLY OPERATIONAL** and ready for production! 🎉

---

**Version**: Test Guide v1.0  
**Date**: 29 April 2026  
**Status**: Ready for Testing

