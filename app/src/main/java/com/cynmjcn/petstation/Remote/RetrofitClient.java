package com.cynmjcn.petstation.Remote;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseURL)
    {

        //RETROFIT LIBRARY to fetch API
        //Instance which uses the interface and the Builder API which allows defining the URL end point for the HTTP operation.
        if(retrofit == null)
        {
            retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build();

        }
        return retrofit;
    }

}
