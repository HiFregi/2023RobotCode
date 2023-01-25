// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
//testing push

package frc.robot.subsystems;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Limelight;
import frc.robot.Constants.DriveConstants;

public class DriveSubsystem extends SubsystemBase {
  private CANSparkMax frontLeftMotor = new CANSparkMax(DriveConstants.frontLeftMotorID, MotorType.kBrushless);
  private CANSparkMax backLeftMotor = new CANSparkMax(DriveConstants.backLeftMotorID, MotorType.kBrushless);
  private CANSparkMax frontRightMotor = new CANSparkMax(DriveConstants.frontRightMotorID, MotorType.kBrushless);
  private CANSparkMax backRightMotor = new CANSparkMax(DriveConstants.backRightMotorID, MotorType.kBrushless);

  private MotorControllerGroup leftMotors = new MotorControllerGroup(frontLeftMotor, backLeftMotor);
  private MotorControllerGroup rightMotors = new MotorControllerGroup(frontRightMotor, backRightMotor);

  private RelativeEncoder frontLeftEncoder = frontLeftMotor.getEncoder();
  private RelativeEncoder frontRightEncoder = frontRightMotor.getEncoder();

  private SparkMaxPIDController leftPIDController = frontLeftMotor.getPIDController();
  private SparkMaxPIDController rightPIDController = frontRightMotor.getPIDController();

  private SlewRateLimiter leftLimiter = new SlewRateLimiter(3.53);
  private SlewRateLimiter rightLimiter = new SlewRateLimiter(3.53);

  // private PhotonCamera limelight = new PhotonCamera("gloworm");
  private Limelight limelight = new Limelight("limelight");

  private AHRS navx = new AHRS(I2C.Port.kMXP);

  private PIDController balancePIDController = new PIDController(0.010, 0, 0.00125);

  private boolean balanceCompleted = false;

  private int smartMotionSlot = 0;
  private int allowedErr;
  private int minVel;
  private double kP = 4e-4;
  private double kI = 0;
  private double kD = 0;
  private double kIz = 0;
  private double kFF = 0.000146;
  private double kMaxOutput = 1;
  private double kMinOutput = -1;
  private double maxVel = 5000;
  private double maxAcc = 2500;

  /** Creates a new DriveSubsystem. */
  public DriveSubsystem() {
    // rightMotors.setInverted(true);

    backLeftMotor.follow(frontLeftMotor);
    backRightMotor.follow(frontRightMotor);

    initializePID(leftPIDController);
    initializePID(rightPIDController);

    SmartDashboard.putData(navx);
  }

  private void initializePID(SparkMaxPIDController p) {
    p.setP(kP);
    p.setI(kI);
    p.setD(kD);
    p.setIZone(kIz);
    p.setFF(kFF);
    p.setOutputRange(kMinOutput, kMaxOutput);
    p.setSmartMotionMaxVelocity(maxVel, smartMotionSlot);
    p.setSmartMotionMinOutputVelocity(minVel, smartMotionSlot);
    p.setSmartMotionMaxAccel(maxAcc, smartMotionSlot);
    p.setSmartMotionAllowedClosedLoopError(allowedErr, smartMotionSlot);
  }

  public void tankDrive(double left, double right) {
    double leftSpeed = leftLimiter.calculate(left);
    double rightSpeed = rightLimiter.calculate(right);

    if (Math.abs(leftSpeed) < 0.06) {
      leftSpeed = 0;
      leftLimiter.reset(0);
    }

    if (Math.abs(rightSpeed) < 0.06) {
      rightSpeed = 0;
      rightLimiter.reset(0);
    }

    leftMotors.set(leftSpeed);
    rightMotors.set(-rightSpeed);
  }

  public void arcadeDrive(double forward, double turn) {
    // if (Math.abs(forward) < 0.06) {
    // forward = 0;
    // }

    // if (Math.abs(turn) < 0.06) {
    // turn = 0;
    // }

    double leftSpeed = forward + turn;

    double rightSpeed = forward - turn;

    leftMotors.set(leftSpeed);
    rightMotors.set(-rightSpeed);
  }

  public void autoDrive(double meters) {
    leftPIDController.setReference(convertDistanceToEncoder(meters), ControlType.kSmartMotion);
    rightPIDController.setReference(-convertDistanceToEncoder(meters), ControlType.kSmartMotion);
  }

  public void resetEncoders() {
    frontLeftEncoder.setPosition(0);
    frontRightEncoder.setPosition(0);
  }

  public void autoBalance() {
    double gyroPitch = navx.getPitch();

    if (Math.abs(gyroPitch) <= 0.5) {
      arcadeDrive(0, 0);
      return;
    }

    if (Math.abs(gyroPitch) < 5.5) {
      balancePIDController.setP(0.0061);
    }

    double forwardSpeed = balancePIDController.calculate(gyroPitch, 0);

    arcadeDrive(forwardSpeed, 0);
  }

  public void resetBalance() {
    balancePIDController.setP(0.010);
    balanceCompleted = false;
  }

  public double getAngleError(double expectedAngle) {
    double angleSubtract = Math.IEEEremainder(expectedAngle, 360) - Math.IEEEremainder(navx.getYaw(), 360);

    if (angleSubtract < -180) {
      return angleSubtract + 360;
    } else if (angleSubtract > 180) {
      return angleSubtract - 360;
    }

    return angleSubtract;
  }

  public boolean alignedToTapeYaw() {
    if (limelight.hasTarget()) {
      return Math.abs(limelight.getTargetX()) <= DriveConstants.tapeAlignmentTolerance;
    }

    return false;
    // PhotonPipelineResult result = limelight.getLatestResult();

    // if (result.hasTargets()) {
    // PhotonTrackedTarget target = result.getBestTarget();

    // return Math.abs(target.getYaw()) <= DriveConstants.tapeAlignmentTolerance;
    // }

    // return false;
  }

  public boolean alignedToTapePitch() {
    if (limelight.hasTarget()) {
      return Math
          .abs(limelight.getTargetY() - DriveConstants.tapeAlignmentPitch) <= DriveConstants.tapeAlignmentTolerance;
    }

    return false;
    // PhotonPipelineResult result = limelight.getLatestResult();

    // if (result.hasTargets()) {
    // PhotonTrackedTarget target = result.getBestTarget();

    // return Math.abs(target.getPitch() - DriveConstants.tapeAlignmentPitch) <=
    // DriveConstants.tapeAlignmentTolerance;
    // }

    // return false;
  }

  // public PhotonCamera getCamera() {
  // return limelight;
  // }

  public Limelight getCamera() {
    return limelight;
  }

  public boolean distanceReached(double distanceMeters) {
    return Math.abs(frontLeftEncoder.getPosition() - convertDistanceToEncoder(distanceMeters)) <= 0.5;
  }

  public double convertDistanceToEncoder(double meters) {
    return 2 * (meters / DriveConstants.wheelCircumference) * 42 / DriveConstants.gearBoxRatio;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Gyro Yaw", navx.getYaw());
    SmartDashboard.putNumber("Gyro Pitch", navx.getPitch());
    SmartDashboard.putNumber("Gyro Roll", navx.getRoll());

    Double[] campose = limelight.getTable().getEntry("campose").getDoubleArray(new Double[0]);

    if (campose.length > 0) {
      double x = campose[0];
      double y = campose[1];
      double z = campose[2];

      double distance = Math.sqrt(x * x + y * y + z * z);

      SmartDashboard.putNumber("Distance?", distance);

      SmartDashboard.putNumber("Camera Translation X", campose[0]);
      SmartDashboard.putNumber("Camera Translation Y", campose[1]);
      SmartDashboard.putNumber("Camera Translation Z", campose[2]);
      SmartDashboard.putNumber("Camera Rotation Pitch", campose[3]);
      SmartDashboard.putNumber("Camera Rotation Yaw", campose[4]);
      SmartDashboard.putNumber("Camera Rotation Roll", campose[5]);
    }
  }
}
