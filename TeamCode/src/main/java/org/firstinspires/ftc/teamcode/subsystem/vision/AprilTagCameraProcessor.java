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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Enhanced camera stream processor for AprilTag detection with FTC Dashboard integration.
 * Provides real-time visualization of AprilTag detections, robot pose estimation,
 * and enhanced debugging information.
 */
public class AprilTagCameraProcessor implements VisionProcessor, CameraStreamSource {
    
    private final AtomicReference<Bitmap> lastFrame = 
            new AtomicReference<>(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));
    
    private final AprilTagProcessor aprilTag;
    private List<AprilTagDetection> currentDetections;
    private int drawCount = 0;
    
    // Pre-created paint objects for better performance
    private Paint boxPaint;
    private Paint textPaint;
    private Paint infoPaint;
    private Paint bgPaint;
    private Paint posePaint;
    private Paint robotPaint;
    private Paint arrowPaint;
    private Paint coordPaint;
    
    /**
     * Constructs a new AprilTag camera processor.
     * 
     * @param aprilTagProcessor The AprilTag processor to use for detection
     */
    public AprilTagCameraProcessor(AprilTagProcessor aprilTagProcessor) {
        this.aprilTag = aprilTagProcessor;
        initializePaints();
    }
    
    /**
     * Initialize all paint objects with optimized settings for performance.
     */
    private void initializePaints() {
        // AprilTag detection paints
        boxPaint = new Paint();
        boxPaint.setColor(Color.CYAN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4);
        
        textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(24);
        textPaint.setFakeBoldText(true);
        
        infoPaint = new Paint();
        infoPaint.setColor(Color.WHITE);
        infoPaint.setStyle(Paint.Style.FILL);
        infoPaint.setTextSize(20);
        
        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAlpha(180);
        
        // Pose estimation paints
        posePaint = new Paint();
        posePaint.setColor(Color.MAGENTA);
        posePaint.setStyle(Paint.Style.STROKE);
        posePaint.setStrokeWidth(3);
        
        robotPaint = new Paint();
        robotPaint.setColor(Color.GREEN);
        robotPaint.setStyle(Paint.Style.FILL);
        
        arrowPaint = new Paint();
        arrowPaint.setColor(Color.RED);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(5);
        
        coordPaint = new Paint();
        coordPaint.setStrokeWidth(2);
        coordPaint.setTextSize(16);
    }
    
    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        lastFrame.set(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565));
    }
    
    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        // Get current AprilTag detections
        currentDetections = aprilTag.getDetections();
        
        // Convert frame to bitmap
        Bitmap b = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, b);
        
        // Draw overlays directly on the bitmap for FTC Dashboard
        Canvas canvas = new Canvas(b);
        drawDashboardOverlays(canvas);
        
        lastFrame.set(b);
        return null;
    }
    
    /**
     * Draw all dashboard overlays including AprilTag detections and pose estimation.
     * 
     * @param canvas The canvas to draw on
     */
    private void drawDashboardOverlays(Canvas canvas) {
        // Draw AprilTag detections with enhanced visualization
        if (currentDetections != null && !currentDetections.isEmpty()) {
            for (AprilTagDetection detection : currentDetections) {
                drawAprilTagDetection(canvas, detection);
            }
        }
        
        // Draw pose estimation overlay
        drawPoseEstimation(canvas);
    }
    
    /**
     * Draw a single AprilTag detection with bounding box and information.
     * 
     * @param canvas The canvas to draw on
     * @param detection The AprilTag detection to visualize
     */
    private void drawAprilTagDetection(Canvas canvas, AprilTagDetection detection) {
        // Draw bounding box around detected tag with proper size
        float tagSize = 80; // Base size for tag visualization
        if (detection.metadata != null) {
            // Scale based on actual tag size and distance
            tagSize = (float) Math.min(120, Math.max(60, 1000 / Math.max(detection.ftcPose.range, 1)));
        }
        
        Rect rect = new Rect(
            (int) (detection.center.x - tagSize/2),
            (int) (detection.center.y - tagSize/2),
            (int) (detection.center.x + tagSize/2),
            (int) (detection.center.y + tagSize/2)
        );
        canvas.drawRect(rect, boxPaint);
        
        // Draw tag ID with background
        String idText = "ID: " + detection.id;
        Rect textRect = new Rect();
        textPaint.getTextBounds(idText, 0, idText.length(), textRect);
        
        canvas.drawRect(
            (int)(detection.center.x + tagSize/2 + 5),
            (int)(detection.center.y - textRect.height() - 5),
            (int)(detection.center.x + tagSize/2 + textRect.width() + 15),
            (int)(detection.center.y + 5),
            bgPaint
        );
        
        canvas.drawText(idText, 
            (float)(detection.center.x + tagSize/2 + 10), 
            (float)detection.center.y, 
            textPaint);
            
        // Draw distance and bearing information
        if (detection.metadata != null) {
            String distanceText = String.format("Dist: %.1f\"", detection.ftcPose.range);
            String bearingText = String.format("Bearing: %.1f°", detection.ftcPose.bearing);
            String yawText = String.format("Yaw: %.1f°", detection.ftcPose.yaw);
            
            float yOffset = (float)(detection.center.y + 30);
            canvas.drawText(distanceText, (float)(detection.center.x + tagSize/2 + 10), yOffset, infoPaint);
            canvas.drawText(bearingText, (float)(detection.center.x + tagSize/2 + 10), yOffset + 25, infoPaint);
            canvas.drawText(yawText, (float)(detection.center.x + tagSize/2 + 10), yOffset + 50, infoPaint);
        }
    }
    
    /**
     * Draw pose estimation overlay showing robot position relative to detected tags.
     * 
     * @param canvas The canvas to draw on
     */
    private void drawPoseEstimation(Canvas canvas) {
        if (currentDetections != null && !currentDetections.isEmpty()) {
            // Draw robot representation at center of frame
            float robotX = canvas.getWidth() / 2.0f;
            float robotY = canvas.getHeight() / 2.0f;
            float robotSize = 30;
            
            // Draw robot as a circle
            canvas.drawCircle(robotX, robotY, robotSize, robotPaint);
            
            // Draw robot orientation arrow
            canvas.drawLine(robotX, robotY, robotX, robotY - robotSize, arrowPaint);
            
            // Draw connections to detected tags
            for (AprilTagDetection detection : currentDetections) {
                if (detection.metadata != null) {
                    canvas.drawLine(robotX, robotY, (float)detection.center.x, (float)detection.center.y, posePaint);
                    
                    // Draw distance text along the line
                    float midX = (robotX + (float)detection.center.x) / 2;
                    float midY = (robotY + (float)detection.center.y) / 2;
                    String distText = String.format("%.1f\"", detection.ftcPose.range);
                    
                    drawTextWithBackground(canvas, distText, midX, midY);
                }
            }
            
            // Draw coordinate system reference
            drawCoordinateSystem(canvas);
        }
    }
    
    /**
     * Draw text with a semi-transparent background for better readability.
     * 
     * @param canvas The canvas to draw on
     * @param text The text to draw
     * @param x X coordinate for text center
     * @param y Y coordinate for text baseline
     */
    private void drawTextWithBackground(Canvas canvas, String text, float x, float y) {
        // Background for text
        Paint distanceBgPaint = new Paint();
        distanceBgPaint.setColor(Color.BLACK);
        distanceBgPaint.setStyle(Paint.Style.FILL);
        distanceBgPaint.setAlpha(150);
        
        Paint distanceTextPaint = new Paint();
        distanceTextPaint.setColor(Color.WHITE);
        distanceTextPaint.setTextSize(18);
        distanceTextPaint.setFakeBoldText(true);
        
        Rect textRect = new Rect();
        distanceTextPaint.getTextBounds(text, 0, text.length(), textRect);
        canvas.drawRect(
            x - textRect.width()/2 - 5,
            y - textRect.height() - 5,
            x + textRect.width()/2 + 5,
            y + 5,
            distanceBgPaint
        );
        
        canvas.drawText(text, x - textRect.width()/2, y, distanceTextPaint);
    }
    
    /**
     * Draw coordinate system reference axes.
     * 
     * @param canvas The canvas to draw on
     */
    private void drawCoordinateSystem(Canvas canvas) {
        float x = canvas.getWidth() - 100;
        float y = canvas.getHeight() - 100;
        
        // X-axis (red)
        coordPaint.setColor(Color.RED);
        canvas.drawLine(x, y, x + 30, y, coordPaint);
        canvas.drawText("X", x + 35, y + 5, coordPaint);
        
        // Y-axis (green)
        coordPaint.setColor(Color.GREEN);
        canvas.drawLine(x, y, x, y - 30, coordPaint);
        canvas.drawText("Y", x + 5, y - 35, coordPaint);
    }
    
    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight,
                            float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        drawCount++;
        // Drawing is now handled in processFrame method for dashboard
    }
    
    /**
     * Get the number of currently detected AprilTags.
     * 
     * @return Number of detections
     */
    public int getDetectionCount() { 
        return (currentDetections != null) ? currentDetections.size() : 0; 
    }
    
    /**
     * Get the number of frames drawn (for debugging).
     * 
     * @return Draw count
     */
    public int getDrawCount() { 
        return drawCount; 
    }
    
    /**
     * Get the current camera frame bitmap.
     * 
     * @return Current frame bitmap
     */
    public Bitmap getCurrentFrame() {
        return lastFrame.get();
    }
    
    /**
     * Get the current AprilTag detections.
     * 
     * @return List of current detections
     */
    public List<AprilTagDetection> getCurrentDetections() {
        return currentDetections;
    }
    
    @Override
    public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
        continuation.dispatch(bitmapConsumer -> bitmapConsumer.accept(lastFrame.get()));
    }
}
