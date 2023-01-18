// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.RobotContainer;
import frc.robot.Constants.IntakeConstants;

public class ElementTransitSubsystem extends SubsystemBase {
  private Compressor pcmCompressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
  private DoubleSolenoid leftShortSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 2);
  private DoubleSolenoid leftLongSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 2);
  private DoubleSolenoid rightShortSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 2);
  private DoubleSolenoid rightLongSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 2);
  private CANSparkMax leftMotor = new CANSparkMax(0, MotorType.kBrushless);
  private CANSparkMax rightMotor = new CANSparkMax(0, MotorType.kBrushless);
  /** Creates a new ElementTransitSubsystem. */
  public ElementTransitSubsystem() {
    pcmCompressor.enableDigital();
    pcmCompressor.disable();
    //upper 4 lines enable/disable compressor, return if compressor active,
    rightLongSolenoid.set(Value.kReverse);//placeholders
    rightShortSolenoid.set(Value.kReverse);
    leftLongSolenoid.set(Value.kReverse);
    leftShortSolenoid.set(Value.kReverse);

  }
  public void runClawMotors() {
    leftMotor.set(IntakeConstants.intakeSpeed);
    rightMotor.set(IntakeConstants.intakeSpeed);
  }
  public void stopClawMotors(){
    leftMotor.set(0);
    rightMotor.set(0);
  }
  // public static void main(String[] args) { //main method
  //   System.out.println("Kurt sucks"); //kurt sucks
  // } charlie's footprint

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    //  boolean motorStart = RobotContainer.operatorStick.getRawButton(1);
    // boolean motorStop = RobotContainer.operatorStick.getRawButton(0);
    // boolean shortExtend = RobotContainer.operatorStick.getRawButton(2);
    // boolean longExtend = RobotContainer.operatorStick.getRawButton(3);
    // boolean shortDeextend = RobotContainer.operatorStick.getRawButton(4);
    // if (motorIntake) {
    //   runMotors();
    // } else if(motorStop){
    //   stopMotors();
    // }
    
  } 

}
