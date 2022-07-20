package com.example.archUnit;

import com.example.archUnit.annotation.Secured;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.base.PackageMatchers;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tngtech.archunit.core.domain.JavaClass.Functions.GET_PACKAGE_NAME;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.example.archUnit",
				importOptions = {ImportOption.DoNotIncludeTests.class,
								ImportOption.DoNotIncludeJars.class})
public class ControllerRulesTest {

	static final GivenClassesConjunction controllers = classes().that().areAnnotatedWith(RestController.class);


	/**
	 * RestController 标注的类 ， 应该放在controller包下
	 */
	@ArchTest
	static final ArchRule controllers_should_reside_in_controller =
					controllers.should().resideInAPackage("..controller..");

	/**
	 * RestController 标注的类 ,只应该调用声明在controller或者java包下的方法, 或者被"xxx"注解标注的方法。
	 * or的优先级会高于指定的包， 比如controller调用了dao包下的被指定注解标注的方法，则不会报错
	 */
	@ArchTest
	static final ArchRule controllers_should_only_call_service_methods =
					controllers.should().onlyCallMethodsThat(areDeclaredInService().or(annotatedWith(Secured.class)));


	/**
	 * RestController 标注的类,只应该调用声明在controller或者java包下的构造器
	 */
	@ArchTest
	static final ArchRule controllers_should_only_call_secured_constructors =
					controllers.should().onlyCallConstructorsThat(areDeclaredInController());

	/**
	 * RestController 标注的类,只应该调用声明在controller或者java包下的代码单元, 或者被"xxx"注解标注的代码单元
	 */
	@ArchTest
	static final ArchRule controllers_should_only_call_secured_code_units =
					classes().that().areAnnotatedWith(RestController.class).should().onlyCallCodeUnitsThat(areDeclaredInService());

	private static DescribedPredicate<JavaMember> areDeclaredInController() {
		DescribedPredicate<JavaClass> aPackageController = GET_PACKAGE_NAME.is(PackageMatchers.of("..controller..", "java.."))
						.as("a package '..controller..'");
		return are(declaredIn(aPackageController));
	}

	/**
	 * 声明service和java包
	 */
	private static DescribedPredicate<JavaMember> areDeclaredInService() {
		return declaredIn(GET_PACKAGE_NAME.is(PackageMatchers.of("..service..", "..java.."))
						.as("a package '..service..'"));
	}

	@Test
	public void controllerRule() {
		// 选择器 ， 用于构建 importer
		ImportOptions importOptions = new ImportOptions()
						.with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
						.with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
						.with(new CustomImportOption());

		// 指定扫描的包
		JavaClasses classes = new ClassFileImporter(importOptions).importPackages("com.example.archUnit");

		/*
		 * 指定规则生效的包
		 * 这一步可以合并到上一步的指定扫描包中， 这么写只为演示功能
		 */
		DescribedPredicate<JavaClass> predicate = new DescribedPredicate<JavaClass>("定义在controller包下的所有类") {
			@Override
			public boolean apply(JavaClass input) {
				return null != input.getPackageName() && input.getPackageName().contains("com.example.archUnit.controller");
			}
		};

		// 指定规则
		ArchCondition<JavaClass> condition = new ArchCondition<JavaClass>("查询方法的形参需要以Query结尾") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
				List<Method> methods = javaClass.getMethods().stream()
								.filter(x -> x.getName().startsWith("list") || x.getName().startsWith("get"))
								.map(JavaMethod::reflect)
								.collect(Collectors.toList());
				for (Method method : methods) {
					Parameter[] parameters = method.getParameters();
					for (Parameter parameter : parameters) {
						parameter.isAnnotationPresent(RequestBody.class);
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
