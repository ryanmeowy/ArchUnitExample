package com.example.archUnit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

public class ryan {
    @Test
    public void controller_class_rule() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.ryan.archunitdemo");
        DescribedPredicate<JavaClass> predicate =
                new DescribedPredicate<JavaClass>("定义在com.ryan.archunitdemo.controller包下的所有类") {
                    @Override
                    public boolean apply(JavaClass input) {
                        return null != input.getPackageName() && input.getPackageName().contains("com.ryan.archunitdemo.controller");
                    }
                };
        ArchCondition<JavaClass> condition1 = new ArchCondition<JavaClass>("类名称以Controller结尾") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                String name = javaClass.getName();
                if (!name.endsWith("Controller")) {
                    conditionEvents.add(SimpleConditionEvent.violated(javaClass, String.format("当前控制器类[%s]命名不以\"Controller\"结尾", name)));
                }
            }
        };
        ArchCondition<JavaClass> condition2 = new ArchCondition<JavaClass>("方法的入参类型命名以\"Request\"结尾，返回参数命名以\"Response\"结尾") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                Set<JavaMethod> javaMethods = javaClass.getMethods();
                String className = javaClass.getName();
                // 其实这里要做严谨一点需要考虑是否使用了泛型参数，这里暂时简化了
                for (JavaMethod javaMethod : javaMethods) {
                    Method method = javaMethod.reflect();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (Class parameterType : parameterTypes) {
                        if (!parameterType.getName().endsWith("Request")) {
                            conditionEvents.add(SimpleConditionEvent.violated(method,
                                    String.format("当前控制器类[%s]的[%s]方法入参不以\"Request\"结尾", className, method.getName())));
                        }
                    }
                    Class<?> returnType = method.getReturnType();
                    if (!returnType.getName().endsWith("Response")) {
                        conditionEvents.add(SimpleConditionEvent.violated(method,
                                String.format("当前控制器类[%s]的[%s]方法返回参数不以\"Response\"结尾", className, method.getName())));
                    }
                }
            }
        };
        ArchRuleDefinition.classes()
                .that(predicate)
                .should(condition1)
                .andShould(condition2)
                .because("定义在controller包下的Controller类的类名称以\"Controller\"结尾，方法的入参类型命名以\"Request\"结尾，返回参数命名以\"Response\"结尾")
                .check(classes);
    }
}
