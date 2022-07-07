package de.fhg.iais.roberta.visitor;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.constants.RobotinoConstants;
import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveActionDistance;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.configuration.ConfigurationComponent;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.syntax.sensor.robotino.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.robotino.OdometryReset;
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
        addMotorMethodsAndHardware();

        requiredComponentVisited(omnidriveAction, omnidriveAction.xVel, omnidriveAction.yVel);
        requiredComponentVisited(omnidriveAction, omnidriveAction.thetaVel);

        return null;
    }

    @Override
    public Void visitOmnidriveActionDistance(OmnidriveActionDistance<Void> omnidriveActionDistance) {
        addMotorMethodsAndHardware();

        requiredComponentVisited(omnidriveActionDistance, omnidriveActionDistance.xVel, omnidriveActionDistance.yVel);
        requiredComponentVisited(omnidriveActionDistance, omnidriveActionDistance.distance);
        usedHardwareBuilder.addUsedSensor(new UsedSensor(null, RobotinoConstants.ODOMETRY, null));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVEFORDISTANCE);

        checkIfBothZeroSpeed(omnidriveActionDistance);
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction<Void> turnAction) {
        requiredComponentVisited(turnAction, turnAction.param.getSpeed(), turnAction.param.getDuration().getValue());
        usedHardwareBuilder.addUsedSensor(new UsedSensor(null, RobotinoConstants.ODOMETRY, null));
        addMotorMethodsAndHardware();
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.TURNFORDEGREES);
        return null;
    }


    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction<Void> omnidrivePositionAction) {
        requiredComponentVisited(omnidrivePositionAction, omnidrivePositionAction.x,
                omnidrivePositionAction.y, omnidrivePositionAction.power);

        usedHardwareBuilder.addUsedSensor(new UsedSensor(null, RobotinoConstants.ODOMETRY, null));

        addMotorMethodsAndHardware();

        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVETOPOSITION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETDIRECTION);

        checkForZeroSpeed(omnidrivePositionAction, omnidrivePositionAction.power);
        return null;
    }

    private void checkIfBothZeroSpeed(OmnidriveActionDistance<Void> omnidriveActionDistance) {
        if (omnidriveActionDistance.xVel.getKind().hasName("NUM_CONST") && omnidriveActionDistance.yVel.getKind().hasName("NUM_CONST")) {

            if (Math.abs(Double.parseDouble(((NumConst<Void>) omnidriveActionDistance.xVel).value)) < DOUBLE_EPS
                    && Math.abs(Double.parseDouble(((NumConst<Void>) omnidriveActionDistance.yVel).value)) < DOUBLE_EPS) {
                addWarningToPhrase(omnidriveActionDistance, "MOTOR_SPEED_0");
            }
        }
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
        requiredComponentVisited(pinWriteValueAction, pinWriteValueAction.value);

        ConfigurationComponent usedConfigurationBlock = this.robotConfiguration.optConfigurationComponent(pinWriteValueAction.port);
        if (usedConfigurationBlock == null) {
            addErrorToPhrase(pinWriteValueAction, "CONFIGURATION_ERROR_ACTOR_MISSING");
        }

        usedHardwareBuilder.addUsedActor(new UsedActor("", SC.DIGITAL_PIN));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.SETDIGITALPIN);
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
        addMotorMethodsAndHardware();
        return null;
    }

    private void addMotorMethodsAndHardware() {
        usedMethodBuilder.addUsedMethod(RobotinoMethods.OMNIDRIVESPEED);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.PUBLISHVEL);
        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
    }
}
