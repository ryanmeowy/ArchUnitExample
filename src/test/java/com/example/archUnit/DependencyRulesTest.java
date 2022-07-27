package com.example.archUnit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

@AnalyzeClasses(packages = "com.example.archUnit",
        importOptions = {CustomImportOption.class,
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class})
public class DependencyRulesTest {

    /**
     * 禁止依赖上层
     */
    @ArchTest
    static final ArchRule no_accesses_to_upper_package = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
}
