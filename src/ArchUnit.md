# ArchUnit

## ArchUnit是什么？

**[官网介绍](https://www.ArchUnit.org/):**

ArchUnit is a free, simple and extensible library for checking the architecture of your Java code using any plain Java unit test framework.That is, ArchUnit can check dependencies between packages and classes, layers and slices, check for cyclic dependencies and more. It does so by analyzing given Java bytecode, importing all classes into a Java code structure. 

ArchUnit是一个免费的、简单的、可扩展的类库，用于检查Java代码的体系结构。提供检查包和类的依赖关系、调用层次和切面的依赖关系、循环依赖检查等其他功能。它通过导入所有类的代码结构，基于Java字节码分析实现这一点。

**应用场景**

相信大部分的的开发人员有遇到过这样的情况：

开始有人画了一些漂亮的架构图，展示了系统应该包含的组件以及它们应该如何交互，大家形成一个约定并达成共识。但是随着项目逐渐变得更大，一般会经历开发人员的调整，包括新开发人员的加入或者老开发人员离开去做其它项目等。当新的需求或者特性添加进来，由于开发人员的差异，可能会出现一些不可预见的违反规范的行为，如：

- *命名不规范*

- *分层代码调用不规范，比如Controller直接调用Dao*

- ... ... 

这些问题可能需要在Review的时候才会被看到，并不是一种很及时的解决方法。

---

>  环境：JDK1.8、ArchUnit0.22.0 、gradle6.7、junit 5

## 快速开始

archunit集成了junit4和junit5，分别在`archunit-junit4`和`archunit-junit5-api`两个模块中； 本次demo使用junit5框架， 功能与junit4一致, 使用方式上有细微差别。

**导入依赖**

```gradle
dependencies {
    testImplementation 'com.tngtech.ArchUnit:ArchUnit:0.22.0'
}
```

**基本使用分三步**

- 导入类
- 断言约束
- 执行测试

### 导入类

archUnit 提供了一个类用于导入类 ， ``ClassFileImporter``，基本使用：

```java
JavaClasses classes = new ClassFileImporter().importPackages("com.example.archUnit");
```

或者：

```java
JavaClasses classes = new ClassFileImporter().importPath("/some/path");
```

JavaClasses是JavaClass的集合,可以简单理解为反射中的Class集合， 后面使用代码规则和规则判断都强依赖于JavaClasses或者JavaClass

指定的package或是path中，可能存在一些需要排除的包或是类， 比如我们并不希望项目的config包也进行规则判断，archUnit提供了一个类用于排除指定包， ``ImportOptions``， archUnit提供了一些常用的排除规则， 比如排除测试包， 排除jar包， 也可以实现``ImportOption``接口，来实现自己的排除规则。

基本使用： 

```java
    @Test
    void importerTest() {
         // 排除jar包 测试包 自定义选择器中的包
        ImportOptions importOptions = new ImportOptions()
                    .with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                    .with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .with(new CustomImportOption());
        // 导入所有class
        JavaClasses classes = new ClassFileImporter(importOptions).importPackages("com.example.archUnit");
        // customImportOption中排查了config包，所以无法获取AppConfig
        JavaClass javaClass = classes.get(AppConfig.class);
        Assertions.assertEquals("AppConfig",javaClass1.getSimpleName());
    }
```

自定义ImpaortOption

```java
public class CustomImportOption implements ImportOption {

    private static final Set<Pattern> EXCLUDED_PATTERN = new HashSet<>(8);
    //定义默认排除的包 ，也可在创建实例的时候传入需要排除的包
    private static final String[] DEF_DONT_SCAN_PACKAGES = {"com/example/archUnit/config",
                    "com/example/archUnit/converter", "com/example/archUnit/annotation"};

    static {
        for (String defDontScanPackage : DEF_DONT_SCAN_PACKAGES) {
            EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", defDontScanPackage)));
        }
    }

    public CustomImportOption() {
    }

    public CustomImportOption(String... packages) {
        for (String eachPackage : packages) {
            EXCLUDED_PATTERN.add(Pattern.compile(String.format(".*/%s/.*", eachPackage)));
        }
    }

    @Override
    public boolean includes(Location location) {
        //遍历pattern依次与location中的uri进行匹配 
        for (Pattern pattern : EXCLUDED_PATTERN) {
            if (location.matches(pattern)) {
                return false;
            }
        }
        return true;
    }
}
```

使用注解导入类

```java
@AnalyzeClasses(packages = "com.gongkongsaas.product",
                importOptions = {CustomImportOption.class,
                                 ImportOption.DoNotIncludeTests.class,
                                 ImportOption.DoNotIncludeJars.class})
   /**
     * 在类上使用AnalyzeClass注解
     * packages表示需要导入的包路径,数组类型,可以导入多个包路径
     * importOptions表示需要使用的importOption, 指定自定义的importOption可以排除指定包路径
     */
```

### 断言约束

ArchUnit提供了一些简单的规则供我们使用，位于GeneralCodingRules中

- NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS：类不能直接抛出通用异常Throwable、Exception或者RuntimeException。

- NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING：禁用 java.util.logging 包下的日志组件。

- NO_CLASSES_SHOULD_USE_JODATIME： 禁用JodaTime （DateTime now = DateTime.now()；） 

- NO_CLASSES_SHOULD_USE_FIELD_INJECTION ： 禁用字段注入 （@Autowird  @Resource  @Inject ）

- NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS: 禁用系统标准流             System.out     System.err    e.printStackTrace()

```java
/**
     * 禁止使用字段注入 (Autowired; Resource; Inject)
     */
    @ArchTest
    private final ArchRule no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    /**
     * 禁止使用系统标准流   System.out   System.err.    e.printStackTrace()
     * ArchIgnore 以这种方式标记的规则将在评估期间跳过
     */
    @ArchIgnore
    @ArchTest
    private final ArchRule NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    /**
     * 禁止抛出通用异常 Throwable, Exception, RuntimeException
     */
    @ArchTest
    private final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    /**
     * 禁止使用java.util.logging 下的日志组件
     */
    @ArchTest
    private final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    private final ArchRule no_class_use_jodaTime = NO_CLASSES_SHOULD_USE_JODATIME;
```

除了内建的规则，archUnit还提供了一个抽象的  “DSL_Like“ fluent API ， 比如想要表达 Services 只被 Controller 和 Service 访问 ，可以这么写

```java
ArchRule myRule = classes()
    .that().resideInAPackage("..service..")
    .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..");
```

..xx.. 代表任意数量的包  （类似 AspectJ Pointcuts 的写法 ）

完整例子： 

```java
JavaClasses importedClasses = new ClassFileImporter().importPackages("xxx.xxx");

ArchRule myRule = classes()
        .that().resideInAPackage("..service..")
        .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..");

myRule.check(importedClasses);
```

使用junit

```java
@RunWith(ArchUnitRunner.class) // Remove this line for JUnit 5!!
@AnalyzeClasses(packages = "xxx.xxx")
public class MyArchitectureTest {

    @ArchTest
    public static final ArchRule myRule = classes()
        .that().resideInAPackage("..service..")
        .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..");

}
```

## 检查了什么

### 包依赖检查

![软件包部门无法访问](https://www.archunit.org/userguide/html/package-deps-no-access.png)

```java
noClasses().that().resideInAPackage("..source..")
    .should().dependOnClassesThat().resideInAPackage("..foo..")
```

 source包中类不可以依赖foo包下的类 

![包 deps 只能访问](https://www.archunit.org/userguide/html/package-deps-only-access.png)

```java
classes().that().resideInAPackage("..foo..")
    .should().onlyHaveDependentClassesThat().resideInAnyPackage("..source.one..", "..foo..")
```

foo包下的类只能被source包下和foo包下的类依赖 

### 类依赖检查

![类命名部门](https://www.archunit.org/userguide/html/class-naming-deps.png)

```java
classes().that().haveNameMatching(".*Bar")
    .should().onlyHaveDependentClassesThat().haveSimpleName("Bar")
```

类名以Bar结尾的类， 只能被Bar这个类依赖 

### 包含检查

![类包包含](https://www.archunit.org/userguide/html/class-package-contain.png)

```java
classes().that().haveSimpleNameStartingWith("Foo")
    .should().resideInAPackage("com.foo")
```

类名以Foo开头的类，需要放在com.foo包下

### 继承检查

![继承命名检查](https://www.archunit.org/userguide/html/inheritance-naming-check.png)

```java
classes().that().implement(Connection.class)
    .should().haveSimpleNameEndingWith("Connection")
```

实现Connection接口的类， 类名应该以Connection结尾

![继承访问检查](https://www.archunit.org/userguide/html/inheritance-access-check.png)

```java
classes().that().areAssignableTo(EntityManager.class)
    .should().onlyHaveDependentClassesThat().resideInAnyPackage("..persistence..")
```

继承EntityManager的类， 应该只被persistence包下的类依赖 

### 层检查

![层检查](https://www.archunit.org/userguide/html/layer-check.png)

```java
layeredArchitecture()
    .layer("Controller").definedBy("..controller..")
    .layer("Service").definedBy("..service..")
    .layer("Persistence").definedBy("..persistence..")

    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
    .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service")
```

首先定义了三层架构 

controller包下的 为Controller层 

service包下的为Service层

persistence包下的为Persistence层 

然后指定访问规则 

Controller层不该被其他层访问

Service层只能被Controller层访问

Persistence层只能被Service层访问

## 自定义规则

archUnit也支持更灵活的自定义， 举个例子， 编码规范要求， 所有查询接口@RequestBody入参需要以Query结尾， 演示代码：

```java
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
		 * 这一步可以合并到上一步的指定扫描包中， 这里为了演示功能
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
		ArchRuleDefinition.classes()
						.that(predicate)
						.should(condition)
						.check(classes);
	}
```
