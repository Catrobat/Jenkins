import javaposse.jobdsl.dsl.Job

/**
 * @see <a href="https://www.cloudbees.com/sites/default/files/2016-jenkins-world-rule_jenkins_with_configuration_as_code_2.pdf">Rule Jenkins with Configuration as Code</a>
 */
class JobBuilder extends Delegator {
    protected def data
    protected def job
    private String description

    protected Set excludedTests = []

    JobBuilder(Job job, def data=null) {
        super(job)
        this.data = data
        this.job = job
    }

    Job make(Closure additionalConfig) {
        jobDefaults()
        runClosure(additionalConfig)

        if (data?.testExclusionsFile && excludedTests) {
            job.steps {
                shell {
                    command('# Run is marked unstable since there are exluded tests.\nexit 1')
                    unstableReturn(1)
                }
            }
        }

        job
    }

    protected void jobDefaults() {
        logRotator(30, 100)
        jdk('java-8-openjdk-amd64')
        job.wrappers {
            preBuildCleanup()
            timestamps()
            maskPasswords()
        }
    }

    void htmlDescription() {
        job.description('<style>\n    @import "/userContent/job_styles.css";\n</style>\n' + description + getExcludedTestClasses())
    }

    void htmlDescription(List bulletPoints, String cssClass='cat-info', String prefix='<p><b>Info:</b></p>') {
        String bulletPointsStr = bulletPoints.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
        String text = """<div class="$cssClass">
    $prefix
    <ul>
$bulletPointsStr    </ul>\n</div>"""

        this.description = text

        htmlDescription();
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

    void excludeTestClasses(List testclasses) {
        if (data?.testExclusionsFile) {
            excludedTests += testclasses
            htmlDescription()
            job.steps {
                shell {
                    command(testclasses.collect{"echo \"$it\" >> $data.testExclusionsFile"}.join('\n'))
                }
            }
        }
    }

    void excludeTestClass(String testclass) {
        excludeTestClasses([testclass])
    }

    private String getExcludedTestClasses(String cssClass='cat-note'){
        if (excludedTests) {
            String text = """<div class="$cssClass">"""
            text += "<p><b>Excluded Testcases:\n</b></p><ul>"
            text += excludedTests.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
            text += "</ul></div>";

            return text
        }

        return ''
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
                }
            }
        }
    }

    protected String retrieveGithubUrl(String repo) {
        if (repo.contains('github'))
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
}
