package otus.homework.reactivecats

import com.google.gson.annotations.SerializedName

data class ActivityResponse(
	@field:SerializedName("activity")
	val activityMessage: String
)