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

package org.firstinspires.ftc.teamcode.subsystem.drivetrain;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.config.AprilTagConfig;

/**
 * Utility class for controlling a mecanum wheel drivetrain.
 * Handles motor initialization, power calculations, and movement commands.
 * 
 * Motor Layout (when viewed from above):
 *     Front
 *   LF    RF
 *     |
 *     |
 *   LB    RB
 *     Back
 * 
 * Where LF = Left Front, RF = Right Front, LB = Left Back, RB = Right Back
 */
public class MecanumDrivetrain {
    
    private final DcMotor frontLeftDrive;
    private final DcMotor frontRightDrive;
    private final DcMotor backLeftDrive;
    private final DcMotor backRightDrive;
    
    /**
     * Initialize the mecanum drivetrain with the specified hardware map.
     * 
     * @param hardwareMap The FTC hardware map containing motor references
     */
    public MecanumDrivetrain(HardwareMap hardwareMap) {
        // Get motor references from hardware map
        frontLeftDrive = hardwareMap.get(DcMotor.class, AprilTagConfig.FRONT_LEFT_MOTOR_NAME);
        frontRightDrive = hardwareMap.get(DcMotor.class, AprilTagConfig.FRONT_RIGHT_MOTOR_NAME);
        backLeftDrive = hardwareMap.get(DcMotor.class, AprilTagConfig.BACK_LEFT_MOTOR_NAME);
        backRightDrive = hardwareMap.get(DcMotor.class, AprilTagConfig.BACK_RIGHT_MOTOR_NAME);
        
        // Configure motor directions
        // Most robots need one side reversed due to opposite axle directions
        // Adjust these based on your robot's first test drive
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);
    }
    
    /**
     * Move the robot according to desired axes motions.
     * 
     * @param x   Desired forward/backward motion (positive = forward)
     * @param y   Desired left/right strafe motion (positive = left)
     * @param yaw Desired rotation motion (positive = counter-clockwise)
     */
    public void moveRobot(double x, double y, double yaw) {
        // Calculate individual wheel powers using mecanum kinematics
        double frontLeftPower = x - y - yaw;
        double frontRightPower = x + y + yaw;
        double backLeftPower = x + y - yaw;
        double backRightPower = x - y + yaw;
        
        // Normalize wheel powers to prevent motor saturation
        normalizeWheelPowers(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    }
    
    /**
     * Normalize wheel powers to ensure no motor exceeds maximum power.
     * This prevents motor saturation and maintains proportional control.
     * 
     * @param frontLeft  Front left wheel power
     * @param frontRight Front right wheel power  
     * @param backLeft   Back left wheel power
     * @param backRight  Back right wheel power
     */
    private void normalizeWheelPowers(double frontLeft, double frontRight, 
                                    double backLeft, double backRight) {
        // Find the maximum absolute power
        double max = Math.max(Math.abs(frontLeft), Math.abs(frontRight));
        max = Math.max(max, Math.abs(backLeft));
        max = Math.max(max, Math.abs(backRight));
        
        // Scale down all powers if any exceeds 1.0
        if (max > 1.0) {
            frontLeft /= max;
            frontRight /= max;
            backLeft /= max;
            backRight /= max;
        }
        
        // Apply powers to motors
        frontLeftDrive.setPower(frontLeft);
        frontRightDrive.setPower(frontRight);
        backLeftDrive.setPower(backLeft);
        backRightDrive.setPower(backRight);
    }
    
    /**
     * Stop all motors immediately.
     */
    public void stop() {
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
    }
    
    /**
     * Get the front left motor reference (for advanced control if needed).
     * 
     * @return Front left DcMotor
     */
    public DcMotor getFrontLeftMotor() {
        return frontLeftDrive;
    }
    
    /**
     * Get the front right motor reference (for advanced control if needed).
     * 
     * @return Front right DcMotor
     */
    public DcMotor getFrontRightMotor() {
        return frontRightDrive;
    }
    
    /**
     * Get the back left motor reference (for advanced control if needed).
     * 
     * @return Back left DcMotor
     */
    public DcMotor getBackLeftMotor() {
        return backLeftDrive;
    }
    
    /**
     * Get the back right motor reference (for advanced control if needed).
     * 
     * @return Back right DcMotor
     */
    public DcMotor getBackRightMotor() {
        return backRightDrive;
    }
}
