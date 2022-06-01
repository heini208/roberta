package de.fhg.iais.roberta.visitor;

import de.fhg.iais.roberta.syntax.action.OmnidriveAction;
import de.fhg.iais.roberta.syntax.action.OmnidrivePositionAction;
import de.fhg.iais.roberta.syntax.sensor.OdometryPosition;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.PinGetValueSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.visitor.hardware.actor.IActors4AutonomousDriveRobots;
import de.fhg.iais.roberta.visitor.hardware.sensor.ISensorVisitor;

/**
 * Interface to be used with the visitor pattern to traverse an AST (and generate code, e.g.).
 */
public interface IRobotinoVisitor<V> extends IActors4AutonomousDriveRobots<V>, ISensorVisitor<V> {

    V visitTouchSensor(TouchSensor<V> touchSensor);

    V visitOmnidriveAction(OmnidriveAction<V> omnidriveAction);

    V visitOmnidrivePositionAction(OmnidrivePositionAction<V> omnidrivePositionAction);

    V visitOdometryPositionSensor(OdometryPosition<V> odometryPosition);

    V visitPinGetValueSensor(PinGetValueSensor<V> pinGetValueSensor);

    V visitInfraredSensor(InfraredSensor<V> infraredSensor);

}