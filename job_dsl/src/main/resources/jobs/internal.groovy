// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def internal = new JobsBuilder(this).folder('Jenkins', {})

internal.job("SeedJob") {
    htmlDescription(['Seed job to create all other jobs.'])

    jenkinsUsersPermissions()
    continuous()
    concurrentBuild(false)

    // Run the job every Sunday before the nightlies.
    // The job should not run as nightly itself. Otherwise multibranch jobs would
    // start scanning the branches and rebuilding everying on changes.
    // Irrespective of the settings for them.
    nightly('H 23 * * 0')

    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master', jenkinsfile: 'jenkinsfile.seedjob')
}
