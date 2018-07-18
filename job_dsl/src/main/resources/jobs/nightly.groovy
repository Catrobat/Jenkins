def pipelinescript = new JobsBuilder(this).pipelineFromScript()

pipelinescript.job("Nightly-Trigger") {
    htmlDescription(['Nightly trigger job for Catroid/dev, Paintroid/dev and Build-Standalone.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    nightly()

    jenkinsfileScriptPath('job_dsl/src/main/resources/jobs/jenkinsfiles/nightly_trigger.jenkinsfile')
}
