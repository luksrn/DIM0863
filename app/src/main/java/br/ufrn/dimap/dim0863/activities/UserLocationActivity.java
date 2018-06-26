package br.ufrn.dimap.dim0863.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.adapter.UserLocationListAdapter;
import br.ufrn.dimap.dim0863.dao.UserLocationDao;
import br.ufrn.dimap.dim0863.domain.Location;
import br.ufrn.dimap.dim0863.util.Session;


public class UserLocationActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private ListView lvUserLocation;
    private List<Location> userLocationList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);

        Button btnAddUserLocation = findViewById(R.id.btn_add_user_location);
        Button btnListUserLocation = findViewById(R.id.btn_list_user_location);

        btnAddUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserLocation();
            }
        });

        btnListUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUserLocationList();
            }
        });

        userLocationList = new ArrayList<>();
        lvUserLocation = findViewById(R.id.lv_user_location);
        lvUserLocation.setOnItemLongClickListener(this);
    }

    private void addUserLocation() {
        Random random = new Random();
        Location userLocation = new Location(new Date(), random.nextDouble(), random.nextDouble());

        Session session = new Session(this);
        String username = session.getusename();

        Uri uri = UserLocationDao.getInstance().add(getContentResolver(), username, userLocation);
        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
    }

    private void loadUserLocationList() {
        Session session = new Session(this);
        String username = session.getusename();

        UserLocationActivity.this.userLocationList = UserLocationDao.getInstance().findByLogin(getContentResolver(), username);
        UserLocationListAdapter userLocationListAdapter = new UserLocationListAdapter(UserLocationActivity.this, R.layout.view_user_location, UserLocationActivity.this.userLocationList);
        lvUserLocation.setAdapter(userLocationListAdapter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Location userLocation = userLocationList.get(i);
        UserLocationDao.getInstance().remove(getContentResolver(), userLocation);

        loadUserLocationList();

        return false;
    }

}
