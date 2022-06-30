package de.fhg.iais.roberta.syntax.lang.expr;

import java.util.Locale;

import de.fhg.iais.roberta.transformer.forClass.NepoExpr;
import de.fhg.iais.roberta.transformer.forField.NepoField;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.ast.BlocklyBlockProperties;
import de.fhg.iais.roberta.util.ast.BlocklyComment;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.dbc.DbcException;

@NepoExpr(category = "EXPR", blocklyNames = {"math_constant"}, name = "MATH_CONST", blocklyType = BlocklyType.NUMBER)
public final class MathConst<V> extends Expr<V> {
    @NepoField(name = "CONSTANT")
    public final Const mathConst;

    public MathConst(BlocklyBlockProperties properties, BlocklyComment comment, Const mathConst) {
        super(properties, comment);
        Assert.isTrue(mathConst != null);
        this.mathConst = mathConst;
        setReadOnly();
    }

    public static <V> MathConst<V> make(Const mathConst, BlocklyBlockProperties properties, BlocklyComment comment) {
        return new MathConst<V>(properties, comment, mathConst);
    }

    public Const getMathConst() {
        return this.mathConst;
    }

    public enum Const {
        GOLDEN_RATIO(), PI(), E(), SQRT2(), SQRT1_2(), INFINITY();

        public final String[] values;

        Const(String... values) {
            this.values = values;
        }

        public static Const get(String s) {
            if ( s == null || s.isEmpty() ) {
                throw new DbcException("Invalid binary operator symbol: " + s);
            }
            String sUpper = s.trim().toUpperCase(Locale.GERMAN);
            for ( Const co : Const.values() ) {
                if ( co.toString().equals(sUpper) ) {
                    return co;
                }
                for ( String value : co.values ) {
                    if ( sUpper.equals(value) ) {
                        return co;
                    }
                }
            }
            throw new DbcException("Invalid binary constant symbol: " + s);
        }
    }
}
