package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET

interface ActivitiesService {

    @GET("/api/activity/")
    fun getActivity() : Call<ActivityResponse>
}