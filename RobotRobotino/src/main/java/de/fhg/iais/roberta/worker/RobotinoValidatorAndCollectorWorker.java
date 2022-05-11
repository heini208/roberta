package de.fhg.iais.roberta.worker;

import com.google.common.collect.ClassToInstanceMap;
import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.components.Project;
import de.fhg.iais.roberta.visitor.validate.CommonNepoValidatorAndCollectorVisitor;
import de.fhg.iais.roberta.visitor.RobotinoValidatorAndCollectorVisitor;

import java.util.*;

public class RobotinoValidatorAndCollectorWorker extends AbstractValidatorAndCollectorWorker {

    private static final List<String> NON_BLOCKING_PROPERTIES = Collections.unmodifiableList(Arrays.asList("MOTOR_L", "MOTOR_R", "BRICK_WHEEL_DIAMETER", "BRICK_TRACK_WIDTH"));

    @Override
    protected CommonNepoValidatorAndCollectorVisitor getVisitor(
            Project project, ClassToInstanceMap<IProjectBean.IBuilder<?>> beanBuilders) {
        return new RobotinoValidatorAndCollectorVisitor(project.getConfigurationAst(), beanBuilders);
    }

    @Override
    protected List<Class<? extends Enum<?>>> getAdditionalMethodEnums() {
        //return Collections.singletonList(Mbot2Methods.class);
        return null;
    }

    @Override
    public void execute(Project project) {
        super.execute(project);
    }

}
