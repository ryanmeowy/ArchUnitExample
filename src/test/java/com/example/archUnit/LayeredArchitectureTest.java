package com.example.archUnit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.base.PackageMatchers;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Functions.GET_PACKAGE_NAME;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.example.archUnit",
        importOptions = {CustomImportOption.class,
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class})
public class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()

            .layer("Controllers").definedBy("..controller..")
            .layer("Services").definedBy("..service..")
            .layer("Persistence").definedBy("..dao..")

            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers")
            .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Services");




    private static DescribedPredicate<JavaClass> areDeclaredInManagerAndMq() {
        return GET_PACKAGE_NAME.is(PackageMatchers.of("..manager..","..mq.."))
                .as("a package '..manager..'");
    }

    private static DescribedPredicate<JavaClass> areDeclaredInDao() {
        return GET_PACKAGE_NAME.is(PackageMatchers.of("..dao.."))
                .as("a package '..dao..'");
    }

    private static DescribedPredicate<JavaClass> areDeclaredInMq() {
        return GET_PACKAGE_NAME.is(PackageMatchers.of("..mq.."))
                .as("a package '..manager..'");
    }

    private static DescribedPredicate<JavaClass> areDeclaredInService() {
        return GET_PACKAGE_NAME.is(PackageMatchers.of("..service.."))
                .as("a package '..dao..'");
    }
}
