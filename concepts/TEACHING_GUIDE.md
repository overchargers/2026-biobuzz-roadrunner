# 🎓 Understanding the Robot TeleOp Code

---

## 🎯 **What Does This Robot Do?**

Imagine a robot that can:
- **Drive around** like a remote control car (but it can move in ANY direction!)
- **Pick up balls** using an intake system
- **Launch balls** at a target
- **Use a camera** to find targets automatically
- **Calculate physics** to hit the target perfectly!

This code controls ALL of that!

---

## 📚 **Table of Contents**
1. [The Big Picture](#1-the-big-picture)
2. [Setting Up the Robot](#2-setting-up-the-robot)
3. [The Main Loop](#3-the-main-loop)
4. [Driving the Robot](#4-driving-the-robot)
5. [Ball Handling Systems](#5-ball-handling-systems)
6. [Auto-Aim: The Cool Physics Part](#6-auto-aim-the-cool-physics-part)
7. [The State Machine](#7-the-state-machine)
8. [Controller Buttons](#8-controller-buttons)

---

## 1. **The Big Picture** 🖼️

### What is TeleOp?
**TeleOp** = "Tele-Operated" = You control the robot with a gamepad controller!

### The Code Structure (Think of it like a recipe)

```
┌─────────────────────────────────────┐
│      INIT BUTTON PRESSED            │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│   1. Initialize (Set everything up) │
│      - Connect to motors            │
│      - Setup camera                 │
│      - Prepare systems              │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│      START BUTTON PRESSED           │
└──────────────┬──────────────────────┘
               │
               ↓
               ┌────────────────────────────┐
               │                            │
               ↓                            │
┌─────────────────────────────────────┐     │
│   2. Main Loop (Repeats forever)    │     │
│      - Handle controls              │     │
│      - Move robot                   │     │
│      - Update all subsystems        │     │
│      - Update telemetry display     │     │
└──────────────┬──────────────────────┘     │
               │                            │
               └────────────────────────────┘  (Loops back! 20x per second)
               │
               ↓ (Press STOP to exit)
               │
┌─────────────────────────────────────┐
│   3. Stop (When you press STOP)     │
│      - Turn everything off safely   │
└─────────────────────────────────────┘
```

---

## 2. **Setting Up the Robot** ⚙️

### The Hardware (Physical Parts)

Think of the robot like a body with different parts:

#### **Drive System** (The Legs) 🦿
```java
private DcMotor frontLeftDrive = null;
private DcMotor frontRightDrive = null;
private DcMotor backLeftDrive = null;
private DcMotor backRightDrive = null;
```

**What this means:**
- `DcMotor` = A motor that can spin forward or backward
- We have **4 motors** (one at each corner)
- This is called **"Mecanum Drive"** or **"Omni Drive"**
- It lets the robot move forward, backward, sideways, and turn!

**Analogy:** Like having wheels that can roll in any direction (like roller skates!)

---

#### **Intake System** (The Mouth) 👄
```java
private CRServo intakeServo = null;
```

**What this means:**
- `CRServo` = A "Continuous Rotation Servo" (a motor that spins continuously)
- This picks up balls from the ground

**Analogy:** Like a vacuum cleaner that sucks in balls!

---

#### **Launcher System** (The Arm) 💪
```java
private DcMotorEx leftLauncherMotor = null;
private DcMotorEx rightLauncherMotor = null;
private Servo angleServo = null;
private Servo ballPusherServo = null;
```

**What this means:**
- Two powerful motors spin really fast to launch balls
- `angleServo` tilts the launcher up or down
- `ballPusherServo` pushes balls into the spinning wheels

**Analogy:** Like a baseball pitching machine!

---

#### **Vision System** (The Eyes) 👁️
```java
private VisionPortal visionPortal;
private AprilTagProcessor aprilTag;
```

**What this means:**
- The robot has a camera
- It can recognize special markers called "AprilTags" (like QR codes)
- This helps it know where the target is!

**Analogy:** Like how you use your eyes to aim when throwing a ball!

---

## 3. **The Main Loop** 🔄

### The Heart of the Program

```java
while (opModeIsActive()) {
    // This runs over and over until you press STOP
    
    handleControls();           // Read gamepad buttons
    updateAprilTagDetection();  // Check camera
    updateRGBLight();           // Update indicator lights
    calculateShootingParameters(); // Do physics math
    updateAllMechanisms();      // Move motors/servos
    updateTelemetry();          // Show info on screen
    
    sleep(20); // Wait 20 milliseconds (1/50th of a second)
}
```

**What happens:**
1. **Read inputs** - What buttons are you pressing?
2. **Process** - What should the robot do?
3. **Output** - Make motors and servos move!
4. **Repeat** - Do it all again 20 times per second!

**Analogy:** Like your brain constantly:
1. Seeing what's happening
2. Deciding what to do
3. Moving your muscles
4. Repeating this cycle super fast!

---

## 4. **Driving the Robot** 🚗

### How the Gamepad Controls Work

```java
double drive = -gamepad1.left_stick_y * DRIVE_SPEED;
double strafe = -gamepad1.left_stick_x * DRIVE_SPEED;
double turn = -gamepad1.right_stick_x * DRIVE_SPEED * 0.8;
```

**Breaking it down:**

| Controller Input | What It Does | Robot Movement |
|-----------------|--------------|----------------|
| Left Stick UP/DOWN | `drive` | Move Forward/Backward |
| Left Stick LEFT/RIGHT | `strafe` | Move Sideways (Strafe) |
| Right Stick LEFT/RIGHT | `turn` | Spin/Rotate |

**The Math Magic:**
```java
double frontLeftPower = x - y - yaw;
double frontRightPower = x + y + yaw;
double backLeftPower = x + y - yaw;
double backRightPower = x - y + yaw;
```

Each wheel gets a different amount of power to make the robot move in the desired direction!

**Analogy:** Like riding a bike and a skateboard at the same time - you can move in ways that seem impossible!

---

### Auto-Alignment Feature (PD Controller)

When you press the **Left Bumper**, the robot automatically turns to face the AprilTag!

```java
if (gamepad1.left_bumper && desiredTag != null) {
    autoAlignEnabled = true;
}
```

**How it works:**
1. Camera sees AprilTag
2. Robot calculates how much to turn
3. Robot turns automatically
4. You still control forward/back movement!

**The "PD Controller":**
- **P = Proportional**: Turn harder when you're further from target
- **D = Derivative**: Slow down as you get close (so you don't overshoot)

**Analogy:** Like power steering in a car - it helps you turn smoothly!

---

## 5. **Ball Handling Systems** ⚽

### The B Button Toggle

```java
if (currentB && !prevB) {
    intakeConveyorToggle = !intakeConveyorToggle;
}
```

**What this means:**
- Press B once → Turn ON intake and conveyor
- Press B again → Turn OFF
- This is called a "toggle" (like a light switch)

**The Code Trick:**
```java
boolean prevB = false; // Remember previous state
```

We check `currentB && !prevB` to detect when the button was **just pressed** (not held down).

**Analogy:** Like turning a light switch on/off - you only want it to change when you press, not while holding!

---

### The Launcher State Machine

This is one of the **coolest and most complex** parts!

```java
private enum PusherState {
    IDLE,
    SPINNING_UP,
    PUSHING,
    WAITING_AT_PUSH,
    RETURNING,
    WAITING_AT_REST
}
```

**What's a "State Machine"?**

Think of it like a flowchart where the robot can only be in ONE state at a time:

```
      IDLE (waiting)
         │
         ↓ (Press Right Bumper)
    SPINNING_UP (motors accelerating)
         │
         ↓ (Motors at speed)
     PUSHING (move servo to push ball)
         │
         ↓ (Wait for servo to move)
  WAITING_AT_PUSH
         │
         ↓ (Servo reached position)
    RETURNING (move servo back)
         │
         ↓ (Wait for servo to move back)
  WAITING_AT_REST
         │
         ↓ (Still holding button? Loop back to PUSHING!)
         └──→ IDLE (Button released)
```

**Why use a state machine?**
- Keeps track of what the robot is doing
- Prevents things from happening out of order
- Makes timing work correctly

**Analogy:** Like a traffic light - it goes Green → Yellow → Red in order, never skipping or going backward!

---

### Timing is Important!

```java
private static final long PUSHER_MOVE_TIME_MS = 450;  // 450 milliseconds
```

The servo takes **450ms** (almost half a second) to physically move. The robot waits for it!

**Why wait?**
If you command the servo to move and immediately command it to move back, it might not have time to actually reach the position!

**Analogy:** Like telling someone to go to the store and immediately saying "come back!" before they even leave the house!

---

## 6. **Auto-Aim: The Cool Physics Part** 🎯

### The Problem

You have a ball launcher. The target is far away and higher up. What angle should you launch at? How hard?

**This is PROJECTILE MOTION from physics class!**

---

### The Physics Formula

```java
// The launcher's projectile motion equation
double cosTheta = Math.cos(angleRadians);
double numerator = GRAVITY * targetDistance * targetDistance;
double denominator = 2.0 * cosTheta * cosTheta * 
                    (targetDistance * Math.tan(angleRadians) - targetElevation);
requiredVelocity = Math.sqrt(numerator / denominator);
```

**What this means (simplified):**

The robot needs to calculate:
1. **How far** the target is (distance)
2. **How high** the target is (elevation)
3. **What angle** to shoot at
4. **How fast** to shoot

**Real-World Physics:**
- Gravity pulls the ball down: `GRAVITY = 386.4 inches/s²`
- The ball follows a curved path (parabola)
- Higher targets need more angle or more power

**Analogy:** Like a basketball player calculating the perfect arc to make a basket!

---

### Camera Perspective

```java
double cameraX = desiredTag.ftcPose.x;  // Left/Right
double cameraY = desiredTag.ftcPose.y;  // Forward distance
```

The camera sees the AprilTag and measures:
- **X**: How far left or right
- **Y**: How far forward
- **Z**: How high or low

The robot uses this to aim!

---

### The "Arch Boost"

```java
private static final double ARCH_BOOST_DEGREES = 15.0;
```

The robot adds **15° extra angle** to make the ball arc higher!

**Why?**
- A higher arc helps the ball go over obstacles
- It's more forgiving if your calculations are slightly off

**Analogy:** Like in basketball - you shoot with an arc, not a straight line!

---

### Manual vs Auto Modes

**AUTO-AIM MODE:**
```java
if (autoAimEnabled && canHitTarget) {
    targetLauncherPower = calculatedPower;
    angleToUse = calculatedAngle;
}
```
Robot does ALL the math automatically!

**MANUAL MODE:**
```java
if (!autoAimEnabled) {
    targetLauncherPower = currentLauncherPower;
    angleToUse = targetLaunchAngle;
}
```
You adjust angle and power with D-Pad buttons!

---

## 7. **The State Machine** 🤖

### Why State Machines Matter

A **state machine** helps organize complex sequences of actions.

**Real-Life Example:**
Think about using a microwave:
1. **IDLE** - Door closed, nothing happening
2. **DOOR_OPEN** - You opened door to put food in
3. **COOKING** - Timer running
4. **DONE** - Beeping
5. Back to **IDLE**

The microwave can't cook while the door is open! States prevent impossible situations.

---

### Our Ball Pusher State Machine

```java
switch (pusherState) {
    case IDLE:
        // Check for button press
        if (currentRightBumper && targetLauncherPower > 0) {
            pusherState = PusherState.SPINNING_UP;
            // Start motors
        }
        break;
        
    case SPINNING_UP:
        // Wait for motors to reach speed
        if (atSpeed || timedOut) {
            pusherState = PusherState.PUSHING;
        }
        break;
        
    // ... more states ...
}
```

**The `switch` statement:**
- Checks what state we're in
- Does the right thing for that state
- Changes to the next state when ready

**Key Insight:** The robot remembers where it is in the sequence, even between loop cycles!

---

## 8. **Controller Buttons** 🎮

### Button Mapping

Here's what each button does:

```java
// === DRIVING ===
Left Stick      → Move forward/back, strafe left/right
Right Stick X   → Turn/rotate robot
Left Bumper     → Hold to auto-align with AprilTag

// === BALL HANDLING ===
Left Trigger    → Reverse conveyor (analog 0-100%)
Right Trigger   → Forward conveyor (analog 0-100%)
Right Bumper    → Hold to fire launcher continuously

// === MODE SWITCHES ===
A Button        → Toggle Auto-Aim / Manual mode
B Button        → Toggle intake and conveyor on/off
Y Button        → Emergency stop all mechanisms

// === MANUAL MODE ONLY ===
D-Pad Up/Down   → Adjust launch angle (±5°)
D-Pad Left/Right → Adjust launcher power (±5%)
```

---

### Edge Detection (Button Press vs Hold)

```java
if (currentA && !prevA) {
    autoAimEnabled = !autoAimEnabled;
}
prevA = currentA;
```

**What this does:**
- `currentA` = Is A button pressed RIGHT NOW?
- `prevA` = Was A button pressed LAST loop?
- `currentA && !prevA` = Button was JUST pressed (not held)

**Why?**
Without this, one button press might trigger 20 times (once per loop cycle)!

**Analogy:** Like a doorbell - you want it to ring once per press, not continuously while your finger is on it!

---

## 9. **Advanced Concepts** 🚀

### Constants vs Variables

**Constants** (never change):
```java
private static final double GRAVITY = 386.4;
private static final double DRIVE_SPEED = 0.8;
```
- `static final` means it CAN'T change
- Written in ALL_CAPS by convention
- Like the speed of light in physics!

**Variables** (can change):
```java
private double currentLauncherPower = 0.7;
private boolean autoAimEnabled = true;
```
- Can be changed throughout the program
- Store the current state

---

### The `null` Keyword

```java
private DcMotor frontLeftDrive = null;
```

**What is `null`?**
- Means "nothing" or "not connected yet"
- We set it to `null` first, then connect to the actual motor later

**Why check for `null`?**
```java
if (intakeServo != null) {
    intakeServo.setPower(power);
}
```
If the motor isn't connected, this prevents a crash!

**Analogy:** Like checking if your phone is plugged in before trying to charge it!

---

### Try-Catch Blocks

```java
try {
    intakeServo = hardwareMap.get(CRServo.class, "intakeServo");
} catch (Exception e) {
    // Non-critical - continue without intake
}
```

**What this does:**
- **Try** to connect to the intake servo
- **If it fails** (Exception), don't crash - just continue without it

**Why?**
So you can test your code even if some hardware isn't connected!

**Analogy:** Like having a backup plan - if Plan A fails, try Plan B!

---

## 10. **The Telemetry System** 📺

### Showing Information on the Screen

```java
telemetry.addData("Launch Angle", "%.1f°", calculatedAngle);
telemetry.update();
```

**What this does:**
- Shows information on the Driver Station phone/tablet
- Helps you debug and monitor the robot
- Format: `telemetry.addData(label, value)`

**Format Specifiers:**
- `%d` = Integer (whole number)
- `%.1f` = Float with 1 decimal place
- `%.2f` = Float with 2 decimal places
- `%s` = String (text)
- `%%` = Percent sign

**Example:**
```java
telemetry.addData("Distance", "%.2f inches", 45.678);
// Displays: "Distance: 45.68 inches"
```

---

## 11. **Programming Concepts Explained** 💡

### 1. **Classes and Objects**

```java
public class SimpleIntegratedTeleOpMode extends LinearOpMode {
    // This is a CLASS - a blueprint for creating a robot controller
}
```

**What's a class?**
- A template that defines what something IS and what it can DO
- Like a blueprint for a house

**What's an object?**
- An actual instance created from a class
- Like the actual house built from the blueprint

---

### 2. **Methods (Functions)**

```java
private void calculateShootingParameters() {
    // Code here
}
```

**What's a method?**
- A named block of code that does ONE specific thing
- Can be called (used) multiple times
- Helps organize code into logical pieces

**Parts:**
- `private` = Only this class can use it
- `void` = Doesn't return a value
- `calculateShootingParameters` = The name
- `()` = Parameters (inputs) - this one has none

---

### 3. **Loops**

```java
while (opModeIsActive()) {
    // Repeat forever until STOP is pressed
}
```

**Types of loops:**
- `while` = Repeat as long as condition is true
- `for` = Repeat a specific number of times
- `do-while` = Do something once, then repeat if condition is true

---

### 4. **If Statements**

```java
if (autoAimEnabled && canHitTarget) {
    // Do auto-aim
} else {
    // Do manual control
}
```

**Conditions:**
- `==` equals
- `!=` not equals
- `>` greater than
- `<` less than
- `&&` AND (both must be true)
- `||` OR (at least one must be true)
- `!` NOT (opposite)

---

### 5. **Enums (Enumerated Types)**

```java
private enum PusherState {
    IDLE,
    SPINNING_UP,
    PUSHING
}
```

**What's an enum?**
- A list of named constants
- The variable can ONLY be one of these values
- Like multiple choice: A, B, C, or D (not E or F!)

**Why use enums?**
- Prevents typos
- Makes code more readable
- IDE can help autocomplete

---

## 12. **Common Patterns & Tricks** 🎩

### Pattern 1: Range Clipping

```java
currentLauncherPower = Range.clip(currentLauncherPower, MIN_POWER, MAX_POWER);
```

**What it does:**
- Keeps a value between a minimum and maximum
- If too low → set to minimum
- If too high → set to maximum

**Example:**
```java
double speed = Range.clip(speed, 0.0, 1.0);
// Speed will always be between 0 and 1
```

---

### Pattern 2: Button Edge Detection

```java
boolean prevButton = false;

// In loop:
boolean current = gamepad1.a;
if (current && !prevButton) {
    // Button was JUST pressed
}
prevButton = current;
```

---

### Pattern 3: Toggle Variables

```java
if (buttonPressed) {
    toggle = !toggle;  // Flip between true and false
}
```

**The `!` operator:**
- `!true` becomes `false`
- `!false` becomes `true`

---

### Pattern 4: Time-Based Waits

```java
long startTime = System.currentTimeMillis();

// Later:
long elapsed = System.currentTimeMillis() - startTime;
if (elapsed >= 450) {
    // 450ms has passed!
}
```

**Why not use `sleep()`?**
- `sleep()` stops EVERYTHING
- Time checks let the robot keep doing other things!

---

## 13. **Debugging Tips** 🐛

### How to Find Problems

1. **Read Error Messages**
   - Look for line numbers
   - Look for null pointer exceptions

2. **Use Telemetry**
   ```java
   telemetry.addData("Debug", "Value is: %d", myVariable);
   ```

3. **Test One Thing at a Time**
   - Comment out code with `//`
   - Test just the drive system first
   - Then add ball handling
   - Then add vision

4. **Check Null Values**
   ```java
   if (motor != null) {
       motor.setPower(1.0);
   } else {
       telemetry.addData("Error", "Motor not found!");
   }
   ```

---

## 14. **Challenge Questions** 🤔

Test your understanding!

### Beginner Questions:

1. **What does `opModeIsActive()` do?**
   <details>
   <summary>Answer</summary>
   It returns `true` while the OpMode is running, `false` when STOP is pressed.
   </details>

2. **Why do we use `sleep(20)` at the end of the main loop?**
   <details>
   <summary>Answer</summary>
   To give the system a brief pause (20ms), preventing the loop from using too much CPU and allowing smooth operation at about 20 cycles per second.
   </details>

3. **What's the difference between `DcMotor` and `Servo`?**
   <details>
   <summary>Answer</summary>
   - DcMotor: Spins continuously at various speeds (for wheels, launchers)
   - Servo: Moves to specific positions between 0.0 and 1.0 (for angles, gates)
   </details>

---

### Intermediate Questions:

4. **Why do we need edge detection for button presses?**
   <details>
   <summary>Answer</summary>
   Without edge detection, one button press would trigger 20+ times per second (once per loop cycle). Edge detection ensures it only triggers ONCE per press.
   </details>

5. **What is the purpose of the state machine in the ball pusher?**
   <details>
   <summary>Answer</summary>
   To ensure actions happen in the correct order with proper timing - motors must spin up before pushing, servo must reach position before returning, etc.
   </details>

6. **Why check for `null` before using hardware devices?**
   <details>
   <summary>Answer</summary>
   If a device isn't connected or configured, the code will crash. Null checks allow the robot to continue working even if some components are missing.
   </details>

---

### Advanced Questions:

7. **How does the PD controller for auto-alignment work?**
   <details>
   <summary>Answer</summary>
   - P (Proportional): Turn harder when error is larger
   - D (Derivative): Reduce turning as error decreases (damping)
   - This creates smooth, stable alignment without oscillation
   </details>

8. **Why does the ballistics calculation need to account for camera tilt?**
   <details>
   <summary>Answer</summary>
   The camera is tilted 30°, so the Y distance it measures isn't the true horizontal distance. We must multiply by cos(30°) to get the actual ground distance.
   </details>

9. **What would happen if you removed the power normalization in `moveRobot()`?**
   <details>
   <summary>Answer</summary>
   If combined motor powers exceed 1.0, the robot would move in unexpected directions because motor speeds would be capped unevenly. Normalization keeps the direction correct while staying within power limits.
   </details>

---

## 15. **Modifications to Try** 🛠️

### Easy Modifications:

1. **Change Drive Speed**
   ```java
   private static final double DRIVE_SPEED = 0.5;  // Slower (50%)
   ```

2. **Change Button Mappings**
   ```java
   // Use X button instead of A for mode toggle
   if (gamepad1.x && !prevX) {
       autoAimEnabled = !autoAimEnabled;
   }
   ```

3. **Add Sound Effects**
   ```java
   // When target is locked
   if (canHitTarget && !wasLocked) {
       // Play sound
   }
   ```

---

### Medium Modifications:

4. **Add a "Turbo" Button**
   ```java
   double speedMultiplier = gamepad1.right_trigger > 0.5 ? 1.5 : 1.0;
   double drive = -gamepad1.left_stick_y * DRIVE_SPEED * speedMultiplier;
   ```

5. **Add a Shot Counter**
   ```java
   private int shotsFired = 0;
   
   // In PUSHING state:
   shotsFired++;
   
   // In telemetry:
   telemetry.addData("Shots Fired", shotsFired);
   ```

6. **Add Field-Centric Drive**
   (More advanced - requires IMU for robot heading)

---

### Advanced Modifications:

7. **Add PID Control for Launcher Speed**
   - Make launcher maintain exact RPM
   - Compensate for battery voltage drop

8. **Add Trajectory Prediction**
   - Calculate where the ball will land
   - Show prediction on camera feed

9. **Add Multi-Target Selection**
   - Cycle through multiple AprilTags
   - Choose closest or highest priority

---

## 16. **Key Takeaways** ✨

### What Makes This Code Good:

1. **Well-Organized**
   - Clear sections for different systems
   - Good comments explaining what things do
   - Constants at the top

2. **Robust**
   - Null checks prevent crashes
   - Try-catch blocks handle errors gracefully
   - Emergency stop for safety

3. **Professional Features**
   - State machines for complex sequences
   - PD controller for smooth alignment
   - Physics-based calculations

4. **Good Telemetry**
   - Shows lots of useful information
   - Helps with debugging
   - Organized into sections

---

### Programming Concepts You've Learned:

✅ Classes and Objects  
✅ Methods/Functions  
✅ Variables and Constants  
✅ Loops (while, for)  
✅ Conditionals (if/else)  
✅ State Machines  
✅ Enums  
✅ Edge Detection  
✅ Time-Based Programming  
✅ Error Handling (try-catch)  
✅ Control Systems (PD Controller)  
✅ Physics Calculations  

---

## 17. **Next Steps** 🚀

### For Students:

1. **Read through the code section by section**
   - Don't try to understand everything at once!
   - Focus on one system (like drive) first

2. **Try small modifications**
   - Change a constant
   - Swap a button
   - Add telemetry

3. **Test on real hardware**
   - See how it actually behaves
   - Adjust constants to fit your robot

4. **Learn more:**
   - Control theory (PID controllers)
   - Physics (projectile motion)
   - Advanced Java concepts

---

### Resources:

- **FTC Documentation**: https://ftc-docs.firstinspires.org/
- **Game Manual 0**: https://gm0.org/ (Amazing FTC resource!)
- **Java Tutorials**: https://docs.oracle.com/javase/tutorial/
- **Physics of Projectiles**: Khan Academy

---

## 18. **Glossary** 📖

**AprilTag**: A visual marker (like a QR code) that robots can detect with cameras

**CRServo**: Continuous Rotation Servo - spins continuously like a motor

**DcMotor**: Direct Current Motor - the main motors that drive wheels

**Edge Detection**: Detecting when a button transitions from not-pressed to pressed

**Enum**: A type with a fixed set of named values

**FTC**: FIRST Tech Challenge - a robotics competition for middle/high school

**Null**: Represents "nothing" or "not initialized"

**OpMode**: A program that runs on the robot during a match

**PD Controller**: Proportional-Derivative controller for smooth automated control

**Servo**: A motor that moves to specific positions (usually 0° to 180°)

**State Machine**: A system that can be in one state at a time and transitions between states

**TeleOp**: Tele-Operated mode where drivers control the robot with gamepads

**Telemetry**: Information displayed on the driver station screen

---

## 🎉 **Congratulations!**

You now understand a 1400+ line professional robot control program!

This code includes:
- ✅ Multiple hardware systems working together
- ✅ Computer vision and auto-aiming
- ✅ Physics calculations
- ✅ State machines
- ✅ Control systems (PD controller)
- ✅ Professional coding practices

**You're ready to build amazing robots!** 🤖✨

---

*Questions? Need clarification? Ask your mentor or post on the FTC forums!*

