package com.twosigma.testing.standalone

import com.twosigma.utils.FileUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.nio.file.Path

/**
 * @author mykola
 */
class StandaloneTestRunner {
    private List<String> staticImports
    private List<StandaloneTest> tests

    private Path currentTestPath
    private GroovyShell groovy
    private List<StandaloneTestListener> testListeners

    StandaloneTestRunner(List<String> staticImports) {
        this.staticImports = staticImports
        this.testListeners = []
        this.tests = []
        this.groovy = prepareGroovy()
    }

    void addListener(StandaloneTestListener listener) {
        testListeners.add(listener)
    }

    void processScriptWithPath(Path scriptPath, delegate) {
        process(scriptPath, FileUtils.fileTextContent(scriptPath), delegate)
    }

    GroovyShell getGroovy() {
        return groovy
    }

    void process(Path scriptPath, String scriptBody, delegate) {
        currentTestPath = scriptPath

        def script = groovy.parse(scriptBody)
        script.setDelegate(delegate)
        script.setProperty("scenario", this.&scenario)

        testListeners.each { l -> l.beforeScriptParse(scriptPath) }
        script.run()
    }

    List<StandaloneTest> getTests() {
        return tests
    }

    int getNumberOfPassed() {
        return tests.count { it.isPassed() }
    }

    int getNumberOfFailed() {
        return tests.count { it.isFailed() }
    }

    int getNumberOfErrored() {
        return tests.count { it.hasError() }
    }

    void runTests() {
        testListeners.each { l -> l.beforeFirstTest() }
        tests.each { test ->
            testListeners.each { l -> l.beforeTestRun(test) }
            test.run()
            testListeners.each { l -> l.afterTestRun(test) }
        }
    }

    private void scenario(String description, Closure code) {
        def test = new StandaloneTest(currentTestPath, description, code)
        tests.add(test)
    }

    private GroovyShell prepareGroovy() {
        def imports = new ImportCustomizer()
        def fullListOfStatics = staticImports + [StandaloneTestRunner.canonicalName]
        println fullListOfStatics

        fullListOfStatics.forEach { imports.addStaticStars(it) }

        def compilerCfg = new CompilerConfiguration()
        compilerCfg.addCompilationCustomizers(imports)
        compilerCfg.scriptBaseClass = DelegatingScript.class.name

        return new GroovyShell(compilerCfg)
    }
}