import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.MultibranchWorkflowJob

class MultibranchPipelineJobBuilder extends JobBuilder {

    MultibranchPipelineJobBuilder(MultibranchWorkflowJob job, DslFactory dslFactory, def projectData) {
        super(job, dslFactory, projectData)
    }

    void gitHubOrganization() {
        job.branchSources {
            branchSource {
                source {
                    github {
                        repoOwner(projectData.repoOwner)
                        repository(projectData.repoName)
                        apiUri('https://api.github.com')
                        credentialsId(projectData.githubOrganizationsJenkinsCredentialsRefId)
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
