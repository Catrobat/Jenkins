import java.security.MessageDigest

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.WorkflowJob

class PiplineFromScriptJobBuilder extends JobBuilder {

    PiplineFromScriptJobBuilder(WorkflowJob job, DslFactory dslFactory, def projectData = null) {
        super(job, dslFactory, projectData)
    }

    protected void jobDefaults() {
        logRotator(30, 100)
    }

    void jenkinsfileScriptPath(String jenkinsfilePath) {
        job.definition {
            cps {
                script(dslFactory.readFileFromWorkspace(jenkinsfilePath))
                sandbox()
            }
        }
    }
}
