// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def catroidReleaseJobBuilder = new JobsBuilder(this).folder('Release_generated', {}).pipeline({new CatroidData()})

catroidReleaseJobBuilder.job("Catroid_to_Google_Play_alpha") {
    htmlDescription(['Build releaseable APK, sign and upload it to the Alpha Channel of Google Play.'])

    git(branch: '${gitBranch}', jenkinsfile: 'Jenkinsfile.releaseAPK')

    logRotator(-1, 100)

    properties {
        rebuild{
            rebuildDisabled()
        }
    }

    permissions('Release-Users',Permission.JobBuild, Permission.JobRead, Permission.JobCancel, Permission.JobWorkspace)

    parameters {
        choiceParameter {
            name('flavor')
            description('Select the flavor you want to build. \'Playground\' is for test purposes to \'org.catrobat.catroid.test\'.')
            choiceType('SINGLE_SELECT')
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
        gitParam('gitBranch'){
            description('Select the branch you want to build e.g. origin/master.')
            type('BRANCH')
            defaultValue('origin/master')
        }
        password {
            name('signingKeystorePassword')
            defaultValue('')
            description('')
        }
        password {
            name('signingKeyAlias')
            defaultValue('')
            description('')
        }
        password {
            name('signingKeyPassword')
            defaultValue('')
            description('For current keystore, this is the same as signingKeystorePassword.')
        }
        password {
            name('googlePlayJsonKey')
            defaultValue('')
            description('Json file used to connect to the API of the Google Developer Console.\n' +
                    'Copy the whole content of the file into this password parameter.')
        }
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
