def folder = 'Paintroid'
Views.folderAndView(this, folder)

def paintroid(String job_name, Closure closure) {
    new AndroidJobBuilder(job(job_name), new PaintroidData()).make(closure)
}

paintroid("$folder/PartialTests") {
    htmlDescription(['Do <b>not</b> start this job manually.',
                     'A job to execute emulator tests defined by external jobs via exclude files.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parameterizedTestExclusionsFile()
    androidEmulator()
    gradle('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest', '-Pjenkins')
}

paintroid("$folder/CustomBranch") {
    htmlDescription(['This job builds and runs tests of the given REPO/BRANCH.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    excludeTests()
    androidEmulator()
    gradle('assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest', '-Pjenkins')
    junit()
}

paintroid("$folder/ParallelTests-CustomBranch") {
    htmlDescription(['This job builds and runs UI tests of the given REPO/BRANCH.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parallelTests("$folder/PartialTests", 2)
}

paintroid("$folder/PullRequest") {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest()
    androidEmulator(androidApi: 18)
    gradle('connectedDebugAndroidTest', '-Pjenkins')
    junit()
}

paintroid("$folder/Nightly") {
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
