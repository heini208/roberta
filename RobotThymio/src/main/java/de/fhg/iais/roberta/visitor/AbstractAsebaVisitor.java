package de.fhg.iais.roberta.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import com.google.common.collect.ClassToInstanceMap;

import de.fhg.iais.roberta.bean.CodeGeneratorSetupBean;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.inter.mode.general.IMode;
import de.fhg.iais.roberta.mode.general.IndexLocation;
import static de.fhg.iais.roberta.mode.general.ListElementOperations.GET;
import static de.fhg.iais.roberta.mode.general.ListElementOperations.GET_REMOVE;
import static de.fhg.iais.roberta.mode.general.ListElementOperations.INSERT;
import static de.fhg.iais.roberta.mode.general.ListElementOperations.REMOVE;
import static de.fhg.iais.roberta.mode.general.ListElementOperations.SET;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.serial.SerialWriteAction;
import de.fhg.iais.roberta.syntax.lang.expr.Binary;
import de.fhg.iais.roberta.syntax.lang.expr.BoolConst;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyExpr;
import de.fhg.iais.roberta.syntax.lang.expr.EmptyList;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.syntax.lang.expr.ExprList;
import de.fhg.iais.roberta.syntax.lang.expr.FunctionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.ListCreate;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst;
import de.fhg.iais.roberta.syntax.lang.expr.NullConst;
import de.fhg.iais.roberta.syntax.lang.expr.Unary;
import de.fhg.iais.roberta.syntax.lang.expr.VarDeclaration;
import de.fhg.iais.roberta.syntax.lang.functions.GetSubFunct;
import de.fhg.iais.roberta.syntax.lang.functions.IndexOfFunct;
import de.fhg.iais.roberta.syntax.lang.functions.LengthOfIsEmptyFunct;
import de.fhg.iais.roberta.syntax.lang.functions.ListGetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.ListRepeat;
import de.fhg.iais.roberta.syntax.lang.functions.ListSetIndex;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastCharFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastStringFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathConstrainFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathNumPropFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathOnListFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathPowerFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomFloatFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomIntFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathSingleFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextCharCastNumberFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextJoinFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextPrintFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextStringCastNumberFunct;
import de.fhg.iais.roberta.syntax.lang.methods.MethodCall;
import de.fhg.iais.roberta.syntax.lang.methods.MethodIfReturn;
import de.fhg.iais.roberta.syntax.lang.methods.MethodReturn;
import de.fhg.iais.roberta.syntax.lang.methods.MethodVoid;
import de.fhg.iais.roberta.syntax.lang.stmt.AssertStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.AssignStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.DebugAction;
import de.fhg.iais.roberta.syntax.lang.stmt.IfStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.RepeatStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtFlowCon;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtTextComment;
import de.fhg.iais.roberta.syntax.lang.stmt.TernaryExpr;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.typecheck.BlocklyType;
import de.fhg.iais.roberta.util.dbc.DbcException;
import de.fhg.iais.roberta.util.syntax.FunctionNames;
import de.fhg.iais.roberta.visitor.lang.codegen.AbstractLanguageVisitor;

public abstract class AbstractAsebaVisitor extends AbstractLanguageVisitor {
    protected Set<String> usedGlobalVarInFunctions = new OrderedHashSet<>();
    protected int stateCounter = 0;
    protected int noOfStates = 0;
    protected int noOfLoopStates = 0; // TODO should be arrays to accommodate nested loops
    protected int loopCounter = 0; // TODO should be arrays to accommodate nested loops
    protected int funcCounter = 0;
    protected boolean isLoop = false;
    protected boolean isFunc = false;

    protected AbstractAsebaVisitor(List<List<Phrase>> programPhrases, ClassToInstanceMap<IProjectBean> beans) {
        super(programPhrases, beans);
    }

    protected static Map<Binary.Op, String> binaryOpSymbols() {
        return Collections
            .unmodifiableMap(
                Stream
                    .of(
                        AbstractLanguageVisitor.entry(Binary.Op.ADD, "+"),
                        AbstractLanguageVisitor.entry(Binary.Op.MINUS, "-"),
                        AbstractLanguageVisitor.entry(Binary.Op.MULTIPLY, "*"),
                        AbstractLanguageVisitor.entry(Binary.Op.DIVIDE, "/"),
                        AbstractLanguageVisitor.entry(Binary.Op.MOD, "%"),
                        AbstractLanguageVisitor.entry(Binary.Op.EQ, "=="),
                        AbstractLanguageVisitor.entry(Binary.Op.NEQ, "!="),
                        AbstractLanguageVisitor.entry(Binary.Op.LT, "<"),
                        AbstractLanguageVisitor.entry(Binary.Op.LTE, "<="),
                        AbstractLanguageVisitor.entry(Binary.Op.GT, ">"),
                        AbstractLanguageVisitor.entry(Binary.Op.GTE, ">="),
                        AbstractLanguageVisitor.entry(Binary.Op.AND, "and"),
                        AbstractLanguageVisitor.entry(Binary.Op.OR, "or"),
                        AbstractLanguageVisitor.entry(Binary.Op.MATH_CHANGE, "+="),
                        AbstractLanguageVisitor.entry(Binary.Op.TEXT_APPEND, "+="),
                        AbstractLanguageVisitor.entry(Binary.Op.IN, "in"),
                        AbstractLanguageVisitor.entry(Binary.Op.ASSIGNMENT, "="),
                        AbstractLanguageVisitor.entry(Binary.Op.ADD_ASSIGNMENT, "+="),
                        AbstractLanguageVisitor.entry(Binary.Op.MINUS_ASSIGNMENT, "-="),
                        AbstractLanguageVisitor.entry(Binary.Op.MULTIPLY_ASSIGNMENT, "*="),
                        AbstractLanguageVisitor.entry(Binary.Op.DIVIDE_ASSIGNMENT, "/="),
                        AbstractLanguageVisitor.entry(Binary.Op.MOD_ASSIGNMENT, "%=")

                    )
                    .collect(AbstractLanguageVisitor.entriesToMap()));
    }

    protected static Map<Unary.Op, String> unaryOpSymbols() {
        return Collections
            .unmodifiableMap(
                Stream
                    .of(
                        AbstractLanguageVisitor.entry(Unary.Op.PLUS, "+"),
                        AbstractLanguageVisitor.entry(Unary.Op.NEG, "-"),
                        AbstractLanguageVisitor.entry(Unary.Op.NOT, "not"),
                        AbstractLanguageVisitor.entry(Unary.Op.POSTFIX_INCREMENTS, "++"),
                        AbstractLanguageVisitor.entry(Unary.Op.PREFIX_INCREMENTS, "++")

                    )
                    .collect(AbstractLanguageVisitor.entriesToMap()));
    }

    @Override
    public String getEnumCode(IMode value) {
        return "'" + value.toString().toLowerCase() + "'";
    }

    @Override
    public String getEnumCode(String value) {
        return "'" + value.toLowerCase() + "'";
    }

    @Override
    public Void visitAssertStmt(AssertStmt assertStmt) {
        this.sb.append("if not ");
        assertStmt.asserts.accept(this);
        this.sb.append(":");
        incrIndentation();
        nlIndent();
        this.sb.append("print(\"Assertion failed: \", \"").append(assertStmt.msg).append("\", ");
        ((Binary) assertStmt.asserts).left.accept(this);
        this.sb.append(", \"").append(((Binary) assertStmt.asserts).op.toString()).append("\", ");
        ((Binary) assertStmt.asserts).getRight().accept(this);
        this.sb.append(")");
        decrIndentation();
        return null;
    }

    @Override
    public Void visitAssignStmt(AssignStmt assignStmt) {
        if ( assignStmt.expr.getClass().equals(Unary.class) ) {
            if ( ((Unary) assignStmt.expr).op.name().equals("NOT") )
                throw new DbcException("Cannot use NOT in assignment operations!");
        }

        if ( !assignStmt.expr.getClass().equals(FunctionExpr.class) ) {
            assignStmt.name.accept(this);
            src.add(" = ");
            assignStmt.expr.accept(this);
        } else {
            if ( ((MathSingleFunct) ((FunctionExpr) assignStmt.expr).getFunction()).functName.toString().equals("ABS") ) {
                assignStmt.name.accept(this);
                src.add(" = ");
                assignStmt.expr.accept(this);
            } else {
                assignStmt.expr.accept(this);
                assignStmt.name.accept(this);
                src.add(" = _result");
            }
        }
        return null;
    }

    @Override
    public Void visitBinary(Binary binary) {
        this.sb.append("( ");
        try {
            VarDeclaration variablePart = (VarDeclaration) binary.left;
            this.sb.append(variablePart.getCodeSafeName());
        } catch ( ClassCastException e ) {
            generateSubExpr(this.sb, false, binary.left, binary);
        }
        Binary.Op op = binary.op;
        String sym = getBinaryOperatorSymbol(op);
        switch ( op.toString() ) {
            case "AND":
            case "OR":
                this.sb.append(" == 1 ").append(sym).append(" 1 == ");
                break;
            default:
                this.sb.append(' ').append(sym).append(' ');
                break;
        }
        generateCodeRightExpression(binary, op);
        this.sb.append(" )");
        return null;
    }

    @Override
    public Void visitBoolConst(BoolConst boolConst) {
        this.sb.append(boolConst.value ? "1" : "0");
        return null;
    }

    @Override
    public Void visitConnectConst(ConnectConst connectConst) {
        return null;
    }

    @Override
    public Void visitDebugAction(DebugAction debugAction) {
        this.sb.append("print(");
        debugAction.value.accept(this);
        this.sb.append(")");
        return null;
    }

    @Override
    public Void visitEmptyExpr(EmptyExpr emptyExpr) {
        switch ( emptyExpr.getDefVal() ) {
            case BOOLEAN:
                this.sb.append("1");
                break;
            case NUMBER_INT:
                this.sb.append("0");
                break;
            case ARRAY:
                this.sb.append("[0]");
                break;
            case NULL:
                break;
            default:
                this.sb.append("[[EmptyExpr [defVal=" + emptyExpr.getDefVal() + "]]]");
                break;
        }
        return null;
    }

    @Override
    public Void visitEmptyList(EmptyList emptyList) {
        this.sb.append("[]");
        return null;
    }

    @Override
    public Void visitGetSubFunct(GetSubFunct getSubFunct) {
        if ( getSubFunct.functName == FunctionNames.GET_SUBLIST ) {
            getSubFunct.param.get(0).accept(this);
            this.sb.append("[");
            switch ( (IndexLocation) getSubFunct.strParam.get(0) ) {
                case FIRST:
                    this.sb.append("0:");
                    break;
                case FROM_END:
                    this.sb.append("-1 -");
                    getSubFunct.param.get(1).accept(this);
                    this.sb.append(":");
                    break;
                case FROM_START:
                    getSubFunct.param.get(1).accept(this);
                    this.sb.append(":");
                    break;
                default:
                    break;
            }
            switch ( (IndexLocation) getSubFunct.strParam.get(1) ) {
                case LAST:
                    // append nothing
                    break;
                case FROM_END:
                    this.sb.append("-1 -");
                    try {
                        getSubFunct.param.get(2).accept(this);
                    } catch ( IndexOutOfBoundsException e ) { // means that our start index does not have a variable
                        getSubFunct.param.get(1).accept(this);
                    }
                    break;
                case FROM_START:
                    try {
                        getSubFunct.param.get(2).accept(this);
                    } catch ( IndexOutOfBoundsException e ) { // means that our start index does not have a variable
                        getSubFunct.param.get(1).accept(this);
                    }
                    break;
                default:
                    break;
            }
            this.sb.append("]");
        }
        return null;
    }

    @Override
    public Void visitIndexOfFunct(IndexOfFunct indexOfFunct) {
        switch ( (IndexLocation) indexOfFunct.location ) {
            case FIRST:
                indexOfFunct.param.get(0).accept(this);
                this.sb.append(".index(");
                indexOfFunct.param.get(1).accept(this);
                this.sb.append(")");
                break;
            case LAST:
                this.sb.append("(len(");
                indexOfFunct.param.get(0).accept(this);
                this.sb.append(") - 1) - ");
                indexOfFunct.param.get(0).accept(this);
                this.sb.append("[::-1].index(");
                indexOfFunct.param.get(1).accept(this);
                this.sb.append(")");
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitLengthOfIsEmptyFunct(LengthOfIsEmptyFunct lengthOfIsEmptyFunct) {
        switch ( lengthOfIsEmptyFunct.functName ) {
            case LIST_LENGTH:
                this.sb.append("len( ");
                lengthOfIsEmptyFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;

            case LIST_IS_EMPTY:
                this.sb.append("not ");
                lengthOfIsEmptyFunct.param.get(0).accept(this);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitListCreate(ListCreate listCreate) {
        if ( listCreate.typeVar.toString().equals("COLOR") ) {
            this.sb.append("var _r[] = ");
            listCreate.exprList.el.get(0).accept(this);
            nlIndent();
            this.sb.append("var _g[] = ");
            listCreate.exprList.el.get(1).accept(this);
            nlIndent();
            this.sb.append("var _b[] = ");
            listCreate.exprList.el.get(2).accept(this);
            return null;
        }
        this.sb.append("[");
        listCreate.exprList.accept(this);
        this.sb.append("]");
        return null;
    }

    @Override
    public Void visitListGetIndex(ListGetIndex listGetIndex) {
        if ( !listGetIndex.param.get(0).getVarType().toString().equals("ARRAY_COLOUR") ) {
            listGetIndex.param.get(0).accept(this);
        }
        if ( listGetIndex.mode == GET ) {
            this.sb.append("[");
        } else if ( listGetIndex.mode == REMOVE || listGetIndex.mode == GET_REMOVE ) {
            this.sb.append(".pop(");
        }

        switch ( (IndexLocation) listGetIndex.location ) {
            case RANDOM: // backwards compatibility
                // TODO?
            case FIRST:
                this.sb.append("0");
                break;
            case FROM_END:
                this.sb.append("-1 -"); // TODO should be correct but how is it handled on other robots?
                listGetIndex.param.get(1).accept(this);
                break;
            case FROM_START:
                listGetIndex.param.get(1).accept(this);
                break;
            case LAST:
                this.sb.append("-1");
                break;
            default:
                break;
        }
        if ( listGetIndex.mode == GET ) {
            this.sb.append("]");
        } else if ( listGetIndex.mode == REMOVE || listGetIndex.mode == GET_REMOVE ) {
            this.sb.append(")");
        }
        return null;
    }

    @Override
    public Void visitListRepeat(ListRepeat listRepeat) {
        this.sb.append("[");
        listRepeat.param.get(1).accept(this);
        this.sb.append("] = [");
        //TODO init list with right number of entries...
        listRepeat.param.get(0).accept(this);
        this.sb.append("]");
        return null;
    }

    @Override
    public Void visitListSetIndex(ListSetIndex listSetIndex) {
        listSetIndex.param.get(0).accept(this);
        if ( listSetIndex.mode == SET ) {
            this.sb.append("[");
        } else if ( listSetIndex.mode == INSERT ) {
            this.sb.append(".insert(");
        }
        switch ( (IndexLocation) listSetIndex.location ) {
            case RANDOM: // backwards compatibility
                // TODO?
            case FIRST:
                this.sb.append("0");
                break;
            case FROM_END:
                this.sb.append("-1 -");
                listSetIndex.param.get(2).accept(this);
                break;
            case FROM_START:
                listSetIndex.param.get(2).accept(this);
                break;
            case LAST:
                this.sb.append("-1");
                break;
            default:
                break;
        }
        if ( listSetIndex.mode == SET ) {
            this.sb.append("] = ");
            listSetIndex.param.get(1).accept(this);
        } else if ( listSetIndex.mode == INSERT ) {
            this.sb.append(", ");
            listSetIndex.param.get(1).accept(this);
            this.sb.append(")");
        }
        return null;
    }

    @Override
    public Void visitMathCastCharFunct(MathCastCharFunct mathCastCharFunct) {
        throw new DbcException("Characters not supported.");
    }

    @Override
    public Void visitMathCastStringFunct(MathCastStringFunct mathCastStringFunct) {
        throw new DbcException("Strings not supported.");
    }

    @Override
    public Void visitMathConst(MathConst mathConst) {
        throw new DbcException("No floats in Aseba");
    }

    @Override
    public Void visitMathConstrainFunct(MathConstrainFunct mathConstrainFunct) {
        this.sb.append("min(max(");
        mathConstrainFunct.param.get(0).accept(this);
        this.sb.append(", ");
        mathConstrainFunct.param.get(1).accept(this);
        this.sb.append("), ");
        mathConstrainFunct.param.get(2).accept(this);
        this.sb.append(")");
        return null;
    }

    @Override
    public Void visitMathNumPropFunct(MathNumPropFunct mathNumPropFunct) {
        switch ( mathNumPropFunct.functName ) {
            case EVEN:
                this.sb.append("(");
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" % 2) == 0");
                break;
            case ODD:
                this.sb.append("(");
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" % 2) == 1");
                break;
            case WHOLE:
                this.sb.append("(");
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" % 1) == 0");
                break;
            case POSITIVE:
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" > 0");
                break;
            case NEGATIVE:
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" < 0");
                break;
            case DIVISIBLE_BY:
                this.sb.append("(");
                mathNumPropFunct.param.get(0).accept(this);
                this.sb.append(" % ");
                mathNumPropFunct.param.get(1).accept(this);
                this.sb.append(") == 0");
                break;
            case PRIME:
                throw new DbcException("Statement not supported by Aseba!");
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitMathOnListFunct(MathOnListFunct mathOnListFunct) {
        throw new DbcException("Statement not yet supported");
    /*    switch ( mathOnListFunct.functName ) {
            case SUM:
                this.sb.append("sum(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case MIN:
                this.sb.append("min(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case MAX:
                this.sb.append("max(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case AVERAGE:
                this.sb.append("float(sum(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append("))/len(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case MEDIAN:
                this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(FunctionNames.MEDIAN)).append("(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case STD_DEV:
                this.sb.append(this.getBean(CodeGeneratorSetupBean.class).getHelperMethodGenerator().getHelperMethodName(FunctionNames.STD_DEV)).append("(");
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append(")");
                break;
            case RANDOM:
                mathOnListFunct.param.get(0).accept(this);
                this.sb.append("[0]");
                break;
            default:
                break;
        }
        return null;*/
    }

    @Override
    public Void visitMathPowerFunct(MathPowerFunct mathPowerFunct) {
        throw new DbcException("Statement not supported by Aseba!");
    }

    @Override
    public Void visitMathRandomFloatFunct(MathRandomFloatFunct mathRandomFloatFunct) {
        throw new DbcException("Floats not supported by Aseba!");
    }

    @Override
    public Void visitMathRandomIntFunct(MathRandomIntFunct mathRandomIntFunct) {
        this.sb.append("math.rand(");
        mathRandomIntFunct.param.get(0).accept(this);
        this.sb.append(")");
        return null;
    }

    @Override
    public Void visitMathSingleFunct(MathSingleFunct mathSingleFunct) {
        switch ( mathSingleFunct.functName ) {
            case ROOT:
                this.sb.append("call math.sqrt( _result, ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(" )");
                nlIndent();
                break;
            case SIN:
                this.sb.append("call math.sin( _result, ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(" )");
                nlIndent();
                break;
            case COS:
                this.sb.append("call math.cos( _result, ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(" )");
                nlIndent();
                break;
            case SQUARE:
                this.sb.append("call math.mul( _result, ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(", ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(" )");
                nlIndent();
                break;
            case ABS:
                this.sb.append("abs( ");
                mathSingleFunct.param.get(0).accept(this);
                this.sb.append(" )");
                nlIndent();
                break;
//            case TAN:
//                this.sb.append("call math.sin( _result, ");
//                mathSingleFunct.param.get(0).accept(this);
//                this.sb.append(" )");
//                nlIndent();
//                this.sb.append("call math.cos( _result, ");
//                mathSingleFunct.param.get(0).accept(this);
//                this.sb.append(" )");
//                nlIndent();
//                this.sb.append("_result = ");
//                break;
//            case ATAN:
//                this.sb.append("call math.atan2( _result, ");
//                break;
            default:
                throw new DbcException("Statement not supported by Aseba!");
        }
        return null;
    }

    @Override
    public Void visitMethodCall(MethodCall methodCall) {
        String stateVar = this.isFunc ? "_funcstate" : this.isLoop ? "_loopstate" : "_state";
        int counter = this.isFunc ? this.funcCounter : this.isLoop ? this.loopCounter : this.stateCounter;
        String ifElseif = counter == 0 ? "if " : "elseif ";
        incrIndentation();
        nlIndent();
        if ( !isLoop ) {
            this.sb.append(ifElseif).append(stateVar).append(" == ").append(counter).append(" then");
            incrIndentation();
            nlIndent();
        }
        this.sb.append("callsub ").append(methodCall.getMethodName());
        if ( isFunc ) {
            this.funcCounter++;
        } else if ( isLoop ) {
            this.loopCounter++;
        } else {
            this.stateCounter++;
        }
        decrIndentation();
//        nlIndent();
        if ( this.noOfStates == counter + 1 ) {
            nlIndent();
            this.sb.append("end");
            nlIndent();
        }
        decrIndentation();
//        nlIndent();
        return null;
    }

    @Override
    public Void visitMethodIfReturn(MethodIfReturn methodIfReturn) {
        this.sb.append("if ");
        methodIfReturn.oraCondition.accept(this);
        if ( !methodIfReturn.oraReturnValue.getKind().hasName("EMPTY_EXPR") ) {
            this.sb.append(": return ");
            methodIfReturn.oraReturnValue.accept(this);
        } else {
            this.sb.append(": return None");
        }
        return null;
    }

    @Override
    public Void visitMethodReturn(MethodReturn methodReturn) {
        nlIndent();
        this.sb.append("def ").append(methodReturn.getMethodName()).append('(');
        List<String> paramList = new ArrayList<>();
        for ( Expr l : methodReturn.getParameters().get() ) {
            paramList.add(((VarDeclaration) l).getCodeSafeName());
        }
        this.sb.append(String.join(", ", paramList));
        this.sb.append("):");
        incrIndentation();
        if ( !this.usedGlobalVarInFunctions.isEmpty() ) {
            nlIndent();
            this.sb.append("global " + String.join(", ", this.usedGlobalVarInFunctions));
        }
        methodReturn.body.accept(this);
        nlIndent();
        this.sb.append("return ");
        methodReturn.returnValue.accept(this);
        decrIndentation();
        return null;
    }

    @Override
    public Void visitMethodVoid(MethodVoid methodVoid) {
        nlIndent();
        this.isFunc = true;
        this.sb.append("sub ").append(methodVoid.getMethodName());
        incrIndentation();
        boolean isMethodBodyEmpty = methodVoid.body.get().isEmpty();
        if ( isMethodBodyEmpty ) {
            nlIndent();
            this.sb.append(" ");
        } else {
            methodVoid.body.accept(this);
        }
        nlIndent();
        this.sb.append("elseif _funcstate == ").append(this.funcCounter).append(" then");
        incrIndentation();
        nlIndent();
        String stateVar = this.isLoop ? "_loopstate" : "_state";
        this.funcCounter = 0;
        this.sb.append("_funcstate = 0");
        nlIndent();
        this.sb.append(stateVar).append("++");
        this.isFunc = false;
        decrIndentation();
        nlIndent();
        this.sb.append("end");
        decrIndentation();
        return null;
    }

    @Override
    public Void visitNullConst(NullConst nullConst) {
        throw new DbcException("No NULL in Aseba");
    }

    @Override
    public Void visitRepeatStmt(RepeatStmt repeatStmt) {
        String stateVar = this.isFunc ? "_funcstate" : this.isLoop ? "_loopstate" : "_state";
        int counter = this.isFunc ? this.funcCounter : this.isLoop ? this.loopCounter : this.stateCounter;
        String ifElseif = counter == 0 ? "if " : "elseif ";
        switch ( repeatStmt.mode ) {
            case FOREVER:
                this.sb.append(ifElseif).append(stateVar).append(" == ").append(counter).append(" then");
//                this.sb.append("if _state == ").append(this.stateCounter).append(" then");
                incrIndentation();
                nlIndent();
                generateCodeFromStmtCondition("if", repeatStmt.expr);
//                incrIndentation();
//                nlIndent();
                if ( isFunc ) {
                    this.funcCounter++;
                }
                if ( isLoop ) {
                    this.loopCounter++;
                } else {
                    this.stateCounter++;
                }
                this.isLoop = true;
                break;
            case UNTIL:
                generateCodeFromStmtCondition("if", repeatStmt.expr);
                break;
            case WHILE:
                this.sb.append("if _loopstate == ").append(this.loopCounter).append(" then");
                incrIndentation();
                nlIndent();
                generateCodeFromStmtCondition("if", repeatStmt.expr);
                decrIndentation();
                nlIndent();
                this.loopCounter++;
                break;
            case TIMES:
            case FOR:
                this.sb.append(ifElseif).append(stateVar).append(" == ").append(counter).append(" then");
                incrIndentation();
                nlIndent();
                generateCodeFromStmtConditionFor("if", repeatStmt.expr, false);
//                decrIndentation();
//                nlIndent();
                if ( isFunc ) {
                    this.funcCounter++;
                }
                if ( isLoop ) {
                    this.loopCounter++;
                } else {
                    this.stateCounter++;
                }
                this.isLoop = true;
                break;
            case WAIT:
                generateCodeFromStmtCondition("if", repeatStmt.expr);
                break;
            case FOR_EACH:
                generateCodeFromStmtCondition("if", repeatStmt.expr);
                break;
            default:
                throw new DbcException("Invalid Repeat Statement!");
        }
        incrIndentation();
        if ( repeatStmt.list.sl.isEmpty() ) {
            nlIndent();
            this.sb.append(stateVar).append("++");
            decrIndentation();
            nlIndent();
            this.sb.append("end");
//            incrIndentation();
//            nlIndent();
//            this.sb.append("timer.period[0] = 10");
        } else {
            repeatStmt.list.accept(this);
            decrIndentation();
            nlIndent();
//            this.sb.append("timer.period[0] = 100");
            if ( repeatStmt.mode == RepeatStmt.Mode.FOR ||
                repeatStmt.mode == RepeatStmt.Mode.TIMES ) {
                generateCodeFromStmtConditionFor("if", repeatStmt.expr, true);
            } else if ( repeatStmt.mode == RepeatStmt.Mode.FOREVER ) {
                this.sb.append("end");
            }
            decrIndentation();
            nlIndent();
            this.sb.append("end");
        }
        decrIndentation();
        nlIndent();
        this.sb.append("end");
//        if ( isLoop ) {
//            this.loopCounter++;
//        }
        decrIndentation();
        nlIndent();
        return null;
    }

    @Override
    public Void visitSerialWriteAction(SerialWriteAction serialWriteAction) {
        return null;
    }

    @Override
    public Void visitStmtFlowCon(StmtFlowCon stmtFlowCon) {
//        if ( this.getBean(UsedHardwareBean.class).getLoopsLabelContainer().get(this.currentLoop.getLast()) != null ) {
//            if ( this.getBean(UsedHardwareBean.class).getLoopsLabelContainer().get(this.currentLoop.getLast()) ) {
//                this.sb.append("raise " + (stmtFlowCon.flow == Flow.BREAK ? "BreakOutOfALoop" : "ContinueLoop"));
//                return null;
//            }
//        }
//        this.sb.append(stmtFlowCon.flow.toString().toLowerCase());
        return null;
    }

    @Override
    public Void visitStmtTextComment(StmtTextComment stmtTextComment) {
        this.sb.append("# " + stmtTextComment.textComment.replace("\n", " "));
        return null;
    }

    @Override
    public Void visitTextCharCastNumberFunct(TextCharCastNumberFunct textCharCastNumberFunct) {
        this.sb.append("ord(");
        textCharCastNumberFunct.param.get(0).accept(this);
        this.sb.append("[");
        textCharCastNumberFunct.param.get(1).accept(this);
        this.sb.append("])");
        return null;
    }

    @Override
    public Void visitTextJoinFunct(TextJoinFunct textJoinFunct) {
        this.sb.append("\"\".join(str(arg) for arg in [");
        textJoinFunct.param.accept(this);
        this.sb.append("])");
        return null;
    }

    @Override
    public Void visitTextPrintFunct(TextPrintFunct textPrintFunct) {
        this.sb.append("print(");
        textPrintFunct.param.get(0).accept(this);
        this.sb.append(")");
        return null;
    }

    @Override
    public Void visitTextStringCastNumberFunct(TextStringCastNumberFunct textStringCastNumberFunct) {
        throw new DbcException("Floats not supported in Aseba!");
    }

    @Override
    public Void visitTimerSensor(TimerSensor timerSensor) {
        return null;
    }

    @Override
    public Void visitUnary(Unary unary) {
        Unary.Op op = unary.op;
        String sym = getUnaryOperatorSymbol(op);
        if ( op == Unary.Op.POSTFIX_INCREMENTS ) {
            generateExprCode(unary, this.sb);
            src.add(sym);
        } else {
            src.add(sym, " ( ");
            if ( unary.expr.getKind().getName().equals("BOOL_CONST") ) {
                src.add("___true == ");
            }
            generateExprCode(unary, this.sb);
            if ( unary.expr.getKind().getName().equals("VAR") ) {
                src.add(" == 1");
            }
            src.add(" ) ");
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration var) {
        this.sb.append("var ").append(var.getCodeSafeName());
        if ( !var.value.getKind().hasName("EMPTY_EXPR") ) {
            if ( var.value.getKind().getName().equals("LIST_CREATE") ) {
                if ( ((ListCreate) var.value).exprList.el.size() == 0 ) {
                    this.sb.append("[10]");
                    return null;
                } else if ( ((ListCreate) var.value).typeVar.toString().equals("COLOR") ) {
                    this.sb.append("[] = [255, 255, 255]");
                    nlIndent();
                    var.value.accept(this);
                    return null;
                } else {
                    this.sb.append("[]");
                }
            }
            if ( var.value.getKind().getName().equals("FUNCTION_EXPR") ) {
                var.value.accept(this);
                return null;
            }
            this.sb.append(" = ");
            if ( var.value.getKind().hasName("EXPR_LIST") ) {
                ExprList list = (ExprList) var.value;
                if ( list.get().size() == 2 ) {
                    list.get().get(1).accept(this);
                } else {
                    list.get().get(0).accept(this);
                }
            } else {
                var.value.accept(this);
            }
        } else {
            this.sb.append(" = 0");
        }
        return null;
    }

    @Override
    public Void visitWaitStmt(WaitStmt waitStmt) {
        return null;
    }

    @Override
    public Void visitWaitTimeStmt(WaitTimeStmt waitTimeStmt) {
        return null;
    }

    @Override
    protected void generateCodeFromTernary(TernaryExpr ternaryExpr) {
        throw new DbcException("No Ternary Expressions in Aseba!");
    }

    @Override
    protected void generateCodeFromIfElse(IfStmt ifStmt) {
        int stmtSize = ifStmt.expr.size();
        for ( int i = 0; i < stmtSize; i++ ) {
            if ( i == 0 ) {
                generateCodeFromStmtCondition("if", ifStmt.expr.get(i));
            } else {
                nlIndent();
                generateCodeFromStmtCondition("elseif", ifStmt.expr.get(i));
            }
            incrIndentation();
            StmtList then = ifStmt.thenList.get(i);
            if ( !then.get().isEmpty() ) {
                then.accept(this);
            }
            decrIndentation();
            nlIndent();
        }
    }

    @Override
    protected void generateCodeFromElse(IfStmt ifStmt) {
        if ( !ifStmt.elseList.get().isEmpty() ) {
            this.sb.append("else");
            incrIndentation();
            ifStmt.elseList.accept(this);
            decrIndentation();
            nlIndent();
        }
        this.sb.append("end");
    }

    @Override
    protected String getLanguageVarTypeFromBlocklyType(BlocklyType type) {
        return "";
    }

    protected void generateCodeFromStmtCondition(String stmtType, Expr expr) {
        this.sb.append(stmtType).append(whitespace());
        if ( expr.getKind().getName().equals("BOOL_CONST") || expr.getKind().getName().equals("VAR") ) {
//            String loop = stmtType.equals("while") ? "___loop == " : "";
            this.sb.append("___true == ");
        }
        expr.accept(this);
        String thenOrDo = stmtType.contains("if") ? " then" : " do";
        this.sb.append(thenOrDo);
    }

    protected void generateCodeFromStmtConditionFor(String stmtType, Expr expr, boolean inc) {
        ExprList expressions = (ExprList) expr;
        if ( inc ) {
            incrIndentation();
//            nlIndent();
            expressions.get().get(0).accept(this);
            this.sb.append("++");
            decrIndentation();
            nlIndent();
            this.sb.append("elseif ");
            expressions.get().get(0).accept(this);
            this.sb.append(" == ");
            expressions.get().get(2).accept(this);
            this.sb.append(" then");
            incrIndentation();
            nlIndent();
            expressions.get().get(0).accept(this);
            this.sb.append(" = 0 ");
            nlIndent();
            this.sb.append("_loopstate++");
            decrIndentation();
            nlIndent();
            this.sb.append("end");
            this.loopCounter++;
            return;
        }
//        incrIndentation();
//        nlIndent();
        this.sb.append(stmtType).append(whitespace());
        expressions.get().get(0).accept(this);
        this.sb.append(whitespace()).append("< ");
        expressions.get().get(2).accept(this);
//        this.sb.append(":");
//        expressions.get().get(2).accept(this);
//        this.sb.append(" step").append(whitespace());
//        expressions.get().get(3).accept(this);
        this.sb.append(" then");
//        incrIndentation();
//        nlIndent();
    }

    @Override
    protected String getBinaryOperatorSymbol(Binary.Op op) {
        return AbstractAsebaVisitor.binaryOpSymbols().get(op);
    }

    @Override
    protected String getUnaryOperatorSymbol(Unary.Op op) {
        return AbstractAsebaVisitor.unaryOpSymbols().get(op);
    }

    @Override
    protected void generateProgramSuffix(boolean withWrapping) {
        if ( !this.getBean(CodeGeneratorSetupBean.class).getUsedMethods().isEmpty() ) {
            String helperMethodImpls =
                this
                    .getBean(CodeGeneratorSetupBean.class)
                    .getHelperMethodGenerator()
                    .getHelperMethodDefinitions(this.getBean(CodeGeneratorSetupBean.class).getUsedMethods());
            this.sb.append(helperMethodImpls);
        }

        nlIndent();
        this.sb.append("if __name__ == \"__main__\":");
        incrIndentation();
        nlIndent();
        this.sb.append("main()");
        decrIndentation();
    }

    private void generateCodeRightExpression(Binary binary, Binary.Op op) {
        if ( op == Binary.Op.TEXT_APPEND ) {
            generateSubExpr(this.sb, false, binary.getRight(), binary);
        } else {
            generateSubExpr(this.sb, parenthesesCheck(binary), binary.getRight(), binary);
        }
    }
}
