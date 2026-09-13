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
 * Configuration class for ball launcher system.
 * Contains all tunable parameters for launcher control, servo angles, and hardware configuration.
 * 
 * IMPORTANT: Update these values based on your specific robot configuration!
 */
public class BallLauncherConfig {
    
    // ============================================================================
    // HARDWARE CONFIGURATION
    // ============================================================================
    
    /**
     * Name of the left launcher wheel motor in the robot configuration.
     */
    public static final String LEFT_LAUNCHER_MOTOR_NAME = "leftLauncher";
    
    /**
     * Name of the right launcher wheel motor in the robot configuration.
     */
    public static final String RIGHT_LAUNCHER_MOTOR_NAME = "rightLauncher";
    
    /**
     * Name of the launcher angle servo in the robot configuration.
     */
    public static final String LAUNCHER_SERVO_NAME = "launcherServo";
    
    // ============================================================================
    // SERVO ANGLE CONFIGURATION
    // ============================================================================
    
    /**
     * Minimum servo angle in degrees (lowest launch angle).
     * Adjust based on your servo's range and mechanical setup.
     */
    public static final double SERVO_MIN_ANGLE = 0.0;
    
    /**
     * Maximum servo angle in degrees (highest launch angle).
     * Adjust based on your servo's range and mechanical setup.
     */
    public static final double SERVO_MAX_ANGLE = 180.0;
    
    /**
     * Default servo angle in degrees (middle position).
     */
    public static final double SERVO_DEFAULT_ANGLE = 90.0;
    
    /**
     * Servo angle increment per button press in degrees.
     * Smaller values = finer control, larger values = faster adjustment.
     */
    public static final double SERVO_ANGLE_INCREMENT = 5.0;
    
    // ============================================================================
    // LAUNCHER WHEEL SPEED CONFIGURATION
    // ============================================================================
    
    /**
     * Maximum power for launcher wheels (0.0 to 1.0).
     * Higher values = faster ball launch speed.
     */
    public static final double MAX_LAUNCHER_POWER = 1.0;
    
    /**
     * Minimum power for launcher wheels (0.0 to 1.0).
     * Minimum power needed to maintain wheel speed.
     */
    public static final double MIN_LAUNCHER_POWER = 0.3;
    
    /**
     * Default launcher wheel power when activated.
     */
    public static final double DEFAULT_LAUNCHER_POWER = 0.8;
    
    /**
     * Power increment per button press (0.0 to 1.0).
     * Smaller values = finer control, larger values = faster adjustment.
     */
    public static final double LAUNCHER_POWER_INCREMENT = 0.1;
    
    // ============================================================================
    // APRILTAG ALIGNMENT CONFIGURATION
    // ============================================================================
    
    /**
     * Desired AprilTag ID for goal alignment.
     * Set to -1 to align to any detected AprilTag.
     */
    public static final int TARGET_APRILTAG_ID = -1;
    
    /**
     * Maximum distance in inches to consider AprilTag for alignment.
     * Tags beyond this distance will be ignored.
     */
    public static final double MAX_ALIGNMENT_DISTANCE = 60.0;
    
    /**
     * Tolerance in degrees for AprilTag alignment.
     * Robot is considered aligned when bearing error is less than this value.
     */
    public static final double ALIGNMENT_TOLERANCE = 3.0;
    
    /**
     * Tolerance in inches for AprilTag distance.
     * Robot is considered at correct distance when range error is less than this value.
     */
    public static final double DISTANCE_TOLERANCE = 6.0;
    
    /**
     * Desired distance from AprilTag in inches.
     */
    public static final double DESIRED_DISTANCE = 24.0;
    
    // ============================================================================
    // CONTROL GAINS
    // ============================================================================
    
    /**
     * Proportional gain for AprilTag alignment rotation.
     */
    public static final double ALIGNMENT_TURN_GAIN = 0.02;
    
    /**
     * Proportional gain for AprilTag distance adjustment.
     */
    public static final double ALIGNMENT_DRIVE_GAIN = 0.015;
    
    /**
     * Proportional gain for AprilTag strafe adjustment.
     */
    public static final double ALIGNMENT_STRAFE_GAIN = 0.01;
    
    /**
     * Maximum speed for automatic alignment movement.
     */
    public static final double MAX_ALIGNMENT_SPEED = 0.4;
    
    // ============================================================================
    // TRAJECTORY CALCULATION PARAMETERS
    // ============================================================================
    
    /**
     * Gravity constant for trajectory calculations (m/s²).
     */
    public static final double GRAVITY = 9.81;
    
    /**
     * Ball mass in kilograms (for trajectory calculations).
     */
    public static final double BALL_MASS = 0.05; // 50g ball
    
    /**
     * Air resistance coefficient (drag factor).
     * Higher values = more air resistance.
     */
    public static final double AIR_RESISTANCE = 0.1;
    
    /**
     * Maximum launcher velocity in m/s.
     * This determines the maximum power needed for calculations.
     */
    public static final double MAX_LAUNCHER_VELOCITY = 15.0; // m/s
    
    /**
     * Minimum launcher velocity in m/s.
     * Below this, the ball won't reach the target.
     */
    public static final double MIN_LAUNCHER_VELOCITY = 3.0; // m/s
    
    /**
     * Launcher height above ground in meters.
     */
    public static final double LAUNCHER_HEIGHT = 0.3; // 30cm
    
    /**
     * Target height above ground in meters.
     */
    public static final double TARGET_HEIGHT = 0.5; // 50cm
    
    /**
     * Power scaling factor to convert calculated velocity to motor power.
     * Adjust based on your launcher's performance characteristics.
     */
    public static final double VELOCITY_TO_POWER_SCALE = 0.067; // 1/15.0
    
    /**
     * Angle adjustment factor for elevation compensation.
     * Higher values = more aggressive angle adjustment.
     */
    public static final double ELEVATION_GAIN = 0.8;
    
    /**
     * Distance adjustment factor for power compensation.
     * Higher values = more aggressive power adjustment.
     */
    public static final double DISTANCE_GAIN = 0.5;
    
    /**
     * Drivetrain direction adjustment gain.
     * Higher values = more aggressive turning to face target.
     */
    public static final double DRIVETRAIN_DIRECTION_GAIN = 0.3;
    
    /**
     * Maximum drivetrain turn speed for direction adjustment.
     */
    public static final double MAX_DIRECTION_TURN_SPEED = 0.4;
    
    /**
     * Tolerance in degrees for drivetrain direction alignment.
     * Robot is considered aligned when bearing error is less than this value.
     */
    public static final double DIRECTION_TOLERANCE = 5.0;
    
    // ============================================================================
    // MANUAL CONTROL CONFIGURATION
    // ============================================================================
    
    /**
     * Speed reduction factor for manual robot movement.
     * Higher values = slower, more controlled movement.
     */
    public static final double MANUAL_SPEED_REDUCTION = 2.0;
    
    /**
     * Maximum manual movement speed.
     */
    public static final double MAX_MANUAL_SPEED = 0.7;
    
    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    
    /**
     * Clamp servo angle to valid range.
     * 
     * @param angle Angle to clamp in degrees
     * @return Clamped angle within valid range
     */
    public static double clampServoAngle(double angle) {
        return Math.max(SERVO_MIN_ANGLE, Math.min(SERVO_MAX_ANGLE, angle));
    }
    
    /**
     * Clamp launcher power to valid range.
     * 
     * @param power Power to clamp (0.0 to 1.0)
     * @return Clamped power within valid range
     */
    public static double clampLauncherPower(double power) {
        return Math.max(0.0, Math.min(1.0, power));
    }
    
    /**
     * Convert servo angle to servo position (0.0 to 1.0).
     * 
     * @param angleDegrees Servo angle in degrees
     * @return Servo position (0.0 to 1.0)
     */
    public static double angleToServoPosition(double angleDegrees) {
        double clampedAngle = clampServoAngle(angleDegrees);
        return (clampedAngle - SERVO_MIN_ANGLE) / (SERVO_MAX_ANGLE - SERVO_MIN_ANGLE);
    }
    
    /**
     * Convert servo position to angle in degrees.
     * 
     * @param position Servo position (0.0 to 1.0)
     * @return Servo angle in degrees
     */
    public static double servoPositionToAngle(double position) {
        double clampedPosition = Math.max(0.0, Math.min(1.0, position));
        return SERVO_MIN_ANGLE + (clampedPosition * (SERVO_MAX_ANGLE - SERVO_MIN_ANGLE));
    }
    
    /**
     * Check if a servo angle is within valid range.
     * 
     * @param angle Angle to check in degrees
     * @return True if angle is within valid range
     */
    public static boolean isServoAngleValid(double angle) {
        return angle >= SERVO_MIN_ANGLE && angle <= SERVO_MAX_ANGLE;
    }
    
    /**
     * Calculate optimal launcher angle based on target distance and elevation.
     * Uses simplified ballistics calculation for projectile motion.
     * 
     * @param distance Distance to target in meters
     * @param elevation Elevation angle to target in degrees
     * @return Optimal launcher angle in degrees
     */
    public static double calculateOptimalAngle(double distance, double elevation) {
        // Convert elevation to radians
        double elevationRad = Math.toRadians(elevation);
        
        // Calculate height difference
        double heightDiff = TARGET_HEIGHT - LAUNCHER_HEIGHT;
        
        // Calculate horizontal distance
        double horizontalDistance = distance * Math.cos(elevationRad);
        
        // Calculate vertical distance (including elevation)
        double verticalDistance = heightDiff + distance * Math.sin(elevationRad);
        
        // Use quadratic formula to solve for launch angle
        // Simplified trajectory equation: y = x*tan(θ) - (g*x²)/(2*v₀²*cos²(θ))
        // For optimal angle: θ = atan((v₀² ± sqrt(v₀⁴ - g*(g*x² + 2*v₀²*y)))/(g*x))
        
        double v0 = MAX_LAUNCHER_VELOCITY; // Use max velocity for calculation
        double g = GRAVITY;
        double x = horizontalDistance;
        double y = verticalDistance;
        
        // Calculate discriminant
        double discriminant = Math.pow(v0, 4) - g * (g * x * x + 2 * v0 * v0 * y);
        
        if (discriminant < 0) {
            // No real solution, use default angle
            return SERVO_DEFAULT_ANGLE;
        }
        
        // Calculate optimal angle (use the lower angle for better accuracy)
        double angleRad = Math.atan((v0 * v0 - Math.sqrt(discriminant)) / (g * x));
        double angleDeg = Math.toDegrees(angleRad);
        
        // Clamp to valid servo range
        return clampServoAngle(angleDeg);
    }
    
    /**
     * Calculate optimal launcher power based on target distance and elevation.
     * 
     * @param distance Distance to target in meters
     * @param elevation Elevation angle to target in degrees
     * @return Optimal launcher power (0.0 to 1.0)
     */
    public static double calculateOptimalPower(double distance, double elevation) {
        // Convert elevation to radians
        double elevationRad = Math.toRadians(elevation);
        
        // Calculate height difference
        double heightDiff = TARGET_HEIGHT - LAUNCHER_HEIGHT;
        
        // Calculate horizontal distance
        double horizontalDistance = distance * Math.cos(elevationRad);
        
        // Calculate vertical distance (including elevation)
        double verticalDistance = heightDiff + distance * Math.sin(elevationRad);
        
        // Calculate required velocity using energy conservation
        // v₀ = sqrt(g * (x² + y²) / (2 * y))
        double requiredVelocity = Math.sqrt(GRAVITY * (horizontalDistance * horizontalDistance + verticalDistance * verticalDistance) / (2 * Math.abs(verticalDistance)));
        
        // Apply distance-based scaling
        double distanceFactor = 1.0 + (horizontalDistance * DISTANCE_GAIN * 0.1);
        requiredVelocity *= distanceFactor;
        
        // Convert velocity to power
        double power = requiredVelocity * VELOCITY_TO_POWER_SCALE;
        
        // Clamp to valid power range
        return clampLauncherPower(power);
    }
    
    /**
     * Calculate launcher direction based on AprilTag bearing.
     * 
     * @param bearing AprilTag bearing in degrees
     * @return Launcher direction adjustment in degrees
     */
    public static double calculateLauncherDirection(double bearing) {
        // Simple bearing-based direction adjustment
        // Positive bearing = target to the right, adjust launcher right
        return bearing * ELEVATION_GAIN * 0.1;
    }
    
    /**
     * Calculate drivetrain direction adjustment based on AprilTag bearing.
     * 
     * @param bearing AprilTag bearing in degrees
     * @return Drivetrain turn speed (-1.0 to 1.0)
     */
    public static double calculateDrivetrainDirection(double bearing) {
        // Calculate turn speed based on bearing
        // Positive bearing = target to the right, turn right (positive turn speed)
        double turnSpeed = bearing * DRIVETRAIN_DIRECTION_GAIN;
        
        // Clamp to maximum turn speed
        return Math.max(-MAX_DIRECTION_TURN_SPEED, Math.min(MAX_DIRECTION_TURN_SPEED, turnSpeed));
    }
    
    /**
     * Check if drivetrain is aligned to target based on bearing.
     * 
     * @param bearing AprilTag bearing in degrees
     * @return True if aligned within tolerance
     */
    public static boolean isDrivetrainAligned(double bearing) {
        return Math.abs(bearing) < DIRECTION_TOLERANCE;
    }
    
    /**
     * Convert AprilTag range from inches to meters.
     * 
     * @param rangeInches Range in inches
     * @return Range in meters
     */
    public static double inchesToMeters(double rangeInches) {
        return rangeInches * 0.0254;
    }
    
    /**
     * Convert meters to inches.
     * 
     * @param meters Distance in meters
     * @return Distance in inches
     */
    public static double metersToInches(double meters) {
        return meters / 0.0254;
    }
    
    // Private constructor to prevent instantiation
    private BallLauncherConfig() {}
}
