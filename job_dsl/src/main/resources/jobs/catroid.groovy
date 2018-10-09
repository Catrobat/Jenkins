def catroidorg = new JobsBuilder(this).gitHubOrganization({new CatroidData()})
def catroidroot = new JobsBuilder(this).pipeline({new CatroidData()})

catroidorg.job("Catroid") {
    htmlDescription(['Job is automatically started on a new commit or a new/updated pull request created on github.',
                     'This job runs the Pipeline defined in the Jenkinsfile inside of the repository.',
                     'The Pipeline should run the code analyisis, the unit and device tests.'])

    // Workspace rights are needed to show the java files for code coverage.
    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel, Permission.JobWorkspace)

    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    gitHubOrganization()
    jenkinsfilePath('Jenkinsfile')
    labelForDockerBuild('Emulator')
}

catroidorg.job("Catroid-SensorBoxTests") {
    htmlDescription(['Job is automatically started on a new commit or a new/updated pull request created on github.',
                     'This job runs the the hardware tests on the sensorbox.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)

    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of the hardware tests

    gitHubOrganization(discoverForkPullRequests: false, discoverOriginPullRequests: false)
    jenkinsfilePath('Jenkinsfile.SensorboxTests')
    labelForDockerBuild('HardwareTest')
}

Views.basic(this, "Catroid", "Catroid/.+")

catroidroot.job("Catroid-ManualEmulatorTest") {
    htmlDescription(['This job runs the tests of the given REPO/BRANCH and CLASS/PACKAGE.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    // allow to build PRs as well with 'origin/pr/<num>/merge'
    parameterizedGit(jenkinsfile: 'Jenkinsfile.ManualTests', refspec: '+refs/pull/*:refs/remotes/origin/pr/* +refs/heads/*:refs/remotes/origin/*')
    job.parameters {
        stringParam {
            name('CLASS_PKG_TO_TEST')
            defaultValue('org.catrobat.catroid.uiespresso.testsuites.PullRequestTriggerSuite')
            description('Configure a single test-class (without java/class-suffix) or a package to test')
            trim(true)
        }
        textParam {
            name('EMUALTOR_CONFIG')
            defaultValue("""# AVD creation
system_image=system-images;android-24;default;x86_64
## properties written to the avd config, prefix here with prop, so the script knows where to use them
prop.hw.camera=yes
prop.hw.ramSize=2048
prop.hw.gpu.enabled=yes
prop.hw.camera.front=emulated
prop.hw.camera.back=emulated
prop.hw.gps=yes
prop.hw.mainKeys=no
prop.hw.keyboard=yes
prop.disk.dataPartition.size=512M
## dpi
screen.density=xxhdpi
## sdcard
sdcard.size=200M
## AVD startup
screen.resolution=1080x1920
device.language=en_US""")
            description('Keep in sync with buildScripts/emulator_config.ini')
        }
    }
}

catroidroot.job("Build-Standalone") {
    htmlDescription(['Builds a Catroid APP as a standalone APK.'])

    // !! DO NOT give Anonymous-Users read permission, otherwise the upload-token would be spoiled
    jenkinsUsersPermissions(Permission.JobRead)

    parameters {
        stringParam('DOWNLOAD', 'https://share.catrob.at/pocketcode/download/821.catrobat', 'Enter the Project ID you want to build as standalone')
        stringParam('SUFFIX', 'standalone', '')
        password {
            name('UPLOAD')
            defaultValue('')
            description('upload url for webserver\n\nSyntax of the upload value is of the form\n' +
                        'https://pocketcode.org/ci/upload/1?token=UPLOADTOKEN')
        }
    }

    // The authentication token should not be on github.
    // That means it cannot be hardcode here.
    // At the same time this information should be visible in the job itself.
    // A workaround to achieve this is to store the information in global properties on jenkins master.
    def token = GLOBAL_STANDALONE_AUTH_TOKEN

    authenticationToken(token)
    git(branch: 'master', jenkinsfile: 'Jenkinsfile.BuildStandalone')
}
