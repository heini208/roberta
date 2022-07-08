package de.fhg.iais.roberta.visitor.codegen;

import java.util.List;

import org.json.JSONObject;

import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.inter.mode.action.ITurnDirection;
import de.fhg.iais.roberta.mode.action.DriveDirection;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.display.ClearDisplayAction;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.light.LightStatusAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorGetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.action.sound.VolumeAction;
import de.fhg.iais.roberta.syntax.lang.expr.ColorConst;
import de.fhg.iais.roberta.syntax.sensor.generic.ColorSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.CompassSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.EncoderSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.GyroSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.KeysSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.UltrasonicSensor;
import de.fhg.iais.roberta.typecheck.NepoInfo;
import de.fhg.iais.roberta.util.basic.C;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.dbc.DbcException;
import de.fhg.iais.roberta.util.syntax.MotorDuration;
import de.fhg.iais.roberta.util.syntax.SC;
import de.fhg.iais.roberta.visitor.IOrbVisitor;
import de.fhg.iais.roberta.visitor.lang.codegen.AbstractStackMachineVisitor;

public class OrbStackMachineVisitor extends AbstractStackMachineVisitor implements IOrbVisitor<Void> {

    public OrbStackMachineVisitor(ConfigurationAst configuration, List<List<Phrase>> phrases, ILanguage language) {
        super(configuration);
        Assert.isTrue(!phrases.isEmpty());
    }

    @Override
    public Void visitColorConst(ColorConst colorConst) {
        String color = "";
        switch ( colorConst.getHexValueAsString().toUpperCase() ) {
            case "#000000":
                color = "BLACK";
                break;
            case "#0057A6":
                color = "BLUE";
                break;
            case "#00642E":
                color = "GREEN";
                break;
            case "#F7D117":
                color = "YELLOW";
                break;
            case "#B30006":
                color = "RED";
                break;
            case "#FFFFFF":
                color = "WHITE";
                break;
            case "#532115":
                color = "BROWN";
                break;
            case "#585858":
                color = "NONE";
                break;
            default:
                colorConst.addInfo(NepoInfo.error("SIM_BLOCK_NOT_SUPPORTED"));
                throw new DbcException("Invalid color constant: " + colorConst.getHexValueAsString());
        }
        JSONObject o = makeNode(C.EXPR).put(C.EXPR, C.COLOR_CONST).put(C.VALUE, color);
        return app(o);
    }

    @Override
    public Void visitLightAction(LightAction lightAction) {
        return null;
    }

    @Override
    public Void visitLightStatusAction(LightStatusAction lightStatusAction) {
        return null;
    }

    @Override
    public Void visitMotorGetPowerAction(MotorGetPowerAction motorGetPowerAction) {
        String port = motorGetPowerAction.getUserDefinedPort();
        JSONObject o = makeNode(C.MOTOR_GET_POWER).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public Void visitDriveAction(DriveAction driveAction) {
        driveAction.param.getSpeed().accept(this);
        boolean speedOnly = !processOptionalDuration(driveAction.param.getDuration());
        DriveDirection driveDirection = (DriveDirection) driveAction.direction;
        JSONObject o =
            makeNode(C.DRIVE_ACTION).put(C.DRIVE_DIRECTION, driveDirection).put(C.NAME, "orb").put(C.SPEED_ONLY, speedOnly).put(C.SET_TIME, false);
        if ( speedOnly ) {
            return app(o);
        } else {
            app(o);
            return app(makeNode(C.STOP_DRIVE).put(C.NAME, "orb"));
        }
    }

    @Override
    public Void visitToneAction(ToneAction toneAction) {
        return null;
    }

    @Override
    public Void visitPlayNoteAction(PlayNoteAction playNoteAction) {
        return null;
    }

    @Override
    public Void visitTurnAction(TurnAction turnAction) {
        turnAction.param.getSpeed().accept(this);
        boolean speedOnly = !processOptionalDuration(turnAction.param.getDuration());
        ITurnDirection turnDirection = turnAction.direction;
        JSONObject o =
            makeNode(C.TURN_ACTION)
                .put(C.TURN_DIRECTION, turnDirection.toString().toLowerCase())
                .put(C.NAME, "orb")
                .put(C.SPEED_ONLY, speedOnly)
                .put(C.SET_TIME, false);
        if ( speedOnly ) {
            return app(o);
        } else {
            app(o);
            return app(makeNode(C.STOP_DRIVE).put(C.NAME, "orb"));
        }
    }

    @Override
    public Void visitCurveAction(CurveAction curveAction) {
        curveAction.paramLeft.getSpeed().accept(this);
        curveAction.paramRight.getSpeed().accept(this);
        boolean speedOnly = !processOptionalDuration(curveAction.paramLeft.getDuration());
        DriveDirection driveDirection = (DriveDirection) curveAction.direction;
        JSONObject o =
            makeNode(C.CURVE_ACTION).put(C.DRIVE_DIRECTION, driveDirection).put(C.NAME, "orb").put(C.SPEED_ONLY, speedOnly).put(C.SET_TIME, false);
        if ( speedOnly ) {
            return app(o);
        } else {
            app(o);
            return app(makeNode(C.STOP_DRIVE).put(C.NAME, "orb"));
        }
    }

    @Override
    public Void visitMotorSetPowerAction(MotorSetPowerAction motorSetPowerAction) {
        String port = motorSetPowerAction.getUserDefinedPort();
        motorSetPowerAction.power.accept(this);
        JSONObject o = makeNode(C.MOTOR_SET_POWER).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public Void visitMotorStopAction(MotorStopAction motorStopAction) {
        String port = motorStopAction.getUserDefinedPort();
        JSONObject o = makeNode(C.MOTOR_STOP).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public Void visitMotorDriveStopAction(MotorDriveStopAction stopAction) {
        JSONObject o = makeNode(C.STOP_DRIVE).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitMotorOnAction(MotorOnAction motorOnAction) {
        motorOnAction.param.getSpeed().accept(this);
        MotorDuration duration = motorOnAction.param.getDuration();
        boolean speedOnly = !processOptionalDuration(duration);
        String port = motorOnAction.getUserDefinedPort();
        JSONObject o = makeNode(C.MOTOR_ON_ACTION).put(C.PORT, port.toLowerCase()).put(C.NAME, port.toLowerCase()).put(C.SPEED_ONLY, speedOnly);
        if ( speedOnly ) {
            return app(o);
        } else {
            app(o);
            return app(makeNode(C.MOTOR_STOP).put(C.PORT, port.toLowerCase()));
        }
    }

    @Override
    public Void visitClearDisplayAction(ClearDisplayAction clearDisplayAction) {
        return null;
    }

    @Override
    public Void visitShowTextAction(ShowTextAction showTextAction) {
        showTextAction.msg.accept(this);
        JSONObject o = makeNode(C.SHOW_TEXT_ACTION).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitTouchSensor(TouchSensor touchSensor) {
        String port = touchSensor.getUserDefinedPort();
        JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.TOUCH).put(C.PORT, port).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitColorSensor(ColorSensor colorSensor) {
        String mode = colorSensor.getMode();
        String port = colorSensor.getUserDefinedPort();
        JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.COLOR).put(C.PORT, port).put(C.MODE, mode.toLowerCase()).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitEncoderSensor(EncoderSensor encoderSensor) {
        String mode = encoderSensor.getMode().toLowerCase();
        String port = encoderSensor.getUserDefinedPort();
        JSONObject o;
        if ( mode.equals(C.RESET) ) {
            o = makeNode(C.ENCODER_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "orb");
        } else {
            o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.ENCODER_SENSOR_SAMPLE).put(C.PORT, port).put(C.MODE, mode).put(C.NAME, "orb");
        }
        return app(o);
    }

    @Override
    public Void visitKeysSensor(KeysSensor keysSensor) {
        String mode = keysSensor.getUserDefinedPort().toLowerCase();
        JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.BUTTONS).put(C.MODE, mode).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitTimerSensor(TimerSensor timerSensor) {
        String port = timerSensor.getUserDefinedPort();
        JSONObject o;
        if ( timerSensor.getMode().equals(SC.DEFAULT) || timerSensor.getMode().equals(SC.VALUE) ) {
            o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.TIMER).put(C.PORT, port).put(C.NAME, "orb");
        } else {
            o = makeNode(C.TIMER_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "orb");
        }
        return app(o);
    }
    @Override
    public Void visitCompassSensor(CompassSensor compassSensor) {
        // TODO check if this is really supported!
        String mode = compassSensor.getMode();
        JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.COMPASS).put(C.MODE, mode.toLowerCase()).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitGyroSensor(GyroSensor gyroSensor) {
        String mode = gyroSensor.getMode().toLowerCase();
        String port = gyroSensor.getUserDefinedPort();
        JSONObject o;
        if ( mode.equals(C.RESET) ) {
            o = makeNode(C.GYRO_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "orb");
        } else {
            o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.GYRO).put(C.MODE, mode).put(C.PORT, port).put(C.NAME, "orb");
        }
        return app(o);
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor infraredSensor) {
        final String mode = infraredSensor.getMode();
        final String port = infraredSensor.getUserDefinedPort();
        final JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.INFRARED).put(C.PORT, port).put(C.MODE, mode.toLowerCase()).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitUltrasonicSensor(UltrasonicSensor ultrasonicSensor) {
        String mode = ultrasonicSensor.getMode();
        String port = ultrasonicSensor.getUserDefinedPort();
        JSONObject o = makeNode(C.GET_SAMPLE).put(C.GET_SAMPLE, C.ULTRASONIC).put(C.PORT, port).put(C.MODE, mode.toLowerCase()).put(C.NAME, "orb");
        return app(o);
    }

    @Override
    public Void visitVolumeAction(VolumeAction volumeAction) {
        return null;
    }

    @Override
    public Void visitPlayFileAction(PlayFileAction playFileAction) {
        return null;
    }
}
