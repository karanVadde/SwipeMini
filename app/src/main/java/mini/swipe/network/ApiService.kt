package mini.swipe.network

import io.reactivex.Observable
import mini.swipe.model.PostResponse
import mini.swipe.model.SwipeData
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

const val BASE_URL = "https://app.getswipe.in/api/public/"

private fun initInterceptor(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(initInterceptor())
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

interface ApiService {
    @GET("get")
    fun getData() : Observable<SwipeData>

    @Multipart
    @POST("add")
    fun postData(@Query("product_name") type: String,
                 @Query("product_type") format: String,
                 @Query("price") price: String,
                 @Query("tax") tax: String,
                 @Query("files[]") files: String
    ) : Observable<PostResponse>
}

object ApiInstance{
    val apiInstance : ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}