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
import de.fhg.iais.roberta.util.syntax.*;

@NepoPhrase(category = "ACTOR", blocklyNames = {"robActions_motorOmni_position"}, name = "MOTOR_OMNIDRIVE_POSITION_ACTION")
public class OmnidrivePositionAction<V> extends Action<V> implements WithUserDefinedPort<V> {
    @NepoValue(name = BlocklyConstants.X, type = BlocklyType.NUMBER)
    public final Expr<V> x;
    @NepoValue(name = BlocklyConstants.Y, type = BlocklyType.NUMBER)
    public final Expr<V> y;
    @NepoValue(name = BlocklyConstants.POWER, type = BlocklyType.NUMBER)
    public final Expr<V> power;

    @NepoField(name = BlocklyConstants.ACTORPORT, value = BlocklyConstants.EMPTY_PORT)
    public final String port;

    @NepoHide
    public final Hide hide;

    public OmnidrivePositionAction(BlocklyBlockProperties properties, BlocklyComment comment, Expr<V> x, Expr<V> y, Expr<V> power, String port, Hide hide) {
        super(properties, comment);
        Assert.nonEmptyString(port);

        this.hide = hide;
        this.x = x;
        this.y = y;
        this.power = power;
        this.port = port;

        setReadOnly();
    }

    @Override
    public String getUserDefinedPort() {
        return this.port;
    }
}