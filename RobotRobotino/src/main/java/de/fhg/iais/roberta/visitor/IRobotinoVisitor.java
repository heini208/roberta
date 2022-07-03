package de.fhg.iais.roberta.visitor;

import de.fhg.iais.roberta.syntax.action.generic.PinWriteValueAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.robotino.OmnidriveActionDistance;
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

    V visitTouchSensor(TouchSensor<V> touchSensor);

    V visitOmnidriveAction(OmnidriveAction<V> omnidriveAction);

    V visitOmnidriveActionDistance(OmnidriveActionDistance<V> omnidriveActionDistance);


    V visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction);

    V visitOmnidrivePositionAction(OmnidrivePositionAction<V> omnidrivePositionAction);

    V visitOdometryPosition(OdometryPosition<V> odometryPosition);

    V visitPinGetValueSensor(PinGetValueSensor<V> pinGetValueSensor);

    V visitInfraredSensor(InfraredSensor<V> infraredSensor);

    V visitOdometryReset(OdometryReset<V> odometryReset);

    V visitPinWriteValueAction(PinWriteValueAction<V> pinWriteValueAction);

    V visitTurnAction(TurnAction<V> turnAction);

}