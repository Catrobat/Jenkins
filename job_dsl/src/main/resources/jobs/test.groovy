def catroidorg = new JobsBuilder(this).gitHubOrganization({new CatroidData()})
def catroidroot = new JobsBuilder(this).pipeline({new CatroidData()})


catroidorg.job("lab/job/Idlir_Rusi_Shkupi/job/Catroid-OutgoingNetworkCallsTests") {
    htmlDescription([' OutgoingNetworkCallsTests '])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)

    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of the hardware tests

    gitHubOrganization(discoverForkPullRequests: false, discoverOriginPullRequests: false)
    jenkinsfilePath('Jenkinsfile.OutgoingNetworkCallsTests')
    labelForDockerBuild('Emulator')
}
