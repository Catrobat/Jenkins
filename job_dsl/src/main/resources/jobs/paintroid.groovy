new PaintroidJobBuilder(job('Paintroid-PartialTests')).make {
    htmlDescription([], '<p>Do <b>not</b> start this job manually.</p>\n' +
                    '<p>A job to execute emulator tests defined by external jobs via exclude files.</p>')

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parameterizedTestExclusionsFile()
    androidEmulator()
    gradle('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest', '-Pjenkins')
}

new PaintroidJobBuilder(job('Paintroid-ParallelTests-CustomBranch')).make {
    htmlDescription(['This job builds and runs UI tests of the given REPO/BRANCH.'])

    jenkinsUsersPermissions(Permission.JobBuild, Permission.JobRead, Permission.JobCancel)

    parameterizedGit()
    parameterizedAndroidVersion()
    parallelTests('Paintroid-PartialTests', 2)
}

new PaintroidJobBuilder(job('Paintroid-PullRequest')).make {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)

    pullRequest()
    androidEmulator(androidApi: 22)
    gradle('connectedDebugAndroidTest', '-Pjenkins')
    junit()
}

new PaintroidJobBuilder(multiJob('Paintroid-Nightly')).make {
    htmlDescription(['Nightly Paintroid job.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()

    steps {
        phase('BuildAndTest') {
            phaseJob('Paintroid-ParallelTests-CustomBranch') {
                parameters {
                    gitRevision()
                }
            }
        }
    }

    gradle('clean assembleDebug', '-Pjenkins')
    uploadApkToFilesCatrobat()
}
