package org.dirtymechanics.frc.component.arm;

import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import org.dirtymechanics.event.impl.ButtonListener;
import org.dirtymechanics.frc.component.arm.event.ScrewDriveDecreaseOffsetButtonEventHandler;
import org.dirtymechanics.frc.component.arm.event.ScrewDriveHighButtonEventHandler;
import org.dirtymechanics.frc.component.arm.event.ScrewDriveIncreaseOffsetButtonEventHandler;
import org.dirtymechanics.frc.component.arm.event.ScrewDrivePassButtonEventHandler;
import org.dirtymechanics.frc.component.arm.event.ScrewDriveResetButtonEventHandler;
import org.dirtymechanics.frc.component.arm.event.ScrewDriveTrussButtonEventHandler;
import org.dirtymechanics.frc.control.OperatorGameController;
import org.dirtymechanics.frc.sensor.StringEncoder;
import org.dirtymechanics.frc.util.Updatable;

/**
 * A controller for a screw drive.
 *
 * @author Daniel Ruess
 */
public class ScrewDrive implements Updatable{
    private final Jaguar motor = new Jaguar(6);
    private final StringEncoder string = new StringEncoder(1);
    NetworkTable server = NetworkTable.getTable("SmartDashboard");
    
    /**
     * The default speed to run at.
     */
    private static final double SPEED = 1D; //TODO: calculate this.


    private double dest = 1;
    private int speedScale = 0;
    //TODO move all these handlers and listeners to the operator controller
    private ScrewDriveHighButtonEventHandler highButtonHandler;
    private ScrewDrivePassButtonEventHandler passButtonHandler;
    private ScrewDriveResetButtonEventHandler resetButtonHandler;
    private ScrewDriveTrussButtonEventHandler trussButtonHandler;
    private ScrewDriveIncreaseOffsetButtonEventHandler increaseOffsetButtonHandler;
    private ScrewDriveDecreaseOffsetButtonEventHandler decreaseOffsetButtonHandler;
    private ButtonListener highButtonListener = new ButtonListener();
    private ButtonListener passButtonListener = new ButtonListener();
    private ButtonListener resetButtonListener = new ButtonListener();
    private ButtonListener trussButtonListener = new ButtonListener();
    private ButtonListener increaseOffsetButtonListener = new ButtonListener();
    private ButtonListener decreaseOffsetButtonListener = new ButtonListener();
    private OperatorGameController operatorController;
    
    private ScrewProperties props;
    
    


    public ScrewDrive(OperatorGameController operatorController) {
        //TODO talk to the team about having this be still when we enable the robot.
//        set(props.pass());
        this.operatorController = operatorController;
    }
    
    public void setProperties(ScrewProperties p){
        props = p;
    }
    public void init() {
        LiveWindow.addSensor("Boom", "String Encoder", string);
        highButtonHandler = new ScrewDriveHighButtonEventHandler(operatorController, this);
        passButtonHandler = new ScrewDrivePassButtonEventHandler(operatorController, this);
        resetButtonHandler = new ScrewDriveResetButtonEventHandler(operatorController, this);
        trussButtonHandler = new ScrewDriveTrussButtonEventHandler(operatorController, this);
        increaseOffsetButtonHandler = new ScrewDriveIncreaseOffsetButtonEventHandler(operatorController, this);
        decreaseOffsetButtonHandler = new ScrewDriveDecreaseOffsetButtonEventHandler(operatorController, this);
        highButtonListener.addHandler(highButtonHandler);
        passButtonListener.addHandler(passButtonHandler);
        resetButtonListener.addHandler(resetButtonHandler);
        trussButtonListener.addHandler(trussButtonHandler);
        increaseOffsetButtonListener.addHandler(increaseOffsetButtonHandler);
        decreaseOffsetButtonListener.addHandler(decreaseOffsetButtonHandler);
        
    }
    
    public void reset() {
        set(props.reset());
        server.putString("ScrewDrive.position", "resetting...");
    }
    
    public void trussShot() {
        set(props.trussShot());
        server.putString("ScrewDrive.position", "truss");
    }
    
    public void pass() {
        set(props.pass());
        server.putString("ScrewDrive.position", "pass");
    }
    
    public void high() {
        set(props.highGoal());
        server.putString("ScrewDrive.position", "high");
    }
    
    public void autonomous() {
        set(props.highGoal());
        server.putString("ScrewDrive.position", "auto shot");
    }
    
    

    /**
     *
     * @param destination
     */
    private void set(Location destination) {
        // if (resetting) {
        //    nextDestination = destination;
        //} else {
        this.dest = destination.loc;
        //}
    }

    public void increaseOffset() {
        dest += .1;
    }

    public void decreaseOffset() {
        dest -= .1;
    }

    public int getPosition() {
        return string.getDistance();
    }

    public void update() {
        seekSetPoint();
        server.putNumber("ScrewDrive.location", string.getAverageVoltage());
        long currentTime = System.currentTimeMillis();
        highButtonListener.updateState(operatorController.isScrewDriveHighPressed(), currentTime);
        passButtonListener.updateState(operatorController.isScrewDrivePassPressed(), currentTime);
        resetButtonListener.updateState(operatorController.isScrewDriveResetPressed(), currentTime);
        trussButtonListener.updateState(operatorController.isScrewDriveTrussPressed(), currentTime);
        server.putNumber("ScrewDrive.destination", dest);
    }

    void seekSetPoint() {
        double loc = string.getAverageVoltage();
        double dif = Math.abs(dest - loc);
        double error = .01;

        if (dif > error) {
            if (dest < loc) {
                if (dif < .1) {
                    motor.set(.4);
                } else {
                    motor.set(SPEED);
                }
            } else {
                if (dif < .07) {
                    motor.set(0);
                } else {
                    motor.set(-1 * SPEED);
                }
            }
        } else {
            motor.set(0);
            if (dest == props.reset().loc) {
                set(props.highGoal());
                server.putString("ScrewDrive.position", "high");
            }
        }
    }

    
    
    /**
     * Represents a location to move the screw drive to.
     */
    public static class Location {

        private final double loc;

        public Location(double loc) {
            this.loc = loc;
        }
    }
}
