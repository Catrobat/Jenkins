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


    // Since JobDSL version 1.70 the authenticationToken value is WRONGLY marked as deprecated.
    // https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration#migrating-to-170
    // Furthermore it is not only marked as deprecated, the current implementation just ignores
    // the value.
    // It will be usable in the next release again, please see the corresponding discussion:
    // https://issues.jenkins-ci.org/browse/JENKINS-31832
    // Until then, we use the configure block to provide the functionality.
    // If JobDSL version 1.71 is out (and contains the fix) this block can simply be removed and
    // the 'upstream-implemenation' will be used as they have the same signature.
    void authenticationToken(def token) {
        configure { Node project ->
            project / authToken(token)
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
