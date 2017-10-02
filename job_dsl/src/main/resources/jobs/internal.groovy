// The file should not be named jenkins.groovy as that leads to a warning as there
// is already a jenkins package.
def internal = new JobsBuilder(this).folderAndView('Jenkins')

internal.job("SeedJob") {
    htmlDescription(['Seed job to create all other jobs.'])

    jenkinsUsersPermissions()
    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master')
    continuous()
    nightly('H 23 * * *') // run the job before all other nightlies
    shell('cp configs/log_parser_rules.groovy $JENKINS_HOME/')
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

internal.job("LocalBackup") {
    htmlDescription(['Creates a local backup of jenkins in /home/jenkins/jenkins_created_backups.',
                     'Useful to run manually before installing updates of plugins.',
                     'Does not replace other forms of updates!'])

    jenkinsUsersPermissions()
    label('master')
    git(repo: 'https://github.com/Catrobat/Jenkins.git', branch: 'master')
    shell('bash -ex ./scripts/backupJenkinsLocally.sh')
}
