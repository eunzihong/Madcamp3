package com.app.madcampweek3.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetroApi {

    @Multipart
    @POST("api/torch/inference")
    Call<String> inferenceImage(@Part MultipartBody.Part imageFile);

}
