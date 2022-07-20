# ArchUnit

## ArchUnit是什么？

**[官网介绍](https://www.ArchUnit.org/):**

ArchUnit is a free, simple and extensible library for checking the architecture of your Java code using any plain Java unit test framework.That is, ArchUnit can check dependencies between packages and classes, layers and slices, check for cyclic dependencies and more. It does so by analyzing given Java bytecode, importing all classes into a Java code structure. 

ArchUnit是`一个`免费的、简单的、可扩展的类库，用于检查Java代码的体系结构。提供检查包和类的依赖关系、调用层次和切面的依赖关系、循环依赖检查等其他功能。它通过导入所有类的代码结构，基于Java字节码分析实现这一点。



**应用场景**

相信大部分的的开发人员有遇到过这样的情况：

开始有人画了一些漂亮的架构图，展示了系统应该包含的组件以及它们应该如何交互，大家形成一个约定并达成共识。但是随着项目逐渐变得更大，一般会经历开发人员的调整，包括新开发人员的加入或者老开发人员离开去做其它项目等。当新的需求或者特性添加进来，由于开发人员的差异，可能会出现一些不可预见的违反规范的行为，如：

- *命名不规范*

- *分层代码调用不规范，比如Controller直接调用Dao*

- ... ... 

这些问题可能需要在Review的时候才会被看到，并不是一种很及时的解决方法。

---

>  环境：JDK1.8、ArchUnit0.22.0 、gradle6.7、SpringBoot 2.3.4.RELEASE、junit 5

## 快速开始

导入依赖

```gradle
dependencies {
    testImplementation 'com.tngtech.ArchUnit:ArchUnit:0.22.0'
    testImplementation 'com.tngtech.ArchUnit:ArchUnit-junit5:0.22.0'
}
```

从下面三个方面来介绍下ArchUnit的基本使用

- 指定需要扫描的包 (注解和代码)
- 内建规则定义 (代码)
- 自定义规则（代码）

本次demo只演示ArchUnit的一部分用法，想了解更多，可以参考：[官方文档](https://www.ArchUnit.org/userguide/html/000_Index.html#_introduction)和[官方example](https://github.com/TNG/ArchUnit-Examples)

**指定扫描包名/路径（代码）** 

```java
    @Test
    void importerTest() {
        /*
           JavaClasses是JavaClass的集合,可以简单理解为反射中的Class集合
           后面使用代码规则和规则判断都强依赖与JavaClasses或者JavaClass
         */

         // 排除jar包和测试包
        ImportOptions importOptions = new ImportOptions()
                    .with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                    .with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .with(new CustomImportOption());
        // 导入所有class
        JavaClasses classes = new ClassFileImporter(importOptions).importPackages("com.gongkongsaas.product");
        JavaClass javaClass = classes.get(AppConfig.class);
        // 排除了 com.ryan.ArchUnitdemo.config 包后, 无法扫描到指定类, 将会报错
        Assertions.assertEquals("AppConfig",javaClass1.getSimpleName());
    }
```

**自定义ImportOption**

```java
/**
 * 自定义ImportOption
 *
 * @author ryan
 */
public class CustomImportOption implements ImportOption {

      private static final Pattern CONFING_PATTERN = Pattern.compile(".*/com/gongkongsaas/product/config/.*");
    private static final Set<Pattern> EXCLUDED_PATTERN = ImmutableSet.of(CONFIG_PATTERN);
    /**
     * @param location Location中包含路径信息 是否jar文件等判断属性的元数据, 方便使用正则表达式或者直接的逻辑判断
     * @return false:
     */
    @Override
    public boolean includes(Location location) {
        for (Pattern pattern : EXCLUDED_PATTERN) {
            if (location.matches(pattern)) {
                return false;
            }
        }
        return true;
    }
}
```

**指定扫描报名/路径 （注解）**

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

**内建规则定义**

- 类扫描完成后，接来下就是将定义的规则应用于所有类并进行断言

- ArchUnit提供了一些简单的规则供我们使用，位于GeneralCodingRules中 
  
  - NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS：不能调用System.out、System.err或者(Exception.)printStackTrace。
  - NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS：类不能直接抛出通用异常Throwable、Exception或者RuntimeException。
  - NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING：不能使用`java.util.logging`包路径下的日志组件。

```java
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

     /**
     * 禁止依赖上层
     */
    @ArchTest
    static final ArchRule no_accesses_to_upper_package = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
```
