new JobsBuilder(this).gitHubOrganization({new GradlePluginsData()}).job('GradlePlugins') {
    htmlDescription(['Job is automatically started on a new commit or a new/updated pull request created on github.',
                     'This job runs the Pipeline defined in the Jenkinsfile inside of the repository.',
                     'The Pipeline builds the project and runs tests on it.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel, Permission.JobWorkspace)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    gitHubOrganization()
    jenkinsfilePath('Jenkinsfile')
    labelForDockerBuild('LimitedEmulator')
}
