// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def folder = 'Jenkins'
Views.folderAndView(this, folder)

new JobBuilder(job("$folder/SeedJob")).make {
    htmlDescription(['Seed job to create all other jobs.'])

    jenkinsUsersPermissions()
    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master')
    continuous()
    nightly('H 23 * * *') // run the job before all other nightlies
    steps {
        jobDsl {
            additionalClasspath('job_dsl/src/main/lib/')
            targets('job_dsl/src/main/resources/jobs/*.groovy')
            failOnMissingPlugin(true)
            removedJobAction('DELETE')
            removedViewAction('DELETE')
            unstableOnDeprecation(true)
        }
    }
}

new JobBuilder(job("$folder/LocalBackup")).make {
    htmlDescription(['Creates a local backup of jenkins in /home/jenkins/jenkins_created_backups.',
                     'Useful to run manually before installing updates of plugins.',
                     'Does not replace other forms of updates!'])

    jenkinsUsersPermissions()
    label('master')
    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master')
    shell('bash -ex ./scripts/backupJenkinsLocally.sh')
}
