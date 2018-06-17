class RedeamerData {
    def name = 'Catroid'
    def repoOwner = 'redeamer'
    def repoName = 'Catroid'
    def repo = 'https://github.com/redeamer/Catroid.git'
    def branch = 'develop'
    def githubUrl = 'https://github.com/redeamer/Catroid'
    def androidVersions = (24..19).grep{it != 20}
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['redeamer']
    def githubOrganizationsJenkinsCredentialsRefId = 'github-organizations-redeamer'
    def pullRequestAdmins = ['redeamer']
    def androidEmulatorParameters = [androidApi: '24', screenDensity: 'xxhdpi', screenResolution: '1080x1920', targetAbi: 'x86_64',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.camera': 'yes', 'hw.ramSize': '2048', 'hw.gpu.enabled': 'yes',
                                                          'hw.camera.front': 'emulated', 'hw.camera.back': 'emulated',
                                                          'hw.gps': 'yes',
                                                          'hw.mainKeys': 'no', 'hw.keyboard': 'yes',
                                                          'disk.dataPartition.size': '512M'],
                                     commandLineOptions: '-no-boot-anim -noaudio']
    def debugApk = 'catroid/build/outputs/apk/catroid/debug/catroid-catroid-debug.apk'
    def staticAnalysisResults = [androidLint: 'catroid/build/reports/lint*.xml',
                                 checkstyle: 'catroid/build/reports/checkstyle.xml',
                                 pmd: 'catroid/build/reports/pmd.xml']
}
