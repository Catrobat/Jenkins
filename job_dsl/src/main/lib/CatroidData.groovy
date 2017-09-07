class CatroidData {
    // TODO check that https://github.com/Catrobat/Catroid/pull/2349 is merged
    def repo = 'https://github.com/Catrobat/Catroid.git'
    def branch = 'develop'
    def githubUrl = 'https://github.com/Catrobat/Catroid'
    def androidVersions = 19..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['Catrobat']
    def pullRequestAdmins = ['84n4n4', 'bernadettespieler', 'Bioxar', 'cheidenreich', 'ElliHeschl',
                             'joseffilzmaier', 'MightyMendor', 'oliewa92', 'robertpainsi', 'thmq',
                             'thomasmauerhofer']
    def androidEmulatorParameters = [screenDensity: 'xhdpi', screenResolution: '768x1280', targetAbi: 'x86_64',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.camera': 'yes', 'hw.ramSize': '800', 'hw.gpu.enabled': 'yes',
                                                          'hw.camera.front': 'emulated',
                                                          'hw.camera.back': 'emulated', 'hw.gps': 'yes'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    def debugApk = 'catroid/build/outputs/apk/catroid-catroid-debug.apk'
    def excludedTests = []
    def staticAnalysisResults = [androidLint: 'catroid/build/reports/lint*.xml',
                                 checkstyle: 'catroid/build/reports/checkstyle.xml',
                                 pmd: 'catroid/build/reports/pmd.xml']
}
