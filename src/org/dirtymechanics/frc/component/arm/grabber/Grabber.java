/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dirtymechanics.frc.component.arm.grabber;

/**
 * Represents a grabber (i.e. the side arms on the robot).
 * 
 * @author Zach Sussman
 */
public abstract class Grabber {
    public abstract void openSmall();
    public abstract void closeSmall();
    public abstract void flipSmall();
    
    public abstract void openLarge();
    public abstract void closeLarge();
    public abstract void flipLarge();
    
    public static Grabber getGrabber() throws Exception {
        throw new Exception("Grabber is too abstract; choose a concrete class");
    }
    
    
}
