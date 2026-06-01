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
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        rule.check(importedClasses);
    }

    @Test
    public void domainShouldNotDependOnApi() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.yowyob.loyalty");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..api..");

        rule.check(importedClasses);
    }
}
