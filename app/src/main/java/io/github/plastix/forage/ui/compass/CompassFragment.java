package io.github.plastix.forage.ui.compass;


import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import icepick.State;
import io.github.plastix.forage.ForageApplication;
import io.github.plastix.forage.R;
import io.github.plastix.forage.ui.PresenterFragment;
import io.github.plastix.forage.util.StringUtils;
import io.github.plastix.forage.util.UnitUtils;

/**
 * Fragment that is responsible for the geocache compass.
 */
public class CompassFragment extends PresenterFragment<CompassPresenter, CompassView> implements CompassView {

    private static final String EXTRA_CACHE_LOCATION = "CACHE_LOCATION";
    private static float CENTER = 0.5f;

    @BindView(R.id.compass_arrow)
    ImageView arrow;

    @BindView(R.id.compass_distance)
    TextView distance;

    @BindView(R.id.compass_accuracy)
    TextView accuracy;

    @State
    float currentAzimuth;

    @State
    Location target;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        injectDependencies();
        super.onCreate(savedInstanceState);

        this.target = getArguments().getParcelable(EXTRA_CACHE_LOCATION);

    }

    private void injectDependencies() {
        ForageApplication.getComponent(getContext())
                .plus(new CompassModule(this)).injectTo(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setTargetLocation(target);

    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_compass;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        arrow.setRotation(currentAzimuth);
    }

    @Override
    public void rotateCompass(final float degrees) {
        // Rotate by negative degrees because Android rotates counter-clockwise
        Animation an = new RotateAnimation(
                currentAzimuth,
                -degrees,
                Animation.RELATIVE_TO_SELF,
                CENTER,
                Animation.RELATIVE_TO_SELF,
                CENTER);
        an.setDuration(250);
        an.setInterpolator(new LinearInterpolator());
        an.setFillAfter(true);
        currentAzimuth = -degrees;
        arrow.startAnimation(an);
    }

    @Override
    public void updateDistance(float distanceInMeters) {
        double miles = UnitUtils.metersToMiles(distanceInMeters);
        String formatted = StringUtils.humanReadableImperialDistance(getResources(), miles);

        this.distance.setText(formatted);
    }

    @Override
    public void updateAccuracy(float accuracyInMeters) {
        double miles = UnitUtils.metersToMiles(accuracyInMeters);
        Resources resources = getResources();
        String formatted = StringUtils.humanReadableImperialDistance(resources, miles);

        this.accuracy.setText(resources.getString(R.string.compass_accuracy, formatted));

    }
}
