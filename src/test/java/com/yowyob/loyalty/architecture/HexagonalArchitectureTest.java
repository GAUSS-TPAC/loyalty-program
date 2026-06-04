package com.yowyob.loyalty.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalArchitectureTest {

    @Test
    public void domainShouldNotDependOnInfrastructure() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.yowyob.loyalty");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameNotEndingWith("Test")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        rule.check(importedClasses);
    }

    @Test
    public void domainShouldNotDependOnApi() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.yowyob.loyalty");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameNotEndingWith("Test")
                .should().dependOnClassesThat().resideInAnyPackage("com.yowyob.loyalty.api..");

        rule.check(importedClasses);
    }
}
