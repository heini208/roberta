package de.fhg.iais.roberta.syntax.action.robotino;

import de.fhg.iais.roberta.blockly.generated.Hide;
import de.fhg.iais.roberta.syntax.action.Action;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.transformer.forClass.NepoPhrase;
import de.fhg.iais.roberta.transformer.forField.NepoField;
import de.fhg.iais.roberta.transformer.forField.NepoHide;
import de.fhg.iais.roberta.transformer.forField.NepoValue;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.ast.BlocklyBlockProperties;
import de.fhg.iais.roberta.util.ast.BlocklyComment;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.syntax.BlocklyConstants;
import de.fhg.iais.roberta.util.syntax.WithUserDefinedPort;


@NepoPhrase(category = "ACTOR", blocklyNames = {"robActions_motorOmni_curve"}, name = "MOTOR_OMNIDRIVE_ACTION")
public class OmnidriveAction<V> extends Action<V> implements WithUserDefinedPort<V> {
    @NepoValue(name = BlocklyConstants.X, type = BlocklyType.NUMBER)
    public final Expr<V> xVel;
    @NepoValue(name = BlocklyConstants.Y, type = BlocklyType.NUMBER)
    public final Expr<V> yVel;
    @NepoValue(name = "THETA", type = BlocklyType.NUMBER)
    public final Expr<V> thetaVel;
    @NepoField(name = BlocklyConstants.ACTORPORT, value = BlocklyConstants.EMPTY_PORT)
    public final String port;
    @NepoHide
    public final Hide hide;

    public OmnidriveAction(BlocklyBlockProperties properties, BlocklyComment comment, Expr<V> xVel, Expr<V> yVel, Expr<V> thetaVel, String port, Hide hide) {
        super(properties, comment);
        Assert.nonEmptyString(port);

        this.hide = hide;
        this.xVel = xVel;
        this.yVel = yVel;
        this.thetaVel = thetaVel;
        this.port = port;

        setReadOnly();
    }

    @Override
    public String getUserDefinedPort() {
        return this.port;
    }
}

