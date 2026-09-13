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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.subsystem.drivetrain.MecanumDrivetrain;
import org.firstinspires.ftc.teamcode.subsystem.vision.AprilTagVisionManager;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

/**
 * Simple Autonomous OpMode that demonstrates basic autonomous capabilities without RoadRunner.
 * 
 * This OpMode is perfect for teams just starting with autonomous programming or those
 * who want a simple, reliable autonomous routine.
 * 
 * Features:
 * - Basic movement patterns (forward, backward, strafe, turn)
 * - AprilTag detection and simple positioning
 * - Comprehensive telemetry
 * - No external dependencies beyond basic FTC libraries
 * 
 * @author FIRST Robotics Team
 * @version 1.0
 */
@Autonomous(name="SimpleAuto", group="Concept")
@Disabled
public class SimpleAuto extends LinearOpMode {
    
    // Core components
    private MecanumDrivetrain drivetrain;
    private AprilTagVisionManager visionManager;
    
    // Autonomous state
    private boolean targetFound = false;
    private AprilTagDetection desiredTag = null;
    
    // Configuration constants
    private static final double MOVE_SPEED = 0.5;
    private static final double TURN_SPEED = 0.3;
    private static final int DESIRED_TAG_ID = 1; // Change this to your desired AprilTag ID
    private static final long MOVE_TIME_MS = 1000; // Time to move in each direction
    
    @Override
    public void runOpMode() {
        // Initialize components
        initializeRobot();
        
        // Display startup information
        displayStartupInfo();
        
        // Wait for driver to press start
        waitForStart();
        
        // Run autonomous sequence
        runAutonomousSequence();
        
        // Clean up resources
        cleanup();
    }
    
    /**
     * Initialize all robot components.
     */
    private void initializeRobot() {
        // Initialize drivetrain
        drivetrain = new MecanumDrivetrain(hardwareMap);
        
        // Initialize vision system
        visionManager = new AprilTagVisionManager(hardwareMap, telemetry);
    }
    
    /**
     * Display startup information and configuration details.
     */
    private void displayStartupInfo() {
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Drive System", "Mecanum Drivetrain");
        telemetry.addData("Vision System", "AprilTag Detection Enabled");
        telemetry.addData("Target Tag ID", DESIRED_TAG_ID);
        telemetry.addData("Move Speed", "%.1f", MOVE_SPEED);
        telemetry.addData("Turn Speed", "%.1f", TURN_SPEED);
        telemetry.addData(">", "Touch START to begin autonomous");
        telemetry.update();
    }
    
    /**
     * Main autonomous sequence with multiple phases.
     */
    private void runAutonomousSequence() {
        telemetry.addData("Phase", "Starting Simple Autonomous Sequence");
        telemetry.update();
        
        // Phase 1: Basic movement test
        runBasicMovementTest();
        
        // Phase 2: AprilTag detection
        runAprilTagDetection();
        
        // Phase 3: Final positioning
        runFinalPositioning();
        
        telemetry.addData("Status", "Autonomous Complete");
        telemetry.update();
    }
    
    /**
     * Phase 1: Basic movement test using simple drivetrain commands.
     */
    private void runBasicMovementTest() {
        telemetry.addData("Phase", "1 - Basic Movement Test");
        telemetry.update();
        
        // Move forward
        telemetry.addData("Action", "Moving Forward");
        telemetry.update();
        drivetrain.moveRobot(MOVE_SPEED, 0, 0);
        sleep(MOVE_TIME_MS);
        
        // Stop
        drivetrain.moveRobot(0, 0, 0);
        sleep(500);
        
        // Strafe right
        telemetry.addData("Action", "Strafing Right");
        telemetry.update();
        drivetrain.moveRobot(0, MOVE_SPEED, 0);
        sleep(MOVE_TIME_MS);
        
        // Stop
        drivetrain.moveRobot(0, 0, 0);
        sleep(500);
        
        // Turn left
        telemetry.addData("Action", "Turning Left");
        telemetry.update();
        drivetrain.moveRobot(0, 0, TURN_SPEED);
        sleep(MOVE_TIME_MS);
        
        // Stop
        drivetrain.moveRobot(0, 0, 0);
        sleep(500);
        
        // Move backward
        telemetry.addData("Action", "Moving Backward");
        telemetry.update();
        drivetrain.moveRobot(-MOVE_SPEED, 0, 0);
        sleep(MOVE_TIME_MS);
        
        // Stop
        drivetrain.moveRobot(0, 0, 0);
        sleep(500);
        
        telemetry.addData("Phase 1", "Complete");
        telemetry.update();
    }
    
    /**
     * Phase 2: AprilTag detection and simple positioning.
     */
    private void runAprilTagDetection() {
        telemetry.addData("Phase", "2 - AprilTag Detection");
        telemetry.update();
        
        // Look for AprilTags for 5 seconds
        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && (System.currentTimeMillis() - startTime) < 5000) {
            updateTargetDetection();
            
            if (targetFound) {
                telemetry.addData("Target Found", "ID: %d", desiredTag.id);
                telemetry.addData("Range", "%.1f inches", desiredTag.ftcPose.range);
                telemetry.addData("Bearing", "%.1f degrees", desiredTag.ftcPose.bearing);
                telemetry.addData("Yaw", "%.1f degrees", desiredTag.ftcPose.yaw);
                
                // Simple approach to the tag
                approachTarget();
                break;
            } else {
                telemetry.addData("Searching", "Looking for AprilTag ID %d", DESIRED_TAG_ID);
                telemetry.addData("Time Remaining", "%d seconds", (5000 - (int)(System.currentTimeMillis() - startTime)) / 1000);
            }
            
            telemetry.update();
            sleep(100);
        }
        
        if (!targetFound) {
            telemetry.addData("Phase 2", "No target found, continuing...");
        } else {
            telemetry.addData("Phase 2", "Target approach complete");
        }
        telemetry.update();
    }
    
    /**
     * Phase 3: Final positioning and cleanup.
     */
    private void runFinalPositioning() {
        telemetry.addData("Phase", "3 - Final Positioning");
        telemetry.update();
        
        // Move to a final position
        telemetry.addData("Action", "Moving to final position");
        telemetry.update();
        
        // Move forward a bit
        drivetrain.moveRobot(MOVE_SPEED * 0.5, 0, 0);
        sleep(1000);
        
        // Strafe left
        drivetrain.moveRobot(0, -MOVE_SPEED * 0.5, 0);
        sleep(1000);
        
        // Stop all movement
        drivetrain.moveRobot(0, 0, 0);
        
        telemetry.addData("Phase 3", "Complete");
        telemetry.update();
    }
    
    /**
     * Update target detection and find the desired AprilTag.
     */
    private void updateTargetDetection() {
        targetFound = false;
        desiredTag = null;
        
        // Find the desired tag using the vision manager
        desiredTag = visionManager.findDesiredTag();
        targetFound = (desiredTag != null);
    }
    
    /**
     * Simple approach to the detected AprilTag.
     */
    private void approachTarget() {
        if (!targetFound || desiredTag == null) return;
        
        telemetry.addData("Approaching", "Target ID %d", desiredTag.id);
        telemetry.update();
        
        // Simple approach: move forward until within 15 inches
        double targetDistance = 15.0;
        double currentDistance = desiredTag.ftcPose.range;
        
        if (currentDistance > targetDistance) {
            // Move forward slowly
            drivetrain.moveRobot(0.3, 0, 0);
            sleep(1000);
            drivetrain.moveRobot(0, 0, 0);
            
            // Update distance
            updateTargetDetection();
            if (targetFound) {
                currentDistance = desiredTag.ftcPose.range;
                telemetry.addData("New Distance", "%.1f inches", currentDistance);
            }
        }
        
        // Try to center on the tag
        if (targetFound) {
            double bearing = desiredTag.ftcPose.bearing;
            if (Math.abs(bearing) > 5) { // If more than 5 degrees off center
                if (bearing > 0) {
                    // Turn right
                    drivetrain.moveRobot(0, 0, -0.2);
                } else {
                    // Turn left
                    drivetrain.moveRobot(0, 0, 0.2);
                }
                sleep(500);
                drivetrain.moveRobot(0, 0, 0);
            }
        }
        
        telemetry.addData("Approach", "Complete - Distance: %.1f inches", currentDistance);
        telemetry.update();
    }
    
    /**
     * Clean up resources and stop all systems.
     */
    private void cleanup() {
        // Stop all movement
        drivetrain.moveRobot(0, 0, 0);
        
        // Close vision system
        if (visionManager != null) {
            visionManager.close();
        }
        
        telemetry.addData("Cleanup", "Complete");
        telemetry.update();
    }
}
