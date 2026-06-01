package com.yowyob.loyaulty.program.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.yowyob.loyaulty.program");
    }

    // ── Règle 1 ───────────────────────────────────────────────────────────
    @Test
    void domain_must_not_depend_on_spring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "io.r2dbc..",
                        "org.apache.kafka..",
                        "io.lettuce..",
                        "org.postgresql.."
                )
                .because("Le domaine doit être pur — zéro dépendance infrastructure");
        rule.check(classes);
    }

    // ── Règle 2 ───────────────────────────────────────────────────────────
    @Test
    void domain_must_not_depend_on_application() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..")
                .because("Le domaine ne connaît pas la couche application");
        rule.check(classes);
    }

    // ── Règle 3 ───────────────────────────────────────────────────────────
    @Test
    void domain_must_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .because("Le domaine ne connaît pas l'infrastructure");
        rule.check(classes);
    }

    // ── Règle 4 ───────────────────────────────────────────────────────────
    @Test
    void domain_must_not_depend_on_api() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..api..")
                .because("Le domaine ne connaît pas la couche API");
        rule.check(classes);
    }

    // ── Règle 5 ───────────────────────────────────────────────────────────
    @Test
    void ports_out_must_be_interfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..port.out..")
                .and().areNotMemberClasses()
                .should().beInterfaces()
                .because("Les ports de sortie sont des contrats — uniquement des interfaces");
        rule.check(classes);
    }

    // ── Règle 6 ───────────────────────────────────────────────────────────
    @Test
    void ports_in_must_be_interfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..port.in..")
                .and().areNotMemberClasses()
                .should().beInterfaces()
                .because("Les ports d'entrée sont des contrats — uniquement des interfaces");
        rule.check(classes);
    }

    // ── Règle 7 ───────────────────────────────────────────────────────────
    @Test
    void domain_models_must_not_have_persistence_annotations() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith("org.springframework.data.relational.core.mapping.Table")
                .because("Les modèles domaine ne sont pas des entités de persistance");
        rule.check(classes);
    }

    // ── Règle 8 ───────────────────────────────────────────────────────────
    @Test
    void api_must_not_call_domain_services_directly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat()
                .resideInAPackage("..domain..service..")
                .because("L'API passe par les use cases de la couche application");
        rule.check(classes);
    }

    // ── Règle 9 ───────────────────────────────────────────────────────────
    @Test
    void infrastructure_must_not_depend_on_application() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..")
                .because("L'infrastructure ne connaît que les ports du domaine, pas la couche application");
        rule.check(classes);
    }

    // ── Règle 10 ──────────────────────────────────────────────────────────
    @Test
    void bonification_adapter_must_implement_port() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.bonification..")
                .and().haveSimpleNameEndingWith("Adapter")
                .and().doNotHaveSimpleName("BonificationEventAdapter")
                .should().implement(
                        com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationPort.class
                )
                .because("L'adapter de bonification doit implémenter le port du domaine");
        rule.check(classes);
    }

    // ── Règle 11 ──────────────────────────────────────────────────────────
    @Test
    void bonification_dtos_must_stay_in_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..infrastructure.bonification.dto..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..domain..", "..api..", "..application..")
                .because("Les DTOs de l'API externe ne doivent pas fuiter hors de l'infrastructure");
        rule.check(classes);
    }
}
