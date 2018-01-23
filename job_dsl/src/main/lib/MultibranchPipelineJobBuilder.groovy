import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.MultibranchWorkflowJob

class MultibranchPipelineJobBuilder extends JobBuilder {

    MultibranchPipelineJobBuilder(MultibranchWorkflowJob job, DslFactory dslFactory, def data) {
        super(job, dslFactory, data)
    }

    void gitHubOrganization() {
        job.branchSources {
            branchSource {
                source {
                    github {
                        repoOwner(data.repoOwner)
                        repository(data.repoName)
                        apiUri('https://api.github.com')
                        credentialsId(data.githubOrganizationsJenkinsCredentialsRefId)
                    }
                }
            } 
        }
    }

    void jenkinsfilePath(String jenkinsfilePath) {
        job.configure {
            it / factory(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory') {
                scriptPath(jenkinsfilePath)
            }
        }
    }
}
