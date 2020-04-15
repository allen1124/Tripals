package com.hku.tripals.NotificationService;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAGd3B14E:APA91bFO40RPYxSD5pCx6zuXuDfcDPFcJ5BA4J0BYL8mbin59kbfbeuxybmK1sII9PXWfulJRQgvpxbIhlN_nxYMMP_XSa-H-WW6-x_aS28JSZonG4oWuZC4XwsgE96B-GtnGT4lGeDp"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
