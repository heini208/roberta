package de.fhg.iais.roberta.visitor;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.CodeGeneratorSetupBean;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.bean.UsedHardwareBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.SC;
import de.fhg.iais.roberta.syntax.action.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.OmnidrivePositionAction;
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
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
import de.fhg.iais.roberta.syntax.sensor.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.visitor.lang.codegen.prog.AbstractPythonVisitor;

import java.util.List;

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
        this.sb.append("import rospy");
        nlIndent();
        this.sb.append("import math, random");
        nlIndent();
        //nur generieren wenn diese messages verwendet werden
        this.sb.append("from geometry_msgs.msg import Twist\n" +
                "from std_msgs.msg import Bool");
        nlIndent();
        nlIndent();
        this.sb.append("rospy.init_node('robotino_go', anonymous=True)");
        nlIndent();
        this.sb.append("_motorPub = rospy.Publisher('cmd_vel_repeating', Twist, queue_size=10)");
        nlIndent();
        this.sb.append("_twist = Twist()");

        nlIndent();


        if (!this.getBean(CodeGeneratorSetupBean.class).getUsedMethods().isEmpty()) {
            String helperMethodImpls =
                    this.getBean(CodeGeneratorSetupBean.class)
                            .getHelperMethodGenerator()
                            .getHelperMethodDefinitions(this.getBean(CodeGeneratorSetupBean.class).getUsedMethods());
            this.sb.append(helperMethodImpls);
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
        nlIndent();
        this.sb.append("global _twist, _motorPub");
        if (!this.usedGlobalVarInFunctions.isEmpty()) {
            nlIndent();
            this.sb.append("global ").append(String.join(", ", this.usedGlobalVarInFunctions));
        }
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
        decrIndentation();
        nlIndent();

        this.sb.append("main()");
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
        return null;
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
        this.sb.append("_twist.linear.x = ");
        omnidriveAction.xVel.accept(this);
        nlIndent();
        this.sb.append("_twist.linear.y = ");
        omnidriveAction.yVel.accept(this);
        nlIndent();
        this.sb.append("_twist.angular.z = ");
        omnidriveAction.thetaVel.accept(this);
        nlIndent();

        this.sb.append("_motorpub.publish(_twist)");
        nlIndent();
        return null;
    }

    @Override
    public Void visitOmnidrivePositionAction(OmnidrivePositionAction<Void> omnidrivePositionAction) {
        this.sb.append("POSITIONGENERATEDTEXT");
        return null;
    }

    @Override
    public Void visitOdometryPositionSensor(OdometryPosition<Void> odometryPosition) {
        return null;
    }

    @Override
    public Void visitPinGetValueSensor(PinGetValueSensor<Void> pinGetValueSensor) {
        this.sb.append("PINVALUE");
        return null;
    }

    @Override
    public Void visitInfraredSensor(InfraredSensor<Void> infraredSensor) {
        this.sb.append(infraredSensor.getUserDefinedPort());
        return null;
    }
}