/**
 * git_cache.groovy
 *
 * Clones each project on each Jenkins-Node to act as a local cache.
 * This allows to speed up git-interaction for jobs.
 * For that a reference-repository (the cache directory) has to be specified
 * in the jobs themselves, see also the JobBuilder.git() function and
 * the referenceRepo field in the Data classes..
 *
 * The cache directories exist on every node on the same relative path.
 * Therefore they can be accessed transparently.
 */

def nodes = ['Slave2_emulator',
             'Slave2_no_device_or_emulator',
             'Slave3_emulator',
             'Slave3_no_device_or_emulator',
]
def projects = [new CatroidData(),
                new PaintroidData(),
]
def gitCache = new JobsBuilder(this).folderAndView('git-cache')

nodes.each{ node ->
    def nodeFolder = gitCache.folder(node)

    projects.each{ project ->
        nodeFolder.job("$project.name") {
            htmlDescription(["Reference repository for ${project.name} to speed up cloning on ${node}."])
            keepWorkspace()
            label(node)

            job.triggers {
                cron('H/5 * * * *')
            }

            shell("""
REPO_NAME="scm"
REPO="${project.repo}"

if [ -d \$REPO_NAME ]; then
    cd \$REPO_NAME
    git fetch
else
    git clone --mirror \$REPO \$REPO_NAME
fi
""")
        }
    }
}
