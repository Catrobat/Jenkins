def catroid = new JobsBuilder(this).pipelineFromSCM({new RedeamerData()})
//def catroidroot = new JobsBuilder(this).android({new RedeamerData()})

catroid.job("lab/MM/ManualEmulatorTest") {
    htmlDescription(['This job runs the tests of the given REPO/BRANCH and CLASS/PACKAGE.',
                     'Use it when you want to build your own branch on Jenkins and run tests on the emulator.',
                     'Using that job early in developement can improve your tests, ' +
                     'so that they not only work on your local device but also on the emulator.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)

    parameterizedGit(jenkinsfile: 'Jenkinsfile.singletest')
    job.parameters {
        stringParam {
            name('CLASS_PKG_TO_TEST')
            defaultValue('test.common.DefaultProjectHandlerTest')
            description('')
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

catroid.job("lab/MM/PullRequest") {
    htmlDescription(['Job is automatically started when a pull request is created on github.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(jenkinsfile: 'Jenkinsfile.PullRequest')
}

catroid.job("lab/MM/PullRequest-Standalone") {
    htmlDescription(['This job is automatically started when a pull request is created on github.',
                     'It checks that the creation of standalone APKs (APK for a Pocketcode app) works, ' +
                     'reducing the risk of breaking gradle changes.',
                     'The resulting APK is not verified itself.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(context: 'Standalone APK', jenkinsfile: 'Jenkinsfile.PullRequestStandaloneAPK')
}

catroid.job("lab/MM/PullRequest-UniqueApk") {
    htmlDescription(['This job is automatically started when a pull request is created on github.',
                     'It checks that the job builds with the parameters to have unique APKs, ' +
                     'reducing the risk of breaking gradle changes.',
                     'The resulting APK is not verified on itself.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(context: 'Unique APK', jenkinsfile: 'Jenkinsfile.PullRequestIndependentAPK')
}

catroid.job("lab/MM/PullRequest-Espresso") {
    htmlDescription(['Job is manually triggered for pull requests on github to run Espresso tests.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    pullRequest(triggerPhrase: /.*please\W+run\W+espresso\W+tests.*/,
                onlyTriggerPhrase: true,
                context: 'Espresso Tests',
                jenkinsfile: 'Jenkinsfile.PullRequestEspresso')
}


// TODO: Standalone as Pipeline
//catroidroot.job("Build-Standalone") {
//    htmlDescription(['Builds a Catroid APP as a standalone APK.'])
//
//    jenkinsUsersPermissions(Permission.JobRead)
//
//    label('Standalone')
//
//    parameters {
//        stringParam('DOWNLOAD', 'https://pocketcode.org/download/821.catrobat', 'Enter the Project ID you want to build as standalone')
//        stringParam('SUFFIX', 'standalone', '')
//        password {
//            name('UPLOAD')
//            defaultValue('')
//            description('upload url for webserver\n\nSyntax of the upload value is of the form\n' +
//                        'https://pocketcode.org/ci/upload/1?token=UPLOADTOKEN')
//        }
//    }
//
//    // The authentication token should not be on github.
//    // That means it cannot be hardcode here.
//    // At the same time this information should be visible in the job itself.
//    // A workaround to achieve this is to store the information in global properties on jenkins master.
//    def token = GLOBAL_STANDALONE_AUTH_TOKEN
//
//    authenticationToken(token)
//    buildName('#${DOWNLOAD}')
//    git(branch: 'master')
//
//    failBuildAfterNoActivity('600')
//
//    job.steps {
//        shell {
//            command('''#!/bin/sh
//SLEEP_TIME=5
//RETRIES=5
//
//HTTP_STATUS_OK=200
//HTTP_STATUS_INVALID_FILE_UPLOAD=528
//
//## check if program is downloadable from web
//## If we can't load it, we retry it ${RETRIES} times
//## On a 528 status (invalid upload), we return 200 which
//## should get interpreted as UNSTABLE build
//while true; do
//    HTTP_STATUS=`curl --write-out %{http_code} --silent --output /dev/null "${DOWNLOAD}"`
//
//    if [ ${HTTP_STATUS} -eq ${HTTP_STATUS_OK} ]; then
//        break
//    fi
//
//
//    RETRIES=$((RETRIES-1))
//    if [ ${RETRIES} -eq 0 ]; then
//        if [ ${HTTP_STATUS} -eq ${HTTP_STATUS_INVALID_FILE_UPLOAD} ]; then
//            echo "Uploaded file seems to be invalid, request to '${DOWNLOAD}' returned HTTP Status ${HTTP_STATUS}"
//            exit 200
//        else
//            echo "Could not download '${DOWNLOAD}', giving up!"
//            exit 1
//        fi
//    fi
//
//    echo "Could not retrieve '${DOWNLOAD}' (HTTP Status ${HTTP_STATUS}), sleep for ${SLEEP_TIME}s and retry a maximum of ${RETRIES} times"
//    sleep ${SLEEP_TIME}
//done
//
//./gradlew -Pdownload="${DOWNLOAD}" -Papk_generator_enabled=true -Psuffix="${SUFFIX}" assembleStandaloneDebug
//
//## +x, otherwise we would spoil the upload token
//set +x
//curl -X POST -k -F upload=@./catroid/build/outputs/apk/catroid-standalone-debug.apk "${UPLOAD}"
//''')
//            unstableReturn(200)
//        }
//    }
//
//    archiveArtifacts('catroid/build/outputs/apk/catroid-standalone-debug.apk', true)
//
//    notifications(true)
//}
