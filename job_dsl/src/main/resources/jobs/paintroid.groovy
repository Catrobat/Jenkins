def paintroid = new JobsBuilder(this).android({new PaintroidData()}).folderAndView('Paintroid-Legacy')
def paintroidorg = new JobsBuilder(this).gitHubOrganization({new PaintroidData()})

def paintroid(String job_name, Closure closure) {
    new AndroidJobBuilder(job(job_name), new PaintroidData()).make(closure)
}

paintroidorg.job("Paintroid") {
    htmlDescription(['Job is automatically started on a new commit or a new/updated pull request created on github.',
                     'This job runs the Pipeline defined in the Jenkinsfile inside of the repository.',
                     'The Pipeline should run the code analyisis, the unit and device tests.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    gitHubOrganization()
    jenkinsfilePath('Jenkinsfile')
    labelForDockerBuild('Emulator')
}

Views.basic(this, "Paintroid", "Paintroid/.+")

paintroid.job("SingleClassEmulatorTest") {
    disabled()

    htmlDescription(['This job runs the tests of the given REPO/BRANCH and CLASS.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    job.parameters {
        stringParam('CLASS', 'test.espresso.NavigationDrawerTest', '')
    }
    parameterizedAndroidVersion()
    buildName('#${BUILD_NUMBER} | ${ENV, var="CLASS"} ')
    androidEmulator()
    gradle('connectedDebugAndroidTest',
           '-Pjenkins -Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.paintroid.$CLASS')
    junit()

    notifications()
}

paintroid.job("SinglePackageEmulatorTest") {
    disabled()

    htmlDescription(['This job runs the tests of the given REPO/BRANCH and PACKAGE.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    job.parameters {
        stringParam('PACKAGE', 'test.espresso', '')
    }
    parameterizedAndroidVersion()
    buildName('#${BUILD_NUMBER} | ${ENV, var="PACKAGE"}')
    androidEmulator()
    gradle('connectedDebugAndroidTest',
           '-Pjenkins -Pandroid.testInstrumentationRunnerArguments.package=org.catrobat.paintroid.$PACKAGE')
    junit()

    notifications()
}

paintroid.job("PullRequest-Tests") {
    disabled()

    htmlDescription(['Job is automatically started when a pull request is created on github.',
                     'The job runs ui tests that take ages.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(context: "UI-Tests")
    androidEmulator()
    gradle('adbDisableAnimationsGlobally connectedDebugAndroidTest ', '-Pjenkins')
    junit()

    notifications()
}

paintroid.job("PullRequest-StaticAnalysis") {
    disabled()

    htmlDescription(['Job is automatically started when a pull request is created on github.',
                     'The job runs static analysis on the code base'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(context: 'Static Analysis')
    createAndroidSdkLicenses()
    gradle('pmd checkstyle lint')
    staticAnalysis()

    notifications()
}

paintroid.job("Nightly") {
    disabled()

    htmlDescription(['Nightly Paintroid job.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator()
    gradle('assembleDebug',
           '-Pjenkins -Pindependent="Paint Nightly #${BUILD_NUMBER}"')
    uploadApkToFilesCatrobat()
    gradle('connectedDebugAndroidTest',
           '-Pjenkins -Pindependent="Paint Nightly #${BUILD_NUMBER} ' +
           '-Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.paintroid.test.espresso')
    junit()

    notifications()
}

paintroid.job("Continuous") {
    disabled()

    htmlDescription(['Job runs continuously on changes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    continuous()
    androidEmulator()
    gradle('connectedDebugAndroidTest',
           '-Pjenkins -Pandroid.testInstrumentationRunnerArguments.class=org.catrobat.paintroid.test.espresso')
    junit()

    notifications()
}
