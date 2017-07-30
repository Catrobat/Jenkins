import groovy.transform.Canonical

@Canonical
class AndroidEmulatorParameters {
    String androidApi = '${ANDROID_VERSION}'
    String screenDensity, screenResolution, targetAbi, noActivityTimeout
    String deviceLocale = 'en_US'
    String sdCardSize = '100M'
    Map hardwareProperties = [:]
    String commandLineOptions = ''
}
