package com.example.indistant.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAptkeAcs:APA91bEUasvFBsnqiXqtbSogSEReMqfdzyQ-sRCH4ax_A7FnqrmmAiLVy1STEGp8N-yo7O7610m8RFSEMva_2DJSVvI6gu-nhAzVcd_bXCFg9Ui_w9-yE20h0yJUkU240QK52EIY8psb"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
