package de.fhg.iais.roberta.syntax.action.thymio;

import de.fhg.iais.roberta.syntax.action.Action;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.transformer.forClass.NepoPhrase;
import de.fhg.iais.roberta.transformer.forField.NepoValue;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.ast.BlocklyProperties;

@NepoPhrase(category = "ACTOR", blocklyNames = {"robActions_red_led"}, name = "THYMIO_RED_LED_ON_ACTION")
public final class RedLedOnAction extends Action {
    @NepoValue(name = "LED1", type = BlocklyType.NUMBER)
    public final Expr led1;
    @NepoValue(name = "LED2", type = BlocklyType.NUMBER)
    public final Expr led2;
    @NepoValue(name = "LED3", type = BlocklyType.NUMBER)
    public final Expr led3;
    @NepoValue(name = "LED4", type = BlocklyType.NUMBER)
    public final Expr led4;

    public RedLedOnAction(BlocklyProperties properties, Expr led1, Expr led2, Expr led3, Expr led4) {
        super(properties);
        this.led1 = led1;
        this.led2 = led2;
        this.led3 = led3;
        this.led4 = led4;
        setReadOnly();
    }
}
