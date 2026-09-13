package org.firstinspires.ftc.teamcode.subsystems;

import android.os.Build;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import java.util.HashMap;
import java.util.Map;

/**
 * AlignmentController - Handles automatic alignment using a PD controller
 * 
 * This controller encapsulates the logic for automatically aligning the robot
 * with a target using a Proportional-Derivative (PD) control algorithm.
 * 
 * FEATURES:
 * - PD controller for smooth, damped alignment
 * - Acceleration limiting for smoother motion
 * - Configurable deadband to prevent oscillation
 * - Minimum power threshold to overcome static friction
 * - AprilTag detection status integration
 * - Target loss detection with frame thresholding
 * - Tunable via FTC Dashboard (@Config)
 * - Reusable across TeleOp and Autonomous modes
 * 
 * USAGE:
 * 1. Initialize with init(hardwareMap)
 * 2. Call calculate(bearingError, aprilTagDetected) each loop to get turn
 * correction
 * 3. Use isAligned(), isInDeadband(), and isTargetDetected() for status checks
 * 4. Call reset() when alignment is not active
 * 
 * APRILTAG INTEGRATION:
 * - Pass AprilTag detection status to calculate() method
 * - Controller returns zero correction when no target is detected
 * - Uses frame thresholding to prevent false target loss from momentary
 * detection gaps
 * - All alignment status methods consider target detection status
 * 
 * PD CONTROLLER EXPLANATION:
 * - P (Proportional): Responds to current error magnitude
 * - D (Derivative): Responds to rate of error change (prevents overshoot)
 * - Acceleration Limit: Prevents sudden changes in turn power
 * - Output: turnCorrection = accelLimit(clip((Kp * error) + (Kd * derivative)))
 * 
 */
@Config
public class AlignmentController {

    // CONFIGURATION

    public static class Params {
        /**
         * Proportional gain (Kp)
         * Higher values = stronger response to error
         * Too high = oscillation/overshoot
         * Too low = slow, weak response
         */
        public double kp = 0.008; // Reduced from 0.012 for smoother response

        /**
         * Derivative gain (Kd)
         * Higher values = stronger damping (prevents overshoot)
         * Too high = sluggish response
         * Too low = oscillation
         */
        public double kd = 0.035; // Increased from 0.025 for better damping

        /**
         * Deadband threshold (degrees)
         * Within this range, no correction is applied
         * Prevents oscillation around target
         */
        public double deadband = 3.5; // Increased from 3.5 for more stability

        /**
         * Maximum turn power
         * Limits output to prevent aggressive movement
         */
        public double maxTurnPower = 0.25;

        /**
         * Minimum turn power
         * Overcomes static friction
         * Below this, motors won't move
         */
        public double minTurnPower = 0.05;

        /**
         * Maximum acceleration (power units per second)
         * Limits how quickly the turn power can change
         * Provides smoother, more controlled movements
         */
        public double maxAcceleration = 2.0;

        /**
         * Maximum valid time delta (seconds)
         * Prevents derivative spikes from long pauses
         */
        public double maxDeltaTime = 0.5;

        /**
         * Minimum valid time delta (seconds)
         * Prevents divide-by-zero and noisy derivatives
         */
        public double minDeltaTime = 0.001;

        /**
         * Derivative filter coefficient (0.0 to 1.0)
         * Higher values = more filtering (smoother but slower response)
         * Lower values = less filtering (more responsive but noisier)
         */
        public double derivativeFilterAlpha = 0.7;
    }

    public static Params PARAMS = new Params();

    // STATE VARIABLES

    // PD controller state
    private double lastError = 0.0;
    private long lastUpdateTime = 0;
    private double lastTurnCorrection = 0.0;
    private double filteredDerivative = 0.0; // For derivative filtering

    // Status tracking
    private boolean isActive = false;
    private int consecutiveAlignedFrames = 0;
    private static final int ALIGNED_FRAME_THRESHOLD = 3; // Must be aligned for 3+ frames

    // Detection status tracking
    private boolean targetDetected = true; // Assume target is available by default
    private int consecutiveNoDetectionFrames = 0;
    private static final int NO_DETECTION_FRAME_THRESHOLD = 5; // Must be no detection for 5+ frames to consider lost

    // INITIALIZATION

    /**
     * Initialize the alignment subsystem
     * 
     * @param hardwareMap Hardware map
     */
    public void init(HardwareMap hardwareMap) {
        reset();
        // Log initialization
        DataLogger.logInfo(CONTROLLER_NAME, "Initialized with PD parameters", DataLogger.DataType.STATUS);
        logPDParameters();
    }

    /**
     * Check if subsystem is initialized
     */


    //  CORE CONTROL

    /**
     * Calculate turn correction using PD controller
     * Call this every loop iteration when alignment is needed
     * 
     * @param bearingError     Current bearing error in degrees (+ = right, - =
     *                         left)
     * @param aprilTagDetected Whether an AprilTag is currently detected
     * @return Turn correction power (-MAX_TURN_POWER to +MAX_TURN_POWER)
     */
    public double calculate(double bearingError, boolean aprilTagDetected) {

        // Update target detection status with frame thresholding
        updateTargetDetectionStatus(aprilTagDetected);

        // If no target detected, reset and return zero correction
        if (!targetDetected) {
            isActive = false;
            return 0.0;
        }

        isActive = true;
        long currentTime = System.nanoTime();

        // Check if within deadband - no correction needed
        if (Math.abs(bearingError) <= PARAMS.deadband) {
            // Update state but don't apply correction
            lastError = bearingError;
            lastUpdateTime = currentTime;
            lastTurnCorrection = 0.0;

            // Track consecutive aligned frames
            if (Math.abs(bearingError) <= PARAMS.deadband) {
                consecutiveAlignedFrames++;
            }

            return 0.0;
        }

        // Outside deadband - calculate PD correction

        // Calculate time delta
        double deltaTime = (currentTime - lastUpdateTime) / 1e9; // Convert to seconds

        // Calculate derivative (rate of change of error) with filtering
        double derivative = 0.0;
        if (lastUpdateTime != 0 && deltaTime >= PARAMS.minDeltaTime && deltaTime <= PARAMS.maxDeltaTime) {
            // Positive derivative = error increasing, negative = error decreasing
            double rawDerivative = (bearingError - lastError) / deltaTime;

            // Apply exponential moving average filter to reduce noise
            filteredDerivative = (PARAMS.derivativeFilterAlpha * rawDerivative) +
                    ((1.0 - PARAMS.derivativeFilterAlpha) * filteredDerivative);
            derivative = filteredDerivative;
        }

        // PD Controller Formula
        double pTerm = PARAMS.kp * bearingError;
        double dTerm = PARAMS.kd * derivative;
        double rawCorrection = pTerm + dTerm;

        // Limit to maximum power (before acceleration limiting)
        rawCorrection = Range.clip(rawCorrection, -PARAMS.maxTurnPower, PARAMS.maxTurnPower);

        // Apply acceleration limiting for smoother motion
        double turnCorrection = rawCorrection;
        if (lastUpdateTime != 0 && deltaTime >= PARAMS.minDeltaTime && deltaTime <= PARAMS.maxDeltaTime) {
            // Calculate maximum allowed change based on time delta
            double maxDelta = PARAMS.maxAcceleration * deltaTime;

            // Limit the correction to not exceed max acceleration
            turnCorrection = Range.clip(
                    rawCorrection,
                    lastTurnCorrection - maxDelta,
                    lastTurnCorrection + maxDelta);
        }

        // Apply minimum threshold to overcome static friction
        if (Math.abs(turnCorrection) > 0 && Math.abs(turnCorrection) < PARAMS.minTurnPower) {
            //COME BACK AND DELETE IF NOT WORKING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                turnCorrection = Math.copySign(PARAMS.minTurnPower, turnCorrection);
            }
        }

        // Update state for next iteration
        lastError = bearingError;
        lastUpdateTime = currentTime;
        lastTurnCorrection = turnCorrection;

        // Reset aligned frame counter when actively correcting
        consecutiveAlignedFrames = 0;

        // Log PD controller data for tuning
        logPDControllerData(bearingError, derivative, pTerm, dTerm, rawCorrection, turnCorrection, deltaTime);

        return turnCorrection;
    }

    /**
     * Update target detection status with frame thresholding
     * 
     * @param currentlyDetected Whether target is currently detected
     */
    private void updateTargetDetectionStatus(boolean currentlyDetected) {
        if (currentlyDetected) {
            targetDetected = true;
            consecutiveNoDetectionFrames = 0;
        } else {
            consecutiveNoDetectionFrames++;
            // Only consider target lost after multiple consecutive frames without detection
            if (consecutiveNoDetectionFrames >= NO_DETECTION_FRAME_THRESHOLD) {
                targetDetected = false;
            }
        }
    }

    /**
     * Reset the PD controller state
     * Call this when alignment is not active or when switching targets
     */
    public void reset() {
        lastError = 0.0;
        lastUpdateTime = 0;
        lastTurnCorrection = 0.0;
        filteredDerivative = 0.0; // Reset filtered derivative
        isActive = false;
        consecutiveAlignedFrames = 0;
        consecutiveNoDetectionFrames = 0;
        targetDetected = true; // Reset to assume target is available
    }

    //  STATUS QUERIES

    /**
     * Check if currently within alignment tolerance
     * 
     * @param currentError Current bearing error in degrees
     * @return true if aligned within TOLERANCE and target is detected
     */
    public boolean isAligned(double currentError) {
        return targetDetected && Math.abs(currentError) <= PARAMS.deadband;
    }

    /**
     * Check if currently within deadband (stable)
     * 
     * @param currentError Current bearing error in degrees
     * @return true if within DEADBAND and target is detected
     */


    /**
     * Get the current alignment status as a descriptive string
     * 
     * @param currentError Current bearing error in degrees
     * @return Status string for telemetry
     */
    public String getStatus(double currentError) {
        if (!isActive) {
            return "Inactive";
        }
        if (!targetDetected) {
            return "No Target Detected";
        }
        if (isAligned(currentError)) {
            return "Aligned";
        }
        return "Correcting";
    }

    /**
     * Get the current alignment status as a descriptive string with current
     * detection status
     * 
     * @param currentError     Current bearing error in degrees
     * @param aprilTagDetected Current AprilTag detection status
     * @return Status string for telemetry
     */
    public String getStatus(double currentError, boolean aprilTagDetected) {
        // Check current detection status first, before checking isActive
        if (!aprilTagDetected) {
            return "No Target Detected";
        }
        if (!isActive) {
            return "Inactive";
        }
        if (!targetDetected) {
            return "Target Lost";
        }
        if (isAligned(currentError)) {
            return "Aligned";
        }
        return "Correcting";
    }

    /**
     * Check if the alignment system is currently active
     */

    public double getLastTurnCorrection() {
        return lastTurnCorrection;
    }

    /**
     * Get last error value (for debugging)
     */
    public double getLastError() {
        return lastError;
    }

    /**
     * Get last update time (for debugging)
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    //  LOGGING METHODS

    // Constants for logging
    private static final String CONTROLLER_TAG = "alignment";
    private static final String CONTROLLER_NAME = "AlignmentController";
    private static final String CONSECUTIVE_ALIGNED_FRAMES_FIELD = "consecutive_aligned_frames";
    private static final String TARGET_DETECTED_TAG = "target_detected";

    /**
     * Log PD controller parameters for tuning
     */
    private void logPDParameters() {

        Map<String, Object> fields = new HashMap<>();
        fields.put("kp", PARAMS.kp);
        fields.put("kd", PARAMS.kd);
        fields.put("deadband", PARAMS.deadband);
        fields.put("max_turn_power", PARAMS.maxTurnPower);
        fields.put("min_turn_power", PARAMS.minTurnPower);
        fields.put("max_acceleration", PARAMS.maxAcceleration);
        fields.put("derivative_filter_alpha", PARAMS.derivativeFilterAlpha);
        fields.put("no_detection_threshold", NO_DETECTION_FRAME_THRESHOLD);

        Map<String, String> tags = new HashMap<>();
        tags.put("controller", CONTROLLER_TAG);
        tags.put("type", "parameters");

        DataLogger.logCustom(CONTROLLER_NAME, "pd_parameters", fields, tags, DataLogger.LogLevel.INFO);
    }

    /**
     * Log PD controller data for tuning analysis
     */
    private void logPDControllerData(double error, double derivative, double pTerm, double dTerm, 
                                   double rawCorrection, double finalCorrection, double deltaTime) {

        Map<String, Object> fields = new HashMap<>();
        fields.put("error", error);
        fields.put("derivative", derivative);
        fields.put("p_term", pTerm);
        fields.put("d_term", dTerm);
        fields.put("raw_correction", rawCorrection);
        fields.put("final_correction", finalCorrection);
        fields.put("delta_time", deltaTime);
        fields.put("filtered_derivative", filteredDerivative);
        fields.put(CONSECUTIVE_ALIGNED_FRAMES_FIELD, consecutiveAlignedFrames);
        fields.put("consecutive_no_detection_frames", consecutiveNoDetectionFrames);

        Map<String, String> tags = new HashMap<>();
        tags.put("controller", CONTROLLER_TAG);
        tags.put("type", "pd_data");
        tags.put(TARGET_DETECTED_TAG, String.valueOf(targetDetected));
        tags.put("is_active", String.valueOf(isActive));

        DataLogger.logCustom(CONTROLLER_NAME, "pd_controller_data", fields, tags, DataLogger.LogLevel.DEBUG);
    }
}
