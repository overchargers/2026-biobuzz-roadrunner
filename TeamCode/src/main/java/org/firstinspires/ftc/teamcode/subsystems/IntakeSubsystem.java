package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

/**
 * IntakeSubsystem - Manages ball intake mechanism
 * 
 * Responsibilities:
 * - Control intake servo motor
 * - Provide intake/reverse/stop functionality
 * - Monitor intake power and status
 */
public class IntakeSubsystem {

    // ==================== CONFIGURATION PARAMETERS ====================

    public static final double MAX_INTAKE_SPEED = 1.0;
    public static final DcMotor.Direction MOTOR_DIRECTION = DcMotor.Direction.REVERSE;
    public static final String HARDWARE_NAME = "intakeMotor";

    // ==================== HARDWARE COMPONENTS ====================

    private DcMotor intakeMotor = null;
    private boolean isInitialized = false;

    // ==================== STATE VARIABLES ====================

    private double currentPower = 0.0;
    private boolean isRunning = false;

    // ==================== CONSTRUCTOR ====================

    /**
     * Create an IntakeSubsystem with default parameters
     */
    public IntakeSubsystem() {
        // Use default values

    }

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the intake subsystem
     * 
     * @param hardwareMap The robot's hardware map
     * @return true if initialization successful
     */
    public boolean init(HardwareMap hardwareMap) {
        try {
            intakeMotor = hardwareMap.get(DcMotor.class, HARDWARE_NAME);
            intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            intakeMotor.setDirection(MOTOR_DIRECTION);
            intakeMotor.setPower(0.0);

            isInitialized = true;
            return true;

        } catch (Exception e) {
            isInitialized = false;
            return false;
        }
    }

    // ==================== CONTROL METHODS ====================

    /**
     * Start intake at specified power
     * 
     * @param power Power level (0.0 to 1.0, will be scaled to maxIntakeSpeed)
     */
    public void start(double power) {
        if (!isInitialized || intakeMotor == null)
            return;

        power = Range.clip(power, 0.0, 1.0);
        currentPower = power * MAX_INTAKE_SPEED;
        intakeMotor.setPower(currentPower);
        isRunning = currentPower > 0.05;
    }

    /**
     * Start intake at maximum speed
     */
    public void start() {
        start(1.0);
    }

    /**
     * Reverse intake at specified power
     * 
     * @param power Power level (0.0 to 1.0, will be scaled to maxIntakeSpeed)
     */
    public void reverse(double power) {
        if (!isInitialized || intakeMotor == null)
            return;

        power = Range.clip(power, 0.0, 1.0);
        currentPower = -power * MAX_INTAKE_SPEED;
        intakeMotor.setPower(currentPower);
        isRunning = Math.abs(currentPower) > 0.05;
    }

    /**
     * Reverse intake at maximum speed
     */
    public void reverse() {
        reverse(1.0);
    }

    /**
     * Stop the intake
     */
    public void stop() {
        if (!isInitialized || intakeMotor == null)
            return;

        currentPower = 0.0;
        intakeMotor.setPower(0.0);
        isRunning = false;
    }

    /**
     * Set intake power directly (-1.0 to 1.0)
     * Negative = reverse, Positive = intake
     * 
     * @param power Power level (-1.0 to 1.0)
     */
    public void setPower(double power) {
        if (!isInitialized || intakeMotor == null)
            return;

        power = Range.clip(power, -1.0, 1.0);
        currentPower = power * MAX_INTAKE_SPEED;
        intakeMotor.setPower(currentPower);
        isRunning = Math.abs(currentPower) > 0.05;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if intake is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get current intake power
     */
    public double getPower() {
        return currentPower;
    }

    /**
     * Check if intake is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get intake status as a string
     */
    public String getStatus() {
        if (!isInitialized)
            return "NOT INITIALIZED";
        if (currentPower > 0.05)
            return "RUNNING";
        return "Running but seems to be dead. :(";
    }
}
