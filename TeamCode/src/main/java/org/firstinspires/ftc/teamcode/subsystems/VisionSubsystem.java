package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.FocusControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * VisionSubsystem - Manages AprilTag detection and camera control
 * 
 * Responsibilities:
 * - Initialize camera and AprilTag processor
 * - Configure camera settings (exposure, gain, focus)
 * - Detect and track AprilTag targets
 * - Provide target information for aiming and alignment
 */
public class VisionSubsystem {

    // ==================== CONFIGURATION PARAMETERS ====================

    public static class Params {
        // Camera mounting
        public double CAMERA_TILT_ANGLE_DEGREES = 20.0; // Camera tilted on ramp
        public double CAMERA_TILT_ANGLE_RADIANS = Math.toRadians(CAMERA_TILT_ANGLE_DEGREES);

        // Camera settings
        public int CAMERA_WIDTH = 1280;
        public int CAMERA_HEIGHT = 720;
        public int EXPOSURE_MS = 18;
        public int GAIN = 250;
        public int FOCUS_LENGTH = 25; // For medium distance AprilTag detection

        // Vision Processing Configuration
        public boolean DRAW_AXES = true;
        public boolean DRAW_CUBE_PROJECTION = true;
        public boolean DRAW_TAG_OUTLINE = true;

        // AprilTag settings
        private int desiredTagId = -1; // -1 = any tag
        public double LENS_INTRINSIC_FX = 622.001;
        public double LENS_INTRINSIC_FY = 622.001;
        public double LENS_INTRINSIC_CX = 319.803;
        public double LENS_INTRINSIC_CY = 241.251;
        public int DECIMATION = 3; // Increased decimation for better stability during vibration

        // Temporal filtering for vibration resistance
        // Since servos are always running, we need more aggressive filtering
        public int STABILITY_FRAMES_REQUIRED = 5; // Increased from 3 to 5 for better stability with constant vibration
        public double MAX_DETECTION_CHANGE_BEARING = 8.0; // Increased tolerance for bearing changes (degrees) due to vibration
        public double MAX_DETECTION_CHANGE_RANGE = 10.0; // Increased tolerance for range changes (inches) due to vibration
        public boolean ENABLE_TEMPORAL_FILTERING = true; // Enable/disable temporal filtering

        // Motion-based update skipping
        public double MAX_MOTION_POWER_FOR_VISION = 0.15; // Skip vision updates if any motor power exceeds this

        // Servo pause/resume for detection when shooting
        public boolean PAUSE_SERVOS_WHEN_SHOOTING = true; // Pause servos when shooting is requested for clean detection
        public long SERVO_PAUSE_SETTLE_TIME_MS = 150; // Time to wait after stopping servos before detecting (ms)
        public long SERVO_RESUME_AFTER_DETECTION_MS = 100; // Wait time after detection before resuming servos (ms)
    }

    public static VisionSubsystem.Params PARAMS = new VisionSubsystem.Params();

    // ==================== HARDWARE COMPONENTS ====================

    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTagProcessor;
    private AprilTagDetection currentTarget = null;
    private boolean isInitialized = false;

    // ==================== TEMPORAL FILTERING STATE ====================

    // Track detection history for stability filtering
    private AprilTagDetection[] detectionHistory;
    private int detectionHistoryIndex = 0;
    private int stableDetectionCount = 0;
    private AprilTagDetection lastStableTarget = null;

    // ==================== SERVO PAUSE/RESUME STATE ====================

    // Track servo pause/resume for clean detection when shooting
    private boolean servosPaused = false;
    private boolean shootingRequested = false;
    private long servoPauseStartTime = 0;
    private boolean targetDetectedAfterPause = false;

    // Camera configuration state machine (non-blocking)
    private enum CameraConfigState {
        WAITING_FOR_STREAMING,
        SETTING_EXPOSURE_MODE,
        SETTING_EXPOSURE_VALUE,
        SETTING_GAIN,
        SETTING_FOCUS_MODE,
        SETTING_FOCUS_VALUE,
        VERIFYING_FOCUS,
        COMPLETE
    }
    private CameraConfigState cameraConfigState = CameraConfigState.WAITING_FOR_STREAMING;
    private long cameraConfigLastActionTime = 0;

    // ==================== CONSTRUCTOR ====================

    /**
     * Create a VisionSubsystem with default parameters
     */
    public VisionSubsystem() {
        // Initialize detection history for temporal filtering
        detectionHistory = new AprilTagDetection[PARAMS.STABILITY_FRAMES_REQUIRED];
    }

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the vision system with camera and AprilTag processor
     * 
     * @param hardwareMap The robot's hardware map
     * @return true if initialization successful
     */
    public boolean init(HardwareMap hardwareMap) {
        try {
            // Create AprilTag processor
            aprilTagProcessor = new AprilTagProcessor.Builder()
                    .setDrawAxes(PARAMS.DRAW_AXES)
                    .setDrawCubeProjection(PARAMS.DRAW_CUBE_PROJECTION)
                    .setDrawTagOutline(PARAMS.DRAW_TAG_OUTLINE)
                    .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                    .setLensIntrinsics(PARAMS.LENS_INTRINSIC_FX, PARAMS.LENS_INTRINSIC_FY, PARAMS.LENS_INTRINSIC_CX, PARAMS.LENS_INTRINSIC_CY)
                    .build();
            aprilTagProcessor.setDecimation(PARAMS.DECIMATION);

            // Create vision portal with webcam
            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                    .setCameraResolution(new Size(PARAMS.CAMERA_WIDTH, PARAMS.CAMERA_HEIGHT))
                    .setStreamFormat(VisionPortal.StreamFormat.MJPEG)
                    .addProcessor(aprilTagProcessor)
                    .build();

            // Camera configuration will happen asynchronously in update()
            // Start the configuration state machine
            cameraConfigState = CameraConfigState.WAITING_FOR_STREAMING;
            cameraConfigLastActionTime = System.currentTimeMillis();

            isInitialized = true;
            return true;

        } catch (Exception e) {
            isInitialized = false;
            return false;
        }
    }

    /**
     * Configure camera exposure, gain, and focus for optimal AprilTag detection
     */
    private boolean configureCamera() {
        if (visionPortal == null || cameraConfigState == CameraConfigState.COMPLETE) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAction = currentTime - cameraConfigLastActionTime;

        try {
            switch (cameraConfigState) {
                case WAITING_FOR_STREAMING:
                    if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
                        cameraConfigState = CameraConfigState.SETTING_EXPOSURE_MODE;
                        cameraConfigLastActionTime = currentTime;
                    }
                    break;

                case SETTING_EXPOSURE_MODE:
                    if (timeSinceLastAction >= 20) { // Small delay between checks
                        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
                        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                            exposureControl.setMode(ExposureControl.Mode.Manual);
                            cameraConfigLastActionTime = currentTime;
                            cameraConfigState = CameraConfigState.SETTING_EXPOSURE_VALUE;
                        } else {
                            cameraConfigState = CameraConfigState.SETTING_EXPOSURE_VALUE;
                        }
                    }
                    break;

                case SETTING_EXPOSURE_VALUE:
                    if (timeSinceLastAction >= 50) { // Wait for mode change to take effect
                        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
                        exposureControl.setExposure((long) PARAMS.EXPOSURE_MS, TimeUnit.MILLISECONDS);
                        cameraConfigLastActionTime = currentTime;
                        cameraConfigState = CameraConfigState.SETTING_GAIN;
                    }
                    break;

                case SETTING_GAIN:
                    if (timeSinceLastAction >= 20) { // Small delay after setting exposure
                        GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
                        gainControl.setGain(PARAMS.GAIN);
                        cameraConfigLastActionTime = currentTime;
                        cameraConfigState = CameraConfigState.SETTING_FOCUS_MODE;
                    }
                    break;

                case SETTING_FOCUS_MODE:
                    if (timeSinceLastAction >= 20) { // Small delay after setting gain
                        FocusControl focusControl = visionPortal.getCameraControl(FocusControl.class);
                        if (focusControl != null) {
                            if (focusControl.getMode() != FocusControl.Mode.Fixed) {
                                focusControl.setMode(FocusControl.Mode.Fixed);
                                cameraConfigLastActionTime = currentTime;
                                cameraConfigState = CameraConfigState.SETTING_FOCUS_VALUE;
                            } else {
                                cameraConfigState = CameraConfigState.SETTING_FOCUS_VALUE;
                            }
                        } else {
                            // No focus control available - skip to complete
                            cameraConfigState = CameraConfigState.COMPLETE;
                        }
                    }
                    break;

                case SETTING_FOCUS_VALUE:
                    if (timeSinceLastAction >= 50) { // Wait for mode change to take effect
                        FocusControl focusControl = visionPortal.getCameraControl(FocusControl.class);
                        if (focusControl != null) {
                            focusControl.setFocusLength(PARAMS.FOCUS_LENGTH);
                            cameraConfigLastActionTime = currentTime;
                            cameraConfigState = CameraConfigState.VERIFYING_FOCUS;
                        } else {
                            cameraConfigState = CameraConfigState.COMPLETE;
                        }
                    }
                    break;

                case VERIFYING_FOCUS:
                    if (timeSinceLastAction >= 20) { // Small delay after setting focus
                        FocusControl focusControl = visionPortal.getCameraControl(FocusControl.class);
                        if (focusControl != null) {
                            if (focusControl.getMode() != FocusControl.Mode.Fixed) {
                                focusControl.setMode(FocusControl.Mode.Fixed);
                                cameraConfigLastActionTime = currentTime;
                            }
                        }
                        cameraConfigState = CameraConfigState.COMPLETE;
                    }
                    break;

                case COMPLETE:
                    return true;
            }
        } catch (Exception e) {
            // If configuration fails, mark as complete to avoid infinite loops
            cameraConfigState = CameraConfigState.COMPLETE;
        }

        return cameraConfigState == CameraConfigState.COMPLETE;
    }

    // ==================== CONTROL METHODS ====================
    public void setDesiredTagId(int tagId) {
        int oldTagId = this.PARAMS.desiredTagId;
        this.PARAMS.desiredTagId = tagId;

        // Clear temporal filter state if tag ID changed to avoid stale detections
        if (oldTagId != tagId) {
            stableDetectionCount = 0;
            detectionHistory = new AprilTagDetection[PARAMS.STABILITY_FRAMES_REQUIRED];
            detectionHistoryIndex = 0;
            lastStableTarget = null;
            currentTarget = null;
        }
    }

    // ==================== UPDATE METHODS ====================

    /**
     * Update target detection - call this in the main loop
     * Manages servo pause/resume when shooting is requested
     *
     * @param skipUpdate If true, skip this update (useful when robot is moving fast)
     * @return true if servos should be running, false if they should be paused
     */
    public boolean update(boolean skipUpdate) {
        if (!isInitialized || aprilTagProcessor == null) {
            currentTarget = null;
            return true; // Allow servos to run if vision not initialized
        }

        // Continue camera configuration if not complete (non-blocking)
        if (cameraConfigState != CameraConfigState.COMPLETE) {
            configureCamera();
        }

        long currentTime = System.currentTimeMillis();

        // Skip update if requested (e.g., robot moving too fast)
        if (skipUpdate) {
            // Keep last stable target but don't update
            currentTarget = lastStableTarget;
            // If not shooting, resume servos
            if (!isShootingRequested && servosPaused) {
                resumeServos(intakeSubsystem, transferSubsystem);
            }
            // Return desired servo state
        }

        performDetection();
        boolean hasTarget = (currentTarget != null);
        if(!hasTarget){
            currentTarget = lastStableTarget;
        }

                    
        

        return hasTarget; // Return desired servo state
    }


    /**
     * Perform April tag detection (internal method)
     */
    private void performDetection() {
        // Get current detections
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        AprilTagDetection rawTarget = null;

        // Find desired tag
        for (AprilTagDetection detection : detections) {
            if (detection.metadata != null) {
                if (PARAMS.desiredTagId < 0 || detection.id == PARAMS.desiredTagId) {
                    rawTarget = detection;
                    break;
                }
            }
        }

        // Apply temporal filtering if enabled
        if (PARAMS.ENABLE_TEMPORAL_FILTERING && rawTarget != null) {
            currentTarget = applyTemporalFilter(rawTarget);
        } else {
            // No filtering - use raw detection
            currentTarget = rawTarget;
            if (rawTarget != null) {
                lastStableTarget = rawTarget;
            }
        }

        // If no detection found, clear temporal filter state
        if (rawTarget == null) {
            stableDetectionCount = 0;
            detectionHistory = new AprilTagDetection[PARAMS.STABILITY_FRAMES_REQUIRED];
        }
    }


    /**
     * Update target detection - call this in the main loop (default, no skip)
     */
    public void update() {
        update(false, null, null);
    }

  
   

    /**
     * Apply temporal filtering to reject unstable detections caused by vibration.
     * Only accepts detections that are consistent across multiple frames.
     * Uses a more lenient approach: accepts detection if majority of frames agree.
     *
     * @param newDetection The new detection to evaluate
     * @return The stable target if found, null otherwise
     */
    private AprilTagDetection applyTemporalFilter(AprilTagDetection newDetection) {
        // Add to history
        detectionHistory[detectionHistoryIndex] = newDetection;
        detectionHistoryIndex = (detectionHistoryIndex + 1) % PARAMS.STABILITY_FRAMES_REQUIRED;

        // Check if we have enough history
        int validDetections = 0;
        for (AprilTagDetection d : detectionHistory) {
            if (d != null) {
                validDetections++;
            }
        }

        if (validDetections < PARAMS.STABILITY_FRAMES_REQUIRED) {
            // Not enough history yet - don't return a target
            stableDetectionCount = 0;
            return null;
        }

        // Use majority voting: find the most common detection in history
        // Count how many detections match each unique detection
        int bestMatchCount = 0;
        AprilTagDetection bestMatch = null;

        for (int i = 0; i < detectionHistory.length; i++) {
            if (detectionHistory[i] == null) continue;

            AprilTagDetection candidate = detectionHistory[i];
            int matchCount = 0;

            // Count how many detections match this candidate
            for (int j = 0; j < detectionHistory.length; j++) {
                if (detectionHistory[j] == null) continue;

                AprilTagDetection other = detectionHistory[j];

                // Check if same tag ID
                if (candidate.id != other.id) continue;

                // Check if bearing is similar (within threshold)
                double bearingDiff = Math.abs(candidate.ftcPose.bearing - other.ftcPose.bearing);
                if (bearingDiff > PARAMS.MAX_DETECTION_CHANGE_BEARING) continue;

                // Check if range is similar (within threshold)
                double rangeDiff = Math.abs(candidate.ftcPose.range - other.ftcPose.range);
                if (rangeDiff > PARAMS.MAX_DETECTION_CHANGE_RANGE) continue;

                matchCount++;
            }

            // Keep track of the best match
            if (matchCount > bestMatchCount) {
                bestMatchCount = matchCount;
                bestMatch = candidate;
            }
        }

        // Require at least 60% of frames to agree (more lenient for vibration)
        int requiredMatches = (int) Math.ceil(PARAMS.STABILITY_FRAMES_REQUIRED * 0.6);

        if (bestMatchCount >= requiredMatches && bestMatch != null) {
            stableDetectionCount++;
            if (stableDetectionCount >= 2) { // Require 2 consecutive good matches
                // Detection is stable - use it
                lastStableTarget = bestMatch;
                return bestMatch;
            }
        } else {
            // Detection not stable enough - reset counter but keep last stable target
            stableDetectionCount = 0;
        }

        // Return last stable target if available (helps maintain lock during brief vibration spikes)
        return lastStableTarget;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if a target is currently detected
     */
    public boolean hasTarget() {
        return currentTarget != null;
    }

    /**
     * Get the current detected target
     */
    public AprilTagDetection getTarget() {
        return currentTarget;
    }

    /**
     * Get the bearing (horizontal angle) to the target in degrees
     * Positive = target is to the right, negative = target is to the left
     */
    public double getTargetBearing() {
        return currentTarget != null ? currentTarget.ftcPose.bearing : 0.0;
    }

    /**
     * Get the distance to the target in inches
     */
    public double getTargetRange() {
        return currentTarget != null ? currentTarget.ftcPose.range : 0.0;
    }

    /**
     * Get the elevation angle to the target in degrees
     */
    public double getTargetElevation() {
        return currentTarget != null ? currentTarget.ftcPose.elevation : 0.0;
    }

    /**
     * Get the target's yaw (rotation) in degrees
     */
    public double getTargetYaw() {
        return currentTarget != null ? currentTarget.ftcPose.yaw : 0.0;
    }

    /**
     * Get the target's lateral offset (X position) in inches
     */
    public double getTargetX() {
        return currentTarget != null ? currentTarget.ftcPose.x : 0.0;
    }

    /**
     * Get the target's forward distance (Y position) in inches
     */
    public double getTargetY() {
        return currentTarget != null ? currentTarget.ftcPose.y : 0.0;
    }

    /**
     * Get the target's vertical offset (Z position) in inches
     */
    public double getTargetZ() {
        return currentTarget != null ? currentTarget.ftcPose.z : 0.0;
    }

    /**
     * Get the target's ID
     */
    public int getTargetId() {
        return currentTarget != null ? currentTarget.id : -1;
    }

    /**
     * Get the target's name
     */
    public String getTargetName() {
        if (currentTarget != null && currentTarget.metadata != null) {
            return currentTarget.metadata.name;
        }
        return "Unknown";
    }

    /**
     * Check if the vision system is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get the vision portal for external access (e.g., dashboard streaming)
     */
    public VisionPortal getVisionPortal() {
        return visionPortal;
    }

    /**
     * Get all current AprilTag detections (for debugging/telemetry)
     */
    public List<AprilTagDetection> getAllDetections() {
        if (aprilTagProcessor == null) {
            return new java.util.ArrayList<>();
        }
        return aprilTagProcessor.getDetections();
    }

    /**
     * Get the desired tag ID that the vision system is looking for
     */
    public int getDesiredTagId() {
        return PARAMS.desiredTagId;
    }

    /**
     * Get the current camera configuration state (for debugging)
     */
    public String getCameraConfigState() {
        return cameraConfigState != null ? cameraConfigState.name() : "UNKNOWN";
    }

    /**
     * Check if camera configuration is complete
     */
    public boolean isCameraConfigured() {
        return cameraConfigState == CameraConfigState.COMPLETE;
    }

    /**
     * Get the current stable detection count (for debugging temporal filtering)
     */
    public int getStableDetectionCount() {
        return stableDetectionCount;
    }

    /**
     * Get the number of valid detections in history (for debugging temporal filtering)
     */
    public int getDetectionHistoryCount() {
        if (detectionHistory == null) return 0;
        int count = 0;
        for (AprilTagDetection d : detectionHistory) {
            if (d != null) count++;
        }
        return count;
    }

    /**
     * Get camera streaming state (for debugging)
     */
    public String getCameraState() {
        if (visionPortal == null) return "NOT_INITIALIZED";
        VisionPortal.CameraState state = visionPortal.getCameraState();
        return state != null ? state.name() : "UNKNOWN";
    }

    // ==================== CLEANUP ====================

    /**
     * Stop camera streaming and release resources
     */
    public void close() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}
