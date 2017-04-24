class AndroidJobBuilder extends JobBuilder {
    protected def data

    AndroidJobBuilder(Job job, def data) {
        super(job)
        this.data = data
    }

    Job make(Closure additionalConfig) {
        logRotator(30, 100)
        label('NoDevice')
        job.concurrentBuild()
        job.wrappers {
            preBuildCleanup()
            timestamps()
        }

        super.make(additionalConfig)
    }

    void htmlDescription(String description) {
        job.description('<style>\n    @import "/userContent/job_styles.css";\n</style>\n' + description)
    }

    void htmlDescription(List bulletPoints=[], String prefix='<p><b>Info:</b></p>', String cssClass='cat-info') {
        bulletPoints += 'Remove this job when it was not run in the last 2 months.'
        String bulletPointsStr = bulletPoints.sum { ' ' * 8 + '<li>' + it + '</li>\n' }
        String text = """<div class="$cssClass">
    $prefix
    <ul>
$bulletPointsStr    </ul>
</div>"""
        htmlDescription(text);
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

    void jenkinsUsersPermissions(Permission... permissions) {
        authorization {
            permissions.each { p ->
                permission(p.permission, 'Jenkins-Users')
            }
        }
    }

    void git() {
        git(data.repo, data.branch)
    }

    void git(String repo_, String branch_) {
        job.properties {
            githubProjectUrl(data.githubUrl)
        }
        job.scm {
            git {
                remote {
                    url(repo_)
                    branch(branch_)
                }
            }
        }
    }

    void androidEmulator(Map params=[:]) {
        label('Emulator')

        def p = new AndroidEmulatorParameters(data.androidEmulatorParameters + params)
        job.wrappers {
            androidEmulator {
                avdName(null)
                osVersion("android-${p.androidApi}")
                screenDensity(p.screenDensity)
                screenResolution(p.screenResolution)
                deviceLocale(p.deviceLocale)
                sdCardSize(p.sdCardSize)
                targetAbi(p.targetAbi)
                avdNameSuffix('')
                hardwareProperties {
                    p.hardwareProperties.each { k, v ->
                        hardwareProperty {
                            key(k)
                            value(v)
                        }
                    }
                }

                wipeData(true)
                showWindow(true)
                useSnapshots(false)

                deleteAfterBuild(false)
                startupDelay(0)
                startupTimeout(120)
                commandLineOptions(p.commandLineOptions)
                executable('')
            }
        }
    }

    void gradle(String tasks_, String switches_='') {
        job.steps {
            gradle {
                switches(switches_)
                tasks(tasks_)
            }
        }
    }

    void parallelTests(String testJobName, int numBatches) {
        job.steps {
            parallelTestExecutor {
                testJob(testJobName)
                patternFile(data.testExclusionsFile)
                parallelism {
                    count {
                        size(numBatches)
                    }
                }
                testReportFiles(data.testResultsPattern)
                archiveTestResults(true)
                parameters {
                    currentBuildParameters()
                    gitRevisionBuildParameters {
                        combineQueuedCommits(false)
                    }
                }
            }
        }

        junit()
    }

    void junit() {
        job.publishers {
            archiveJunit(data.testResultsPattern) {
            }
        }
    }

    void parameterizedAndroidVersion() {
        job.parameters {
            choiceParam('ANDROID_VERSION', data.androidVersions, 'The android version to use by API level \nhttp://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels')
        }
    }

    void parameterizedGit() {
        job.parameters {
            stringParam('REPO', data.repo, '')
            stringParam('BRANCH', data.branch, '')
        }

        git('$REPO', '$BRANCH')
    }

    void parameterizedTestExclusionsFile() {
        job.parameters {
            fileParam(data.testExclusionsFile, 'Optional file listing the tests to exclude (both .java and .class files).\nNeeded for parallel test job execution.')
        }
    }

    void nightly() {
        job.concurrentBuild(false)
        job.triggers {
            cron('H 0 * * *')
        }
    }

    void pullRequest() {
        job.concurrentBuild(false)

        job.parameters {
            stringParam('sha1', data.branch, '')
        }
        git(data.repo, '${sha1}')
        buildName('#${BUILD_NUMBER} | ${ENV, var="sha1"}')

        job.triggers {
            githubPullRequest {
                admins(data.pullRequestAdmins)
                orgWhitelist(data.githubOrganizations)
                cron('H/2 * * * *')
                triggerPhrase('.*test\\W+this\\W+please.*')
            }
        }
    }

    void uploadApkToFilesCatrobat() {
        job.steps {
            shell {
                command("echo put ${data.debugApk} | " +
                        'sftp -b- -i /home/catroid/.ssh/jenkins-file-upload file-downloads@files.catrob.at:www')
            }
        }

        publishers {
            archiveArtifacts {
                pattern(data.debugApk)
            }
        }
    }
}
