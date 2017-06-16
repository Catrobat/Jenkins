import groovy.transform.Canonical
import hudson.tasks.Builder
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

enum Permission {
    JobRead('hudson.model.Item.Read'),
    JobBuild('hudson.model.Item.Build'),
    JobCancel('hudson.model.Item.Cancel')

    String permission

    Permission(String permission) {
        this.permission = permission
    }
}

def createListView(String name_, String pattern) {
    listView(name_) {
        jobs {
            regex(pattern)
        }

        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
            builtOnColumn()
        }
    }

}

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

    void git(String repo_, String branch_) {
        def githubUrl = retrieveGithubUrl(repo_)
        if (githubUrl) {
            job.properties {
                githubProjectUrl(githubUrl)
            }
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

    void shell(String... commands) {
        job.steps {
            shell(commands.join('\n'))
        }
    }
}

class AndroidJobBuilder extends JobBuilder {

    AndroidJobBuilder(Job job, def data) {
        super(job, data)
    }

    protected void jobDefaults() {
        super.jobDefaults()
        label('NoDevice')
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

    void git() {
        git(data.repo, data.branch)
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
                showWindow(true)
                useSnapshots(false)

                deleteAfterBuild(false)
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

@Canonical
class AndroidEmulatorParameters {
    String androidApi = '${ANDROID_VERSION}'
    String screenDensity, screenResolution, targetAbi, noActivityTimeout
    String deviceLocale = 'en_US'
    String sdCardSize = '100M'
    Map hardwareProperties = [:]
    String commandLineOptions = ''
}

folder("Catroid")
folder("Paintroid")
folder("Jenkins")
folder("Experimental")

class CalculatorJobBuilder extends AndroidJobBuilder {
    CalculatorJobBuilder(Job job) {
        super(job, new Calculator())
    }
}

class Calculator {
    def repo = 'https://github.com/vinzynth/catroidCalculator.git'
    def branch = 'master'
    def githubUrl = 'https://github.com/vinzynth/catroidCalculator'
    def androidVersions = 18..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = ''
    def githubOrganizations = []
    def pullRequestAdmins = []
    def androidEmulatorParameters = [screenDensity: '160', screenResolution: '480x800', targetAbi: 'x86_64',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.keyboard': 'yes', 'hw.ramSize': '800', 'vm.heapSize': '128'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    //def debugApk = 'Calculator/build/outputs/apk/Calculator-debug.apk'
}


new CalculatorJobBuilder(job('Experimental/Calculator-Nightly-DSL-Generated')).make {
    htmlDescription(['Nightly Calculator Job. Created by DSL-Seed Job for testing purposes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator(androidApi: 22)

    //Build Actions
    gradle('clean')
    gradle('test')
    gradle('assembleDebug')

    //Post-Build Actions
    //uploadApkToFilesCatrobat()
    //junit()
}

new CalculatorJobBuilder(job('Experimental/Calculator-Nightly-DSL-Generated-With-Exclusions')).make {
    htmlDescription(['Nightly Calculator Job. Created by DSL-Seed Job for testing purposes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator(androidApi: 22)

    excludeTestClass('catrobat.calculator.test.CalculationsTest')

    //Build Actions
    gradle('clean')
    gradle('test')
    gradle('assembleDebug')

    //Post-Build Actions
    //uploadApkToFilesCatrobat()
    //junit()
}
class PaintroidJobBuilder extends AndroidJobBuilder {
    PaintroidJobBuilder(Job job) {
        super(job, new Paintroid())
    }
}

class Paintroid {
    def repo = 'https://github.com/Catrobat/Paintroid.git'
    def branch = 'develop'
    def githubUrl = 'https://github.com/Catrobat/Paintroid'
    def androidVersions = 18..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['Catrobat']
    def pullRequestAdmins = ['thmq', '84n4n4']
    def androidEmulatorParameters = [screenDensity: '240', screenResolution: '480x800', targetAbi: 'x86',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.keyboard': 'yes', 'hw.ramSize': '800', 'vm.heapSize': '128'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    def debugApk = 'Paintroid/build/outputs/apk/Paintroid-debug.apk'
    def excludedTests = ['ActivityOpenedFromPocketCodeNewImageTest', 'ActivityOpenedFromPocketCodeTest',
                         'BitmapIntegrationTest', 'BrushPickerIntegrationTest', 'ButtonTopLayers_RTL_LayoutTest',
                         'ColorDialogIntegrationTest', 'EraserToolIntegrationTest', 'FillToolIntegrationTest',
                         'FlipToolIntegrationTest', 'LandscapeTest', 'LayerIntegrationTest',
                         'MainActivityIntegrationTest', 'MenuFileActivityIntegrationTest', 'MultilingualTest',
                         'RectangleFillToolIntegrationTest', 'RotationToolIntegrationTest',
                         'ScrollingViewIntegrationTest', 'StampToolIntegrationTest', 'TextToolIntegrationTest',
                         'ToolOnBackPressedTests', 'ToolSelectionIntegrationTest', 'TransformToolIntegrationTest',
                         'UndoRedoIntegrationTest'
                        ].collect{"**/$it*"} +
                        ['**/junit/**']
}

new PaintroidJobBuilder(job('Paintroid/PartialTests')).make {
    htmlDescription(['Do <b>not</b> start this job manually.',
                     'A job to execute emulator tests defined by external jobs via exclude files.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parameterizedTestExclusionsFile()
    androidEmulator()
    gradle('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest', '-Pjenkins')
}

new PaintroidJobBuilder(job('Paintroid/ParallelTests-CustomBranch')).make {
    htmlDescription(['This job builds and runs UI tests of the given REPO/BRANCH.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parallelTests('Paintroid/PartialTests', 2)
}

new PaintroidJobBuilder(job('Paintroid/PullRequest')).make {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    pullRequest()
    androidEmulator(androidApi: 18)
    gradle('connectedDebugAndroidTest', '-Pjenkins')
    junit()
}

new PaintroidJobBuilder(job('Paintroid/Nightly')).make {
    htmlDescription(['Nightly Paintroid job.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    excludeTests()
    androidEmulator(androidApi: 18)
    gradle('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest',
           '-Pjenkins -Pindependent="Paint Nightly #${BUILD_NUMBER}"')
    uploadApkToFilesCatrobat()
    junit()
}

new PaintroidJobBuilder(job('Experimental/Paintroid-Find-Broken-Tests')).make {
    htmlDescription(['Job to find broken Paintroid tests, to not tamper with the nightly.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    excludeTests()
    androidEmulator(androidApi: 18)
    gradle('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest', '-Pjenkins')
    junit()
}

new JobBuilder(job('Jenkins/SeedJob')).make {
    htmlDescription(['Seed job to create all other jobs.'])

    jenkinsUsersPermissions()
    git('https://github.com/Catrobat/Jenkins.git', 'master')
    nightly('H 23 * * *') // run the job before all other nightlies
    steps {
        jobDsl {
            targets('job_dsl/src/main/resources/jobs/*.groovy')
            failOnMissingPlugin(true)
            removedJobAction('DISABLE')
            unstableOnDeprecation(true)
        }
    }
}

new JobBuilder(job('Jenkins/LocalBackup')).make {
    htmlDescription(['Creates a local backup of jenkins in /home/jenkins/jenkins_created_backups.',
                     'Useful to run manually before installing updates of plugins.',
                     'Does not replace other forms of updates!'])

    jenkinsUsersPermissions()
    label('master')
    git('https://github.com/Catrobat/Jenkins.git', 'master')
    shell('bash -ex ./scripts/backupJenkinsLocally.sh')
}

class CatroidJobBuilder extends AndroidJobBuilder {
    CatroidJobBuilder(Job job) {
        super(job, new Catroid())
    }
}

class Catroid {
    def repo = 'https://github.com/vinzynth/Catroid.git' // TODO change once https://github.com/Catrobat/Catroid/pull/2349 was merged
    def branch = 'develop'
    def githubUrl = 'https://github.com/vinzynth/Catroid'
    def androidVersions = 19..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['Catrobat']
    def pullRequestAdmins = ['84n4n4', 'bernadettespieler', 'Bioxar', 'cheidenreich', 'ElliHeschl',
                             'joseffilzmaier', 'MightyMendor', 'oliewa92', 'robertpainsi', 'thmq',
                             'thomasmauerhofer']
    def androidEmulatorParameters = [screenDensity: 'xhdpi', screenResolution: '768x1280', targetAbi: 'x86_64',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.camera': 'yes', 'hw.ramSize': '800', 'hw.camera.front': 'emulated',
                                                          'hw.camera.back': 'emulated', 'hw.gps': 'yes'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    def debugApk = 'catroid/build/outputs/apk/catroid-catroid-debug.apk'
    def excludedTests = []
}

new CatroidJobBuilder(job('Catroid/PartialTests')).make {
    htmlDescription(['Do <b>not</b> start this job manually.',
                     'A job to execute emulator tests defined by external jobs via exclude files.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parameterizedTestExclusionsFile()
    androidEmulator()
    gradle('connectedCatroidDebugAndroidTest')
}

new CatroidJobBuilder(job('Catroid/ParallelTests-CustomBranch')).make {
    htmlDescription(['This job builds and runs UI tests of the given REPO/BRANCH.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parallelTests('Catroid/PartialTests', 2)
}

new CatroidJobBuilder(job('Catroid/PullRequest')).make {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    pullRequest()
    androidEmulator(androidApi: 22)
    gradle('clean check test connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.test')
    junit()
}

new CatroidJobBuilder(job('Catroid/Nightly')).make {
    htmlDescription(['Nightly Catroid job.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    excludeTests()
    androidEmulator(androidApi: 22)
    gradle('assembleDebug connectedCatroidDebugAndroidTest',
           '-Pindependent="Code Nightly #${BUILD_NUMBER}"')
    uploadApkToFilesCatrobat()
    junit()
}
