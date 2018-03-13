import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class FreeStyleJobBuilder extends JobBuilder {
    FreeStyleJobBuilder(Job job, DslFactory dslFactory, def projectData) {
        super(job, dslFactory, projectData)
    }

    protected void jobDefaults() {
        logRotator(30, 100)
        jdk('(System)')
        job.wrappers {
            timestamps()
            maskPasswords()
        }
    }

    void buildName(String template_) {
        job.wrappers {
            buildNameSetter {
                template(template_)
                runAtStart(true)
                runAtEnd(true)
            }
        }
    }

    void git(Map params=[:]) {
        params = [repo: projectData?.repo, branch: projectData?.branch] + params

        def githubUrl = retrieveGithubUrl(params.repo)
        if (githubUrl) {
            job.properties {
                githubProjectUrl(githubUrl)
            }
        }

        job.scm {
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
    }

    protected String retrieveGithubUrl(String repo) {
        if (projectData?.githubUrl)
            return projectData.githubUrl
        else if (repo.contains('github'))
            return repo - ~/\.git$/
        else
            return ''
    }

    void gradle(String tasks_, String switches_='') {
        job.steps {
            configure {
                it / builders << 'hudson.plugins.gradle.Gradle' {
                    switches(switches_)
                    tasks(tasks_)
                    rootBuildScriptDir('')
                    buildFile('')
                    gradleName('(Default)')
                    useWrapper(true)
                    makeExecutable(false)
                    useWorkspaceAsHome(false)
                    passAllAsSystemProperties(false)
                    passAllAsProjectProperties(false)
                }
            }
        }
    }

    void shell(String... commands) {
        job.steps {
            shell(commands.join('\n'))
        }
    }

    void archiveArtifacts(String pattern_) {
        publishers {
            archiveArtifacts {
                pattern(pattern_)
            }
        }
    }

    /**
     * notify about build results
     *  - Slack: Only first failures and back to normal messages
     *           except standalone: All failures, no unstable (see: #WEB-460)
     */
    void notifications(boolean informStandalone = false) {
        publishers {
            slackNotifier {
                startNotification(false)
                notifyAborted(false)
                notifyFailure(true)
                notifyNotBuilt(true)
                notifyRegression(false)
                notifySuccess(false)

                if (informStandalone) {
                    notifyUnstable(false)
                    notifyBackToNormal(false)
                    notifyRepeatedFailure(true)
                } else {
                    notifyUnstable(true)
                    notifyBackToNormal(true)
                    notifyRepeatedFailure(false)
                }

                includeTestSummary(false)
                includeCustomMessage(false)
                customMessage('')

                commitInfoChoice('NONE')               // 'nothing about commits'
                //commitInfoChoice('AUTHORS')            // 'commit list with authors only'
                //commitInfoChoice('AUTHORS_AND_TITLES') // 'commit list with authors and titles'

                // channel config
                String channels = "#ci-status"
                if (informStandalone) {
                    channels += ",#ci-status-standalone"
                }
                room(channels)

                // use from main config
                teamDomain('')
                authTokenCredentialId('')
                sendAs('')
            }
        }
    }

    void junit() {
        job.publishers {
                archiveJunit(projectData.testResultsPattern) {
            }
        }
    }
}
