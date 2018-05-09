package com.cynmjcn.petstation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cynmjcn.petstation.Model.Photos;
import com.cynmjcn.petstation.Model.PlaceDetails;
import com.cynmjcn.petstation.Remote.IGoogleApiService;
import com.squareup.picasso.Picasso;

import java.net.URI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondActivity extends AppCompatActivity {

    ImageView ivPhoto;
    RatingBar rating;
    Button btnMaps;
    TextView placeNameTv;
    TextView addressTv;
    TextView openingHrsTv;

    PlaceDetails myPlaces;

    IGoogleApiService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mService = Common.getGoogleAPIServices();
        rating = (RatingBar)findViewById(R.id.ratingBar);
        btnMaps = (Button)findViewById(R.id.btnShowWeb);
        placeNameTv =(TextView)findViewById(R.id.tvPlaceName);
        addressTv = (TextView)findViewById(R.id.tvAddress);
        openingHrsTv = (TextView)findViewById(R.id.tvOpeningHr);
        ivPhoto =(ImageView)findViewById(R.id.picture);

        openingHrsTv.setText("");
        addressTv.setText("");
        placeNameTv.setText("");


        //URI.Parse ContentProvider what we want to access by reference
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myPlaces.getResult().getUrl()));
                startActivity(intent);
            }
        });


        // ------ LOAD PHOTO ---------
        //Check If image is not null
        if (Common.currentResult.getPhotos() != null && Common.currentResult.getPhotos().length > 0 )
        {
                                 //getPhotos is an array hence im taking the first item in photo reference
            Picasso.with(this).load(getPlaceImage(Common.currentResult.getPhotos()[0]
                    .getPhoto_reference(),1000))
                    .placeholder(R.drawable.vimage)
                    .error(R.drawable.verror)
                    .into(ivPhoto);



        }
        // -------RATING ---------------
        //dont use string.isEmpty bc it returns null/not null
        //!TextUtils will always return boolean
        if (Common.currentResult.getRating() != null && !TextUtils.isEmpty(Common.currentResult.getRating()))
        {
            rating.setRating(Float.parseFloat(Common.currentResult.getRating()));
        }
        else {
            rating.setVisibility(View.GONE);
        }

        // ----Opening Hours
        if (Common.currentResult.getOpening_hours() != null)
        {
            openingHrsTv.setText("Open Now: " + Common.currentResult.getOpening_hours().getOpen_now());
        }
        else {
            openingHrsTv.setVisibility(View.GONE);
        }

        // --------FETCH NAME AND ADDRESS WEBSERVICE

        mService.getPlaceInfo(getPlaceInformationURL(Common.currentResult.getPlace_id())).enqueue(new Callback<PlaceDetails>() {
            @Override
            public void onResponse(Call<PlaceDetails> call, Response<PlaceDetails> response) {
                myPlaces = response.body();

                addressTv.setText(myPlaces.getResult().getFormatted_address());
                placeNameTv.setText(myPlaces.getResult().getName());

            }

            @Override
            public void onFailure(Call<PlaceDetails> call, Throwable t) {

            }
        });





    }

    private String getPlaceInformationURL(String place_id) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json");
        url.append("?placeid=" + place_id);
        url.append("&key=" +getResources().getString(R.string.browser_key));

        return url.toString();


    }


    //gOOGLE API Place photo URL
    private String getPlaceImage(String photo_reference, int maxWidth) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo");
        url.append("?maxwidth="+ maxWidth);
        url.append("&photoreference="+ photo_reference);
        url.append("&key=" +getResources().getString(R.string.browser_key));
        return url.toString();

    }
}
