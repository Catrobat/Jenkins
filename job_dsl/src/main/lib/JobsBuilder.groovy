import javaposse.jobdsl.dsl.Job

class JobsBuilder {
    private def outerScope
    private def dataCreator
    private def folder
    private def jobBuilderClass

    JobsBuilder(def outerScope) {
        this(outerScope, null, "", null)
    }

    private JobsBuilder(def outerScope, def dataCreator, def folder, def jobBuilderClass) {
        this.outerScope = outerScope
        this.dataCreator = dataCreator?.clone() ?: { null }
        this.folder = folder
        this.jobBuilderClass = jobBuilderClass ?: JobBuilder.class
    }

    JobsBuilder android(def dataCreator) {
        new JobsBuilder(outerScope, dataCreator, folder, AndroidJobBuilder.class)
    }

    JobsBuilder folder(String name, Closure closure = null) {
	if (closure == null) {
            closure = {
		jenkinsUsersPermissions(Permission.JobRead)
                anonymousUsersPermissions(Permission.JobRead)
            }
        }

        String newFolder = folder + name
        def folderJob = outerScope.folder(newFolder)
        (new JobBuilder(folderJob, outerScope, null)).makeNoDefaults(closure)

        new JobsBuilder(outerScope, dataCreator, "$newFolder/", jobBuilderClass)
    }

    JobsBuilder folderAndView(String name, Closure closure = null) {
        Views.basic(outerScope, name, "${folder + name}/.+")
        folder(name, closure)
    }

    JobsBuilder job(String name, Closure closure) {
        job(outerScope.job(folder + name), closure)
        this
    }

    def job(Job job, Closure closure) {
        jobBuilderClass.newInstance([job, outerScope, dataCreator()] as Object[]).make(closure)
    }
}
