import java.security.MessageDigest
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.MultibranchWorkflowJob

class MultibranchPipelineJobBuilder extends JobBuilder {

    MultibranchPipelineJobBuilder(MultibranchWorkflowJob job, DslFactory dslFactory, def projectData) {
        super(job, dslFactory, projectData)
    }

    protected String getHtmlDescription(List bulletPoints, String cssClass, String prefix) {
        super.getHtmlDescription(bulletPoints,cssClass,prefix)+"<div id=\"jenkins-info\" style=\"display: none;\">multibranch pipeline</div>"
    }

    void gitHubOrganization(Map params=[:]) {
        job.branchSources {
            branchSource {
                source {
                    github {
                        id(md5sum(projectData.githubUrl))
                        repoOwner(projectData.repoOwner)
                        repository(projectData.repoName)
                        repositoryUrl(projectData.githubUrl)
                        configuredByUrl(false)
                        apiUri('https://api.github.com')
                        credentialsId(projectData.githubOrganizationsJenkinsCredentialsRefId)

                        traits {
                            cleanBeforeCheckoutTrait {
                                extension {
                                    deleteUntrackedNestedRepositories(false)
                                }
                        }
                    }
                }
            }
        }

        job.orphanedItemStrategy {
            discardOldItems {
                numToKeep(1)
            }
        }

        job.triggers {
            // Scan at most once per day
            periodicFolderTrigger {
                interval('1d')
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
            //    - 1: Merging the pull request with the current target branch revision
            //    - 2: The current pull request revision (No merge)
            //    - 3: Both
            //    - Trust: From Users with Admin or Write permission
            if (params.get('discoverForkPullRequests', true)) {
                traits << 'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait' {
                    strategyId(1)
                    trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
                }
            }
            // Discover pull requests from origin: Strategy definitions see 'Discover pull requests from forks'
            if (params.get('discoverOriginPullRequests', true)) {
                traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
                    strategyId(1)
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

    void labelForDockerBuild(String label) {
        job.configure {
            it / 'properties' / 'org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig' {
                dockerLabel(label)
            }
        }
    }

    def md5sum(String input) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(input.bytes)
        return new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }
}
