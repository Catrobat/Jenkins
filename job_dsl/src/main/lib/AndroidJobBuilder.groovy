import javaposse.jobdsl.dsl.Job

class AndroidJobBuilder extends JobBuilder {

    AndroidJobBuilder(Job job, def data) {
        super(job, data)
    }

    protected void jobDefaults() {
        super.jobDefaults()
        label('NoDevice')
    }

    void git(Map params=[:]) {
        params = [repo: data.repo, branch: data.branch] + params
        super.git(params)
    }

    protected String retrieveGithubUrl(String repo) {
        if (data)
            return data.githubUrl
        else
            return super.retrieveGithubUrl(repo)
    }

    void androidEmulator(Map params=[:]) {
        label('Emulator')

        def p = new AndroidEmulatorParameters(data.androidEmulatorParameters + params)
        job.wrappers {
            buildTimeoutWrapper {
                strategy {
                    noActivityTimeOutStrategy {
                        timeoutSecondsString(p.noActivityTimeout)
                    }
                }
                operationList {
                    abortOperation()
                    failOperation()
                }
                timeoutEnvVar('')
            }

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
                showWindow(false)
                useSnapshots(false)

                deleteAfterBuild(true)
                startupDelay(0)
                startupTimeout(120)
                commandLineOptions(p.commandLineOptions)
                executable('')
            }
        }

        createAndroidSdkLicenses()

        job.publishers {
            textFinder(/\[android\] Emulator did not appear to start; giving up/, '', true, false, false)
        }
    }

    void createAndroidSdkLicenses() {
        // When gradle tries to download android-sdk related files it needs licenses.
        // Unfortunately the already existing licenses might be overwritten.
        // Thus this function (re)creates the needed licenses.

        shell('''
TOOLS_DIR="$WORKSPACE/../../tools"

if [ -d "$ANDROID_HOME" ]; then
    TOOLS_DIR="$ANDROID_HOME/.."
fi

if [ -d "$TOOLS_DIR" ]; then
    LICENSES_DIR="$TOOLS_DIR/android-sdk/licenses"
    mkdir -p "$LICENSES_DIR"
    echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55" > "$LICENSES_DIR/android-sdk-license"
    echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$LICENSES_DIR/android-sdk-preview-license"
    exit 0
else
    echo "There is no tools directory accessible from '$WORKSPACE'."
    exit 1
fi
''')
    }

    void excludeTests() {
        excludeTestClasses(data.excludedTests)
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

    void staticAnalysis() {
        job.publishers {
            data.staticAnalysisResults.each{ tool, resultPattern ->
                "$tool"(resultPattern) {
                    canRunOnFailed(true)
                    thresholds(unstableTotal: [all: 0])
                }
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

        git(repo: '$REPO', branch: '$BRANCH')
    }

    void parameterizedTestExclusionsFile() {
        job.parameters {
            fileParam(data.testExclusionsFile, 'Optional file listing the tests to exclude (both .java and .class files).\nNeeded for parallel test job execution.')
        }
    }

    void pullRequest(Map params=[triggerPhrase: /.*test\W+this\W+please.*/,
                                 onlyTriggerPhrase: false,
                                 context: 'Unit Tests and Static Analysis']) {
        job.concurrentBuild(false)

        job.parameters {
            stringParam('sha1', data.branch,
                        'Can be used run pull request tests by typing: origin/pr/*pullrequestnumber*/merge')
        }
        git(repo: data.repo, branch: '${sha1}')
        buildName('#${BUILD_NUMBER} | ${ENV, var="sha1"}')

        job.triggers {
            githubPullRequest {
                admins(data.pullRequestAdmins)
                orgWhitelist(data.githubOrganizations)
                cron('H/2 * * * *')
                triggerPhrase(params.triggerPhrase)
                if (params.onlyTriggerPhrase) {
                    onlyTriggerPhrase()
                }
                extensions {
                    commitStatus {

                        // The context allows to have multiple PR jobs for a single PR.
                        // To not overwrite each other the job results are then differentiated by the context.
                        context(params.context)
                    }
                }
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

        archiveArtifacts(data.debugApk)
    }
}
