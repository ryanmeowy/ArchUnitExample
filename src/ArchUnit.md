# ArchUnit

### ArchUnit是什么？

ArchUnit is a free, simple and extensible library for checking the architecture of your Java code using any plain Java unit test framework.That is, ArchUnit can check dependencies between packages and classes, layers and slices, check for cyclic dependencies and more. It does so by analyzing given Java bytecode, importing all classes into a Java code structure. 

ArchUnit是一个免费的、简单的、可扩展的类库，用于检查Java代码的体系结构。提供检查包和类的依赖关系、调用层次和切面的依赖关系、循环依赖检查等其他功能。它通过导入所有类的代码结构，基于Java字节码分析实现这一点。

官网：[ArchUnit.org]( https://www.ArchUnit.org/)

---

### 如何使用

环境：JDK1.8、ArchUnit0.22.0 、gradle6.7、SpringBoot 2.3.4.RELEASE

导入依赖

~~~gradle
dependencies {
    testImplementation 'com.tngtech.ArchUnit:ArchUnit:0.22.0'
    testImplementation 'com.tngtech.ArchUnit:ArchUnit-junit5:0.22.0'
}
~~~



从下面两个方面来介绍下ArchUnit的基本使用

- 指定需要扫描的包
- 内建规则定义

本次demo只演示ArchUnit的一部分用法，想了解更多，可以参考：[官方文档](https://www.ArchUnit.org/userguide/html/000_Index.html#_introduction)

**指定扫描包名/路径** 

~~~java
	@Test
    void importerTest() {
        /*
           JavaClasses是JavaClass的集合,可以简单理解为反射中的Class集合
           后面使用代码规则和规则判断都强依赖与JavaClasses或者JavaClass
         */
        // simple importer
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.ryan.ArchUnitdemo");
        JavaClass javaClass = javaClasses.get(ryan.class);
        // 扫描指定包下所有类
        Assertions.assertEquals("ryan",javaClass.getSimpleName());

        // custom importOptions
        ImportOptions importOptions = new ImportOptions()
                // 不扫描jar包
                .with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                // 不扫描 测试包
                .with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                // 自定义排除的包
                .with(new CustomImportOption("com.ryan.ArchUnitdemo.dontImport","com.ryan.ArchUnitdemo.config"));
        ClassFileImporter classFileImporter = new ClassFileImporter(importOptions);
        JavaClasses javaClasses1 = classFileImporter.importPackages("com.ryan.ArchUnitdemo");
        JavaClass javaClass1 = javaClasses1.get(config.class);
        // 排除了 com.ryan.ArchUnitdemo.config 包后, 无法扫描到指定类, 将会报错
        Assertions.assertEquals("config",javaClass1.getSimpleName());
    }
~~~

![image-20211210143452066](C:\Users\dell\AppData\Roaming\Typora\typora-user-images\image-20211210143452066.png)

**自定义ImportOption需要实现ImportOption接口**

~~~java

/**
 * 自定义ImportOption
 *
 * @author ryan
 */
public class CustomImportOption implements ImportOption {

    private final Set<Pattern> EXCLUDED_PATTERN;

    public CustomImportOption(String... packages) {
        EXCLUDED_PATTERN = new HashSet<>(8);
        for (String eachPackage : packages) {
            EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", eachPackage.replace("/", "."))));
        }
    }

    /**
     * @param location Location中包含路径信息 是否jar文件等判断属性的元数据, 方便使用正则表达式或者直接的逻辑判断
     */
    @Override
    public boolean includes(Location location) {
        for (Pattern pattern : EXCLUDED_PATTERN) {
            return !location.matches(pattern);
        }
        return true;
    }
}
~~~

**内建规则定义**

- 类扫描完成后，接来下就是将定义的规则应用于所有类并进行断言

- ArchUnit提供了一些简单的规则供我们使用，位于GeneralCodingRules中 
  - NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS：不能调用System.out、System.err或者(Exception.)printStackTrace。
  - NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS：类不能直接抛出通用异常Throwable、Exception或者RuntimeException。
  - NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING：不能使用`java.util.logging`包路径下的日志组件。

- 同时也支持自定义规则，规则的定义依赖于ArchRuleDefinition类，通过它来构建ArchRule实例，举几个例子：

包依赖检查

~~~java
	@Test
    void ruleTest01() {
        //指定要扫描的包
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.ryan.archunitdemo");
        /*
          使用这种链式编程去构建ArchRule,其实非常好理解
          no class that resdie in package service access chasses taht reside in package controller
          在service包中,没有类应该去访问controller包中的类,因为.....
         */
        ArchRule archRule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAPackage("..controller..")
                .because("不能在service包中调用controller中的类");
        // 对所有的JavaClasses进行判断
        archRule.check(javaClasses);
    }
~~~

类依赖检查

~~~java
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
~~~

类位置检查

~~~java
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
~~~

继承关系检查

~~~java
@Test
    void ruleTest05() {
        /*
         * 继承关系检查
         * 实现DemoInterface接口的类名必须以Impl结尾
         */
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().implement(DemoInterface.class)
                .should().haveSimpleNameEndingWith("Impl");

        /*
         * 只有service包中的类可以访问DemoImpl
         */
        ArchRule archRule1 = ArchRuleDefinition.classes()
                .that().areAssignableTo(DemoImpl.class)
                .should().onlyBeAccessed().byAnyPackage("..service..");

        archRule.check(classes);
        archRule1.check(classes);
    }
~~~

注解检查

~~~java
@Test
    void ruleTest06() {
        /*
         * 注解检查
         * 只有标注了service注解的类可以访问DemoImpl
         */
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().areAssignableTo(DemoImpl.class)
                .should().onlyBeAccessed().byClassesThat().areAnnotatedWith(Service.class);

        archRule.check(classes);
    }
~~~







