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
                     'This job runs the the hardware tests on the sensorbox.',
                     '<p style="color:red;"><b>HINT! IF TESTS FAIL CHECK IF NEXUS IS UNLOCKED AND CONNECTED TO HIDDEN WLAN robo-arduino</b></p>'])

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
        choiceParam('TYPE', ['class', 'package'], 'Are the tests in a package or in a class?')
        stringParam {
            name('NAME')
            defaultValue('org.catrobat.catroid.uiespresso.testsuites.ApiLevel19RegressionTestsSuite')
            description('The fully qualified name of the test class or test package.')
            trim(true)
        }
        choiceParam('EMULATOR', ['android19', 'android24', 'android28'], 'The emulator to use, as specified in build.gradle')
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
