package com.example.serba.hygenechecker.activities;

import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.serba.hygenechecker.R;
import com.example.serba.hygenechecker.models.DataCache;
import com.example.serba.hygenechecker.models.DatabaseInstance;
import com.example.serba.hygenechecker.models.Establishment;
import com.example.serba.hygenechecker.models.RequestWrapper;
import com.example.serba.hygenechecker.models.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

public class EstablishmentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Establishment currentItem;
    private GoogleMap googleMap;
    private boolean locationAdded = false;
    private ShimmerFrameLayout loadingView;
    private View errorView;
    private ImageView favouriteStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment_details);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        favouriteStar = findViewById(R.id.favourite_star);

        loadingView = findViewById(R.id.loading_view);
        loadingView.startShimmerAnimation();

        errorView = findViewById(R.id.no_results_message);

        RequestWrapper requestWrapper = RequestWrapper.getInstance(getApplicationContext());

        String id = getIntent().getStringExtra(ResultsActivity.ESTABLISHMENT_ID);
        requestWrapper.addJsonObjectRequest(Request.Method.GET, "establishments/" + id, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        Gson gson = new Gson();
                        currentItem = gson.fromJson(response.toString(), Establishment.class);
                        if (currentItem != null) {
                            setTitle(currentItem.getBusinessName());
                            fillViews(currentItem);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                loadingView.stopShimmerAnimation();
                TransitionManager.beginDelayedTransition((ConstraintLayout) findViewById(R.id.main_details_layout));
                findViewById(R.id.details_scroll_view).setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                loadingView.stopShimmerAnimation();
                TransitionManager.beginDelayedTransition((ConstraintLayout) findViewById(R.id.main_details_layout));
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
                Toast.makeText(EstablishmentDetailsActivity.this, getResources().getString(R.string.details_load_error), Toast.LENGTH_SHORT).show();
            }
        });

        favouriteStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DataCache.getInstance().getFavouritesIds().indexOf(currentItem.getFHRSID()) == -1) {
                    DatabaseInstance.getInstance().getDb(getApplicationContext()).establishmentDao().insertEstablishment(currentItem);
                    DataCache.getInstance().addFavouriteId(currentItem.getFHRSID());
                    ((ImageView) view).setImageResource(R.drawable.star_on);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.favourite_add_message), Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseInstance.getInstance().getDb(getApplicationContext()).establishmentDao().deleteEstablishment(currentItem);
                    ((ImageView) view).setImageResource(R.drawable.star_off);
                    DataCache.getInstance().removeFavouriteId(currentItem.getFHRSID());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.favourite_remove_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
    }

    private void fillViews(Establishment currentItem) {
        ((TextView) findViewById(R.id.business_name_tv)).setText(currentItem.getBusinessName());
        ((TextView) findViewById(R.id.business_type_tv)).setText(currentItem.getBusinessType());
        ((TextView) findViewById(R.id.address_first_line_tv)).setText(currentItem.getAddressFirstLine());
        ((TextView) findViewById(R.id.address_second_line_tv)).setText(currentItem.getAddressSecondLine());
        ((TextView) findViewById(R.id.post_code_tv)).setText(currentItem.getPostCode());
        ((TextView) findViewById(R.id.auth_name_tv)).setText(currentItem.getLocalAuthorityName());
        ((TextView) findViewById(R.id.auth_email_tv)).setText(currentItem.getLocalAuthorityEmailAddress());
        ((TextView) findViewById(R.id.auth_website_tv)).setText(currentItem.getLocalAuthorityWebSite());

        if (currentItem.getSchemeType().equals("FHRS")) {
            findViewById(R.id.scores_card_view).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.hygiene_score_label)).setText(String.valueOf(currentItem.getScores().getHygiene()));
            ((TextView) findViewById(R.id.confidence_score_label)).setText(String.valueOf(currentItem.getScores().getConfidenceInManagement()));
            ((TextView) findViewById(R.id.structural_score_label)).setText(String.valueOf(currentItem.getScores().getStructural()));
        }
        try {

            String dateText = Utils.formatDateString(currentItem.getRatingDate(), "yyyy-MM-dd'T'hh:mm:ss", "dd.MM.yyyy");
            ((TextView) findViewById(R.id.rating_date_text_view)).setText(dateText);
        } catch (Exception ex) {
            findViewById(R.id.textView23).setVisibility(View.GONE);
            findViewById(R.id.rating_date_text_view).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.rating_scheme_label)).setText(currentItem.getSchemeType());
        if (currentItem.getSchemeType().equals("FHRS")) {
            try {
                int score = Integer.parseInt(currentItem.getRatingValue());
                ((ImageView) findViewById(R.id.rating_image_view)).setImageResource(getRatingImage(score));
            } catch (NumberFormatException ex) {
            }
        } else {
            findViewById(R.id.rating_image_view).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.fhis_rating_label)).setText(currentItem.getRatingValue());
            findViewById(R.id.fhis_rating_label).setVisibility(View.VISIBLE);
        }

        if (DataCache.getInstance().getFavouritesIds().indexOf(currentItem.getFHRSID()) == -1) {
            favouriteStar.setImageResource(R.drawable.star_off);
        } else {
            favouriteStar.setImageResource(R.drawable.star_on);
        }

        if (currentItem.hasGeocode()) {
            addLocationOnMap();
        } else {
            findViewById(R.id.map_card_view).setVisibility(View.GONE);
        }
    }

    private int getRatingImage(int score) {
        switch (score) {
            case 0:
                return R.drawable.rating_0;
            case 1:
                return R.drawable.rating_1;
            case 2:
                return R.drawable.rating_2;
            case 3:
                return R.drawable.rating_3;
            case 4:
                return R.drawable.rating_4;
            case 5:
                return R.drawable.rating_5;
        }
        return 0;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        addLocationOnMap();
    }

    private void addLocationOnMap() {
        if (googleMap == null || currentItem == null || locationAdded) {
            return;
        }

        locationAdded = true;
        googleMap.addMarker(new MarkerOptions().position(currentItem.getGeocode().toLatLong()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentItem.getGeocode().toLatLong(), 10));
    }
}
