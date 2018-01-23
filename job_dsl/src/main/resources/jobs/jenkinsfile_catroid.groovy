//folder('Catroid-Jenkinsfile');
//
//multibranchPipelineJob('Catroid-Jenkinsfile/Standalone-Jenkinsfile') {
//
//    description('''This job is automatically started when a pull request is created on github.
//It checks that the creation of standalone APKs (APK for a Pocketcode app) works,
//reducing the risk of breaking gradle changes.
//The resulting APK is not verified itself.''')
//
//    //TODO: set permissions
//    //jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
//    //anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues
//
//    branchSources {
//        branchSource {
//            source {
//                github {
//                    repoOwner('redeamer')
//                    repository('Catroid')
//                    apiUri('https://api.github.com')
//                    credentialsId('github')
//                }
//            }
//        } 
//    }
//
//    configure {
//        it / factory(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory') {
//            scriptPath('Jenkinsfile')
//        }
//    }
//}

def catorg = new JobsBuilder(this).gitHubOrganization({new CatroidData()}).folderAndView('Catroid-Jenkinsfile2')

catorg.job("Standalone-Nightly") {
    htmlDescription(['Nightly builds of the "Tic-Tac-Toe Master" standalone APP using develop.',
                     'This allows to find issues with standalone builds before the next release.'])

    gitHubOrganization()
    jenkinsfilePath('Jenkinsfile.test')
}
