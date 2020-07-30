//Creates a pipeline in the jenkins base directory
def baseImageJobBuilder = new JobsBuilder(this).pipeline()

baseImageJobBuilder.job("Build_Docker_Base_Image") {
    htmlDescription(['Builds the docker base Image and pushes it to the dockerhub'])

    git(repo: 'https://github.com/Catrobat/Catroid', branch: '${gitBranch}', jenkinsfile: 'Jenkinsfile.baseDocker')

    parameters {
        booleanParam('TAG_STABLE', false, 'When selected image will be tagged stable')
        booleanParam('TAG_TESTING', true, 'When selected image will be tagged testing')
        stringParam('IMAGE_NAME', 'catrobat-android', 'Name for docker image to build')
        gitParam('gitBranch') {
            description('Select the branch you want to build e.g. origin/master.')
            type('BRANCH')
            defaultValue('origin/master')
        }
    }
    nightly()
}

