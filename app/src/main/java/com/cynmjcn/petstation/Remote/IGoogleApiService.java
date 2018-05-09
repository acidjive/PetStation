package com.cynmjcn.petstation.Remote;
import com.cynmjcn.petstation.Model.PlaceDetails;
import com.cynmjcn.petstation.Model.vetPlaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;


//INTERFACE
public interface IGoogleApiService {

    //METHODS TO FETCH API

    @GET
    Call<vetPlaces> getNearByPlaces(@Url String url);


    @GET
    Call<PlaceDetails> getPlaceInfo(@Url String url);




}
