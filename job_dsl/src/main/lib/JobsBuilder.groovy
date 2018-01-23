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
        this.jobBuilderClass = jobBuilderClass ?: JobBuilder.class
    }

    JobsBuilder android(def dataCreator) {
        new JobsBuilder(dslFactory, dataCreator, folder, AndroidJobBuilder.class)
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
        job(dslFactory.job(folder + name), closure)
        this
    }

    def job(Job job, Closure closure) {
        jobBuilderClass.newInstance([job, dslFactory, dataCreator()] as Object[]).make(closure)
    }
}
