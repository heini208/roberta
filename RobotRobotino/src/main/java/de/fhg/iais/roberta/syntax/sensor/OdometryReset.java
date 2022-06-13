package de.fhg.iais.roberta.syntax.sensor;

import de.fhg.iais.roberta.blockly.generated.Hide;
import de.fhg.iais.roberta.syntax.*;
import de.fhg.iais.roberta.transformer.NepoField;
import de.fhg.iais.roberta.transformer.NepoHide;
import de.fhg.iais.roberta.transformer.NepoPhrase;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.syntax.*;

@NepoPhrase(containerType = "ODOMETRY_RESET")
public class OdometryReset<V> extends Sensor<V> implements WithUserDefinedPort<V> {
    @NepoField(name = BlocklyConstants.SENSORPORT, value = BlocklyConstants.EMPTY_PORT)
    public final String port;
    @NepoField(name = BlocklyConstants.SLOT, value = BlocklyConstants.EMPTY_SLOT)
    public final String slot;
    @NepoHide
    public final Hide hide;

    public OdometryReset(BlockType kind, BlocklyBlockProperties properties, BlocklyComment comment, String port, String slot, Hide hide) {
        super(kind, properties, comment);
        Assert.nonEmptyString(port);
        this.port = port;
        this.slot = slot;
        this.hide = hide;
        setReadOnly();
    }

    /**
     * Creates instance of {@link OdometryReset}. This instance is read only and can not be modified.
     *
     * @param properties of the block (see {@link BlocklyBlockProperties}),
     * @param comment    added from the user,
     * @return read only object of class {@link OdometryReset}
     */
    public static <V> OdometryReset<V> make(BlocklyBlockProperties properties, BlocklyComment comment, String port, String slot, Hide hide) {
        return new OdometryReset<>(BlockTypeContainer.getByName("ODOMETRY_RESET"), properties, comment, port, slot, hide);
    }

    @Override
    public String getUserDefinedPort() {
        return this.port;
    }
}
