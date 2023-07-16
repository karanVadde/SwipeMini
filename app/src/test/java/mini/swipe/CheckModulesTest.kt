package mini.swipe

import mini.swipe.kmod.appModule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @Test
    fun checkAllModules() {
        //verify if we setup koin correctly.
        appModule.verify()
    }
}