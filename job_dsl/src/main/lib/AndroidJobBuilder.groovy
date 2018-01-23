import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class AndroidJobBuilder extends FreeStyleJobBuilder {

    AndroidJobBuilder(Job job, DslFactory dslFactory, def projectData) {
        super(job, dslFactory, projectData)
    }

    protected void jobDefaults() {
        super.jobDefaults()
        label('NoDevice')

        // Use the globally accessible log-parser-rules.
        def log_parser_rules_file = dslFactory.JENKINS_HOME + '/log_parser_rules.groovy'
        publishers {
            consoleParsing {
                globalRules(log_parser_rules_file)
            }
        }
    }

    void androidEmulator(Map params=[:]) {
        label('Emulator')

        def p = new AndroidEmulatorParameters(projectData.androidEmulatorParameters + params)
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

            xvnc {
                takeScreenshot(false)
                useXauthority(true)
            }

            androidEmulator {
                avdName(null)
                osVersion("android-${p.androidApi}")
                screenDensity(p.screenDensity)
                screenResolution(p.screenResolution)
                deviceDefinition('')
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

        shell('''\
TOOLS_DIR="$WORKSPACE/../../tools"

if [ -d "$ANDROID_HOME" ]; then
    TOOLS_DIR="$ANDROID_HOME/.."
fi

if [ -d "$TOOLS_DIR" ]; then
    LICENSES_DIR="$TOOLS_DIR/android-sdk/licenses"
    mkdir -p "$LICENSES_DIR"
    printf "\\nd56f5187479451eabf01fb78af6dfcb131a6481e" > "${LICENSES_DIR}/android-sdk-license"
    printf "\\n84831b9409646a918e30573bab4c9c91346d8abd" > "${LICENSES_DIR}/android-sdk-preview-license"
    exit 0
else
    echo "There is no tools directory accessible from '$WORKSPACE'."
    exit 1
fi
''')
    }

    void parallelTests(String testJobName, int numBatches) {
        job.steps {
            parallelTestExecutor {
                testJob(testJobName)
                patternFile(projectData.testExclusionsFile)
                parallelism {
                    count {
                        size(numBatches)
                    }
                }
                testReportFiles(projectData.testResultsPattern)
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

    void staticAnalysis() {
        job.publishers {
            projectData.staticAnalysisResults.each{ tool, resultPattern ->
                "$tool"(resultPattern) {
                    canRunOnFailed(true)
                    thresholds(unstableTotal: [all: 0])
                }
            }
        }
    }

    void parameterizedAndroidVersion() {
        job.parameters {
            choiceParam('ANDROID_VERSION', projectData.androidVersions, 'The android version to use by API level \nhttp://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels')
        }
        projectData.androidEmulatorParameters.androidApi = '$ANDROID_VERSION'
    }

    void parameterizedGit() {
        job.parameters {
            stringParam('REPO', projectData.repo, '')
            stringParam('BRANCH', projectData.branch, '')
        }

        git(repo: '$REPO', branch: '$BRANCH')
    }

    void parameterizedTestExclusionsFile() {
        job.parameters {
            fileParam(projectData.testExclusionsFile, 'Optional file listing the tests to exclude (both .java and .class files).\nNeeded for parallel test job execution.')
        }
    }

    void pullRequest(Map params=[:]) {
        Map defaultParams = [triggerPhrase: /.*test\W+this\W+please.*/,
                             onlyTriggerPhrase: false,
                             context: 'Unit Tests and Static Analysis']
        params = defaultParams + params
        job.concurrentBuild(false)

        job.parameters {
            stringParam('sha1', projectData.branch,
                        'Can be used run pull request tests by typing: origin/pr/*pullrequestnumber*/merge')
        }
        git(repo: projectData.repo,
            branch: '${sha1}',
            name: 'origin',
            refspec: '+refs/pull/*:refs/remotes/origin/pr/*')

        buildName('#${BUILD_NUMBER} | ${ENV, var="sha1"}')

        job.triggers {
            githubPullRequest {
                admins(projectData.pullRequestAdmins)
                orgWhitelist(projectData.githubOrganizations)
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
                command("echo put ${projectData.debugApk} | " +
                        'sftp -b- -i /home/catroid/.ssh/jenkins-file-upload file-downloads@files.catrob.at:www')
            }
        }

        archiveArtifacts(projectData.debugApk)
    }
}
