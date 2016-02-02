package io.github.plastix.forage.ui.cachelist;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.plastix.forage.R;
import io.github.plastix.forage.ui.BaseRetainedFragmentActivity;
import io.github.plastix.forage.util.ActivityUtils;
import io.github.plastix.forage.util.PermissionUtils;

/**
 * Activity that represents the main Geocache list screen of the app. This is a container activity
 * for {@link CacheListFragment}.
 */
public class CacheListActivity extends BaseRetainedFragmentActivity<CacheListFragment> {

    private static final String CACHE_LIST_FRAG = "io.github.plastix.forage.ui.cachelist.cachelistfragment";

    @IdRes
    private static final int CACHE_LIST_FRAME_ID = R.id.cachelist_content_frame;

    private static final int LOCATION_REQUEST_CODE = 0;
    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    @Bind(R.id.cachelist_toolbar)
    Toolbar toolbar;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_list);

        // Inject Butterknife views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
    }

    /**
     * Request location permissions every time the Activity is started. This is important because
     * a user can disable the permission while the activity is running in the background and return
     * to the activity.
     */
    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.requestPermissions(this, LOCATION_REQUEST_CODE, LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (!PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    showPermissionDialog();
                }
            }
        }
    }

    private void showPermissionDialog() {
        buildDialog();

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    private void buildDialog() {
        if (dialog == null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
            dialogBuilder.setMessage(R.string.cachelist_nolocation);
            dialogBuilder.setNegativeButton(R.string.cachelist_exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            final Intent settingsIntent = ActivityUtils.getApplicationSettingsIntent(this);
            dialogBuilder.setPositiveButton(R.string.cachelist_open_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(settingsIntent);
                }
            });
            dialogBuilder.setCancelable(false);
            this.dialog = dialogBuilder.create();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cache_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getFragmentTag() {
        return CACHE_LIST_FRAG;
    }

    @Override
    protected CacheListFragment getFragmentInstance() {
        return new CacheListFragment();
    }

    @Override
    protected int getContainerViewId() {
        return CACHE_LIST_FRAME_ID;
    }
}
