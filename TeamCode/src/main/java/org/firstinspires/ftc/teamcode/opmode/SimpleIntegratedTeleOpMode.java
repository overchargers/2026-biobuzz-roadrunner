/* Copyright (c) 2023 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.opmode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.OuttakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TelemetrySubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransferSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;

/**
 * Simple Integrated TeleOp Mode with Auto-Aim and Manual Override
 * REFACTORED VERSION - Using modular subsystems
 *
 * This OpMode provides comprehensive robot control with:
 * - Ball Intake with continuous operation (AUTOMATIC)
 * - Ball Launcher with SWITCHABLE auto-aim or manual control
 * - Physics-based ballistics calculations (AUTO MODE)
 * - Manual angle and power adjustments (MANUAL MODE)
 * - Conveyor Belt with continuous operation (AUTOMATIC)
 * - Omni-directional drive
 * 
 * ===== SINGLE GAMEPAD CONTROLS =====
 * 
 * DRIVING:
 * - Left Stick: Drive (forward/back, strafe left/right)
 * - Right Stick X: Turn/rotate robot
 * - Left Bumper: Hold to auto-align with AprilTag (both AUTO-AIM and MANUAL
 * modes)
 * 
 * BALL HANDLING:
 * - Left Trigger: Reverse conveyor servo (analog control, 0-100%)
 * - Right Trigger: Forward conveyor servo (analog control, 0-100%)
 * - Right Bumper: Hold to fire launcher continuously (auto-cycles ball pusher)
 * 
 * SHOOTING MODES (TOGGLE WITH A BUTTON):
 * 
 * AUTO-AIM MODE (Default):
 * - Point camera at AprilTag
 * - Hold LEFT BUMPER to align (turns to face target)
 * - System automatically calculates optimal trajectory
 * - Uses physics-based ballistics for angle and power
 * - Accounts for distance, elevation, and projectile motion
 * - High-arc trajectory (15° boost) for obstacle clearance
 * - Right bumper fires when aligned and locked on target
 * - D-Pad controls disabled in this mode
 * 
 * MANUAL MODE:
 * - D-Pad Up/Down: Adjust launch angle (±5° per press)
 * - D-Pad Left/Right: Adjust launcher power (±5% per press)
 * - Left bumper: Hold to manually align with AprilTag (optional)
 * - Right bumper fires at current manual settings
 * - Full control over angle and power
 * - No AprilTag required
 * 
 * BUTTONS:
 * - A Button: Toggle Auto-Aim / Manual mode
 * - B Button: Toggle intake and conveyor on/off (both at full speed)
 * - Right Bumper: Hold to continuously fire launcher (releases balls one by
 * one)
 * - Y Button: Emergency stop all mechanisms (toggle)
 *
 * SOFTWARE FLOW
 * 
 * ----- Initialize Button Pressed
 * 1. Initialize
 *  - Connect to motors
 *  - Boot up camera
 *  - Prepare subsystems
 * ----- Start Button Pressed
 * 2. Main Loop (infinite, 20x per second):
 *  - Handle controls
 *  - Move robot
 *  - Update subsystems
 *  - Update telemetry
 * ----- Stop button pressed
 * 3. Shut everything off; stop safely
 */
@TeleOp(name = "🎮 SimpleIntegratedTeleOpMode", group = "Competition")
//@Disabled
public class SimpleIntegratedTeleOpMode extends LinearOpMode {

    //  SUBSYSTEMS 

    private VisionSubsystem vision;
    private IntakeSubsystem intake;
    private TransferSubsystem transfer;
    private OuttakeSubsystem outtake;
    private DriveSubsystem drive;
    private TelemetrySubsystem telemetryDisplay;

    //  DASHBOARD 

    private FtcDashboard dashboard;

    //  CONFIGURATION 

    private static final double DRIVE_SPEED = 0.8;
    private static final double DRIVE_SPEED_SLOW = 0.3;
    private static final double POWER_INCREMENT = 0.05; // for launcher motors
    private static final double ANGLE_INCREMENT = 5.0;  // for the angle servo

    // AprilTag ID configuration for red and blue alliances
    // Typical FTC games use tags 1-3 for red and 4-6 for blue
    // Adjust these values based on your specific game's AprilTag layout
    private static final int RED_TAG_ID = 24;  // Red alliance tag ID (or -1 for any red tag)
    private static final int BLUE_TAG_ID = 20; // Blue alliance tag ID (or -1 for any blue tag)

    // ==================== STATE VARIABLES ====================

    // Mode control
    private boolean emergencyStop = false;
    private boolean isRedAlliance = false; // Start with blue alliance

    // Drive speed control
    private double currentDriveSpeed = DRIVE_SPEED; // Start with normal speed



    // Button edge detection
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevY = false;
    private boolean prevStart = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;

    // Loop timing
    private long lastLoopTime = 0;
    private long loopInterval = 0;

    //  MAIN OPMODE 

    @Override
    public void runOpMode() {
        // Initialize FTC Dashboard
        dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        // Initialize telemetry subsystem
        telemetryDisplay = new TelemetrySubsystem(telemetry);

        telemetryDisplay.addData("Status", "Initializing subsystems...");
        telemetryDisplay.update();

        // Initialize all subsystems
        initializeSubsystems();

        // Display startup information
        telemetryDisplay.displayStartup(drive, vision, intake, transfer, outtake);

        // Wait for driver to press START
        waitForStart();

        // Stream camera to Dashboard
        if (vision != null && vision.isInitialized()) {
            dashboard.startCameraStream(vision.getVisionPortal(), 0);
        }

        telemetry.addData("Status", "Running!");
        telemetry.update();

        // Initialize loop timing
        lastLoopTime = System.nanoTime();

        // Main control loop
        while (opModeIsActive()) {
            // Calculate loop interval
            long currentTime = System.nanoTime();
            loopInterval = (currentTime - lastLoopTime) / 1000000; // Convert to milliseconds
            lastLoopTime = currentTime;

            // Handle all driver controls
            handleControls();

            // Update subsystems
            updateSubsystems();

            // Update telemetry
            updateTelemetryDisplay();

            sleep(20); // Small delay for system stability
        }

        // Cleanup when opmode ends
        cleanup();
    }

    //  INITIALIZATION 

    /**
     * Initialize all subsystems with configuration
     */
    private void initializeSubsystems() {
        // Create subsystem instances
        vision = new VisionSubsystem();
        intake = new IntakeSubsystem();
        transfer = new TransferSubsystem();
        outtake = new OuttakeSubsystem();
        drive = new DriveSubsystem();

        // Configure Drive
        drive.init(hardwareMap);

        // Configure Vision - start with red alliance
        vision.setDesiredTagId(isRedAlliance ? RED_TAG_ID : BLUE_TAG_ID);
        vision.init(hardwareMap);

        // Configure Intake
        intake.init(hardwareMap);

        // Configure Transfer
        transfer.init(hardwareMap);

        // Configure Outtake
        outtake.init(hardwareMap);
    
    }

    //  CONTROL HANDLING 

    /**
     * Handle all driver controls
     */
    private void handleControls() {
        // Get current button states
        boolean currentY = gamepad1.y;
        // Use options button for start/options button on gamepad
        // Note: Some gamepads use 'options', others might use 'start'
        // If 'options' doesn't work, try 'gamepad1.start' instead
        boolean currentStart = gamepad1.options;
        boolean currentDpadUp = gamepad1.dpad_up;
        boolean currentDpadDown = gamepad1.dpad_down;
        boolean currentDpadLeft = gamepad1.dpad_left;
        boolean currentDpadRight = gamepad1.dpad_right;


        // Toggle drive speed between normal and slow with Y button
        if (currentY && !prevY) {
            if (currentDriveSpeed == DRIVE_SPEED) {
                currentDriveSpeed = DRIVE_SPEED_SLOW;
            } else {
                currentDriveSpeed = DRIVE_SPEED;
            }
        }

        // Toggle between red and blue alliance AprilTag IDs with Start button
        if (currentStart && !prevStart) {
            isRedAlliance = !isRedAlliance;
            if (vision != null) {
                int newTagId = isRedAlliance ? RED_TAG_ID : BLUE_TAG_ID;
                vision.setDesiredTagId(newTagId);
                telemetry.addData("AprilTag Alliance", isRedAlliance ? "RED (Tag " + RED_TAG_ID + ")" : "BLUE (Tag " + BLUE_TAG_ID + ")");
                telemetry.update();
            }
            if (status != null) {
                if (isRedAlliance) {
                    status.redAlliance();
                } else {
                    status.blueAlliance();
                }
            }
        }

        // Update previous button states
        prevA = currentA;
        prevB = currentB;
        prevY = currentY;
        prevStart = currentStart;
        prevDpadUp = currentDpadUp;
        prevDpadDown = currentDpadDown;
        prevDpadLeft = currentDpadLeft;
        prevDpadRight = currentDpadRight;

        handleDriving();
    }

    /**
     * Handle robot driving with optional auto-alignment
     */
    private void handleDriving() {
        if (drive == null || !drive.isInitialized())
            return;

        double forward = -gamepad1.left_stick_y;
        double strafe = -gamepad1.left_stick_x;
        double turn = -gamepad1.right_stick_x;

        // Apply current drive speed multiplier
        forward *= currentDriveSpeed;
        strafe *= currentDriveSpeed;
        turn *= currentDriveSpeed;

        // Drive the robot using subsystem
        drive.drive(forward, strafe, turn);
    }

    //  SUBSYSTEM UPDATES 

    /**
     * Update all subsystems based on current state
     */
    private void updateSubsystems() {
        if (emergencyStop) {
            // Emergency stop - halt everything
            if (intake != null)
                intake.stop();
            if (transfer != null)
                transfer.stop();
            if (outtake != null)
                outtake.stop();
            if (drive != null)
                drive.stop();
            return;
        }

        
        if (vision != null) {
            // Vision update
            // Temporal filtering will handle unstable detections from motion blur
            vision.update(false);
        }


        // Update intake - respect vision's servo pause/resume control
        if (intake != null) {
            if (intakeConveyorToggle) {
                intake.start();
            } else {
                intake.stop();
            }
        }

        // Update transfer (conveyor)
        if (transfer != null) {
            
            
        }

        // Update outtake (launcher)
        if (outtake != null) {
            
        }

        // Update status light (skip when shooting to avoid frequent updates)
        if (status != null && !gamepad1.right_bumper) {
            if (emergencyStop) {
                status.error();
            } else if (autoAimEnabled && vision != null && vision.hasTarget() && ballistics != null
                    && ballistics.canHitTarget()) {
                status.readyToFire();
            } else if (vision != null && vision.hasTarget()) {
                status.targetDetected();
            } else if (!autoAimEnabled) {
                status.manualMode();
            } else {
                if (isRedAlliance) {
                    status.redAlliance();
                } else {
                    status.blueAlliance();
                }
            }
        }
    }

    //  TELEMETRY 

    /**
     * Update telemetry display using telemetry subsystem
     */
    private void updateTelemetryDisplay() {
        if (telemetryDisplay == null || !telemetryDisplay.isInitialized())
            return;

        // Display all sections
        telemetryDisplay.displaySystemStatus(loopInterval, emergencyStop);

        telemetryDisplay.displayDriveStatus(currentDriveSpeed, autoAlignEnabled, vision, autoAimEnabled,
                alignment != null ? alignment.PARAMS.deadband : 0,
                alignment != null ? alignment.PARAMS.deadband : 0);
        telemetryDisplay.displayVisionStatus(vision);

        // Display current alliance selection
        telemetry.addData("🎯 AprilTag Alliance", isRedAlliance ?
            String.format("RED (Tag %d)", RED_TAG_ID) :
            String.format("BLUE (Tag %d)", BLUE_TAG_ID));

        telemetryDisplay.displaySubsystemStatus(drive, vision, intake, transfer, outtake);

        telemetryDisplay.displayControls();

        // Update display
        telemetryDisplay.update();
    }

    // ==================== CLEANUP ====================

    /**
     * Cleanup resources when opmode ends
     */
    private void cleanup() {
        // Stop all subsystems
        if (drive != null)
            drive.stop();
        if (intake != null)
            intake.stop();
        if (transfer != null)
            transfer.stop();
        if (outtake != null)
            outtake.stop();
        if (vision != null)
            vision.close();

        // Stop camera streaming
        if (dashboard != null) {
            dashboard.stopCameraStream();
        }

        // Display shutdown message
        if (telemetryDisplay != null) {
            telemetryDisplay.displayShutdown();
        }
    }
}
