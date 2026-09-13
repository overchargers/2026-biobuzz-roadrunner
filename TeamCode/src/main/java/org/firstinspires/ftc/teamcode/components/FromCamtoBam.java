package org.firstinspires.ftc.teamcode.components;

public class FromCamtoBam {
    private float gravity = 32.174f;
    private int rpm = 1000;
    public float goalX;
    private float goalY;
    public float goalZ;
    private float heightAtGoal;
    private boolean doesItHit;

    public FromCamtoBam(float x, float y, float z){
        this.goalX = x;
        this.goalY = 23f/6f;
        this.goalZ = z;
    }

    public float rotateX(){
        float rotate = (float) Math.toDegrees(Math.atan(this.goalX/this.goalZ));
        this.goalZ = (float) Math.sqrt(Math.pow(this.goalZ, 2.0) + Math.pow(this.goalX, 2));
        if(this.goalX > 0){
            return rotate;
        }else{
            return -rotate;
        }
    }
    private float helperMethod(float x, float initialVelocity, float initialLaunchAngle){
        return (float)((x/(initialVelocity*Math.cos(initialLaunchAngle))) *
               (initialVelocity * Math.sin(initialLaunchAngle) - 
               (this.gravity * (x/(initialVelocity*Math.cos(initialLaunchAngle)))/2)));
    }
    private float adjustUpDown(float initialVelocity,  float angle){
        for(float i = 0; i < Math.toRadians(40)-angle; i+=0.01){
            if(this.heightAtGoal<(this.goalY - 0.2f)){
                angle+=Math.toRadians(0.5f);
                this.heightAtGoal = helperMethod(this.goalZ, initialVelocity, angle);
                continue;
            }else if(this.heightAtGoal>(this.goalY+0.2f)){
                angle-=Math.toRadians(0.5f);
                this.heightAtGoal = helperMethod(this.goalZ, initialVelocity, angle);
                continue;
            }else{
                this.doesItHit = true;
                break;
            }
        }
        return angle;
    }
    public float scaleNum(float angle){
        float forp = (float) (((-(30-angle))/20)+0.5);
        forp = Math.max(0, Math.min(forp, 1));
        return forp;
    }
    public void setRPM(int baseRPM) {
        this.rpm = baseRPM;
    }
    public float[] getLaunchAngleRPM(double x, double y, double z){
        float initialVelocity = (float) ((float)Math.PI*0.24*this.rpm);
        float initialLaunchAngle = (float)Math.atan(23.0/(6*this.goalZ));
        this.heightAtGoal = helperMethod(this.goalZ, initialVelocity, initialLaunchAngle);

        this.doesItHit = false;
        while(!this.doesItHit){
            initialLaunchAngle = adjustUpDown(initialVelocity, initialLaunchAngle);
            this.rpm += 100;
        }
        while(testMaxHeight(initialLaunchAngle, initialVelocity)){
            this.rpm += 50;
            initialVelocity = (float) ((float)Math.PI*0.24*this.rpm);
            initialLaunchAngle = adjustUpDown(initialVelocity, initialLaunchAngle);
        }
        return new float[] {(float) scaleNum(initialLaunchAngle), (float) this.rpm};
    }   
    
    private boolean testMaxHeight(float initialLaunchAngle, float initialVelocity){
        float maxHeightReached = 0;
        boolean isTooHigh = false;
        for(int i = 0; i < (int) Math.floor(this.goalZ); i++){
            float currentHeight = helperMethod((float)i, initialVelocity, initialLaunchAngle);
            if(maxHeightReached < currentHeight){
                maxHeightReached = currentHeight;
            }
            if(maxHeightReached > 6){
                isTooHigh = true;
                break;
            }   
        }
        return isTooHigh;
    }
}
