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
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveDistanceAction;
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

import java.util.Map;


public class RobotinoValidatorAndCollectorVisitor extends MotorValidatorAndCollectorVisitor implements IRobotinoVisitor<Void> {

    public RobotinoValidatorAndCollectorVisitor(ConfigurationAst robotConfiguration, ClassToInstanceMap<IProjectBean.IBuilder> beanBuilders) {
        super(robotConfiguration, beanBuilders);
    }

    @Override
    public Void visitTimerSensor(TimerSensor timerSensor) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(timerSensor.getUserDefinedPort(), SC.TIMER, timerSensor.getMode()));
        return null;
    }

    @Override
    public Void visitTouchSensor(TouchSensor touchSensor) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(touchSensor.getUserDefinedPort(), SC.TOUCH, touchSensor.getMode()));
        return null;
    }

    @Override
    public Void visitOmnidriveAction(OmnidriveAction omnidriveAction) {
        addMotorMethodsAndHardware();

        requiredComponentVisited(omnidriveAction, omnidriveAction.xVel, omnidriveAction.yVel);
        requiredComponentVisited(omnidriveAction, omnidriveAction.thetaVel);

        return null;
    }

    @Override
    public Void visitOmnidriveDistanceAction(OmnidriveDistanceAction omnidriveDistanceAction) {
        addMotorMethodsAndHardware();

        requiredComponentVisited(omnidriveDistanceAction, omnidriveDistanceAction.xVel, omnidriveDistanceAction.yVel);
        requiredComponentVisited(omnidriveDistanceAction, omnidriveDistanceAction.distance);
        usedHardwareBuilder.addUsedSensor(new UsedSensor(getConfigPort(RobotinoConstants.ODOMETRY), RobotinoConstants.ODOMETRY, null));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVEFORDISTANCE);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETPOSITION);


        checkIfBothZeroSpeed(omnidriveDistanceAction);
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction turnAction) {
        requiredComponentVisited(turnAction, turnAction.param.getSpeed(), turnAction.param.getDuration().getValue());
        usedHardwareBuilder.addUsedSensor(new UsedSensor(getConfigPort(RobotinoConstants.ODOMETRY), RobotinoConstants.ODOMETRY, null));
        addMotorMethodsAndHardware();
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.TURNFORDEGREES);
        return null;
    }


    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction omnidrivePositionAction) {
        requiredComponentVisited(omnidrivePositionAction, omnidrivePositionAction.x,
                omnidrivePositionAction.y, omnidrivePositionAction.power);

        usedHardwareBuilder.addUsedSensor(new UsedSensor(getConfigPort(RobotinoConstants.ODOMETRY), RobotinoConstants.ODOMETRY, null));

        addMotorMethodsAndHardware();

        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.DRIVETOPOSITION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETDIRECTION);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETPOSITION);

        checkForZeroSpeed(omnidrivePositionAction, omnidrivePositionAction.power);
        return null;
    }

    private void checkIfBothZeroSpeed(OmnidriveDistanceAction omnidriveDistanceAction) {
        if (omnidriveDistanceAction.xVel.getKind().hasName("NUM_CONST") && omnidriveDistanceAction.yVel.getKind().hasName("NUM_CONST")) {

            if (Math.abs(Double.parseDouble(((NumConst) omnidriveDistanceAction.xVel).value)) < DOUBLE_EPS
                    && Math.abs(Double.parseDouble(((NumConst) omnidriveDistanceAction.yVel).value)) < DOUBLE_EPS) {
                addWarningToPhrase(omnidriveDistanceAction, "MOTOR_SPEED_0");
            }
        }
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor infraredSensor) {
        usedMethodBuilder.addUsedMethod(RobotinoMethods.GETDISTANCE);
        usedHardwareBuilder.addUsedSensor(new UsedSensor(infraredSensor.getUserDefinedPort(), SC.INFRARED, infraredSensor.getMode()));
        return null;
    }

    @Override
    public Void visitOdometryPosition(OdometryPosition odometryPosition) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(odometryPosition.getUserDefinedPort(), RobotinoConstants.ODOMETRY, odometryPosition.slot));
        if (odometryPosition.slot.equals("THETA")) {
            usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        }
        return null;
    }

    @Override
    public Void visitOdometryReset(OdometryReset odometryReset) {
        usedHardwareBuilder.addUsedSensor(new UsedSensor(odometryReset.getUserDefinedPort(), RobotinoConstants.ODOMETRY, odometryReset.slot));
        if (!odometryReset.slot.equals("THETA")) {
            usedMethodBuilder.addUsedMethod(RobotinoMethods.GETORIENTATION);
        }
        if (!odometryReset.slot.equals("ALL")) {
            usedMethodBuilder.addUsedMethod(RobotinoMethods.RESETODOMETRY);
        }
        return null;
    }

    @Override
    public Void visitPinWriteValueAction(PinWriteValueAction pinWriteValueAction) {
        requiredComponentVisited(pinWriteValueAction, pinWriteValueAction.value);

        ConfigurationComponent usedConfigurationBlock = this.robotConfiguration.optConfigurationComponent(pinWriteValueAction.port);
        if (usedConfigurationBlock == null) {
            addErrorToPhrase(pinWriteValueAction, "CONFIGURATION_ERROR_ACTOR_MISSING");
        }

        usedHardwareBuilder.addUsedActor(new UsedActor(getConfigPort(pinWriteValueAction.port), SC.DIGITAL_PIN));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.SETDIGITALPIN);
        return null;
    }

    @Override
    public Void visitPinGetValueSensor(PinGetValueSensor pinGetValueSensor) {
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
    public Void visitMotorDriveStopAction(MotorDriveStopAction stopAction) {
        addMotorMethodsAndHardware();
        return null;
    }

    private void addMotorMethodsAndHardware() {
        usedMethodBuilder.addUsedMethod(RobotinoMethods.OMNIDRIVESPEED);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.PUBLISHVEL);
        usedHardwareBuilder.addUsedActor(new UsedActor(getConfigPort(RobotinoConstants.OMNIDRIVE), RobotinoConstants.OMNIDRIVE));
    }

    private String getConfigPort(String name) {
        Map<String, ConfigurationComponent> configComponents = this.robotConfiguration.getConfigurationComponents();
        for (ConfigurationComponent component : configComponents.values()) {
            if (component.componentType.equals(name)) {
                return component.userDefinedPortName;
            }
        }
        return "";
    }
}
