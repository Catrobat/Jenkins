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
    protected def projectData

    /**
     * @param job A job created by the <a href="https://jenkinsci.github.io/job-dsl-plugin/">Job DSL</a>.
     * @param dslFactory The outermost scope, which has direct access to the Job DSL
     *                   <a href="https://jenkinsci.github.io/job-dsl-plugin/">top-level methods</a>.
     * @param projectData ProjectData is an object that contains common information of a project, like its git-repository.
     *             Some methods rely directly on fields of projectData as default values.
     */
    JobBuilder(def job, DslFactory dslFactory, def projectData=null) {
        super(job)
        this.job = job
        this.dslFactory = dslFactory
        this.projectData = projectData
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
    }

    void htmlDescription(List bulletPoints, String cssClass='cat-info', String prefix='<p><b>Info:</b></p>') {
        String bulletPointsStr = bulletPoints.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
        String text = """<div class="$cssClass">
    $prefix
    <ul>
$bulletPointsStr    </ul>\n</div>"""

        job.description('<style>\n    @import "/userContent/job_styles.css";\n</style>\n' + text)
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

    void nightly(String schedule='H 0 * * *') {
        job.triggers {
            cron(schedule)
        }
    }

    void continuous(String schedule='H/5 * * * *') {
        job.triggers {
            scm(schedule)
        }
    }
}
