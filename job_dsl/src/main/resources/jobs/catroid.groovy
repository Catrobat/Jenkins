def catroid = new JobsBuilder(this).android({new CatroidData()}).folderAndView('Catroid-Legacy')
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

Views.basic(this, "Catroid", "Catroid/.+")

catroid.job("SingleClassEmulatorTest") {
    htmlDescription(['This job runs the tests of the given REPO/BRANCH and CLASS.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    job.parameters {
        stringParam('CLASS', 'test.common.DefaultProjectHandlerTest', '')
    }
    parameterizedAndroidVersion()
    buildName('#${BUILD_NUMBER} | ${ENV, var="CLASS"} ')
    androidEmulator()
    gradle('adbDisableAnimationsGlobally connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.$CLASS')
    junit()

    notifications()
}

catroid.job("SinglePackageEmulatorTest") {
    htmlDescription(['This job runs the tests of the given REPO/BRANCH and PACKAGE.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    job.parameters {
        stringParam('PACKAGE', 'test', '')
    }
    parameterizedAndroidVersion()
    buildName('#${BUILD_NUMBER} | ${ENV, var="PACKAGE"}')
    androidEmulator()
    gradle('adbDisableAnimationsGlobally connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.$PACKAGE')
    junit()

    notifications()
}

catroid.job("PullRequest") {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest()
    androidEmulator()
    gradle('clean check adbDisableAnimationsGlobally test connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.test')
    staticAnalysis()
    junit()

    notifications()
}

catroid.job("PullRequest-Standalone") {
    htmlDescription(['This job is automatically started when a pull request is created on github.',
                     'It checks that the creation of standalone APKs (APK for a Pocketcode app) works, ' +
                     'reducing the risk of breaking gradle changes.',
                     'The resulting APK is not verified itself.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues
    git()

    pullRequest(context: 'Standalone APK')

    gradle('assembleStandaloneDebug',
           '-Pdownload="https://pocketcode.org/download/817.catrobat" -Papk_generator_enabled=true -Psuffix="generated821"')

    notifications()
}

catroid.job("PullRequest-UniqueApk") {
    htmlDescription(['This job is automatically started when a pull request is created on github.',
                     'It checks that the job builds with the parameters to have unique APKs, ' +
                     'reducing the risk of breaking gradle changes.',
                     'The resulting APK is not verified on itself.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues
    git()

    pullRequest(context: 'Unique APK')
    gradle('assembleCatroidDebug', '-Pindependent="Code Nightly #${BUILD_NUMBER}"')

    notifications()
}

catroid.job("PullRequest-Espresso") {
    htmlDescription(['Job is manually triggered for pull requests on github to run Espresso tests.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(triggerPhrase: /.*please\W+run\W+espresso\W+tests.*/,
                onlyTriggerPhrase: true,
                context: 'Espresso Tests')
    androidEmulator()
    gradle('adbDisableAnimationsGlobally connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.uiespresso.testsuites.PullRequestTriggerSuite')
    junit()

    notifications()
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
