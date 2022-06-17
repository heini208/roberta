package de.fhg.iais.roberta.visitor;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.constants.RobotinoConstants;
import de.fhg.iais.roberta.syntax.action.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.configuration.ConfigurationComponent;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyExpr;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.sensor.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.OdometryReset;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.util.syntax.SC;
import de.fhg.iais.roberta.visitor.validate.MotorValidatorAndCollectorVisitor;


public class RobotinoValidatorAndCollectorVisitor extends MotorValidatorAndCollectorVisitor implements IRobotinoVisitor<Void> {

    public RobotinoValidatorAndCollectorVisitor(ConfigurationAst robotConfiguration, ClassToInstanceMap<IProjectBean.IBuilder<?>> beanBuilders) {
        super(robotConfiguration, beanBuilders);
    }

    @Override
    public Void visitTimerSensor(TimerSensor<Void> timerSensor) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(timerSensor.getUserDefinedPort(), SC.TIMER, timerSensor.getMode()));
        return null;
    }

    @Override
    public Void visitTouchSensor(TouchSensor<Void> touchSensor) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(touchSensor.getUserDefinedPort(), SC.TOUCH, touchSensor.getMode()));
        return null;
    }

    @Override
    public Void visitOmnidriveAction(OmnidriveAction<Void> omnidriveAction) {
        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.OMNIDRIVESPEED);

        requiredComponentVisited(omnidriveAction, omnidriveAction.xVel, omnidriveAction, omnidriveAction.yVel);

        if (!(omnidriveAction.distance instanceof EmptyExpr)) {
            requiredComponentVisited(omnidriveAction, omnidriveAction.distance);

            usedHardwareBuilder.addUsedSensor(new UsedSensor(null, RobotinoConstants.ODOMETRY, null));
            usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVEFORDISTANCE);

            checkIfBothZeroSpeed(omnidriveAction);
        } else {
            requiredComponentVisited(omnidriveAction, omnidriveAction.thetaVel);
        }
        return null;
    }

    private void checkIfBothZeroSpeed(OmnidriveAction<Void> omnidriveAction) {
        if (omnidriveAction.xVel.getKind().hasName("NUM_CONST") && omnidriveAction.yVel.getKind().hasName("NUM_CONST")) {

            if (Math.abs(Double.parseDouble(((NumConst<Void>) omnidriveAction.xVel).getValue())) < DOUBLE_EPS
                    && Math.abs(Double.parseDouble(((NumConst<Void>) omnidriveAction.yVel).getValue())) < DOUBLE_EPS) {
                addWarningToPhrase(omnidriveAction, "MOTOR_SPEED_0");
            }
        }
    }

    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction<Void> omnidrivePositionAction) {
        requiredComponentVisited(omnidrivePositionAction, omnidrivePositionAction.x,
                omnidrivePositionAction, omnidrivePositionAction.y,
                omnidrivePositionAction, omnidrivePositionAction.power);

        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.OMNIDRIVESPEED);
        usedHardwareBuilder.addUsedSensor(new UsedSensor(null, RobotinoConstants.ODOMETRY, null));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVETOPOSITION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETDIRECTION);
        checkForZeroSpeed(omnidrivePositionAction, omnidrivePositionAction.power);
        return null;
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor<Void> infraredSensor) {
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETDISTANCE);
        usedHardwareBuilder.addUsedSensor(new UsedSensor(infraredSensor.getUserDefinedPort(), SC.INFRARED, infraredSensor.getMode()));
        return null;
    }

    @Override
    public Void visitOdometryPosition(OdometryPosition<Void> odometryPosition) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(odometryPosition.getUserDefinedPort(), RobotinoConstants.ODOMETRY, odometryPosition.slot));
        if (odometryPosition.slot.equals("THETA")) {
            usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        }
        return null;
    }

    @Override
    public Void visitOdometryReset(OdometryReset<Void> odometryReset) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(odometryReset.getUserDefinedPort(), RobotinoConstants.ODOMETRY, odometryReset.slot));
        if (!odometryReset.slot.equals("THETA")) {
            usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        }
        return null;
    }

    @Override
    public Void visitPinWriteValueAction(PinWriteValueAction<Void> pinWriteValueAction) {
        requiredComponentVisited(pinWriteValueAction, pinWriteValueAction.getValue());

        ConfigurationComponent usedConfigurationBlock = this.robotConfiguration.optConfigurationComponent(pinWriteValueAction.getPort());
        if (usedConfigurationBlock == null) {
            addErrorToPhrase(pinWriteValueAction, "CONFIGURATION_ERROR_ACTOR_MISSING");
        }

        usedHardwareBuilder.addUsedActor(new UsedActor("", SC.DIGITAL_PIN));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.SETDIGITALPIN);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.RESETDIGITALPIN);
        return null;
    }

    @Override
    public Void visitPinGetValueSensor(PinGetValueSensor<Void> pinGetValueSensor) {
        ConfigurationComponent usedConfigurationBlock = this.robotConfiguration.optConfigurationComponent(pinGetValueSensor.getUserDefinedPort());
        if (usedConfigurationBlock == null) {
            addErrorToPhrase(pinGetValueSensor, "CONFIGURATION_ERROR_SENSOR_MISSING");
        }

        if (pinGetValueSensor.getMode().equals(SC.ANALOG)) {
            usedHardwareBuilder.addUsedSensor(new UsedSensor(pinGetValueSensor.getUserDefinedPort(), SC.ANALOG_INPUT, pinGetValueSensor.getMode()));
        } else if (pinGetValueSensor.getMode().equals(SC.DIGITAL)) {
            usedHardwareBuilder.addUsedSensor(new UsedSensor(pinGetValueSensor.getUserDefinedPort(), SC.DIGITAL_INPUT, pinGetValueSensor.getMode()));
        }
        return null;
    }

    @Override
    public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
        return null;
    }
}
