package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import java.util.HashMap;
import java.util.Map;

/**
 * DriveSubsystem - Manages robot drive system
 * 
 * Responsibilities:
 * - Control four drive motors (mecanum/omni configuration)
 * - Provide mecanum drive kinematics
 * - Support field-centric and robot-centric modes
 * - Handle motor configuration and power normalization
 */
public class DriveSubsystem {

    // ==================== CONFIGURATION PARAMETERS ====================

    // Motor hardware names
    public static final String FRONT_LEFT_NAME = "frontLeft";
    public static final String FRONT_RIGHT_NAME = "frontRight";
    public static final String BACK_LEFT_NAME = "backLeft";
    public static final String BACK_RIGHT_NAME = "backRight";

    // Motor directions
    public static final DcMotor.Direction FRONT_LEFT_DIRECTION = DcMotor.Direction.REVERSE;
    public static final DcMotor.Direction FRONT_RIGHT_DIRECTION = DcMotor.Direction.FORWARD;
    public static final DcMotor.Direction BACK_LEFT_DIRECTION = DcMotor.Direction.REVERSE;
    public static final DcMotor.Direction BACK_RIGHT_DIRECTION = DcMotor.Direction.FORWARD;

    // Drive modes
    public static final DcMotor.RunMode RUN_MODE = DcMotor.RunMode.RUN_WITHOUT_ENCODER;
    public static final DcMotor.ZeroPowerBehavior ZERO_POWER_BEHAVIOR = DcMotor.ZeroPowerBehavior.BRAKE;

    // Speed constraints
    public static final double MAX_DRIVE_SPEED = 1.0;
    public static final double TURN_SPEED_MULTIPLIER = 0.8;

    // ==================== HARDWARE COMPONENTS ====================

    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    private boolean isInitialized = false;

    // ==================== STATE VARIABLES ====================

    private double currentFrontLeftPower = 0.0;
    private double currentFrontRightPower = 0.0;
    private double currentBackLeftPower = 0.0;
    private double currentBackRightPower = 0.0;
    
    // Logging configuration
    private static final String SUBSYSTEM_NAME = "DriveSubsystem";
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 100; // Log every 100ms

    // ==================== CONSTRUCTOR ====================

    /**
     * Create a DriveSubsystem with default parameters
     */
    public DriveSubsystem() {
        // Use default values
    }

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the drive subsystem
     * 
     * @param hardwareMap The robot's hardware map
     * @return true if initialization successful
     */
    public boolean init(HardwareMap hardwareMap) {
        try {
            // Initialize motors
            frontLeftDrive = hardwareMap.get(DcMotor.class, FRONT_LEFT_NAME);
            frontRightDrive = hardwareMap.get(DcMotor.class, FRONT_RIGHT_NAME);
            backLeftDrive = hardwareMap.get(DcMotor.class, BACK_LEFT_NAME);
            backRightDrive = hardwareMap.get(DcMotor.class, BACK_RIGHT_NAME);

            // Set directions
            frontLeftDrive.setDirection(FRONT_LEFT_DIRECTION);
            frontRightDrive.setDirection(FRONT_RIGHT_DIRECTION);
            backLeftDrive.setDirection(BACK_LEFT_DIRECTION);
            backRightDrive.setDirection(BACK_RIGHT_DIRECTION);

            // Set run mode
            frontLeftDrive.setMode(RUN_MODE);
            frontRightDrive.setMode(RUN_MODE);
            backLeftDrive.setMode(RUN_MODE);
            backRightDrive.setMode(RUN_MODE);

            // Set zero power behavior
            frontLeftDrive.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
            frontRightDrive.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
            backLeftDrive.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
            backRightDrive.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);

            // Initialize all motors to zero power
            stop();

            isInitialized = true;
            
            return true;

        } catch (Exception e) {
            isInitialized = false;
            DataLogger.logError(SUBSYSTEM_NAME, "Failed to initialize drive subsystem", e);
            return false;
        }
    }

    // ==================== CONTROL METHODS ====================

    /**
     * Drive using mecanum kinematics
     * 
     * @param forward Forward/backward movement (positive = forward, negative =
     *                backward)
     * @param strafe  Left/right movement (positive = right, negative = left)
     * @param turn    Rotation (positive = clockwise, negative = counter-clockwise)
     */
    public void drive(double forward, double strafe, double turn) {
        if (!isInitialized)
            return;

        // Apply speed constraints
        forward *= MAX_DRIVE_SPEED;
        strafe *= MAX_DRIVE_SPEED;
        turn *= MAX_DRIVE_SPEED * TURN_SPEED_MULTIPLIER;

        // Calculate mecanum drive motor powers
        double frontLeftPower = forward - strafe - turn;
        double frontRightPower = forward + strafe + turn;
        double backLeftPower = forward + strafe - turn;
        double backRightPower = forward - strafe + turn;

        // Normalize powers to ensure they're within [-1.0, 1.0]
        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));

        if (max > 1.0) {
            frontLeftPower /= max;
            frontRightPower /= max;
            backLeftPower /= max;
            backRightPower /= max;
        }

        // Set motor powers
        setMotorPowers(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
        
        // Log drive data periodically
        logDriveData(forward, strafe, turn, frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    }

    /**
     * Set motor powers directly (with normalization)
     */
    private void setMotorPowers(double frontLeft, double frontRight, double backLeft, double backRight) {
        if (!isInitialized)
            return;

        currentFrontLeftPower = frontLeft;
        currentFrontRightPower = frontRight;
        currentBackLeftPower = backLeft;
        currentBackRightPower = backRight;

        if (frontLeftDrive != null)
            frontLeftDrive.setPower(frontLeft);
        if (frontRightDrive != null)
            frontRightDrive.setPower(frontRight);
        if (backLeftDrive != null)
            backLeftDrive.setPower(backLeft);
        if (backRightDrive != null)
            backRightDrive.setPower(backRight);
    }

    /**
     * Stop all drive motors
     */
    public void stop() {
        setMotorPowers(0.0, 0.0, 0.0, 0.0);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if drive system is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get front left motor power
     */
    public double getFrontLeftPower() {
        return currentFrontLeftPower;
    }

    /**
     * Get front right motor power
     */
    public double getFrontRightPower() {
        return currentFrontRightPower;
    }

    /**
     * Get back left motor power
     */
    public double getBackLeftPower() {
        return currentBackLeftPower;
    }

    /**
     * Get back right motor power
     */
    public double getBackRightPower() {
        return currentBackRightPower;
    }

    /**
     * Get front left motor position (encoder ticks)
     */
    public int getFrontLeftPosition() {
        return (frontLeftDrive != null) ? frontLeftDrive.getCurrentPosition() : 0;
    }

    /**
     * Get front right motor position (encoder ticks)
     */
    public int getFrontRightPosition() {
        return (frontRightDrive != null) ? frontRightDrive.getCurrentPosition() : 0;
    }

    /**
     * Get back left motor position (encoder ticks)
     */
    public int getBackLeftPosition() {
        return (backLeftDrive != null) ? backLeftDrive.getCurrentPosition() : 0;
    }

    /**
     * Get back right motor position (encoder ticks)
     */
    public int getBackRightPosition() {
        return (backRightDrive != null) ? backRightDrive.getCurrentPosition() : 0;
    }

    /**
     * Check if robot is moving
     */
    public boolean isMoving() {
        double threshold = 0.01;
        return Math.abs(currentFrontLeftPower) > threshold ||
                Math.abs(currentFrontRightPower) > threshold ||
                Math.abs(currentBackLeftPower) > threshold ||
                Math.abs(currentBackRightPower) > threshold;
    }

    /**
     * Get drive status as string
     */
    public String getStatus() {
        if (!isInitialized)
            return "NOT INITIALIZED";
        if (isMoving())
            return "MOVING";
        return "STOPPED";
    }

    // ==================== ENCODER METHODS ====================

    /**
     * Reset all motor encoders
     */
    public void resetEncoders() {
        if (!isInitialized)
            return;

        if (frontLeftDrive != null)
            frontLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (frontRightDrive != null)
            frontRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (backLeftDrive != null)
            backLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if (backRightDrive != null)
            backRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Restore run mode
        if (frontLeftDrive != null)
            frontLeftDrive.setMode(RUN_MODE);
        if (frontRightDrive != null)
            frontRightDrive.setMode(RUN_MODE);
        if (backLeftDrive != null)
            backLeftDrive.setMode(RUN_MODE);
        if (backRightDrive != null)
            backRightDrive.setMode(RUN_MODE);
    }
    
}
