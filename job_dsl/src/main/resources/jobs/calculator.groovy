folder("Experimental")

def calculator(String job_name, Closure closure) {
    new AndroidJobBuilder(job(job_name), new CalculatorData()).make(closure)
}

calculator('Experimental/Calculator-Nightly-DSL-Generated') {
    htmlDescription(['Nightly Calculator Job. Created by DSL-Seed Job for testing purposes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator(androidApi: 22)

    //Build Actions
    gradle('clean')
    gradle('test')
    gradle('assembleDebug')

    //Post-Build Actions
    //uploadApkToFilesCatrobat()
    //junit()
}

calculator('Experimental/Calculator-Nightly-DSL-Generated-With-Exclusions') {
    htmlDescription(['Nightly Calculator Job. Created by DSL-Seed Job for testing purposes.'])

    jenkinsUsersPermissions(Permission.JobRead)

    git()
    nightly()
    androidEmulator(androidApi: 22)

    excludeTestClass('catrobat.calculator.test.CalculationsTest')

    //Build Actions
    gradle('clean')
    gradle('test')
    gradle('assembleDebug')

    //Post-Build Actions
    //uploadApkToFilesCatrobat()
    //junit()
}
