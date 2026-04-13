package com.glpi.identity.compiler;

import net.jqwik.api.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * Preservation property tests for the compiler parameters flag fix.
 *
 * These tests MUST PASS on UNFIXED code — they capture baseline behavior
 * that must be preserved after the fix is applied.
 *
 * Part A: Non-paginated endpoint parameters are unaffected by the -parameters flag.
 * Part B: Existing Docker profile files (ticket-service, notification-service) are intact.
 * Part C: Root pom.xml retains existing compiler plugin configuration.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
 */
class CompilerParametersFlagPreservationProperties {

    /**
     * Resolves the multi-module project root by walking up from the current
     * working directory until we find a pom.xml containing the parent artifactId.
     */
    private static Path resolveProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path pom = current.resolve("pom.xml");
            if (Files.exists(pom)) {
                try {
                    String content = Files.readString(pom);
                    if (content.contains("<packaging>pom</packaging>")
                            && content.contains("<artifactId>glpi-microservices-backend</artifactId>")) {
                        return current;
                    }
                } catch (Exception ignored) {
                }
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find project root (pom.xml with glpi-microservices-backend)");
    }

    private static final Path PROJECT_ROOT = resolveProjectRoot();

    // --- Part A: Non-Paginated Endpoint Parameter Preservation ---

    record ControllerInfo(String className, List<String> paginatedMethods) {
        @Override
        public String toString() {
            return className.substring(className.lastIndexOf('.') + 1);
        }
    }

    private static final List<ControllerInfo> CONTROLLERS = List.of(
            new ControllerInfo(
                    "com.glpi.identity.adapter.in.rest.UserController",
                    List.of("listUsers")),
            new ControllerInfo(
                    "com.glpi.identity.adapter.in.rest.EntityController",
                    List.of("listEntities")),
            new ControllerInfo(
                    "com.glpi.identity.adapter.in.rest.GroupController",
                    List.of("listGroups")),
            new ControllerInfo(
                    "com.glpi.identity.adapter.in.rest.ProfileController",
                    List.of("listProfiles")),
            new ControllerInfo(
                    "com.glpi.identity.adapter.in.rest.AuthController",
                    List.of())
    );

    record NonPaginatedMethod(String controllerName, String methodName) {
        @Override
        public String toString() {
            return controllerName.substring(controllerName.lastIndexOf('.') + 1) + "." + methodName;
        }
    }

    @Provide
    Arbitrary<NonPaginatedMethod> nonPaginatedControllerMethods() {
        List<NonPaginatedMethod> methods = new ArrayList<>();

        for (ControllerInfo info : CONTROLLERS) {
            try {
                Class<?> clazz = Class.forName(info.className());
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!info.paginatedMethods().contains(method.getName())
                            && isEndpointMethod(method)) {
                        methods.add(new NonPaginatedMethod(info.className(), method.getName()));
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Controller class not found: " + info.className(), e);
            }
        }

        return Arbitraries.of(methods);
    }

    /**
     * Property: For all non-paginated methods across identity-service controllers,
     * no @RequestParam annotation exists without an explicit 'value' attribute.
     * These methods use @PathVariable, @RequestBody, or @RequestHeader — they are
     * unaffected by the -parameters flag.
     *
     * Validates: Requirements 3.1, 3.6
     */
    @Property
    void nonPaginatedMethodsMustNotHaveRequestParamWithoutExplicitValue(
            @ForAll("nonPaginatedControllerMethods") NonPaginatedMethod npm) throws Exception {

        Class<?> clazz = Class.forName(npm.controllerName());
        Method method = findMethod(clazz, npm.methodName());

        for (Parameter param : method.getParameters()) {
            RequestParam rp = param.getAnnotation(RequestParam.class);
            if (rp != null) {
                // If @RequestParam is present, it must have an explicit 'value' attribute
                String value = rp.value();
                assert !value.isEmpty() :
                        npm + " parameter '" + param.getName()
                                + "' has @RequestParam without explicit value attribute";
            }
        }
    }

    // --- Part B: Existing Docker Profile File Preservation ---

    /**
     * Example: ticket-service application-docker.yml exists and contains Kafka config.
     *
     * Validates: Requirements 3.4
     */
    @Example
    boolean ticketServiceDockerProfileFileExistsWithKafkaConfig() throws Exception {
        Path filePath = PROJECT_ROOT.resolve(
                Paths.get("ticket-service", "src", "main", "resources", "application-docker.yml"));
        assert Files.exists(filePath) :
                "ticket-service/src/main/resources/application-docker.yml does not exist";

        String content = Files.readString(filePath);
        assert content.contains("bootstrap-servers") :
                "ticket-service application-docker.yml does not contain Kafka bootstrap-servers config";
        assert content.contains("${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}") :
                "ticket-service application-docker.yml missing expected Kafka bootstrap-servers pattern";

        return true;
    }

    /**
     * Example: notification-service application-docker.yml exists and contains Kafka config.
     *
     * Validates: Requirements 3.4, 3.5
     */
    @Example
    boolean notificationServiceDockerProfileFileExistsWithKafkaConfig() throws Exception {
        Path filePath = PROJECT_ROOT.resolve(
                Paths.get("notification-service", "src", "main", "resources", "application-docker.yml"));
        assert Files.exists(filePath) :
                "notification-service/src/main/resources/application-docker.yml does not exist";

        String content = Files.readString(filePath);
        assert content.contains("bootstrap-servers") :
                "notification-service application-docker.yml does not contain Kafka bootstrap-servers config";
        assert content.contains("${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}") :
                "notification-service application-docker.yml missing expected Kafka bootstrap-servers pattern";

        return true;
    }

    // --- Part C: Compiler Plugin Baseline Preservation ---

    /**
     * Example: Root pom.xml contains maven-compiler-plugin with source, target, and encoding config.
     *
     * Validates: Requirements 3.3
     */
    @Example
    boolean rootPomContainsCompilerPluginBaselineConfig() throws Exception {
        Path pomPath = PROJECT_ROOT.resolve("pom.xml");
        assert Files.exists(pomPath) : "Root pom.xml does not exist";

        String content = Files.readString(pomPath);

        assert content.contains("maven-compiler-plugin") :
                "Root pom.xml does not contain maven-compiler-plugin";
        assert content.contains("<source>") :
                "Root pom.xml maven-compiler-plugin missing <source> configuration";
        assert content.contains("<target>") :
                "Root pom.xml maven-compiler-plugin missing <target> configuration";
        assert content.contains("<encoding>") :
                "Root pom.xml maven-compiler-plugin missing <encoding> configuration";

        return true;
    }

    // --- Helpers ---

    private boolean isEndpointMethod(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            String annotationName = annotation.annotationType().getSimpleName();
            if (annotationName.equals("GetMapping")
                    || annotationName.equals("PostMapping")
                    || annotationName.equals("PutMapping")
                    || annotationName.equals("DeleteMapping")
                    || annotationName.equals("PatchMapping")
                    || annotationName.equals("RequestMapping")) {
                return true;
            }
        }
        return false;
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Method " + methodName + " not found in " + clazz.getSimpleName()));
    }
}
