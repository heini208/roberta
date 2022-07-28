package de.fhg.iais.roberta.visitor;

import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveDistanceAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.syntax.sensor.robotino.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.robotino.OdometryReset;
import de.fhg.iais.roberta.visitor.hardware.sensor.ISensorVisitor;

/**
 * Interface to be used with the visitor pattern to traverse an AST (and generate code, e.g.).
 */
public interface IRobotinoVisitor<V> extends ISensorVisitor<V> {

    V visitTouchSensor(TouchSensor touchSensor);

    V visitOmnidriveAction(OmnidriveAction omnidriveAction);

    V visitOmnidriveDistanceAction(OmnidriveDistanceAction omnidriveDistanceAction);


    V visitMotorDriveStopAction(MotorDriveStopAction stopAction);

    V visitOmnidrivePositionAction(OmnidrivePositionAction omnidrivePositionAction);

    V visitOdometryPosition(OdometryPosition odometryPosition);

    V visitPinGetValueSensor(PinGetValueSensor pinGetValueSensor);

    V visitInfraredSensor(InfraredSensor infraredSensor);

    V visitOdometryReset(OdometryReset odometryReset);

    V visitPinWriteValueAction(PinWriteValueAction pinWriteValueAction);

    V visitTurnAction(TurnAction turnAction);

}