package io.github.plastix.forage.ui.cachedetail;


import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.plastix.forage.ForageApplication;
import io.github.plastix.forage.R;
import io.github.plastix.forage.data.local.Cache;
import io.github.plastix.forage.util.StringUtils;


/**
 * Fragment that is responsible for showing the Geocache detail UI.
 */
public class CacheDetailFragment extends Fragment implements CacheDetailView, AppBarLayout.OnOffsetChangedListener {

    private static final String EXTRA_CACHE_CODE = "CACHE_CODE";
    private static final int PERCENTAGE_TO_HIDE_FAB = 40;

    @Bind(R.id.cachedetail_appbar)
    AppBarLayout appBarLayout;

    @Bind(R.id.cachedetail_toolbar)
    Toolbar toolbar;

    @Bind(R.id.cachedetail_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.cachedetail_fab)
    FloatingActionButton fab;

    @Bind(R.id.cachedetail_map)
    MapView map;

    @Bind(R.id.cachedetail_description)
    TextView description;

    @Bind(R.id.cachedetail_difficulty)
    TextView difficulty;

    @Bind(R.id.cachedetail_terrain)
    TextView terrain;

    @Inject
    CacheDetailPresenter presenter;

    @Inject
    Resources resources;

    private String cacheCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        this.cacheCode = getArguments().getString(EXTRA_CACHE_CODE);

        injectDependencies();
    }

    private void injectDependencies() {
        ForageApplication.getComponent(getContext())
                .plus(new CacheDetailModule(this)).injectTo(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cache_detail, container, false);

        // Inject Butterknife views
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActivityActionBar();

        setupFab();

        // TODO Hacky fix for Google Maps
        map.onCreate(null);
        map.setVisibility(View.GONE);

        presenter.getGeocache(cacheCode);
    }

    private void setActivityActionBar() {
        AppCompatActivity parent = ((AppCompatActivity) getActivity());
        parent.setSupportActionBar(toolbar);

        parent.getDelegate().getSupportActionBar().setDisplayShowHomeEnabled(true);
        parent.getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupFab() {
        appBarLayout.addOnOffsetChangedListener(this);

        fab.setImageDrawable(new IconicsDrawable(getContext(), CommunityMaterial.Icon.cmd_compass).color(Color.WHITE));
        // TODO Fab clicks
    }

    @Override
    public void returnedGeocache(Cache cache) {
        collapsingToolbarLayout.setTitle(cache.getName());
        description.setText(cache.getDescription());

        difficulty.setText(resources.getString(R.string.cachedetail_difficulty, cache.getDifficulty()));
        terrain.setText(resources.getString(R.string.cachedetail_terrain, cache.getTerrain()));

        MapListener mapListener = new MapListener(cache.getLocation());
        map.getMapAsync(mapListener);
    }

    @Override
    public void onError() {
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Callback to animate the FAB in and out depending on the scroll
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int currentScrollPercentage = (Math.abs(i)) * 100 / appBarLayout.getTotalScrollRange();

        if (currentScrollPercentage >= PERCENTAGE_TO_HIDE_FAB) {
            fab.hide();
        } else {
            fab.show();
        }
    }

    /**
     * Class to encapsulate Google Map config logic.
     */
    private class MapListener implements OnMapReadyCallback {

        private Location location;

        public MapListener(String location) {
            this.location = StringUtils.stringToLocation(location);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // Add marker for geocache and move camera
            LatLng markerPos = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(markerPos));

            CameraPosition camerPosition = new CameraPosition.Builder()
                    .target(markerPos)
                    .zoom(13).build();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camerPosition));

            animateIntoView();
        }

        private void animateIntoView() {
            map.setVisibility(View.VISIBLE);
            AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(500);
            map.startAnimation(animation);
        }

    }

}
