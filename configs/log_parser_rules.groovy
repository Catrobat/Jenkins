# Ignore Android Emulator plugin related warnings.
# These warnings are partially related to the way the android emulator plugin works.
# As such there is no way to get rid of them.
ok /WARNING:\.\/android\/base\/files\/IniFile\.cpp.*/
ok /WARNING: Force to use classic engine to support snapshot./
ok /WARNING: Classic qemu does not support SMP/
ok /WARNING: Requested adb port \(\d+\) is outside the recommended range/
ok /If you are not using NDK, unset the NDK variable from ANDROID_NDK_HOME or local.properties to remove this warning./

error /(?i)error:/
error /(?i)failed/
warning /(?i)warning:/

start /Cloning the remote Git repository/
start /android list target/
start /\[Gradle\] - Launching build/
