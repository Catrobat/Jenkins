import javaposse.jobdsl.dsl.DslFactory

/**
 * Provides convenience DSL elements with Pocketcode specific behavior.
 * This allows to create complex jobs with just a few function calls, while keeping consistency.
 * At the same time the functionality of <a href="https://jenkinsci.github.io/job-dsl-plugin/">Job DSL jobs</a> is still accessible.
 * <p>
 * Note: Functions are first delegated to JobBuilder to then fall back to the Job DSL.
 * </p>
 *
 * @see <a href="https://www.cloudbees.com/sites/default/files/2016-jenkins-world-rule_jenkins_with_configuration_as_code_2.pdf">Rule Jenkins with Configuration as Code</a>
 */
class JobBuilder extends Delegator {
    protected def job
    protected DslFactory dslFactory
    protected def data

    /**
     * @param job A job created by the <a href="https://jenkinsci.github.io/job-dsl-plugin/">Job DSL</a>.
     * @param dslFactory The outermost scope, which has direct access to the Job DSL
     *                   <a href="https://jenkinsci.github.io/job-dsl-plugin/">top-level methods</a>.
     * @param data Data is an object that contains common information of a project, like its git-repository.
     *             Some methods rely directly on fields of data as default values.
     */
    JobBuilder(def job, DslFactory dslFactory, def data=null) {
        super(job)
        this.job = job
        this.dslFactory = dslFactory
        this.data = data
    }

   /**
    * Create the configuration based on the handed in closure.
    * From within the closure you can call methods of this class and its subclasses
    * as well as methods of the job you handed in the consructor.
    * For example:
    * <pre>
    * <code>jobBuilder.make {
    *     git(repo: 'git://example.com/example.git', branch: 'master') // git is a method from JobBuilder
    *     label('GitInstalled')                                        // method from the job
    *     logRotator {                                                 // method from the job
    *         daysToKepp(42)
    *     }
    * }
    * </code>
    * </pre>
    *
    * @param additionalConfig Provides additional configuration aside from the defaults.
    *                         A job without this additional configuration will not be functional.
    */
    def make(Closure additionalConfig) {
        jobDefaults()
        runClosure(additionalConfig)
        job
    }

    def makeNoDefaults(Closure config) {
        runClosure(config)
        job
    }

    protected void jobDefaults() {
        logRotator(30, 100)
        jdk('(System)')
        job.wrappers {
            timestamps()
            maskPasswords()
        }
    }

    void htmlDescription(List bulletPoints, String cssClass='cat-info', String prefix='<p><b>Info:</b></p>') {
        String bulletPointsStr = bulletPoints.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
        String text = """<div class="$cssClass">
    $prefix
    <ul>
$bulletPointsStr    </ul>\n</div>"""

        job.description('<style>\n    @import "/userContent/job_styles.css";\n</style>\n' + text)
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

    void anonymousUsersPermissions(Permission... permissions_) {
        permissions('Anonymous', *permissions_)
    }

    void jenkinsUsersPermissions(Permission... permissions_) {
        permissions('Jenkins-Users', *permissions_)
    }

    void permissions(String user, Permission... permissions_) {
        authorization {
            permissions_.each { p ->
                permission(p.permission, user)
            }
        }
    }

    void git(Map params=[:]) {
        params = [repo: data?.repo, branch: data?.branch] + params

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
        if (data?.githubUrl)
            return data.githubUrl
        else if (repo.contains('github'))
            return repo - ~/\.git$/
        else
            return ''
    }

    void nightly(String schedule='H 0 * * *') {
        job.concurrentBuild(false)
        job.triggers {
            cron(schedule)
        }
    }

    void continuous(String schedule='H/5 * * * *') {
        job.triggers {
            scm(schedule)
        }
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
     */
    void notifications(boolean informStandalone = false) {
        publishers {
            slackNotifier {
                startNotification(false)
                notifyAborted(false)
                notifyFailure(true)
                notifyNotBuilt(true)
                notifySuccess(false)
                notifyUnstable(true)
                notifyRegression(false)
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)

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
                archiveJunit(data.testResultsPattern) {
            }
        }
    }
}
