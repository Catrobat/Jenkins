import javaposse.jobdsl.dsl.Job

/**
 * @see <a href="https://www.cloudbees.com/sites/default/files/2016-jenkins-world-rule_jenkins_with_configuration_as_code_2.pdf">Rule Jenkins with Configuration as Code</a>
 */
class JobBuilder {
    protected def data
    protected def job
    private String description

    protected Set excludedTests = []

    JobBuilder(Job job, def data=null) {
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
        job.wrappers {
            preBuildCleanup()
            timestamps()
            maskPasswords()
        }
    }

    private void runClosure(Closure closure) {
        // Create clone of closure for threading access.
        closure = closure.clone()

        // Set delegate of closure to this builder.
        closure.delegate = this

        // And only use this builder as the closure delegate.
        closure.resolveStrategy = Closure.DELEGATE_ONLY

        // Run closure code.
        closure()
    }

    // Delegate everything else to Job DSL
    def methodMissing(String name, argument) {
        job.invokeMethod(name, (Object[]) argument)
    }

    void htmlDescription() {
        job.description('<style>\n    @import "/userContent/job_styles.css";\n</style>\n' + description + getExcludedTestClasses())
    }

    void htmlDescription(List bulletPoints, String cssClass='cat-info', String prefix='<p><b>Info:</b></p>') {
        bulletPoints += 'Remove this job when it was not run in the last 2 months.'
        String bulletPointsStr = bulletPoints.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
        String text = """<div class="$cssClass">
    $prefix
    <ul>
$bulletPointsStr    </ul></div>"""

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

    void jenkinsUsersPermissions(Permission... permissions) {
        authorization {
            permissions.each { p ->
                permission(p.permission, 'Jenkins-Users')
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
