import com.diffplug.gradle.spotless.FormatExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import java.nio.charset.StandardCharsets

plugins {
    `java-library`
    `java-test-fixtures`
    idea
    jacoco
    io.freefair.lombok
    com.diffplug.spotless
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    test {
        useJUnitPlatform()
    }
}

spotless {
    encoding(StandardCharsets.UTF_8)

    fun FormatExtension.defaults() {
        toggleOffOn("@formatter:off", "@formatter:on")
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }

    if (plugins.hasPlugin(JavaBasePlugin::class)) java {
        cleanthat().version("2.20")
            .sourceCompatibility(java.sourceCompatibility.majorVersion)
            .addMutator("SafeAndConsensual")
            .addMutator("SafeButNotConsensual")
            .addMutator("ArithmethicAssignment")
            .addMutator("ArraysDotStream")
            .addMutator("AvoidMultipleUnaryOperators")
            .addMutator("CollectionIndexOfToContains")
            .addMutator("LiteralsFirstInComparisons")
            .addMutator("ModifierOrder")
            .addMutator("OptionalNotEmpty")
            .addMutator("RemoveExplicitCallToSuper")
            .addMutator("SimplifyBooleanExpression")
            .addMutator("SimplifyBooleanInitialization")
            .addMutator("SimplifyStartsWith")
            .addMutator("StreamAnyMatch")
            .addMutator("StringIndexOfToContains")
            .addMutator("StringToString")
            .addMutator("UnnecessaryBoxing")
            .addMutator("UnnecessaryModifier")
            .addMutator("UseCollectionIsEmpty")
            .addMutator("UseStringIsEmpty")
            .addMutator("UseUnderscoresInNumericLiterals")
            .addMutator("AvoidUncheckedExceptionsInSignatures")
            .addMutator("CollectionToOptional")
            .addMutator("EmptyControlStatement")
            .addMutator("EnumsWithoutEquals")
            .addMutator("EnumsWithoutEquals")
            .addMutator("ForEachIfBreakToStreamFindFirst")
            .addMutator("ForEachIfToIfStreamAnyMatch")
            .addMutator("LambdaIsMethodReference")
            .addMutator("LambdaReturnsSingleStatement")
            .addMutator("LocalVariableTypeInference")
            .addMutator("PrimitiveWrapperInstantiation")
            .addMutator("RedundantLogicalComplementsInStream")
            .addMutator("RemoveAllToClearCollection")
            .addMutator("StreamFlatMapStreamToFlatMap")
            .addMutator("StreamForEachNestingForLoopToFlatMap")
            .addMutator("StringFromString")
            .addMutator("UnnecessaryFullyQualifiedName")
            .addMutator("UnnecessaryImport")
            .addMutator("UnnecessaryLambdaEnclosingParameters")
            .addMutator("UnnecessarySemicolon")
            .addMutator("UseDiamondOperatorJdk8")
            .addMutator("UsePredefinedStandardCharset")
        palantirJavaFormat("2.50.0").style("PALANTIR").formatJavadoc(false)
        removeUnusedImports()
        importOrder(
            "java", "javax", "", "com.rassafel",
            "\\#java", "\\#javax", "\\#", "\\#com.rassafel"
        )
        defaults()
    }

    if (plugins.hasPlugin(GroovyBasePlugin::class)) groovy {
        importOrder(
            "java", "javax", "", "com.rassafel",
            "\\#java", "\\#javax", "\\#", "\\#com.rassafel"
        )
//      FixMe: spock
//        greclipse()
        excludeJava()
        defaults()
    }

    if (plugins.hasPlugin(KotlinBasePlugin::class)) kotlin {
        ktlint()
        defaults()
    }

    kotlinGradle {
        ktlint()
        defaults()
    }
}

dependencies {
    api(platform(project(":bom")))
    api("org.slf4j:slf4j-api")
}
