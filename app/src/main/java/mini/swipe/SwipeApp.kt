package mini.swipe

import android.app.Application
import mini.swipe.kmod.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SwipeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        //
        startKoin{
            androidContext(this@SwipeApp)
            modules(appModule)
        }
    }

}