package sh.ivan.yup;

import cz.habarta.typescript.generator.GsonConfiguration;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Jackson2Configuration;
import cz.habarta.typescript.generator.JsonLibrary;
import cz.habarta.typescript.generator.JsonbConfiguration;
import cz.habarta.typescript.generator.Logger;
import cz.habarta.typescript.generator.Output;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generates TypeScript declaration file from specified java classes.
 * For more information see README and Wiki on GitHub.
 */
@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
public class GenerateYupSchemasMojo extends AbstractMojo {

    /**
     * Path and name of generated TypeScript file.
     */
    @Parameter
    private File outputFile;

    /**
     * Classes to process.
     */
    @Parameter
    private List<String> classes;

    /**
     * Classes to process specified using glob patterns
     * so it is possible to specify package or class name suffix.
     * Glob patterns support two wildcards:
     * <ul>
     * <li>Single <code>*</code> wildcard matches any character except for <code>.</code> and <code>$</code>.</li>
     * <li>Double <code>**</code> wildcard matches any character.</li>
     * </ul>
     * For more information and examples see <a href="https://github.com/vojtechhabarta/typescript-generator/wiki/Class-Names-Glob-Patterns">Class Names Glob Patterns</a> Wiki page.
     */
    @Parameter
    private List<String> classPatterns;

    /**
     * Classes to process specified by annotations.
     */
    @Parameter
    private List<String> classesWithAnnotations;

    /**
     * Classes to process specified by implemented interface.
     */
    @Parameter
    private List<String> classesImplementingInterfaces;

    /**
     * Classes to process specified by extended superclasses.
     */
    @Parameter
    private List<String> classesExtendingClasses;

    /**
     * Scans specified JAX-RS {@link javax.ws.rs.core.Application} for classes to process.
     * Parameter contains fully-qualified class name.
     * It is possible to exclude particular REST resource classes using {@link #excludeClasses} parameter.
     */
    @Parameter
    private String classesFromJaxrsApplication;

    /**
     * Scans JAX-RS resources for JSON classes to process.
     * It is possible to exclude particular REST resource classes using {@link #excludeClasses} parameter.
     */
    @Parameter
    private boolean classesFromAutomaticJaxrsApplication;

    /**
     * Allows to speed up classpath scanning by limiting scanning to specified packages.
     * This optimization applies to following parameters:
     * <ul>
     * <li><code>classPatterns</code></li>
     * <li><code>classesImplementingInterfaces</code></li>
     * <li><code>classesExtendingClasses</code></li>
     * <li><code>classesWithAnnotations</code></li>
     * <li><code>classesFromAutomaticJaxrsApplication</code></li>
     * </ul>
     * This parameter is passed directly to underlying classpath scanning library (ClassGraph) without any validation or interpretation.
     */
    @Parameter
    private List<String> scanningAcceptedPackages;

    /**
     * List of classes excluded from processing.
     */
    @Parameter
    private List<String> excludeClasses;

    /**
     * Excluded classes specified using glob patterns.
     * For more information and examples see <a href="https://github.com/vojtechhabarta/typescript-generator/wiki/Class-Names-Glob-Patterns">Class Names Glob Patterns</a> Wiki page.
     */
    @Parameter
    private List<String> excludeClassPatterns;

    /**
     * If this list is not empty then only properties with any of these annotations will be included.
     */
    @Parameter
    private List<String> includePropertyAnnotations;

    /**
     * Properties with any of these annotations will be excluded.
     */
    @Parameter
    private List<String> excludePropertyAnnotations;

    /**
     * Library used in JSON classes.
     * Supported values are:
     * <ul>
     * <li><code>jackson2</code> - annotations from `com.fasterxml.jackson.annotation` package</li>
     * <li><code>jaxb</code> - annotations from `javax.xml.bind.annotation` package<li>
     * <li><code>gson</code> - annotations from `com.google.gson.annotations` package<li>
     * <li><code>jsonb</code> - annotations from `javax.json.bind.annotation` package<li>
     * </ul>
     * Required parameter, recommended value is <code>jackson2</code>.
     */
    @Parameter(required = true)
    private JsonLibrary jsonLibrary;

    /**
     * Specifies Jackson 2 global configuration.
     * Description of individual parameters is in
     * <a href="https://github.com/vojtechhabarta/typescript-generator/blob/main/typescript-generator-core/src/main/java/cz/habarta/typescript/generator/Jackson2Configuration.java">Jackson2Configuration</a>
     * class on GitHub (latest version).
     */
    @Parameter
    private Jackson2Configuration jackson2Configuration;

    /**
     * Specifies Gson global configuration.
     * Description of individual parameters is in
     * <a href="https://github.com/vojtechhabarta/typescript-generator/blob/main/typescript-generator-core/src/main/java/cz/habarta/typescript/generator/GsonConfiguration.java">GsonConfiguration</a>
     * class on GitHub (latest version).
     */
    @Parameter
    private GsonConfiguration gsonConfiguration;

    /**
     * Specifies JSON-B global configuration.
     * Description of individual parameters is in
     * <a href="https://github.com/vojtechhabarta/typescript-generator/blob/main/typescript-generator-core/src/main/java/cz/habarta/typescript/generator/JsonbConfiguration.java">Jackson2Configuration</a>
     * class on GitHub (latest version).
     */
    @Parameter
    private JsonbConfiguration jsonbConfiguration;

    /**
     * If <code>true</code> Spring REST application will be loaded and scanned for classes to process.
     * It is needed to specify application class using another parameter (for example {@link #classes}).
     */
    @Parameter
    private boolean scanSpringApplication;

    /**
     * Turns on Jackson2 automatic module discovery.
     */
    @Parameter
    private boolean jackson2ModuleDiscovery;

    /**
     * Specifies Jackson2 modules to use.
     */
    @Parameter
    private List<String> jackson2Modules;

    /**
     * Specifies level of logging output.
     * Supported values are:
     * <ul>
     * <li><code>Debug</code></li>
     * <li><code>Verbose</code></li>
     * <li><code>Info</code></li>
     * <li><code>Warning</code></li>
     * <li><code>Error</code></li>
     * </ul>
     * Default value is <code>Verbose</code>.
     */
    @Parameter
    private Logger.Level loggingLevel;

    @Parameter(property = "java.to.yup.skip")
    private boolean skip;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private String projectBuildDirectory;

    private Settings createSettings(URLClassLoader classLoader) {
        final Settings settings = new Settings();
        settings.setExcludeFilter(excludeClasses, excludeClassPatterns);
        settings.jsonLibrary = jsonLibrary;
        settings.setJackson2Configuration(classLoader, jackson2Configuration);
        settings.gsonConfiguration = gsonConfiguration;
        settings.jsonbConfiguration = jsonbConfiguration;
        settings.scanSpringApplication = scanSpringApplication;
        settings.loadIncludePropertyAnnotations(classLoader, includePropertyAnnotations);
        settings.loadExcludePropertyAnnotations(classLoader, excludePropertyAnnotations);
        settings.jackson2ModuleDiscovery = jackson2ModuleDiscovery;
        settings.loadJackson2Modules(classLoader, jackson2Modules);
        settings.classLoader = classLoader;
        return settings;
    }

    @Override
    public void execute() {
        TypeScriptGenerator.setLogger(new Logger(loggingLevel));
        TypeScriptGenerator.printVersion();
        if (skip) {
            TypeScriptGenerator.getLogger().info("Skipping plugin execution");
            return;
        }

        // class loader
        final List<URL> urls = new ArrayList<>();
        try {
            for (String element : project.getCompileClasspathElements()) {
                urls.add(new File(element).toURI().toURL());
            }
        } catch (DependencyResolutionRequiredException | IOException e) {
            throw new RuntimeException(e);
        }

        try (URLClassLoader classLoader = Settings.createClassLoader(
                project.getArtifactId(),
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader())) {

            final Settings settings = createSettings(classLoader);

            final Input.Parameters parameters = new Input.Parameters();
            parameters.classNames = classes;
            parameters.classNamePatterns = classPatterns;
            parameters.classesWithAnnotations = classesWithAnnotations;
            parameters.classesImplementingInterfaces = classesImplementingInterfaces;
            parameters.classesExtendingClasses = classesExtendingClasses;
            parameters.jaxrsApplicationClassName = classesFromJaxrsApplication;
            parameters.automaticJaxrsApplication = classesFromAutomaticJaxrsApplication;
            parameters.isClassNameExcluded = settings.getExcludeFilter();
            parameters.classLoader = classLoader;
            parameters.scanningAcceptedPackages = scanningAcceptedPackages;
            parameters.debug = loggingLevel == Logger.Level.Debug;

            final File output = outputFile != null
                    ? outputFile
                    : new File(
                            new File(projectBuildDirectory, "typescript-generator"),
                            project.getArtifactId() + settings.getExtension());
            settings.validateFileName(output);

            new TypeScriptGenerator(settings).generateTypeScript(Input.from(parameters), Output.to(output));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}