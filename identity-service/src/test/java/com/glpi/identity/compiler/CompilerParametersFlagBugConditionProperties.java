package com.glpi.identity.compiler;

import net.jqwik.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Bug condition exploration test for the missing -parameters compiler flag.
 *
 * Part A: Verifies that paginated controller method parameters retain their real names
 *         (page, size, sort, order) instead of arg0, arg1, arg2, arg3.
 *         On UNFIXED code this test FAILS — confirming the bug exists.
 *
 * Part B: Verifies that application-docker.yml exists for the 7 missing services.
 *         On UNFIXED code this test FAILS — confirming the gap exists.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4
 */
class CompilerParametersFlagBugConditionProperties {

    // --- Part A: Parameter Name Resolution (PRIMARY) ---

    record ControllerMethod(String className, String methodName) {
        @Override
        public String toString() {
            return className.substring(className.lastIndexOf('.') + 1) + "." + methodName;
        }
    }

    @Provide
    Arbitrary<ControllerMethod> paginatedControllerMethods() {
        List<ControllerMethod> methods = List.of(
                new ControllerMethod(
                        "com.glpi.identity.adapter.in.rest.UserController", "listUsers"),
                new ControllerMethod(
                        "com.glpi.identity.adapter.in.rest.EntityController", "listEntities"),
                new ControllerMethod(
                        "com.glpi.identity.adapter.in.rest.GroupController", "listGroups"),
                new ControllerMethod(
                        "com.glpi.identity.adapter.in.rest.ProfileController", "listProfiles")
        );
        return Arbitraries.of(methods);
    }

    /**
     * Property: For every paginated controller method in identity-service,
     * the first 4 parameters must retain their real names (page, size, sort, order).
     *
     * On UNFIXED code (no -parameters flag): parameters are named arg0..arg3 → FAILS
     * On FIXED code (with -parameters flag): parameters retain real names → PASSES
     *
     * Validates: Requirements 1.1, 1.2
     */
    @Property
    void paginatedMethodParametersMustRetainRealNames(
            @ForAll("paginatedControllerMethods") ControllerMethod cm) throws Exception {

        Class<?> clazz = Class.forName(cm.className());
        Method method = findMethod(clazz, cm.methodName());

        Parameter[] params = method.getParameters();
        List<String> expectedNames = List.of("page", "size", "sort", "order");

        for (int i = 0; i < expectedNames.size(); i++) {
            String actual = params[i].getName();
            String expected = expectedNames.get(i);
            assert actual.equals(expected) :
                    cm + " parameter " + i + " is named '" + actual + "' instead of '" + expected + "'";
        }
    }

    // --- Part B: Docker Profile File Existence (SECONDARY) ---

    @Provide
    Arbitrary<String> missingDockerProfileServices() {
        List<String> services = List.of(
                "identity-service",
                "api-gateway",
                "problem-service",
                "change-service",
                "asset-service",
                "sla-service",
                "knowledge-service"
        );
        return Arbitraries.of(services);
    }

    /**
     * Property: For every service that is missing application-docker.yml,
     * the file must exist at src/main/resources/application-docker.yml.
     *
     * On UNFIXED code: files do not exist → FAILS
     * On FIXED code: files exist → PASSES
     *
     * Validates: Requirements 1.4
     */
    @Property
    void dockerProfileFileMustExistForAllServices(
            @ForAll("missingDockerProfileServices") String serviceName) {

        Path filePath = Paths.get(serviceName, "src", "main", "resources", "application-docker.yml");
        assert Files.exists(filePath) :
                serviceName + "/src/main/resources/application-docker.yml does not exist";
    }

    // --- Helper ---

    private Method findMethod(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Method " + methodName + " not found in " + clazz.getSimpleName()));
    }
}
