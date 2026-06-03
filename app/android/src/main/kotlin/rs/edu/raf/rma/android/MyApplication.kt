package rs.edu.raf.rma.android

import android.app.Application
import android.util.Log
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import rs.edu.raf.rma.AppContextHolder
import rs.edu.raf.rma.di.initKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this.applicationContext)
        Napier.base(DebugAntilog())
        Log.d("Test", "App:onCreate()")
        initKoin {
            androidContext(this@MyApplication)
        }
    }
}
