package de.fhg.iais.roberta.syntax.action;

import de.fhg.iais.roberta.blockly.generated.Hide;
import de.fhg.iais.roberta.syntax.*;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.transformer.NepoField;
import de.fhg.iais.roberta.transformer.NepoHide;
import de.fhg.iais.roberta.transformer.NepoPhrase;
import de.fhg.iais.roberta.transformer.NepoValue;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.dbc.Assert;

@NepoPhrase(containerType = "MOTOR_OMNIDRIVE_ACTION")
public class OmnidriveAction<V> extends Action<V> implements WithUserDefinedPort<V> {
    @NepoValue(name = BlocklyConstants.X, type = BlocklyType.NUMBER)
    public final Expr<V> xVel;
    @NepoValue(name = BlocklyConstants.Y, type = BlocklyType.NUMBER)
    public final Expr<V> yVel;
    @NepoValue(name = BlocklyConstants.Z, type = BlocklyType.NUMBER)
    public final Expr<V> zVel;
    @NepoField(name = BlocklyConstants.TYPE, value = "LINEAR_VEL")
    public final String velType;
    @NepoField(name = BlocklyConstants.ACTORPORT, value = BlocklyConstants.EMPTY_PORT)
    public final String port;

    @NepoHide
    public final Hide hide;

    public OmnidriveAction(BlockType kind, BlocklyBlockProperties properties, BlocklyComment comment, Expr<V> xVel, Expr<V> yVel, Expr<V> zVel, String velType, String port, Hide hide) {
        super(kind, properties, comment);
        Assert.nonEmptyString(velType);
        Assert.nonEmptyString(port);

        this.hide = hide;
        this.xVel = xVel;
        this.yVel = yVel;
        this.zVel = zVel;
        this.velType = velType;
        this.port = port;

        setReadOnly();
    }

    @Override
    public String getUserDefinedPort() {
        return this.port;
    }
}

