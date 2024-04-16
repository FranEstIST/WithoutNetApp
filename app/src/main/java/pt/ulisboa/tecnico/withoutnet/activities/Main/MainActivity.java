package pt.ulisboa.tecnico.withoutnet.activities.Main;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.CachedUpdatesActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.DebugActivity;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;
import pt.ulisboa.tecnico.withoutnet.services.ble.BleService;
import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;
import pt.ulisboa.tecnico.withoutnet.services.ble.TestService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private GlobalClass globalClass;

    public ActivityMainBinding binding;

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);
        WorkManager.getInstance(getApplicationContext()).cancelAllWork();

        globalClass = (GlobalClass) getApplicationContext();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        /*Drawable drawable = binding.toolbar.getNavigationIcon();
        if(drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.wn_blue_3), PorterDuff.Mode.SRC_ATOP));
        }*/

        DrawerLayout drawerLayout = binding.activityMainDrawerLayout;
        NavigationView navView = binding.navView;

        navController = Navigation.findNavController(this, R.id.navHostFragmentContainerView);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.HomeFragment
                    , R.id.NodesFragment
                    , R.id.NetworksFragment
                    , R.id.SettingsFragment
                    , R.id.DebugFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_start_test_service:
                return true;
            case R.id.action_scan_nodes:
                intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_upload_updates:
                /*new Thread(() -> {
                    GlobalClass globalClass = MainActivity.this.globalClass;
                    for (Node node : globalClass.getAllUpdates().keySet()) {
                        int status = globalClass.getFrontend().sendUpdateToServer(globalClass.getMostRecentUpdate(node));
                        Log.d(TAG, "Upload updates status: " + status);
                    }
                }).start();*/
                return true;
            case R.id.action_download_updates:
                /*new Thread(() -> {
                    GlobalClass globalClass = MainActivity.this.globalClass;
                    for (Node node : globalClass.getAllUpdates().keySet()) {
                        Update update = globalClass.getFrontend().getMostRecentUpdateByNodeFromServer(node);
                        if(update != null) {
                            globalClass.addUpdate(update);
                            Log.d(TAG, "Downloaded update from server: " + update);
                        } else {
                            Log.d(TAG, "No update was found on the server for node: " + node);
                        }
                    }
                }).start();*/
                return true;
            case R.id.action_view_cached_updates:
                intent = new Intent(getApplicationContext(), CachedUpdatesActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.navHostFragmentContainerView);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public Menu getMenu() {
        return menu;
    }
}