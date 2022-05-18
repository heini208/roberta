package de.fhg.iais.roberta.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ClassToInstanceMap;

import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.display.ClearDisplayAction;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.action.sound.VolumeAction;
import de.fhg.iais.roberta.syntax.action.thymio.PlayRecordingAction;
import de.fhg.iais.roberta.syntax.action.thymio.RedLedOnAction;
import de.fhg.iais.roberta.syntax.action.thymio.YellowLedOnAction;
import de.fhg.iais.roberta.syntax.configuration.ConfigurationComponent;
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.sensor.generic.AccelerometerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.KeysSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.LightSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.SoundSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TemperatureSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.syntax.BlocklyConstants;
import de.fhg.iais.roberta.util.syntax.SC;
import de.fhg.iais.roberta.util.syntax.WithUserDefinedPort;
import de.fhg.iais.roberta.visitor.validate.DifferentialMotorValidatorAndCollectorVisitor;


public class ThymioValidatorAndCollectorVisitor extends DifferentialMotorValidatorAndCollectorVisitor implements IThymioVisitor<Void> {
    private static final Map<String, String> SENSOR_COMPONENT_TYPE_MAP = new HashMap<String, String>() {{
        put("SOUND_RECORD", SC.SOUND);
//        put("QUAD_COLOR_SENSING", ThymioConstants.MBUILD_QUADRGB);
        put("GYRO_AXIS_RESET", SC.GYRO);
    }};

    public ThymioValidatorAndCollectorVisitor(ConfigurationAst robotConfiguration, ClassToInstanceMap<IProjectBean.IBuilder> beanBuilders) {
        super(robotConfiguration, beanBuilders);
    }

    @Override
    public Void visitAccelerometerSensor(AccelerometerSensor accelerometerSensor) {
        return null;
    }

    @Override
    public Void visitClearDisplayAction(ClearDisplayAction clearDisplayAction) {
        return null;
    }

    @Override
    public Void visitCurveAction(CurveAction curveAction) {
        requiredComponentVisited(curveAction, curveAction.paramLeft.getSpeed(), curveAction.paramRight.getSpeed());
        usedMethodBuilder.addUsedMethod(ThymioMethods.DIFFDRIVE);
        if ( curveAction.paramRight.getDuration() != null ) {
            requiredComponentVisited(curveAction, curveAction.paramRight.getDuration().getValue());
            usedMethodBuilder.addUsedMethod(ThymioMethods.STOP);
        }
        return null;
    }

    @Override
    public Void visitDriveAction(DriveAction driveAction) {
        requiredComponentVisited(driveAction, driveAction.param.getSpeed());
        usedMethodBuilder.addUsedMethod(ThymioMethods.DIFFDRIVE);
        if ( driveAction.param.getDuration() != null ) {
            requiredComponentVisited(driveAction, driveAction.param.getDuration().getValue());
            usedMethodBuilder.addUsedMethod(ThymioMethods.STOP);
        }
        return null;
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor infraredSensor) {
        return null;
    }

    @Override
    public Void visitKeysSensor(KeysSensor keysSensor) {
        return null;
    }

    @Override
    public Void visitLightAction(LightAction lightAction) {
        requiredComponentVisited(lightAction, lightAction.rgbLedColor);
        usedHardwareBuilder.addUsedActor(new UsedActor(BlocklyConstants.EMPTY_PORT, SC.RGBLED));
        return null;
    }

    @Override
    public Void visitLightSensor(LightSensor lightSensor) {
        return null;
    }

    @Override
    public Void visitMainTask(MainTask mainTask) {
        requiredComponentVisited(mainTask, mainTask.variables);
        return null;
    }

    @Override
    public Void visitMotorDriveStopAction(MotorDriveStopAction stopAction) {
        usedMethodBuilder.addUsedMethod(ThymioMethods.STOP);
        return null;
    }

    @Override
    public Void visitMotorOnAction(MotorOnAction motorOnAction) {
        requiredComponentVisited(motorOnAction, motorOnAction.param.getSpeed());
        if ( motorOnAction.param.getDuration() != null ) {
            requiredComponentVisited(motorOnAction, motorOnAction.param.getDuration().getValue());
        }
        return null;
    }

    @Override
    public Void visitMotorStopAction(MotorStopAction motorStopAction) {
        return null;
    }

    @Override
    public Void visitPlayFileAction(PlayFileAction playFileAction) {
        return null;
    }

    @Override
    public Void visitPlayNoteAction(PlayNoteAction playNoteAction) {
        return null;
    }

    @Override
    public Void visitPlayRecordingAction(PlayRecordingAction playRecordingAction) {
        return null;
    }

    @Override
    public Void visitRedLedOnAction(RedLedOnAction redLedOnAction) {
        return null;
    }

    @Override
    public Void visitShowTextAction(ShowTextAction showTextAction) {
        return null;
    }

    @Override
    public Void visitSoundSensor(SoundSensor soundSensor) {
        return null;
    }

    @Override
    public Void visitTemperatureSensor(TemperatureSensor temperatureSensor) {
        return null;
    }

    @Override
    public Void visitTimerSensor(TimerSensor timerSensor) {
        return null;
    }

    @Override
    public Void visitToneAction(ToneAction toneAction) {
        requiredComponentVisited(toneAction, toneAction.duration, toneAction.frequency);
        usedHardwareBuilder.addUsedActor(new UsedActor(toneAction.port, SC.BUZZER));
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction turnAction) {
        requiredComponentVisited(turnAction, turnAction.param.getSpeed());
        usedMethodBuilder.addUsedMethod(ThymioMethods.DIFFDRIVE);
        if ( turnAction.param.getDuration() != null ) {
            requiredComponentVisited(turnAction, turnAction.param.getDuration().getValue());
            usedMethodBuilder.addUsedMethod(ThymioMethods.STOP);
        }
        return null;
    }

    @Override
    public Void visitVolumeAction(VolumeAction volumeAction) {
        return null;
    }

    @Override
    public Void visitYellowLedOnAction(YellowLedOnAction yellowLedOnAction) {
        return null;
    }

    private void checkActorPort(WithUserDefinedPort action) {
        Assert.isTrue(action instanceof Phrase, "checking Port of a non Phrase");
        ConfigurationComponent usedConfigurationBlock = this.robotConfiguration.optConfigurationComponent(action.getUserDefinedPort());
        if ( usedConfigurationBlock == null ) {
            Phrase actionAsPhrase = (Phrase) action;
            addErrorToPhrase(actionAsPhrase, "CONFIGURATION_ERROR_ACTOR_MISSING");
        }
    }

    private void checkSensorPort(WithUserDefinedPort sensor) {
        Assert.isTrue(sensor instanceof Phrase, "checking Port of a non Phrase");
        Phrase sensorAsSensor = (Phrase) sensor;

        String userDefinedPort = sensor.getUserDefinedPort();
        ConfigurationComponent configurationComponent = this.robotConfiguration.optConfigurationComponent(userDefinedPort);
        if ( configurationComponent == null ) {
            configurationComponent = getSubComponent(userDefinedPort);
            if ( configurationComponent == null ) {
                addErrorToPhrase(sensorAsSensor, "CONFIGURATION_ERROR_SENSOR_MISSING");
                return;
            }
        }
        checkSensorType(sensorAsSensor, configurationComponent);
    }

    private void checkSensorType(Phrase sensor, ConfigurationComponent configurationComponent) {
        String expectedComponentType = SENSOR_COMPONENT_TYPE_MAP.get(sensor.getKind().getName());
        String typeWithoutSensing = sensor.getKind().getName().replace("_SENSING", "");
        if ( !(typeWithoutSensing.equalsIgnoreCase(configurationComponent.componentType)) ) {
            if ( expectedComponentType != null && !expectedComponentType.equalsIgnoreCase(configurationComponent.componentType) ) {
                addErrorToPhrase(sensor, "CONFIGURATION_ERROR_SENSOR_WRONG");
            }
        }
    }

    private ConfigurationComponent getSubComponent(String userDefinedPort) {
        for ( ConfigurationComponent component : this.robotConfiguration.getConfigurationComponentsValues() ) {
            try {
                for ( List<ConfigurationComponent> subComponents : component.getSubComponents().values() ) {
                    for ( ConfigurationComponent subComponent : subComponents ) {
                        if ( subComponent.userDefinedPortName.equals(userDefinedPort) ) {
                            return subComponent;
                        }
                    }
                }
            } catch ( UnsupportedOperationException e ) {
                continue;
            }
        }
        return null;
    }
}
