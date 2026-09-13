package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * TelemetrySubsystem - Manages telemetry display and formatting
 * 
 * Responsibilities:
 * - Display system status information
 * - Format telemetry for all subsystems
 * - Provide structured status updates
 * - Handle startup and shutdown messages
 * - Support debug and competition display modes
 */
public class TelemetrySubsystem {

    /**
     * Telemetry display modes
     */
    public enum TelemetryMode {
        DEBUG, // Show all detailed information
        COMPETITION // Show only essential information
    }

    // ==================== CONFIGURATION PARAMETERS ====================

    // Status indicators
    private static final String STATUS_ONLINE = "✓ Online";
    private static final String STATUS_OFFLINE = "✗ Offline";
    private static final String STATUS_YES = "✓ YES";
    private static final String STATUS_NO = "✗ NO";
    private static final String STATUS_ALIGNED = "✓";
    private static final String STATUS_NOT_ALIGNED = "↻";

    // Telemetry format strings
    private static final String FORMAT_PERCENTAGE = "%s (%.0f%%)";
    private static final String FORMAT_ANGLE = "%.2f°";
    private static final String FORMAT_DISTANCE = "%.1f inches";
    private static final String FORMAT_BEARING = "%.1f°";

    // ==================== REFERENCES ====================

    private Telemetry telemetry;
    private boolean isInitialized = false;
    private TelemetryMode mode = TelemetryMode.DEBUG; // Default to debug mode

    // ==================== CONSTRUCTOR ====================

    /**
     * Create a TelemetrySubsystem
     * 
     * @param telemetry The telemetry object to use
     */
    public TelemetrySubsystem(Telemetry telemetry) {
        this.telemetry = telemetry;
        this.isInitialized = (telemetry != null);
        this.mode = TelemetryMode.COMPETITION; // Default to debug mode
    }

    /**
     * Create a TelemetrySubsystem with specified mode
     * 
     * @param telemetry The telemetry object to use
     * @param mode      The telemetry display mode
     */
    public TelemetrySubsystem(Telemetry telemetry, TelemetryMode mode) {
        this.telemetry = telemetry;
        this.isInitialized = (telemetry != null);
        this.mode = mode;
    }

    // ==================== MODE MANAGEMENT ====================

    /**
     * Set the telemetry display mode
     * 
     * @param mode The mode to set
     */
    public void setMode(TelemetryMode mode) {
        this.mode = mode;
    }

    /**
     * Get the current telemetry display mode
     * 
     * @return The current mode
     */
    public TelemetryMode getMode() {
        return mode;
    }

    /**
     * Check if currently in debug mode
     * 
     * @return true if in debug mode
     */
    public boolean isDebugMode() {
        return mode == TelemetryMode.DEBUG;
    }

    /**
     * Check if currently in competition mode
     * 
     * @return true if in competition mode
     */
    public boolean isCompetitionMode() {
        return mode == TelemetryMode.COMPETITION;
    }

    // ==================== DISPLAY METHODS ====================

    /**
     * Display startup information
     */
    public void displayStartup(DriveSubsystem drive, VisionSubsystem vision,
            IntakeSubsystem intake, TransferSubsystem transfer,
            OuttakeSubsystem outtake) {
        if (!isInitialized)
            return;

        telemetry.clear();
        telemetry.addData("🎮", "[Season] TeleOp - Modular Subsystems");

        if (isDebugMode()) {
            // Display subsystem status
            displaySubsystemStatus(drive, vision, intake, transfer, outtake);

            // Display controls
            displayControls();
        } else {
            // Competition mode - show only essential info
            displayCompetitionStartup(drive, vision, outtake);
        }

        telemetry.update();
    }

    /**
     * Display system status header
     */
    public void displaySystemStatus(long loopInterval, boolean emergencyStop) {
        if (!isInitialized)
            return;

        if (isDebugMode()) {
            telemetry.addData("═══ SYSTEM STATUS ═══", "");

            // Loop timing
            double loopFrequency = loopInterval > 0 ? 1000.0 / loopInterval : 0;
            telemetry.addData("⏱️ Loop Time", "%d ms (%.1f Hz)", loopInterval, loopFrequency);
        }

        if (emergencyStop) {
            telemetry.addData("⚠️ EMERGENCY STOP", "ACTIVE - Press Y to resume");
            telemetry.addLine();
        }
    }

    /**
     * Display drive status
     */
    public void displayDriveStatus(double driveSpeed,
            VisionSubsystem vision) {
        if (!isInitialized)
            return;

        if (isDebugMode()) {
            telemetry.addData("🚗 Drive Mode", "Manual Control");
            telemetry.addData("Drive Speed", "%.0f%%", driveSpeed * 100);

            telemetry.addLine();
        } else {
            // Competition mode - show only essential alignment info
            if (vision != null && vision.isInitialized()) {
               
            } else {
               
            }
        }
    }

    /**
     * Display AprilTag detection status
     */
    public void Status(VisionSubsystem vision) {
        if (!isInitialized)
            return;

        if (isDebugMode()) {
            telemetry.addData("═══ APRILTAG DETECTION ═══", "");

            if (vision != null && vision.isInitialized()) {
                if (vision.hasTarget()) {
                    telemetry.addData("🎯 AprilTag", "ID %d - %s", vision.getTargetId(), vision.getTargetName());
                    telemetry.addData("  X (Lateral)", FORMAT_DISTANCE, vision.getTargetX());
                    telemetry.addData("  Y (Forward)", FORMAT_DISTANCE, vision.getTargetY());
                    telemetry.addData("  Z (Vertical)", FORMAT_DISTANCE, vision.getTargetZ());
                    telemetry.addData("  Range", FORMAT_DISTANCE, vision.getTargetRange());
                    telemetry.addData("  Bearing", FORMAT_BEARING, vision.getTargetBearing());
                    telemetry.addData("  Elevation", FORMAT_BEARING, vision.getTargetElevation());
                    telemetry.addData("  Yaw", FORMAT_BEARING, vision.getTargetYaw());
                } else {
                    telemetry.addData("🎯 AprilTag", "No targets detected");
                }
            } else {
                telemetry.addData("🎯 AprilTag", "N/A - Not initialized");
            }

            telemetry.addLine();
        } else {
            // Competition mode - show only essential vision info
            if (vision != null && vision.isInitialized() && vision.hasTarget()) {
                telemetry.addData("🎯 Target", "ID %d %.1f°", vision.getTargetId(), vision.getTargetBearing());
            }
        }
    }

    /**
     * Display all subsystem status
     */
    public void displaySubsystemStatus(DriveSubsystem drive, VisionSubsystem vision,
            IntakeSubsystem intake, TransferSubsystem transfer,
            OuttakeSubsystem outtake) {
        if (!isInitialized)
            return;

        if (isDebugMode()) {
            telemetry.addData("═══ SUBSYSTEM STATUS ═══", "");
            telemetry.addData("Drive", getStatus(drive));
            telemetry.addData("Vision", getStatus(vision));
            telemetry.addData("Intake", getStatus(intake));
            telemetry.addData("Transfer", getStatus(transfer));
            telemetry.addData("Outtake", getStatus(outtake));

            telemetry.addLine();
        }
        // Competition mode doesn't show subsystem status
    }

    /**
     * Display controls reminder
     */
    public void displayControls() {
        if (!isInitialized)
            return;

        if (isDebugMode()) {
            telemetry.addData("═══ CONTROLS ═══", "");
            telemetry.addData("Sticks", "L: Drive | R-X: Turn");
            //more contolllllsss
        }
        // Competition mode doesn't show controls
    }

    /**
     * Display shutdown message
     */
    public void displayShutdown() {
        if (!isInitialized)
            return;

        telemetry.addData("Status", "Shutdown complete");
        telemetry.update();
    }

    /**
     * Display competition mode startup (simplified)
     */
    private void displayCompetitionStartup(DriveSubsystem drive,
            VisionSubsystem vision, OuttakeSubsystem outtake) {
        if (!isInitialized)
            return;

        // Show only essential status
        if (drive != null && drive.isInitialized()) {
            telemetry.addData("Drive", "✓ Ready");
        }
        if (vision != null && vision.isInitialized()) {
            telemetry.addData("Vision", "✓ Ready");
        }
        if (outtake != null) {
            telemetry.addData("Launcher", "✓ Ready");
        }

        telemetry.addData("Mode", mode);
    }

    /**
     * Update the telemetry display
     */
    public void update() {
        if (isInitialized) {
            telemetry.update();
        }
    }

    /**
     * Clear the telemetry display
     */
    public void clear() {
        if (isInitialized) {
            telemetry.clear();
        }
    }


    // ==================== HELPER METHODS ====================

    /**
     * Get status string for a subsystem
     */
    private String getStatus(Object subsystem) {
        if (subsystem == null)
            return STATUS_OFFLINE;

        try {
            // Use reflection to call isInitialized() if it exists
            java.lang.reflect.Method method = subsystem.getClass().getMethod("isInitialized");
            Boolean initialized = (Boolean) method.invoke(subsystem);
            return initialized ? STATUS_ONLINE : STATUS_OFFLINE;
        } catch (Exception e) {
            return STATUS_OFFLINE;
        }
    }
    /**
     * Check if telemetry subsystem is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
