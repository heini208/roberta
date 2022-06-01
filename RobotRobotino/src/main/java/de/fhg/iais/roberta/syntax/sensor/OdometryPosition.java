package de.fhg.iais.roberta.syntax.sensor;

import de.fhg.iais.roberta.blockly.generated.Hide;
import de.fhg.iais.roberta.blockly.generated.Mutation;
import de.fhg.iais.roberta.syntax.*;
import de.fhg.iais.roberta.transformer.NepoField;
import de.fhg.iais.roberta.transformer.NepoHide;
import de.fhg.iais.roberta.transformer.NepoMutation;
import de.fhg.iais.roberta.transformer.NepoPhrase;
import de.fhg.iais.roberta.util.dbc.Assert;

@NepoPhrase(containerType = "ODOMETRY_SENSING")
public class OdometryPosition<V> extends Sensor<V> implements WithUserDefinedPort<V> {
    @NepoField(name = BlocklyConstants.SENSORPORT, value = BlocklyConstants.EMPTY_PORT)
    public final String port;
    @NepoField(name = BlocklyConstants.SLOT, value = BlocklyConstants.EMPTY_SLOT)
    public final String slot;
    @NepoHide
    public final Hide hide;

    public OdometryPosition(BlockType kind, BlocklyBlockProperties properties, BlocklyComment comment, String port, String slot, Hide hide) {
        super(kind, properties, comment);
        Assert.nonEmptyString(port);
        this.port = port;
        this.slot = slot;
        this.hide = hide;
        setReadOnly();
    }

    /**
     * Creates instance of {@link OdometryPosition}. This instance is read only and can not be modified.
     *
     * @param properties of the block (see {@link BlocklyBlockProperties}),
     * @param comment    added from the user,
     * @return read only object of class {@link OdometryPosition}
     */
    public static <V> OdometryPosition<V> make(SensorMetaDataBean sensorMetaDataBean, BlocklyBlockProperties properties, BlocklyComment comment) {
        return new OdometryPosition<>(BlockTypeContainer.getByName("JOYSTICK_SENSING"), properties, comment, sensorMetaDataBean.getPort(), sensorMetaDataBean.getSlot(), null);
    }

    @Override
    public String getUserDefinedPort() {
        return this.port;
    }
}
