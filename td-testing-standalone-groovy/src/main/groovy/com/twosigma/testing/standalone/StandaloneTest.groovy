package com.twosigma.testing.standalone

import com.twosigma.testing.reporter.StepReporter
import com.twosigma.testing.reporter.StepReporters
import com.twosigma.testing.reporter.TestStep
import com.twosigma.testing.standalone.report.GroovyStackTraceUtils
import com.twosigma.utils.TraceUtils

import java.nio.file.Path

import static com.twosigma.testing.standalone.StandaloneTestStatus.Errored
import static com.twosigma.testing.standalone.StandaloneTestStatus.Failed
import static com.twosigma.testing.standalone.StandaloneTestStatus.Passed

/**
 * Most of the testing API can be used outside standard JUnit/TestNG setup.
 * One way is to define a simple script. TODO refer example here
 * @author mykola
 */
class StandaloneTest implements StepReporter {
    private static StandaloneTestIdGenerator idGenerator = new StandaloneTestIdGenerator()

    private String id
    private Path filePath
    private String description
    private Closure code

    private Throwable exception
    private String assertionMessage

    private List<StandaloneTestResultPayload> payloads
    private List<TestStep> steps

    StandaloneTest(Path filePath, String description, Closure code) {
        this.id = idGenerator.generate(filePath)
        this.filePath = filePath
        this.description = description
        this.code = code
        this.steps = []
        this.payloads = []
    }

    List<TestStep> getSteps() {
        return steps
    }

    Path getFilePath() {
        return filePath
    }

    boolean isPassed() {
        return !hasError() && !isFailed()
    }

    boolean hasError() {
        return exception != null && assertionMessage == null
    }

    boolean isFailed() {
        return assertionMessage != null
    }

    String getDescription() {
        return description
    }

    Throwable getException() {
        return exception
    }

    StandaloneTestStatus getStatus() {
        if (hasError()) {
            return Errored
        }

        if (isFailed()) {
            return Failed
        }

        return Passed
    }

    void addResultPayload(StandaloneTestResultPayload payload) {
        payloads.add(payload)
    }

    Map<String, ?> toMap() {
        def testAsMap = [id                : id,
                         scenario          : description,
                         fileName          : filePath.fileName.toString(),
                         status            : getStatus().toString(),
                         assertion         : assertionMessage,
                         contextDescription: steps.find { it.isFailed() }?.firstAvailableContext?.toString(),
                         exceptionMessage  : exception ? renderExceptionNameAndMessage(exception) : null,
                         shortStackTrace   : exception ? GroovyStackTraceUtils.renderStackTraceWithoutLibCalls(exception) : null,
                         fullStackTrace    : exception ? TraceUtils.stackTrace(exception) : null]

        payloads.each { testAsMap << it.toMap() }

        return testAsMap
    }

    void run() {
        try {
            StepReporters.add(this)
            code.run()
        } catch (AssertionError e) {
            exception = e
            assertionMessage = e.message
        } catch (Throwable e) {
            exception = e
        } finally {
            StepReporters.remove(this)
        }
    }

    @Override
    void onStepStart(TestStep step) {
        if (step.getNumberOfParents() == 0) {
            steps.add(step)
        }
    }

    @Override
    void onStepSuccess(TestStep step) {
    }

    @Override
    void onStepFailure(TestStep step) {
    }

    private static String renderExceptionNameAndMessage(Throwable t) {
        def message = t.getMessage()
        return t.getClass().canonicalName + (message != null ? ": " + message : "")
    }
}
