package de.fhg.iais.roberta.syntax.sensor;

import de.fhg.iais.roberta.blockly.generated.Hide;
import de.fhg.iais.roberta.syntax.*;
import de.fhg.iais.roberta.transformer.NepoField;
import de.fhg.iais.roberta.transformer.NepoHide;
import de.fhg.iais.roberta.transformer.NepoPhrase;
import de.fhg.iais.roberta.util.dbc.Assert;

@NepoPhrase(containerType = "ODOMETRY_RESET")
public class OdometryReset<V> extends Sensor<V> {

    public OdometryReset(BlockType kind, BlocklyBlockProperties properties, BlocklyComment comment) {
        super(kind, properties, comment);
        setReadOnly();
    }

    /**
     * Creates instance of {@link OdometryReset}. This instance is read only and can not be modified.
     *
     * @param properties of the block (see {@link BlocklyBlockProperties}),
     * @param comment    added from the user,
     * @return read only object of class {@link OdometryReset}
     */
    public static <V> OdometryReset<V> make(BlocklyBlockProperties properties, BlocklyComment comment) {
        return new OdometryReset<>(BlockTypeContainer.getByName("JOYSTICK_SENSING"), properties, comment);
    }

}
