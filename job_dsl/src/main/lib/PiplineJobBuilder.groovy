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

    void importJenkinsfileFromWS(String jenkinsfilePath) {
        job.definition {
            cps {
                script(dslFactory.readFileFromWorkspace(jenkinsfilePath))
                sandbox()
            }
        }
    }

    void defaultReleasePatameter() {

        parameters {
            gitParam('gitBranch') {
                description('Select the branch you want to build e.g. origin/master.')
                type('BRANCH')
                defaultValue('origin/master')
            }
            nonStoredPasswordParam('signingKeystorePassword', '')
            nonStoredPasswordParam('signingKeyAlias', '')
            nonStoredPasswordParam('signingKeyPassword', 'For current keystore, this is the same as signingKeystorePassword.')
            nonStoredPasswordParam('googlePlayJsonKey', 'Json file used to connect to the API of the Google Developer Console.\n' +
                        'Copy the whole content of the file into this password parameter.')
        }

    }
}
