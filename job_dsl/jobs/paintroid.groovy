job('Paintroid-PartialTests') {
    description('''<style>
@import "/userContent/job_styles.css";
</style>
<div class="cat-info">
    <p>Do <b>not</b> start this job manually.</p>
    <p>A job to execute emulator tests defined by external jobs via exclude files.</p>
    <ul>
        <li>Remove this job when it was not run in the last 2 months.</li>
    </ul>
</div>''')

    authorization {
        permission('hudson.model.Item.Cancel', 'Jenkins-Users')
        permission('hudson.model.Item.Read', 'Jenkins-Users')
    }

    logRotator(30, 100)

    parameters {
        stringParam('REPO', 'https://github.com/Catrobat/Paintroid.git', '')
        stringParam('BRANCH', 'develop', '')
        choiceParam('ANDROID_VERSION', 18..24, 'The android version to use by API level \nhttp://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels')
        fileParam('testexclusions.txt', 'Optional file listing the tests to exclude (both .java and .class files).\nNeeded for parallel test job execution.')
    }

    label('Emulator')
    concurrentBuild()

    scm {
        git {
            remote {
                url('$REPO')
                branch('$BRANCH')
            }
        }
    }

    wrappers {
        preBuildCleanup()
        timestamps()

        androidEmulator {
            avdName(null)
            osVersion('android-${ANDROID_VERSION}')
            screenDensity('240')
            screenResolution('480x800')
            deviceLocale('en_US')
            sdCardSize('151M')
            targetAbi('x86')
            avdNameSuffix('')
            hardwareProperties {
                hardwareProperty {
                    key('hw.keyboard')
                    value('yes')
                }
                hardwareProperty {
                    key('hw.ramSize')
                    value('800')
                }
                hardwareProperty {
                    key('vm.heapSize')
                    value('128')
                }
            }

            wipeData(true)
            showWindow(true)
            useSnapshots(false)

            deleteAfterBuild(false)
            startupDelay(0)
            startupTimeout(120)
            commandLineOptions('-no-boot-anim -noaudio -qemu -m 800 -enable-kvm')
            executable('')
        }
    }

    steps {
        gradle {
            switches('-Pjenkins')
            tasks('clean assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest')
        }
    }
}

job('Paintroid-ParallelTests-CustomBranch') {
    description('''<style>
@import "/userContent/job_styles.css";
</style>
<div class="cat-info">
    <p><b>Info:</b></p>
    <ul>
        <li>This job builds and runs UI tests of the given REPO/BRANCH.</li>
        <li>Remove this job when it was not run in the last 2 months.</li>
    </ul>
</div>''')

    authorization {
        permission('hudson.model.Item.Build', 'Jenkins-Users')
        permission('hudson.model.Item.Cancel', 'Jenkins-Users')
        permission('hudson.model.Item.Read', 'Jenkins-Users')
    }

    logRotator(30, 100)

    parameters {
        stringParam('REPO', 'https://github.com/Catrobat/Paintroid.git', '')
        stringParam('BRANCH', 'develop', '')
        choiceParam('ANDROID_VERSION', 18..24, 'The android version to use by API level \nhttp://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels')
    }

    label('NoDevice')
    concurrentBuild()

    scm {
        git {
            remote {
                url('$REPO')
                branch('$BRANCH')
            }
        }
    }

    wrappers {
        timestamps()
    }

    steps {
        parallelTestExecutor {
            testJob('Paintroid-PartialTests')
            patternFile('testexclusions.txt')
            parallelism {
                count {
                    size(2)
                }
            }
            testReportFiles('**/*TEST*.xml')
            archiveTestResults(true)
            parameters {
                currentBuildParameters()
                gitRevisionBuildParameters {
                    combineQueuedCommits(false)
                }
            }
        }
    }

    publishers {
        archiveJunit('**/*TEST*.xml') {
        }
    }
}

listView('Paintroid') {
    jobs {
        regex(/Paintroid.+/)
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
