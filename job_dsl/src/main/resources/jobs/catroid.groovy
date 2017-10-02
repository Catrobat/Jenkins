def catroid = new JobsBuilder(this).android({new CatroidData()}).folderAndView('Catroid')

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
    gradle('connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.$CLASS')
    junit()
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
    gradle('connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.$PACKAGE')
    junit()
}

catroid.job("PullRequest") {
    disabled()
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest()
    androidEmulator()
    gradle('clean check test connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.test')
    staticAnalysis()
    junit()
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

    // Running this in two steps to find more issues, like CAT-2400
    gradle('buildStandalone',
           '-Pdownload="https://pocketcode.org/download/817.catrobat" -Papk_generator_enabled=true -Psuffix="generated821"')
    gradle('assembleStandaloneDebug',
           '-Pdownload="https://pocketcode.org/download/817.catrobat" -Papk_generator_enabled=true -Psuffix="generated821"')
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
    gradle('assembleDebug', '-Pindependent="Code Nightly #${BUILD_NUMBER}"')
}

catroid.job("PullRequest-Espresso") {
    disabled()
    htmlDescription(['Job is manually triggered for pull requests on github to run Espresso tests.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(triggerPhrase: /.*please\W+run\W+espresso\W+tests.*/,
                onlyTriggerPhrase: true,
                context: 'Espresso Tests')
    androidEmulator()
    gradle('connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.uiespresso.testsuites.PullRequestTriggerSuite')
    junit()
}

catroid.job("Nightly") {
    htmlDescription(['Nightly Catroid job.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator()
    gradle('assembleDebug', '-Pindependent="Code Nightly #${BUILD_NUMBER}"')
    uploadApkToFilesCatrobat()
    gradle('check test connectedCatroidDebugAndroidTest',
           '-Pindependent="Code Nightly #${BUILD_NUMBER}" ' +
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.test')
    shell("# ensure that the following test run does not override these results\n" +
          'mv catroid/build/outputs/androidTest-results catroid/build/outputs/androidTest-results1')
    gradle('connectedCatroidDebugAndroidTest',
           '-Pindependent="Code Nightly #${BUILD_NUMBER}" ' +
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.uiespresso.testsuites.PullRequestTriggerSuite')
    staticAnalysis()
    junit()
}

catroid.job("Continuous") {
    htmlDescription(['Job runs continuously on changes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    continuous()
    androidEmulator()
    gradle('check test connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.catroid.test')
    shell("# ensure that the following test run does not override these results\n" +
          'mv catroid/build/outputs/androidTest-results catroid/build/outputs/androidTest-results1')
    gradle('connectedCatroidDebugAndroidTest',
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.catroid.uiespresso.testsuites.PullRequestTriggerSuite')
    staticAnalysis()
    junit()
}

catroid.job("Standalone") {
    htmlDescription(['Builds a Catroid APP as a standalone APK.'])

    jenkinsUsersPermissions(Permission.JobRead)

    parameters {
        stringParam('DOWNLOAD', 'https://pocketcode.org/download/821.catrobat', 'Enter the Project ID you want to build as standalone')
        stringParam('SUFFIX', 'standalone', '')
        password {
            name('UPLOAD')
            defaultValue('')
            description('upload url for webserver\n\nSyntax of the upload value is of the form\n' +
                        'https://pocketcode.org/ci/upload/1?token=UPLOADTOKEN')
        }
    }

    // Both the authentication token and the mail recipients should not be on github.
    // That means they cannot be hardcode here.
    // At the same time this information should be visible in the job itself.
    // A workaround to achieve this is to store the information in global properties on jenkins master.
    def token = GLOBAL_STANDALONE_AUTH_TOKEN
    def mail_recipients = GLOBAL_STANDALONE_MAIL_RECIPIENTS

    authenticationToken(token)
    buildName('#${DOWNLOAD}')
    git(branch: 'master')
    gradle('buildStandalone assembleStandaloneDebug',
           '-Pdownload="${DOWNLOAD}" -Papk_generator_enabled=true -Psuffix="${SUFFIX}"')
    shell('curl -X POST -k -F upload=@./catroid/build/outputs/apk/catroid-standalone-debug.apk $UPLOAD')
    archiveArtifacts('catroid/build/outputs/apk/catroid-standalone-debug.apk')
    publishers {
        mailer {
            recipients(mail_recipients)
            notifyEveryUnstableBuild(true)
            sendToIndividuals(false)
        }
    }
}

catroid.job("Standalone-Nightly") {
    htmlDescription(['Nightly builds of the "Tic-Tac-Toe Master" standalone APP using develop.',
                     'This allows to find issues with standalone builds before the next release.'])

    jenkinsUsersPermissions(Permission.JobRead)
    git()
    nightly()

    // Running this in two steps to find more issues, like CAT-2400
    gradle('buildStandalone',
           '-Pdownload="https://pocketcode.org/download/817.catrobat" -Papk_generator_enabled=true -Psuffix="generated821"')
    gradle('assembleStandaloneDebug',
           '-Pdownload="https://pocketcode.org/download/817.catrobat" -Papk_generator_enabled=true -Psuffix="generated821"')

    archiveArtifacts('catroid/build/outputs/apk/catroid-standalone-debug.apk')
}
