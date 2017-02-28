import java.util.Map
import org.gradle.model.*
import org.gradle.platform.base.*
import org.gradle.nativeplatform.toolchain.*
import org.gradle.api.InvalidUserDataException
import org.gradle.nativeplatform.toolchain.internal.msvcpp.*;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec

@Managed interface BuildConfig {
    void setArchitecture(String arch)
    String getArchitecture()

    void setOperatingSystem(String os)
    String getOperatingSystem()

    void setToolChainPrefix(String prefix)
    String getToolChainPrefix()

    void setCompilerArgs(List<String> args)
    List<String> getCompilerArgs()

    void setLinkerArgs(List<String> args)
    List<String> getLinkerArgs()

    void setCrossCompile(boolean cc)
    boolean getCrossCompile()

    void setCompilerFamily(String family)
    String getCompilerFamily()

    void setExclude(List<String> toExclude)
    List<String> getExclude()
}

@Managed interface ComponentConfig {
    void setCrossCompile(boolean cc)
    boolean getCrossCompile()

    void setComponent(ComponentSpec component)
    ComponentSpec getComponent()
}

class BuildConfigRules extends RuleSource {
    @Model void buildConfigs(ModelMap<BuildConfig> configs) {}
    @Model void configComponents(ModelMap<ComponentConfig> components) {}

    @Validate void validateCompilerFamilyExists(ModelMap<BuildConfig> configs) {
        configs.each { config ->
            assert config.compilerFamily == 'VisualCpp' ||
                   config.compilerFamily == 'Gcc' ||
                   config.compileFamily == 'Clang'
        }
    }

    @Validate void setTargetPlatforms(ComponentSpecContainer components, ModelMap<BuildConfig> configs) {
        components.each { component ->
            configs.each { config ->
                if (config.exclude == null || !config.exclude.contains(component.name)) {
                    component.targetPlatform config.architecture
                }
            }
        }
    }

    @Mutate void disableCrossCompileGoogleTest(BinaryContainer binaries, ModelMap<BuildConfig> configs) {
        def crossCompileConfigs = configs.findAll { it.crossCompile }.collect { it.architecture }
        if (crossCompileConfigs != null && !crossCompileConfigs.empty) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { spec ->
                if (crossCompileConfigs.contains(spec.targetPlatform.architecture.name)) {
                    spec.buildable = false
                }
            }
        }
    }

    @Mutate void createPlatforms(PlatformContainer platforms, ModelMap<BuildConfig> configs) {
        if (configs == null) {
            return
        }

        configs.each { config ->
            if (config.architecture != null) {
                platforms.create(config.architecture) {
                    architecture config.architecture
                    if (config.operatingSystem != null) {
                        operatingSystem config.operatingSystem
                    }
                }
            }
        }
    }

    @Mutate void createToolChains(NativeToolChainRegistry toolChains, ModelMap<BuildConfig> configs) {
        if (configs == null) {
            return
        }

        def vcppConfigs = configs.findAll { it.compilerFamily == 'VisualCpp' }
        if (vcppConfigs != null && !vcppConfigs.empty) {
            toolChains.create('visualCpp', VisualCpp.class) { t ->
                t.eachPlatform { toolChain ->
                    def config = vcppConfigs.find { it.architecture == toolChain.platform.architecture.name }
                    if (config != null) {
                        if (config.toolChainPrefix != null) {
                            toolChain.cCompiler.executable = config.toolChainPrefix + toolChain.cCompiler.executable
                            toolChain.cppCompiler.executable = config.toolChainPrefix + toolChain.cppCompiler.executable
                            toolChain.linker.executable = config.toolChainPrefix + toolChain.linker.executable
                            toolChain.assembler.executable = config.toolChainPrefix + toolChain.assembler.executable
                            toolChain.staticLibArchiver.executable = config.toolChainPrefix + toolChain.staticLibArchiver.executable
                        }

                        if (config.compilerArgs != null) {
                            toolChain.cppCompiler.withArguments { args -> 
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            toolChain.linker.withArguments { args -> 
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }

        def gccConfigs = configs.findAll { it.compilerFamily == 'Gcc' }
        if (gccConfigs != null && !gccConfigs.empty) {
            toolChains.create('gcc', Gcc.class) {
                gccConfigs.each { config ->
                    target(config.architecture) {
                        if (config.toolChainPrefix != null) {
                            cCompiler.executable = config.toolChainPrefix + cCompiler.executable
                            cppCompiler.executable = config.toolChainPrefix + cppCompiler.executable
                            linker.executable = config.toolChainPrefix + linker.executable
                            assembler.executable = config.toolChainPrefix + assembler.executable
                            staticLibArchiver.executable = config.toolChainPrefix + staticLibArchiver.executable
                        }

                        if (config.compilerArgs != null) {
                            cppCompiler.withArguments { args -> 
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            linker.withArguments { args -> 
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }

        def clangConfigs = configs.findAll { it.compilerFamily == 'Clang' }
        if (clangConfigs != null && !clangConfigs.empty) {
            toolChains.create('clang', Clang.class) {
                clangConfigs.each { config ->
                    target(config.architecture) {
                        if (config.toolChainPrefix != null) {
                            cCompiler.executable = config.toolChainPrefix + cCompiler.executable
                            cppCompiler.executable = config.toolChainPrefix + cppCompiler.executable
                            linker.executable = config.toolChainPrefix + linker.executable
                            assembler.executable = config.toolChainPrefix + assembler.executable
                            staticLibArchiver.executable = config.toolChainPrefix + staticLibArchiver.executable
                        }

                        if (config.compilerArgs != null) {
                            cppCompiler.withArguments { args -> 
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            linker.withArguments { args -> 
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }
    }
}