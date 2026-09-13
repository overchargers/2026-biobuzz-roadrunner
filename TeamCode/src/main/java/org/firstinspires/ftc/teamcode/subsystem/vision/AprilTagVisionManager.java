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

package org.firstinspires.ftc.teamcode.subsystem.vision;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.config.AprilTagConfig;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages the AprilTag vision system including camera setup, detection processing,
 * and FTC Dashboard integration.
 */
public class AprilTagVisionManager {
    
    private final VisionPortal visionPortal;
    private final AprilTagProcessor aprilTag;
    private final AprilTagCameraProcessor cameraProcessor;
    private final HardwareMap hardwareMap;
    
    /**
     * Initialize the AprilTag vision system.
     * 
     * @param hardwareMap The FTC hardware map
     * @param telemetry The telemetry object for status updates
     */
    public AprilTagVisionManager(HardwareMap hardwareMap, org.firstinspires.ftc.robotcore.external.Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        
        // Initialize AprilTag processor
        this.aprilTag = createAprilTagProcessor();
        
        // Initialize camera processor
        this.cameraProcessor = new AprilTagCameraProcessor(aprilTag);
        
        // Initialize vision portal
        this.visionPortal = createVisionPortal();
        
        // Start FTC Dashboard camera stream
        FtcDashboard.getInstance().startCameraStream(cameraProcessor, AprilTagConfig.DASHBOARD_STREAM_FPS);
        
        // Configure camera settings if using webcam
        if (AprilTagConfig.USE_WEBCAM) {
            setManualExposure(AprilTagConfig.MANUAL_EXPOSURE_MS, AprilTagConfig.MANUAL_GAIN, telemetry);
        }
    }
    
    /**
     * Create and configure the AprilTag processor.
     * 
     * @return Configured AprilTag processor
     */
    private AprilTagProcessor createAprilTagProcessor() {
        return new AprilTagProcessor.Builder()
                .setDrawAxes(AprilTagConfig.DRAW_AXES)
                .setDrawCubeProjection(AprilTagConfig.DRAW_CUBE_PROJECTION)
                .setDrawTagOutline(AprilTagConfig.DRAW_TAG_OUTLINE)
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .build();
    }
    
    /**
     * Create and configure the vision portal.
     * 
     * @return Configured vision portal
     */
    private VisionPortal createVisionPortal() {
        VisionPortal.Builder builder = new VisionPortal.Builder()
                .addProcessor(aprilTag)
                .addProcessor(cameraProcessor)
                .setCameraResolution(new Size(AprilTagConfig.CAMERA_WIDTH, AprilTagConfig.CAMERA_HEIGHT))
                .setStreamFormat(VisionPortal.StreamFormat.MJPEG);
        
        if (AprilTagConfig.USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, AprilTagConfig.WEBCAM_NAME))
                   .enableLiveView(AprilTagConfig.ENABLE_LIVE_VIEW);
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }
        
        return builder.build();
    }
    
    /**
     * Manually set the camera gain and exposure for webcams.
     * This can only be called AFTER the vision portal is initialized.
     * 
     * @param exposureMS Exposure time in milliseconds
     * @param gain Camera gain value
     * @param telemetry Telemetry object for status updates
     */
    private void setManualExposure(int exposureMS, int gain, org.firstinspires.ftc.robotcore.external.Telemetry telemetry) {
        // Wait for the camera to be open, then use the controls
        if (visionPortal == null) {
            return;
        }
        
        // Make sure camera is streaming before we try to set the exposure controls
        if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addData("Camera", "Waiting");
            telemetry.update();
            while (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            telemetry.addData("Camera", "Ready");
            telemetry.update();
        }
        
        // Set camera controls
        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
            exposureControl.setMode(ExposureControl.Mode.Manual);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        exposureControl.setExposure((long) exposureMS, TimeUnit.MILLISECONDS);
        
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
        gainControl.setGain(gain);
        
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Get all current AprilTag detections.
     * 
     * @return List of current detections
     */
    public List<AprilTagDetection> getDetections() {
        return aprilTag.getDetections();
    }
    
    /**
     * Find a specific AprilTag by ID, or any tag if DESIRED_TAG_ID is -1.
     * 
     * @return The desired AprilTag detection, or null if not found
     */
    public AprilTagDetection findDesiredTag() {
        List<AprilTagDetection> detections = getDetections();
        
        for (AprilTagDetection detection : detections) {
            if (detection.metadata != null) {
                // Check if this is the tag we want to track
                if ((AprilTagConfig.DESIRED_TAG_ID < 0) || (detection.id == AprilTagConfig.DESIRED_TAG_ID)) {
                    return detection;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the camera processor for dashboard integration.
     * 
     * @return The camera processor
     */
    public AprilTagCameraProcessor getCameraProcessor() {
        return cameraProcessor;
    }
    
    /**
     * Get the AprilTag processor for advanced control.
     * 
     * @return The AprilTag processor
     */
    public AprilTagProcessor getAprilTagProcessor() {
        return aprilTag;
    }
    
    /**
     * Get the vision portal for advanced control.
     * 
     * @return The vision portal
     */
    public VisionPortal getVisionPortal() {
        return visionPortal;
    }
    
    /**
     * Check if the vision system is ready.
     * 
     * @return True if vision portal is streaming
     */
    public boolean isReady() {
        return visionPortal != null && visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING;
    }
    
    /**
     * Close the vision portal and clean up resources.
     */
    public void close() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}
