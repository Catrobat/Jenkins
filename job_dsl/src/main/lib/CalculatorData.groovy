class CalculatorData {
    def repo = 'https://github.com/vinzynth/catroidCalculator.git'
    def branch = 'master'
    def githubUrl = 'https://github.com/vinzynth/catroidCalculator'
    def androidVersions = 18..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = ''
    def githubOrganizations = []
    def pullRequestAdmins = []
    def androidEmulatorParameters = [screenDensity: '160', screenResolution: '480x800', targetAbi: 'x86_64',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.keyboard': 'yes', 'hw.ramSize': '800', 'vm.heapSize': '128'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    //def debugApk = 'Calculator/build/outputs/apk/Calculator-debug.apk'
}
