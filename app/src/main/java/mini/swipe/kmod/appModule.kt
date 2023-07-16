package mini.swipe.kmod

import mini.swipe.DefaultViewModel
import mini.swipe.network.DataRepository
import mini.swipe.network.DataRepositoryImpl
import org.koin.dsl.module

val appModule = module {
    single<DataRepository> { DataRepositoryImpl() }
    factory { DefaultViewModel() }
}
