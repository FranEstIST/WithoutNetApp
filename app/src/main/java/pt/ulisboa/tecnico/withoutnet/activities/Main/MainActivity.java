package pt.ulisboa.tecnico.withoutnet.activities.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.CachedUpdatesActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.DebugActivity;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.withoutnet.db.WithoutNetAppDatabase;

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

        WithoutNetAppDatabase withoutNetAppDatabase = Room
                .databaseBuilder(this.getApplicationContext(), WithoutNetAppDatabase.class, "wn_app_database.db")
                .fallbackToDestructiveMigration()
                .build();

        globalClass.setWithoutNetAppDatabase(withoutNetAppDatabase);

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
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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