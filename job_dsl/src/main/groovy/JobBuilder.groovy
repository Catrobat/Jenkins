import javaposse.jobdsl.dsl.Job

/**
 * @see <a href="https://www.cloudbees.com/sites/default/files/2016-jenkins-world-rule_jenkins_with_configuration_as_code_2.pdf">Rule Jenkins with Configuration as Code</a>
 */
class JobBuilder {
    protected Job job

    JobBuilder(Job job) {
        this.job = job
    }

    Job make(Closure additionalConfig) {
        runClosure(additionalConfig)
        job
    }

    private void runClosure(Closure closure) {
        // Create clone of closure for threading access.
        closure = closure.clone()

        // Set delegate of closure to this builder.
        closure.delegate = this

        // And only use this builder as the closure delegate.
        closure.resolveStrategy = Closure.DELEGATE_ONLY

        // Run closure code.
        closure()
    }

    // Delegate everything else to Job DSL
    def methodMissing(String name, argument) {
        job.invokeMethod(name, (Object[]) argument)
    }
}

