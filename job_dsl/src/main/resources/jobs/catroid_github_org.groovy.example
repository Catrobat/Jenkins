def catorg = new JobsBuilder(this).gitHubOrganization({new CatroidData()}).folderAndView('Catroid-GH-Organization-Test')

catorg.job("Jenkinsfile-Test") {
    htmlDescription(['Simple Test-Job for Github-Organization Jobs.',
                     'Currently this jobs does nothing.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    gitHubOrganization()
    jenkinsfilePath('Jenkinsfile.test')
}
