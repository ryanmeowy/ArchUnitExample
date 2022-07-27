package com.example.archUnit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.example.archUnit",
        importOptions = {CustomImportOption.class,
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class})
public class NamingConventionTest {

    @ArchTest
    static ArchRule services_should_be_suffixed =
            classes().that().resideInAPackage("..service..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service")
                    .andShould().resideInAPackage("..service..");

    @ArchTest
    static ArchRule entity_should_be_in_package_entity =
            classes().that().haveSimpleNameEndingWith("Entity")
                    .should().resideInAPackage("..entity..");

}
