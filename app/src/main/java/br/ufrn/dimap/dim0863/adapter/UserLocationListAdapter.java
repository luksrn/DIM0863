package br.ufrn.dimap.dim0863.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.domain.Location;
import br.ufrn.dimap.dim0863.util.DateUtil;


public class UserLocationListAdapter extends ArrayAdapter<Location> {

    private final List<Location> userLocationList;
    private final LayoutInflater layoutInflater;
    private final int viewResourceId;


    public UserLocationListAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<Location> userLocationList) {
        super(context, textViewResourceId, userLocationList);

        this.userLocationList = userLocationList;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.viewResourceId = textViewResourceId;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = this.layoutInflater.inflate(viewResourceId, null);
        }

        Location userLocation = userLocationList.get(position);

        if(userLocation != null) {
            TextView tvLatLng = convertView.findViewById(R.id.tv_user_location_lat_lng);
            TextView tvDate = convertView.findViewById(R.id.tv_user_location_date);

            String latLng = String.format("(%.3f, %.3f)", userLocation.getLat(), userLocation.getLon());
            String date = DateUtil.convertToString(userLocation.getDate());

            tvLatLng.setText(latLng);
            tvDate.setText(date);
        }
        return convertView;
    }
}
