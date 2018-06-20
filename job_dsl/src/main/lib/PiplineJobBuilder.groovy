import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.WorkflowJob

// EITHER use git/pullrequest to define the Jenkinsfile inside the
// repository (add parameter 'jenkinsfile' in  the params map, to
// use a non-default Jenkinsfile-location
// OR import the jenkinsfile from the current workspace (like typing
// the Script inside the job config) via 'importJenkinsfileFromWS'
class PiplineJobBuilder extends JobBuilder {

    PiplineJobBuilder(WorkflowJob job, DslFactory dslFactory, def projectData = null) {
        super(job, dslFactory, projectData)
    }

    protected void jobDefaults() {
        logRotator(30, 100)
    }

    void git(Map params=[:]) {
        params = [repo: projectData?.repo, branch: projectData?.branch] + params

        def githubUrl = retrieveGithubUrl(params.repo)
        if (githubUrl) {
            job.properties {
                githubProjectUrl(githubUrl)
            }
        }

        String jenkinsfilePath = 'Jenkinsfile'
        if (params?.jenkinsfile)
            jenkinsfilePath = params.jenkinsfile

        job.definition {
            cpsScm {
                lightweight(false)

                scm {
                    git {
                        remote {
                            url(params.repo)
                            branch(params.branch)
                            refspec(params.refspec)
                            name(params.name)
                        }

                        extensions {
                            cleanBeforeCheckout()
                        }
                    }
                }

                scriptPath(jenkinsfilePath)
            }
        }
    }

    protected String retrieveGithubUrl(String repo) {
        if (projectData?.githubUrl)
            return projectData.githubUrl
        else if (repo.contains('github'))
            return repo - ~/\.git$/
        else
            return ''
    }

    void parameterizedGit(Map params=[:]) {
        job.parameters {
            stringParam {
                name('REPO')
                defaultValue(projectData.repo)
                description('')
                trim(true)
            }
            stringParam {
                name('BRANCH')
                defaultValue(projectData.branch)
                description('')
                trim(true)
            }
        }

        params.repo = '$REPO'
        params.branch = '$BRANCH'

        git(params)
    }

    // Automatically sets up git to the values provided in the projectData field
    void pullRequest(Map params=[:]) {
        Map defaultParams = [triggerPhrase: /.*test\W+this\W+please.*/,
                             onlyTriggerPhrase: false,
                             context: 'Unit Tests and Static Analysis']
        params = defaultParams + params

        job.parameters {
            stringParam {
                name('sha1')
                defaultValue(projectData.branch)
                description('Can be used run pull request tests by typing: origin/pr/*pullrequestnumber*/merge')
                trim(true)
            }
        }

        // set defaults projectData/pull request info
        params.repo = projectData.repo
        params.branch = '${sha1}'
        params.name = 'origin'
        params.refspec = '+refs/pull/*:refs/remotes/origin/pr/* +refs/heads/*:refs/remotes/origin/*'

        git(params)

        job.triggers {
            githubPullRequest {
                admins(projectData.pullRequestAdmins)
                orgWhitelist(projectData.githubOrganizations)
                cron('H/2 * * * *')
                triggerPhrase(params.triggerPhrase)
                if (params.onlyTriggerPhrase) {
                    onlyTriggerPhrase()
                }
                useGitHubHooks(true)
                extensions {
                    commitStatus {

                        // The context allows to have multiple PR jobs for a single PR.
                        // To not overwrite each other the job results are then differentiated by the context.
                        context(params.context)
                    }
                }
            }
            gitHubPushTrigger()
        }
    }

    void importJenkinsfileFromWS(String jenkinsfilePath) {
        job.definition {
            cps {
                script(dslFactory.readFileFromWorkspace(jenkinsfilePath))
                sandbox()
            }
        }
    }
}
