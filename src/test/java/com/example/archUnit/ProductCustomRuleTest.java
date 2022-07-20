package com.example.archUnit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * custom rule
 *
 * @author ryan
 */
class ProductCustomRuleTest {

	// 排除jar包和测试包
	ImportOptions importOptions = new ImportOptions()
					.with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
					.with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
					.with(new CustomImportOption());
	// 导入所有class
	JavaClasses classes = new ClassFileImporter(importOptions).importPackages("com.gongkongsaas.product");

	@Test
	void controllerRule() {
		// 指定规则生效的包
		DescribedPredicate<JavaClass> predicate =
						new DescribedPredicate<JavaClass>("定义在com.gongkongsaas.product.controller包下的所有类") {
							@Override
							public boolean apply(JavaClass input) {
								return null != input.getPackageName() && input.getPackageName().contains("com.gongkongsaas.product.controller");
							}
						};
		// controller包下的类中的查询方法,形参需要以Query结尾
		ArchCondition<JavaClass> condition = new ArchCondition<JavaClass>("controller包下的类中的查询方法,形参需要以Query结尾") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
				List<JavaMethod> methods = javaClass.getMethods().stream()
								.filter(x -> x.getName().startsWith("list") || x.getName().startsWith("get"))
								.collect(Collectors.toList());
				for (JavaMethod javaMethod : methods) {
					Method method = javaMethod.reflect();
					Class<?>[] parameterTypes = method.getParameterTypes();
					Annotation[][] parameterAnnotations = method.getParameterAnnotations();
					for (int i = 0; i < parameterAnnotations.length; i++) {
						for (Annotation annotation : parameterAnnotations[i]) {
							if (annotation.annotationType() == RequestBody.class) {
								Class<?> parameterType = parameterTypes[i];
								if (parameterType != List.class && !parameterType.getName().endsWith("Query")) {
									conditionEvents.add(SimpleConditionEvent.violated(method,
													String.format("当前控制器类[%s]的[%s]查询方法形参不以\"Query\"结尾", javaClass.getName(), method.getName())));
								}
							}
						}
					}
				}
			}
		};

		// controller包下的类中的方法,需要添加ApiOperate注解
//		ArchCondition<JavaClass> condition1 = new ArchCondition<JavaClass>("controller包下的类中的方法,需要添加ApiOperate注解") {
//			@Override
//			public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
//				Set<JavaMethod> methods = javaClass.getMethods();
//				for (JavaMethod javaMethod : methods) {
//					Method method = javaMethod.reflect();
//					boolean annotationPresent = method.isAnnotationPresent(ApiOperation.class);
//					if (annotationPresent) {
//						conditionEvents.add(SimpleConditionEvent.violated(method,
//										String.format("当前控制器类[%s]的[%s]方法没有添加ApiOperate注解", javaClass.getName(), method.getName())));
//					}
//				}
//			}
//		};
		ArchRuleDefinition.classes()
						.that(predicate)
						.should(condition)
//						.andShould(condition1)
						.check(classes);
	}

	@Test
	void serviceRule() {

		DescribedPredicate<JavaClass> predicate =
						new DescribedPredicate<JavaClass>("定义在com.gongkongsaas.product.service包下的所有类") {
							@Override
							public boolean apply(JavaClass input) {
								return null != input.getPackageName() && input.getPackageName().contains("com.gongkongsaas.product.service");
							}
						};

		// service层的出参需要以VO结尾
		ArchCondition<JavaClass> condition = new ArchCondition<JavaClass>("service层的出参需要以VO结尾") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
				if (javaClass.getName().contains("$")) {
					return;
				}
				Set<JavaMethod> javaMethods = javaClass.getMethods();
				List<JavaMethod> javaMethodList = javaMethods.stream()
								.filter(x -> !x.getName().contains("lambda"))
								.collect(Collectors.toList());
				for (JavaMethod javaMethod : javaMethodList) {
					Method method = javaMethod.reflect();
					if (method.getModifiers() == Modifier.PRIVATE) {
						continue;
					}
					Class<?> returnType = method.getReturnType();
					if (!returnType.getSimpleName().endsWith("VO")) {
						conditionEvents.add(SimpleConditionEvent.violated(method,
										String.format("当前service[%s]的[%s]方法返回值没有以VO结尾", javaClass.getName(), method.getName())));
					}
				}
			}
		};

		ArchRuleDefinition.classes()
						.that(predicate)
						.should(condition)
						.check(classes);

	}

}
