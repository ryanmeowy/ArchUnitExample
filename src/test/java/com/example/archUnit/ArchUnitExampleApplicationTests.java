package com.example.archUnit;

import com.example.archUnit.config.DemoConfig;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;


@SpringBootTest
class ArchUnitExampleApplicationTests {

    JavaClasses classes = new ClassFileImporter().importPackages("com.ryan.archunitdemo");

    @Test
    void importerTest() {
        /*
           JavaClasses是JavaClass的集合,可以简单理解为反射中的Class集合
           后面使用代码规则和规则判断都强依赖与JavaClasses或者JavaClass
         */
        // simple importer
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.ryan.archunitdemo");
        JavaClass javaClass = javaClasses.get(ryan.class);
        // 扫描指定包下所有类
        Assertions.assertEquals("ryan",javaClass.getSimpleName());

        // custom importOptions
        ImportOptions importOptions = new ImportOptions()
                // 不扫描jar包
                .with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                // 不扫描 测试包
                .with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);
        ClassFileImporter classFileImporter = new ClassFileImporter(importOptions);
        JavaClasses javaClasses1 = classFileImporter.importPackages("com.ryan.archunitdemo");
        JavaClass javaClass1 = javaClasses1.get(DemoConfig.class);
        // 排除了 com.ryan.archunitdemo.DontImport 包后, 无法扫描到指定类, 将会报错
        Assertions.assertEquals("config",javaClass1.getSimpleName());
    }

    @Test
    void ruleTest01() {
        /*
          使用这种链式编程去构建ArchRule,其实非常好理解
          字面意思就是 在service包中,没有类应该去访问controller包中的类,因为.....
         */
        ArchRule archRule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAPackage("..controller..")
                .because("不能在service包中调用controller中的类");
        // 对所有的JavaClasses进行判断
        archRule.check(classes);
    }

    @Test
    void ruleTest02() {
        /*
         * 在service包中,没有类应该依赖controller包中类
         */
        ArchRule archRule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..");

        /*
         * service包只能访问dao包
         */
        ArchRule archRule2 = ArchRuleDefinition.classes()
                .that().resideInAPackage("..service..")
                .should().onlyAccessClassesThat().resideInAnyPackage("..service..","..dao..")
                .because("service should only access dao and itself");
//        archRule.check(classes);
        archRule2.check(classes);
    }

    @Test
    void ruleTest03() {
        /*
         * 类依赖
         * Dao结尾的类只允许DemoService访问
         */
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveNameMatching(".*Dao")
                .should().onlyBeAccessed().byClassesThat().haveSimpleName("DemoService");

        /*
         * DemoService只允许Controller结尾的类访问
         */
        ArchRule archRule2 = ArchRuleDefinition.classes()
                .that().haveSimpleName("DemoService")
                .should().onlyBeAccessed().byClassesThat().haveNameMatching(".*Controller");

        archRule.check(classes);
        archRule2.check(classes);
    }


    @Test
    void ruleTest04() {
        /*
         * 类位置检查
         * Entity结尾的类只能在entity包中
         */
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveNameMatching(".*Entity")
                .should().resideInAPackage("com.ryan.archunitdemo.entity");

        archRule.check(classes);
    }

//    @Test
//    void ruleTest05() {
//        /*
//         * 继承关系检查
//         * 实现DemoInterface接口的类名必须以Impl结尾
//         */
//        ArchRule archRule = ArchRuleDefinition.classes()
//                .that().implement(DemoInterface.class)
//                .should().haveSimpleNameEndingWith("Impl");
//
//        /*
//         * 只有service包中的类可以访问DemoImpl
//         */
//        ArchRule archRule1 = ArchRuleDefinition.classes()
//                .that().areAssignableTo(DemoImpl.class)
//                .should().onlyBeAccessed().byAnyPackage("..service..");
//
//        archRule.check(classes);
//        archRule1.check(classes);
//    }

    @Test
    void ruleTest06() {
        /*
         * 注解检查
         * 只有标注了service注解的类可以访问DemoImpl
         */
//        ArchRule archRule = ArchRuleDefinition.classes()
//                .that().areAssignableTo(DemoImpl.class)
//                .should().onlyBeAccessed().byClassesThat().areAnnotatedWith(Service.class);
        layeredArchitecture()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("dao").definedBy("..dao..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("dao").mayOnlyBeAccessedByLayers("Service");
//        slices().matching("com.myapp.(*)..").should().beFreeOfCycles();


//        archRule.check(classes);
    }

}
