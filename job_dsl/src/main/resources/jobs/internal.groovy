// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def internal = new JobsBuilder(this).folderAndView('Jenkins', {})

internal.job("SeedJob") {
    htmlDescription(['Seed job to create all other jobs.'])

    jenkinsUsersPermissions()
    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master', jenkinsfile: 'jenkinsfile.seedjob')
    configure { it / definition / lightweight(true)}

    notifications()
}

