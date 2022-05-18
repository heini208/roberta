package de.fhg.iais.roberta.visitor;

import de.fhg.iais.roberta.syntax.action.thymio.PlayRecordingAction;
import de.fhg.iais.roberta.syntax.action.thymio.RedLedOnAction;
import de.fhg.iais.roberta.syntax.action.thymio.YellowLedOnAction;
import de.fhg.iais.roberta.visitor.hardware.actor.IActors4AutonomousDriveRobots;
import de.fhg.iais.roberta.visitor.hardware.sensor.ISensorVisitor;

public interface IThymioVisitor<V> extends IActors4AutonomousDriveRobots<V>, ISensorVisitor<V> {
    V visitPlayRecordingAction(PlayRecordingAction playRecordingAction);

    V visitRedLedOnAction(RedLedOnAction redLedOnAction);

    V visitYellowLedOnAction(YellowLedOnAction yellowLedOnAction);
}
