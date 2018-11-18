package com.example.dima.colorrecognition;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DevicesActivity extends AppCompatActivity {

    ListView listDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        Intent intent = getIntent();

        ArrayList<String> names = intent.getStringArrayListExtra("names");
        ArrayList<String> addresses = intent.getStringArrayListExtra("addresses");

        ArrayList<Map<String, Object>> data = new ArrayList<>(names.size());
        Map<String, Object> m;
        for (int i = 0; i < names.size(); i++) {
            m = new HashMap<>();
            m.put("name", names.get(i));
            m.put("address", addresses.get(i));
            data.add(m);
        }

        String[] from = { "name", "address" };
        int[] to = { R.id.deviceName, R.id.deviceAddr };

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.device_item, from, to);

        listDevices = findViewById(R.id.listDevices);
        listDevices.setAdapter(adapter);
        listDevices.setOnItemClickListener(onItemClick);
    }

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView address = view.findViewById(R.id.deviceAddr);

            Intent result = new Intent();
            result.putExtra("address", address.getText().toString());
            setResult(RESULT_OK, result);
            finish();
        }
    };
}
