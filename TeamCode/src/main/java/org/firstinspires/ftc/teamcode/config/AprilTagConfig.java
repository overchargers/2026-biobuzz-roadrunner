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

package org.firstinspires.ftc.teamcode.config;

/**
 * Configuration class containing all constants and parameters for AprilTag autonomous driving.
 * This centralizes all configurable values to make tuning easier and code more maintainable.
 */
public class AprilTagConfig {
    
    // Robot Hardware Configuration
    public static final String FRONT_LEFT_MOTOR_NAME = "frontLeft";
    public static final String FRONT_RIGHT_MOTOR_NAME = "frontRight";
    public static final String BACK_LEFT_MOTOR_NAME = "backLeft";
    public static final String BACK_RIGHT_MOTOR_NAME = "backRight";
    public static final String WEBCAM_NAME = "Webcam 1";
    
    // Camera Configuration
    public static final boolean USE_WEBCAM = true;
    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;
    public static final int MANUAL_EXPOSURE_MS = 6;
    public static final int MANUAL_GAIN = 250;
    public static final int APRIL_TAG_DECIMATION = 2;
    
    // AprilTag Detection Configuration
    public static final int DESIRED_TAG_ID = -1; // -1 for ANY tag
    public static final double DESIRED_DISTANCE_INCHES = 36.0;
    
    // Control Gains - Adjust these to control robot responsiveness
    public static final double SPEED_GAIN = 0.02;    // Forward/backward speed control
    public static final double STRAFE_GAIN = 0.015;  // Left/right strafe control  
    public static final double TURN_GAIN = 0.01;     // Rotation control
    
    // Maximum speeds to prevent robot from moving too aggressively
    public static final double MAX_AUTO_SPEED = 0.5;   // Max forward/backward speed
    public static final double MAX_AUTO_STRAFE = 0.5;  // Max left/right speed
    public static final double MAX_AUTO_TURN = 0.3;    // Max rotation speed
    
    // Manual control speed reductions for better driver control
    public static final double MANUAL_DRIVE_REDUCTION = 2.0;  // Divide joystick input by this
    public static final double MANUAL_STRAFE_REDUCTION = 2.0; // Divide joystick input by this  
    public static final double MANUAL_TURN_REDUCTION = 3.0;   // Divide joystick input by this
    
    // Vision Processing Configuration
    public static final boolean DRAW_AXES = true;
    public static final boolean DRAW_CUBE_PROJECTION = true;
    public static final boolean DRAW_TAG_OUTLINE = true;
    public static final boolean ENABLE_LIVE_VIEW = true;
    
    // Camera Stream Configuration for FTC Dashboard
    public static final int DASHBOARD_STREAM_FPS = 0; // 0 = auto
    public static final String DASHBOARD_URL = "http://192.168.43.1:8080/dash";
    
    // Private constructor to prevent instantiation
    private AprilTagConfig() {}
}
