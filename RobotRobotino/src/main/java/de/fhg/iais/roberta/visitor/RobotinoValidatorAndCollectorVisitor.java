package de.fhg.iais.roberta.visitor;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.ConfigurationAst;

import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.constants.RobotinoConstants;
import de.fhg.iais.roberta.syntax.SC;
import de.fhg.iais.roberta.syntax.action.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.action.display.ClearDisplayAction;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.light.LightStatusAction;

import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.action.sound.VolumeAction;

import de.fhg.iais.roberta.syntax.sensor.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.OdometryReset;
import de.fhg.iais.roberta.syntax.sensor.generic.*;

import de.fhg.iais.roberta.visitor.validate.MotorValidatorAndCollectorVisitor;


public class RobotinoValidatorAndCollectorVisitor extends MotorValidatorAndCollectorVisitor implements IRobotinoVisitor<Void> {

    public RobotinoValidatorAndCollectorVisitor(ConfigurationAst robotConfiguration, ClassToInstanceMap<IProjectBean.IBuilder<?>> beanBuilders) {
        super(robotConfiguration, beanBuilders);
    }

    @Override
    public Void visitClearDisplayAction(ClearDisplayAction<Void> clearDisplayAction) {
        return null;
    }

    @Override
    public Void visitShowTextAction(ShowTextAction<Void> showTextAction) {
        return null;
    }

    @Override
    public Void visitLightAction(LightAction<Void> lightAction) {
        return null;
    }

    @Override
    public Void visitLightStatusAction(LightStatusAction<Void> lightStatusAction) {
        return null;
    }

    @Override
    public Void visitToneAction(ToneAction<Void> toneAction) {
        return null;
    }

    @Override
    public Void visitPlayNoteAction(PlayNoteAction<Void> playNoteAction) {
        return null;
    }

    @Override
    public Void visitVolumeAction(VolumeAction<Void> volumeAction) {
        return null;
    }

    @Override
    public Void visitPlayFileAction(PlayFileAction<Void> playFileAction) {
        return null;
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

        requiredComponentVisited(omnidriveAction, omnidriveAction.xVel);
        requiredComponentVisited(omnidriveAction, omnidriveAction.yVel);
        requiredComponentVisited(omnidriveAction, omnidriveAction.thetaVel);


        return null;
    }

    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction<Void> omnidrivePositionAction) {
        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.OMNIDRIVESPEED);
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
        return null;
    }

    @Override
    public Void visitPinWriteValueAction(PinWriteValueAction<Void> pinWriteValueAction) {
        usedHardwareBuilder.addUsedActor(new UsedActor("", SC.DIGITAL_PIN));
        usedMethodBuilder.addUsedMethod(RobotinoMethods.SETDIGITALPIN);
        usedMethodBuilder.addUsedMethod(RobotinoMethods.RESETDIGITALPIN);
        return null;
    }

    @Override
    public Void visitPinGetValueSensor(PinGetValueSensor<Void> pinGetValueSensor) {
        if (pinGetValueSensor.getMode().equals(SC.ANALOG)) {
            usedHardwareBuilder.addUsedSensor(new UsedSensor(pinGetValueSensor.getUserDefinedPort(), SC.ANALOG_INPUT, pinGetValueSensor.getMode()));
        } else if (pinGetValueSensor.getMode().equals(SC.DIGITAL)) {
            usedHardwareBuilder.addUsedSensor(new UsedSensor(pinGetValueSensor.getUserDefinedPort(), SC.DIGITAL_INPUT, pinGetValueSensor.getMode()));
        }
        return null;
    }

    @Override
    public Void visitDriveAction(DriveAction<Void> driveAction) {
        return null;
    }

    @Override
    public Void visitCurveAction(CurveAction<Void> curveAction) {
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction<Void> turnAction) {
        return null;
    }

    @Override
    public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
        usedHardwareBuilder.addUsedActor(new UsedActor("", RobotinoConstants.OMNIDRIVE));
        return null;
    }
}
