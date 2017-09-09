class PaintroidData {
    def repo = 'https://github.com/Catrobat/Paintroid.git'
    def branch = 'develop'
    def githubUrl = 'https://github.com/Catrobat/Paintroid'
    def androidVersions = 24..18
    def testExclusionsFile = 'testexclusions.txt'
    def testResultsPattern = '**/*TEST*.xml'
    def githubOrganizations = ['Catrobat']
    def pullRequestAdmins = ['thmq', '84n4n4']
    def androidEmulatorParameters = [androidApi: '24', screenDensity: '240', screenResolution: '480x800', targetAbi: 'x86',
                                     noActivityTimeout: '1200',
                                     hardwareProperties: ['hw.keyboard': 'yes', 'hw.ramSize': '800', 'vm.heapSize': '128'],
                                     commandLineOptions: '-no-boot-anim -noaudio -qemu -m 800 -enable-kvm']
    def debugApk = 'Paintroid/build/outputs/apk/Paintroid-debug.apk'
    def excludedTests = ['ActivityOpenedFromPocketCodeNewImageTest', 'ActivityOpenedFromPocketCodeTest',
                         'BitmapIntegrationTest', 'BrushPickerIntegrationTest', 'ButtonTopLayers_RTL_LayoutTest',
                         'ColorDialogIntegrationTest', 'EraserToolIntegrationTest', 'FillToolIntegrationTest',
                         'FlipToolIntegrationTest', 'LandscapeTest', 'LayerIntegrationTest',
                         'MainActivityIntegrationTest', 'MenuFileActivityIntegrationTest', 'MultilingualTest',
                         'RectangleFillToolIntegrationTest', 'RotationToolIntegrationTest',
                         'ScrollingViewIntegrationTest', 'StampToolIntegrationTest', 'TextToolIntegrationTest',
                         'ToolOnBackPressedTests', 'ToolSelectionIntegrationTest', 'TransformToolIntegrationTest',
                         'UndoRedoIntegrationTest'
                        ].collect{"**/$it*"} +
                        ['**/junit/**']
    def staticAnalysisResults = [:]
}
