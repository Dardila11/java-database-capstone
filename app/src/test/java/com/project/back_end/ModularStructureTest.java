package com.project.back_end;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularStructureTest {

    static final ApplicationModules modules =
            ApplicationModules.of(BackEndApplication.class);

    @Test
    void modulesAreCompliant() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }


}
