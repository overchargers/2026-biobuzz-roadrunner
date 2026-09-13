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
 * Configuration class for turret tracking system.
 * Contains all tunable parameters for turret control, PID settings, and hardware configuration.
 * 
 * IMPORTANT: Update these values based on your specific robot configuration!
 */
public class TurretConfig {
    
    // ============================================================================
    // HARDWARE CONFIGURATION
    // ============================================================================
    
    /**
     * Name of the turret motor in the robot configuration.
     * Make sure this matches the name you assigned in the Robot Controller app.
     */
    public static final String TURRET_MOTOR_NAME = "turretMotor";
    
    /**
     * Gear ratio between the turret motor and the turret mechanism.
     * Example: If motor turns 3 times for 1 turret rotation, this should be 3.0
     */
    public static final double TURRET_GEAR_RATIO = 1.0;
    
    /**
     * Number of encoder ticks per revolution of the turret motor.
     * For most REV Core Hex motors, this is 28.0
     * For other motors, check the motor specifications.
     */
    public static final double TURRET_ENCODER_TICKS_PER_REVOLUTION = 28.0;
    
    /**
     * Maximum turret rotation angle in degrees (positive direction).
     * Set this to prevent the turret from over-rotating and damaging cables.
     */
    public static final double TURRET_MAX_ANGLE = 180.0;
    
    /**
     * Minimum turret rotation angle in degrees (negative direction).
     * Set this to prevent the turret from over-rotating and damaging cables.
     */
    public static final double TURRET_MIN_ANGLE = -180.0;
    
    // ============================================================================
    // PID CONTROL PARAMETERS
    // ============================================================================
    
    /**
     * Proportional gain for turret PID control.
     * Higher values = faster response, but may cause oscillation.
     * Start with 0.02 and adjust based on testing.
     */
    public static final double TURRET_KP = 0.02;
    
    /**
     * Integral gain for turret PID control.
     * Helps eliminate steady-state error.
     * Start with 0.001 and adjust based on testing.
     */
    public static final double TURRET_KI = 0.001;
    
    /**
     * Derivative gain for turret PID control.
     * Helps reduce overshoot and oscillation.
     * Start with 0.005 and adjust based on testing.
     */
    public static final double TURRET_KD = 0.005;
    
    /**
     * Tolerance in degrees for considering the turret "on target".
     * If the error is less than this value, the turret will stop moving.
     */
    public static final double TURRET_TOLERANCE = 2.0;
    
    // ============================================================================
    // POWER LIMITS
    // ============================================================================
    
    /**
     * Maximum power that can be applied to the turret motor.
     * Prevents the turret from moving too aggressively.
     */
    public static final double TURRET_MAX_POWER = 0.8;
    
    /**
     * Minimum power threshold for turret movement.
     * If the calculated power is below this value, the motor won't move.
     * This prevents the motor from stalling at very low powers.
     */
    public static final double TURRET_MIN_POWER = 0.1;
    
    // ============================================================================
    // TRACKING PARAMETERS
    // ============================================================================
    
    /**
     * Timeout in seconds for losing AprilTag target.
     * If no target is detected for this long, the turret will return to center.
     */
    public static final double TARGET_TIMEOUT = 1.0;
    
    /**
     * Default target AprilTag ID.
     * Set to -1 to track any AprilTag, or set to a specific ID to track only that tag.
     */
    public static final int DEFAULT_TARGET_TAG_ID = -1;
    
    // ============================================================================
    // MOVEMENT PARAMETERS
    // ============================================================================
    
    /**
     * Maximum forward/backward speed for robot movement.
     */
    public static final double MAX_DRIVE_SPEED = 0.7;
    
    /**
     * Maximum left/right strafe speed for robot movement.
     */
    public static final double MAX_STRAFE_SPEED = 0.7;
    
    /**
     * Maximum rotation speed for robot movement.
     */
    public static final double MAX_TURN_SPEED = 0.5;
    
    // ============================================================================
    // CALIBRATION HELPERS
    // ============================================================================
    
    /**
     * Get the total encoder ticks per full turret rotation.
     * This is calculated from the gear ratio and motor encoder ticks.
     */
    public static double getTicksPerTurretRevolution() {
        return TURRET_ENCODER_TICKS_PER_REVOLUTION * TURRET_GEAR_RATIO;
    }
    
    /**
     * Convert encoder ticks to turret angle in degrees.
     * 
     * @param encoderTicks Current encoder position
     * @return Turret angle in degrees
     */
    public static double encoderTicksToAngle(int encoderTicks) {
        double revolutions = encoderTicks / getTicksPerTurretRevolution();
        return (revolutions * 360.0) % 360.0;
    }
    
    /**
     * Convert turret angle in degrees to encoder ticks.
     * 
     * @param angleDegrees Desired turret angle in degrees
     * @return Required encoder position
     */
    public static int angleToEncoderTicks(double angleDegrees) {
        double revolutions = angleDegrees / 360.0;
        return (int) (revolutions * getTicksPerTurretRevolution());
    }
    
    /**
     * Check if a given angle is within the valid turret range.
     * 
     * @param angle Angle to check in degrees
     * @return True if angle is within valid range
     */
    public static boolean isAngleValid(double angle) {
        return angle >= TURRET_MIN_ANGLE && angle <= TURRET_MAX_ANGLE;
    }
    
    /**
     * Clamp an angle to the valid turret range.
     * 
     * @param angle Angle to clamp in degrees
     * @return Clamped angle within valid range
     */
    public static double clampAngle(double angle) {
        while (angle > TURRET_MAX_ANGLE) angle -= 360.0;
        while (angle < TURRET_MIN_ANGLE) angle += 360.0;
        return angle;
    }
    
    // Private constructor to prevent instantiation
    private TurretConfig() {}
}
