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
    protected def outerScope
    protected def data
    protected boolean cleanWorkspace = true

    /**
     * @param job A job created by the <a href="https://jenkinsci.github.io/job-dsl-plugin/">Job DSL</a>.
     * @param outerScope The outermost scope, which has direct access to the Job DSL
     *                   <a href="https://jenkinsci.github.io/job-dsl-plugin/">top-level methods</a>.
     * @param data Data is an object that contains common information of a project, like its git-repository.
     *             Some methods rely directly on fields of data as default values.
     */
    JobBuilder(def job, def outerScope, def data=null) {
        super(job)
        this.job = job
        this.outerScope = outerScope
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

        job.wrappers {
            if (this.cleanWorkspace) {
                preBuildCleanup()
            }
        }

        job
    }

    def makeNoDefaults(Closure config) {
        runClosure(config)
        job
    }

    protected void jobDefaults() {
        logRotator(30, 100)
        jdk('java-8-openjdk-amd64')
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

    void keepWorkspace() {
        this.cleanWorkspace = false
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
        params = [repo: data?.repo, branch: data?.branch, reference: data?.referenceRepo] + params

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

                if (params.reference) {
                    extensions {
                        cloneOptions {
                            reference(params.reference)
                        }
                    }
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
            gradle {
                switches(switches_)
                tasks(tasks_)
                passAsProperties(false)
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

    void junit() {
        job.publishers {
                archiveJunit(data.testResultsPattern) {
            }
        }
    }

}
