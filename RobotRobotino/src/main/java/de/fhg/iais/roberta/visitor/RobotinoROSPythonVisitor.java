package de.fhg.iais.roberta.visitor;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.CodeGeneratorSetupBean;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.bean.UsedHardwareBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.ConfigurationComponent;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.constants.RobotinoConstants;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.SC;
import de.fhg.iais.roberta.syntax.action.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.action.display.ClearDisplayAction;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
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
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyExpr;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
import de.fhg.iais.roberta.syntax.sensor.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.OdometryReset;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.util.dbc.DbcException;
import de.fhg.iais.roberta.visitor.lang.codegen.prog.AbstractPythonVisitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is implementing {@link IVisitor}. All methods are implemented and they append a human-readable Python code representation of a phrase to a
 * StringBuilder. <b>This representation is correct Python code.</b> <br>
 */
public final class RobotinoROSPythonVisitor extends AbstractPythonVisitor implements IRobotinoVisitor<Void> {

    private final ConfigurationAst configurationAst;

    /**
     * initialize the Python code generator visitor.
     *
     * @param programPhrases to generate the code from
     */
    public RobotinoROSPythonVisitor(
            List<List<Phrase<Void>>> programPhrases, ClassToInstanceMap<IProjectBean> beans, ConfigurationAst configurationAst) {
        super(programPhrases, beans);
        this.configurationAst = configurationAst;
    }

    @Override
    protected void generateProgramPrefix(boolean withWrapping) {
        this.sb.append("#!/usr/bin/env python3");
        nlIndent();
        this.sb.append("import rospy");
        nlIndent();
        this.sb.append("import math, random");
        nlIndent();
        generateImports();
        nlIndent();
        this.sb.append("rospy.init_node('robotino_go', anonymous=True)");
        nlIndent();
        generatePublishers();
        generateTimerVariables();
        nlIndent();
        if (!this.getBean(CodeGeneratorSetupBean.class).getUsedMethods().isEmpty()) {
            String helperMethodImpls =
                    this.getBean(CodeGeneratorSetupBean.class)
                            .getHelperMethodGenerator()
                            .getHelperMethodDefinitions(this.getBean(CodeGeneratorSetupBean.class).getUsedMethods());
            this.sb.append(helperMethodImpls);
        }
    }

    private void generateImports() {
        if (this.getBean(UsedHardwareBean.class).isActorUsed(RobotinoConstants.OMNIDRIVE)) {
            this.sb.append("from geometry_msgs.msg import Twist");
            nlIndent();
        }
        if (this.getBean(UsedHardwareBean.class).isSensorUsed(SC.TOUCH)) {
            this.sb.append("from std_msgs.msg import Bool");
            nlIndent();
        }
        if (this.getBean(UsedHardwareBean.class).isActorUsed(SC.DIGITAL_PIN) || this.getBean(UsedHardwareBean.class).isSensorUsed(SC.DIGITAL_INPUT)) {
            this.sb.append("from robotino_msgs.msg import DigitalReadings");
            nlIndent();
        }
        if (this.getBean(UsedHardwareBean.class).isSensorUsed(RobotinoConstants.ODOMETRY)) {
            this.sb.append("from nav_msgs.msg import Odometry\n");
            this.sb.append("from robotino_msgs.srv import ResetOdometry");
            nlIndent();
        }
    }

    private void generatePublishers() {
        if (this.getBean(UsedHardwareBean.class).isActorUsed(RobotinoConstants.OMNIDRIVE)) {
            this.sb.append("_motorPub = rospy.Publisher('cmd_vel_repeating', Twist, queue_size=10)");
            nlIndent();
        }
        if (this.getBean(UsedHardwareBean.class).isActorUsed(SC.DIGITAL_PIN)) {
            this.sb.append("_digitalPinPub = rospy.Publisher('set_digital_values', DigitalReadings, queue_size=10)");
            nlIndent();
            this.sb.append("_digitalPinValues = [False for i in range(8)]");
            nlIndent();
        }
    }

    @Override
    public Void visitMainTask(MainTask<Void> mainTask) {
        StmtList<Void> variables = mainTask.getVariables();
        variables.accept(this);
        generateUserDefinedMethods();
        nlIndent();
        this.sb.append("def run():");
        incrIndentation();
        if (!this.usedGlobalVarInFunctions.isEmpty()) {
            nlIndent();
            this.sb.append("global ").append(String.join(", ", this.usedGlobalVarInFunctions));
        }
        nlIndent();
        this.sb.append("print(\"starting roberta node...\")");
        if (this.getBean(UsedHardwareBean.class).isSensorUsed(RobotinoConstants.ODOMETRY)) {
            this.sb.append("rospy.ServiceProxy('reset_odometry', ResetOdometry)(0, 0, 0)");
        }
        //cannot immediately publish after determining publishers so waiting time is added at the beginning
        nlIndent();
        this.sb.append("rospy.sleep(0.3)");
        nlIndent();
        nlIndent();

        return null;
    }

    @Override
    protected void generateProgramSuffix(boolean withWrapping) {
        if (!withWrapping) {
            return;
        }
        decrIndentation(); // everything is still indented from main program
        nlIndent();
        nlIndent();
        this.sb.append("def main():");
        incrIndentation();
        nlIndent();
        this.sb.append("try:");
        incrIndentation();
        nlIndent();
        this.sb.append("run()");
        decrIndentation();
        nlIndent();
        this.sb.append("except Exception as e:");
        incrIndentation();
        nlIndent();
        this.sb.append("raise");
        decrIndentation();
        nlIndent();
        decrIndentation();
        generateFinally();
        decrIndentation();
        nlIndent();

        this.sb.append("main()");
    }

    private void generateFinally() {
        if (this.getBean(UsedHardwareBean.class).isActorUsed(RobotinoConstants.OMNIDRIVE) || this.getBean(UsedHardwareBean.class).isActorUsed(SC.DIGITAL_PIN)) {
            this.sb.append("finally:");
        }
        incrIndentation();
        incrIndentation();

        nlIndent();
        if (this.getBean(UsedHardwareBean.class).isActorUsed(RobotinoConstants.OMNIDRIVE)) {
            this.sb.append("_motorPub.publish(Twist())");
            nlIndent();
        }
        if (this.getBean(UsedHardwareBean.class).isActorUsed(SC.DIGITAL_PIN)) {
            this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(RobotinoMethods.RESETDIGITALPIN))
                    .append("()");
            nlIndent();
        }
        decrIndentation();
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
        this.sb.append("_motorPub.publish(Twist())");
        nlIndent();
        return null;
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
    public Void visitMotorGetPowerAction(MotorGetPowerAction<Void> motorGetPowerAction) {
        return null;
    }

    @Override
    public Void visitMotorOnAction(MotorOnAction<Void> motorOnAction) {
        return null;
    }

    @Override
    public Void visitMotorSetPowerAction(MotorSetPowerAction<Void> motorSetPowerAction) {
        return null;
    }

    @Override
    public Void visitMotorStopAction(MotorStopAction<Void> motorStopAction) {
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
    public Void visitConnectConst(ConnectConst<Void> connectConst) {
        return null;
    }


    @Override
    public Void visitTimerSensor(TimerSensor<Void> timerSensor) {
        switch (timerSensor.getMode()) {
            case SC.DEFAULT:
            case SC.VALUE:
                this.sb.append("((rospy.get_time() - _timer").append(timerSensor.getUserDefinedPort()).append(")*1000)");
                break;
            case SC.RESET:
                this.sb.append("_timer").append(timerSensor.getUserDefinedPort()).append(" = rospy.get_time()");
                break;
            default:
                throw new DbcException("Invalid Time Mode!");
        }
        return null;
    }

    private void generateTimerVariables() {
        this.getBean(UsedHardwareBean.class)
                .getUsedSensors()
                .stream()
                .filter(usedSensor -> usedSensor.getType().equals(SC.TIMER))
                .collect(Collectors.groupingBy(UsedSensor::getPort))
                .keySet()
                .forEach(port -> {
                    this.usedGlobalVarInFunctions.add("_timer" + port);
                    this.sb.append("_timer").append(port).append(" = rospy.get_time()");
                    nlIndent();
                });
    }

    @Override
    public Void visitWaitStmt(WaitStmt<Void> waitStmt) {
        return null;
    }

    @Override
    public Void visitWaitTimeStmt(WaitTimeStmt<Void> waitTimeStmt) {
        this.sb.append("rospy.sleep(");
        waitTimeStmt.getTime().accept(this);
        this.sb.append("/1000)");
        return null;
    }


    @Override
    public Void visitTouchSensor(TouchSensor<Void> touchSensor) {
        this.sb.append("rospy.wait_for_message(\"bumper\", Bool)");
        return null;
    }

    @Override
    public Void visitOmnidriveAction(OmnidriveAction<Void> omnidriveAction) {

        if (!(omnidriveAction.distance instanceof EmptyExpr)) {
            this.sb.append("checkdistancePLACEHOLDER");
        } else {
            this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(RobotinoMethods.OMNIDRIVESPEED));
            this.sb.append("(");
            omnidriveAction.xVel.accept(this);
            this.sb.append(", ");
            omnidriveAction.yVel.accept(this);
            this.sb.append(", ");
            omnidriveAction.thetaVel.accept(this);
            this.sb.append(")");
        }
        return null;
    }

    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction<Void> omnidrivePositionAction) {
        this.sb.append("DriveToPositionPlaceHolder");
        return null;
    }

    @Override
    public Void visitOdometryPosition(OdometryPosition<Void> odometryPosition) {
        if (odometryPosition.slot.equals("THETA")) {
            this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(RobotinoMethods.GETORIENTATION))
                    .append("()");
        } else {
            this.sb.append("rospy.wait_for_message(\"odom\", Odometry).pose.pose.position.")
                    .append(odometryPosition.slot.toLowerCase());
        }
        return null;
    }

    @Override
    public Void visitPinGetValueSensor(PinGetValueSensor<Void> pinGetValueSensor) {
        this.sb.append("PINVALUE ");
        this.sb.append("port: " + configurationAst.getConfigurationComponent(pinGetValueSensor.getUserDefinedPort()).getComponentProperties().get("OUTPUT"));

        return null;
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor<Void> infraredSensor) {
        this.sb.append(infraredSensor.getUserDefinedPort() + "infrared placeholder");
        return null;
    }

    @Override
    public Void visitOdometryReset(OdometryReset<Void> odometryReset) {
        this.sb.append("odometryReset");
        return null;
    }

    @Override
    public Void visitPinWriteValueAction(PinWriteValueAction<Void> pinWriteValueAction) {
        this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(RobotinoMethods.SETDIGITALPIN))
                .append("(")
                .append(configurationAst.getConfigurationComponent(pinWriteValueAction.getPort()).getComponentProperties().get("INPUT"))
                .append(", ");
        pinWriteValueAction.getValue().accept(this);
        this.sb.append(")");
        return null;
    }
}