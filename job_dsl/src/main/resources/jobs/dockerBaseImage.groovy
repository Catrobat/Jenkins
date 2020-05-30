//Creates a pipeline in the jenkins base directory
def baseImageJobBuilder = new JobsBuilder(this).pipeline()

baseImageJobBuilder.job("Build_Docker_Base_Image") {
    htmlDescription(['Builds the docker base Image and pushes it to the dockerhub'])

    git(repo: 'https://github.com/Catrobat/Catroid', branch: '${gitBranch}', jenkinsfile: 'Jenkinsfile.baseDocker')

    parameters {
        gitParam('gitBranch') {
            description('Select the branch you want to build e.g. origin/master.')
            type('BRANCH')
            defaultValue('origin/develop')
        }
    }
    nightly()
}

