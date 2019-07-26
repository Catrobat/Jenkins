// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def releaseJobBuilder = new JobsBuilder(this).folder('Release_generated', {})
def catroidReleaseJobBuilder = releaseJobBuilder.pipeline({ new CatroidData() })
def paintroidReleaseJobBuilder = releaseJobBuilder.pipeline({ new PaintroidData() })

catroidReleaseJobBuilder.job("Catroid_to_Google_Play_alpha") {
    htmlDescription(['Build releaseable APK, sign and upload it to the Alpha Channel of Google Play.'])

    git(branch: '${gitBranch}', jenkinsfile: 'Jenkinsfile.releaseAPK')

    logRotator(-1, 100)

    properties {
        rebuild {
            rebuildDisabled()
        }
    }

    permissions('Release-Users', Permission.JobBuild, Permission.JobRead, Permission.JobCancel, Permission.JobWorkspace)

    parameters {
        choiceParameter {
            name('flavor')
            description('Select the flavor you want to build. \'Playground\' is for test purposes to \'org.catrobat.catroid.test\'.')
            choiceType('PT_SINGLE_SELECT')
            filterable(false)
            filterLength(1)
            randomName('flavor')
            script {
                groovyScript {
                    script {
                        script('[\'Catroid\': \'Pocket Code\'] + [\'CreateAtSchool\', \'LunaAndCat\', \'Phiro\', \'Playground\', \'All\'].collectEntries{[it, it]}')
                        sandbox(true)
                    }
                    fallbackScript {
                        script('')
                        sandbox(false)
                    }
                }
            }
        }
    }

    defaultReleasePatameter()

    parameters {
        password {
            name('firebaseApiKey')
            defaultValue('')
            description('')
        }
        password {
            name('fabricApiKey')
            defaultValue('')
            description('')
        }
        password {
            name('fabricApiSecret')
            defaultValue('')
            description('')
        }
    }
}

paintroidReleaseJobBuilder.job("Paintroid_to_Google_Play_alpha") {
    htmlDescription(['Build releaseable APK, sign and upload it to the Alpha Channel of Google Play.'])

    git(branch: '${gitBranch}', jenkinsfile: 'Jenkinsfile.releaseAPK')

    logRotator(-1, 100)

    properties {
        rebuild {
            rebuildDisabled()
        }
    }

    permissions('Release-Users', Permission.JobBuild, Permission.JobRead, Permission.JobCancel, Permission.JobWorkspace)

    defaultReleasePatameter()

    parameters {
        password {
            name('bintrayUser')
            defaultValue('')
            description('User used for gradle bintrayUpload task')
        }
        password {
            name('bintrayKey')
            defaultValue('')
            description('Key used for gradle bintrayUpload task')
        }
    }
}
