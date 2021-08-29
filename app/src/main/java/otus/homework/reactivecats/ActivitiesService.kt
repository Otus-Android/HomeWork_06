package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.http.GET

interface ActivitiesService {

    @GET("/api/activity/")
    fun getActivity() : Single<ActivityResponse>
}