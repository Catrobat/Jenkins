class PaintroidJobBuilder extends AndroidJobBuilder {
    PaintroidJobBuilder(Job job) {
        super(job, new Paintroid())
    }
}

class Paintroid {
    def repo = 'https://github.com/Catrobat/Paintroid.git'
    def branch = 'develop'
    def githubUrl = 'https://github.com/Catrobat/Paintroid'
    def androidVersions = 18..24
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['Catrobat']
    def pullRequestAdmins = ['thmq', '84n4n4']
    def androidEmulatorParameters = [screenDensity: '240', screenResolution: '480x800', targetAbi: 'x86',
                                     hardwareProperties: ['hw.keyboard': 'yes', 'hw.ramSize': '800', 'vm.heapSize': '128'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    def debugApk = 'Paintroid/build/outputs/apk/Paintroid-debug.apk'
}
