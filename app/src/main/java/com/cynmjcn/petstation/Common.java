package com.cynmjcn.petstation;

import com.cynmjcn.petstation.Model.Results;
import com.cynmjcn.petstation.Model.vetPlaces;
import com.cynmjcn.petstation.Remote.IGoogleApiService;
import com.cynmjcn.petstation.Remote.RetrofitClient;

import retrofit2.Retrofit;

public class Common {

    //static object to hold PLACES -> Variable for the details
    public static Results currentResult;

    //RETROFIT TO FETCH API
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";


    public static IGoogleApiService getGoogleAPIServices(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleApiService.class);
    }
}
