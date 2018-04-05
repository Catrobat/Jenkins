def pipelinescript = new JobsBuilder(this).pipelineFromScript()

pipelinescript.job("lab/PipelineFromScript-Example") {
    htmlDescription(['This is a simple example job to demonstrate the usage of the pipelineFromScript jobDslHelper.',
                     'This job shall be removed, once this is really used by an job.'])

    jenkinsUsersPermissions(Permission.JobRead, Permission.JobBuild, Permission.JobCancel)
    anonymousUsersPermissions(Permission.JobRead) // allow anonymous users to see the results of PRs to fix their issues

    nightly()

    jenkinsfileScriptPath('job_dsl/src/main/resources/jobs/jenkinsfiles/pipeline_from_script_example.jenkinsfile')
}
