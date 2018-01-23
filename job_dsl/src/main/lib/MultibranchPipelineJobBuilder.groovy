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

        job.configure {
            // workaround for JENKINS-46202 (https://issues.jenkins-ci.org/browse/JENKINS-46202)
            def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
            // Discover Branches: All Branches
            traits << 'org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait' {
                strategyId(3)
            }
            // Discover pull requests from forks
            //    - Merge PR with current Target
            //    - Trust: From Users with Admin or Write permission
            traits << 'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait' {
                strategyId(1)
                trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
            }
            // Discover pull requests from origin: Merge PR with current Target
            traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
                strategyId(1)
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
