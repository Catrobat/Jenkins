import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class JobsBuilder {
    private DslFactory dslFactory
    private def dataCreator
    private def folder
    private def jobBuilderClass

    JobsBuilder(def dslFactory) {
        this(dslFactory, null, "", null)
    }

    private JobsBuilder(DslFactory dslFactory, def dataCreator, def folder, def jobBuilderClass) {
        this.dslFactory = dslFactory
        this.dataCreator = dataCreator?.clone() ?: { null }
        this.folder = folder
        this.jobBuilderClass = jobBuilderClass ?: PiplineJobBuilder.class
    }

    JobsBuilder gitHubOrganization(def dataCreator) {
        new JobsBuilder(dslFactory, dataCreator, folder, MultibranchPipelineJobBuilder.class)
    }

    JobsBuilder pipeline(def dataCreator = null) {
        new JobsBuilder(dslFactory, dataCreator, folder, PiplineJobBuilder.class)
    }

    JobsBuilder folder(String name, Closure closure = null) {
        if (closure == null) {
            closure = {
                jenkinsUsersPermissions(Permission.JobRead)
                anonymousUsersPermissions(Permission.JobRead)
            }
        }

        String newFolder = folder + name
        def folderJob = dslFactory.folder(newFolder)
        (new JobBuilder(folderJob, dslFactory, null)).makeNoDefaults(closure)

        new JobsBuilder(dslFactory, dataCreator, "$newFolder/", jobBuilderClass)
    }

    JobsBuilder folderAndView(String name, Closure closure = null) {
        Views.basic(dslFactory, name, "${folder + name}/.+")
        folder(name, closure)
    }

    JobsBuilder job(String name, Closure closure) {
        if (MultibranchPipelineJobBuilder.class.isAssignableFrom(this.jobBuilderClass)) {
            job(dslFactory.multibranchPipelineJob(folder + name), closure)
        } else if (PiplineJobBuilder.class.isAssignableFrom(this.jobBuilderClass)) {
            job(dslFactory.pipelineJob(folder + name), closure)
        } else {
            assert false //only pipeline jobs are supported
        }
        this
    }

    def job(def job, Closure closure) {
        jobBuilderClass.newInstance([job, dslFactory, dataCreator()] as Object[]).make(closure)
    }
}
