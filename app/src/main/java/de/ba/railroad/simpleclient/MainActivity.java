package de.ba.railroad.simpleclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import de.ba.railroadclient.LocomotiveJSONProxy;
import de.ba.railroadclient.LocomotiveListAdapter;
import de.ba.railroadclient.SwitchJSONProxy;
import de.ba.railroadclient.SwitchListAdapter;
import model.Locomotive;
import model.LocomotiveServer;
import model.SwitchGroup;
import model.SwitchServer;

public class MainActivity extends AppCompatActivity implements LocomotiveJSONProxy.LocomotiveListener, SwitchJSONProxy.SwitchListener {

    /**
     * Locomotive to display
     */
    private LocomotiveJSONProxy locomotive = null;

    /**
     * URL of the RailroadServlet. This servlet knows all active LocomotiveServers
     */
    private static String RAILROAD_SERVER = "http://dv-git01.dv.ba-dresden.local:8095/locomotive";

    /**
     * URL of the RailroadServlet. This servlet knows all active LocomotiveServers
     */
    private static String SWITCH_SERVER = "http://dv-git01.dv.ba-dresden.local:8095/switch";

    private SwitchJSONProxy switchGroup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // listener to display errors
        Response.ErrorListener locomotiveErrorListener = error -> {
            TextView errorView = (TextView) MainActivity.this.findViewById(R.id.locomotiveErrors);
            errorView.setText(error.getMessage());
        };

        // create a request que for HTTP POST and GET
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Adapter for the locomotiveSpinner view element. If we add or remove a LocomotiveServer
        // here, the view will be updated and the user can select this server to control a locomotive
        LocomotiveListAdapter adapter = new LocomotiveListAdapter(this, RAILROAD_SERVER, requestQueue, locomotiveErrorListener);
        Spinner locomotiveSpinner = (Spinner) findViewById(R.id.locomotiveSpinner);
        locomotiveSpinner.setAdapter(adapter);

        locomotive = new LocomotiveJSONProxy(requestQueue, locomotiveErrorListener, this);
        locomotive.setLocomotiveServer(null);

        locomotiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // get the current locomotive server
                LocomotiveServer locomotiveServer = (LocomotiveServer) parent.getAdapter().getItem(position);
                locomotive.setLocomotiveServer(locomotiveServer);

                TextView errorView = (TextView) MainActivity.this.findViewById(R.id.locomotiveErrors);
                errorView.setText(locomotiveServer.getRestURL());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("main", "nothing selected");
                locomotive.setLocomotiveServer(null);
            }
        });

        // associate direction buttons with their action methods
        findViewById(R.id.forward).setOnClickListener(v -> locomotive.setDirection(Locomotive.DIRECTION_FORWARD));

        findViewById(R.id.backward).setOnClickListener(v -> locomotive.setDirection(Locomotive.DIRECTION_BACKWARD));

        findViewById(R.id.stop).setOnClickListener(v -> locomotive.setSpeed(0));

        SeekBar seekBar = findViewById(R.id.speedSlider);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int progress = seekBar.getProgress();
        tvProgressLabel = findViewById(R.id.sliderText);
        tvProgressLabel.setText("Progress: " + progress);

        findViewById(R.id.send).setOnClickListener(v -> {
            TextView speedView = (TextView) MainActivity.this.findViewById(R.id.speed);
            int speed = Integer.parseInt(String.valueOf(speedView.getText()));
            locomotive.setSpeed(speed);
        });

        // associate light buttons
        findViewById(R.id.horn).setOnClickListener(v -> locomotive.setHornSound(!locomotive.isHornSound()));

        // listener to display errors
        Response.ErrorListener switchErrorListener = error -> {
            TextView errorView = (TextView) MainActivity.this.findViewById(R.id.locomotiveErrors);
            errorView.setText(error.getMessage());
        };

        // Adapter for the switchSpinner view element. If we add or remove a SwitchServer
        // here, the view will be updated and the user can select this server to control a switch group.
        SwitchListAdapter switchListAdapter = new SwitchListAdapter(this, SWITCH_SERVER, requestQueue, switchErrorListener);
        Spinner switchSpinner = (Spinner) findViewById(R.id.switchSpinner);
        switchSpinner.setAdapter(switchListAdapter);

        switchGroup = new SwitchJSONProxy(requestQueue, switchErrorListener, this);
        switchGroup.setSwitchServer(null);

        switchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // get the current locomotive server
                SwitchServer switchServer = (SwitchServer) parent.getAdapter().getItem(position);
                switchGroup.setSwitchServer(switchServer);

                TextView errorView = (TextView) MainActivity.this.findViewById(R.id.switchErrors);
                errorView.setText(switchServer.getRestURL());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("main", "nothing selected");
                switchGroup.setSwitchServer(null);
            }
        });

        findViewById(R.id.sw1).setOnClickListener(v -> {
            if (switchGroup.getSwitchTrack1() == SwitchGroup.TRACK_STRAIGHT) {
                switchGroup.setSwitchTrack1(SwitchGroup.TRACK_DIVERGING);
            } else {
                switchGroup.setSwitchTrack1(SwitchGroup.TRACK_STRAIGHT);
            }
        });

        findViewById(R.id.sw2).setOnClickListener(v -> {
            if (switchGroup.getSwitchTrack2() == SwitchGroup.TRACK_STRAIGHT) {
                switchGroup.setSwitchTrack2(SwitchGroup.TRACK_DIVERGING);
            } else {
                switchGroup.setSwitchTrack2(SwitchGroup.TRACK_STRAIGHT);
            }
        });

        findViewById(R.id.sw3).setOnClickListener(v -> {
            if (switchGroup.getSwitchTrack3() == SwitchGroup.TRACK_STRAIGHT) {
                switchGroup.setSwitchTrack3(SwitchGroup.TRACK_DIVERGING);
            } else {
                switchGroup.setSwitchTrack3(SwitchGroup.TRACK_STRAIGHT);
            }
        });

        findViewById(R.id.sw4).setOnClickListener(v -> {
            if (switchGroup.getSwitchTrack4() == SwitchGroup.TRACK_STRAIGHT) {
                switchGroup.setSwitchTrack4(SwitchGroup.TRACK_DIVERGING);
            } else {
                switchGroup.setSwitchTrack4(SwitchGroup.TRACK_STRAIGHT);
            }
        });
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
            tvProgressLabel.setText("Speed " + speed);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            locomotive.setSpeed(speed);
            // called after the user finishes moving the SeekBar
        }
    };

    /**
     * Update the content of all GUI elements with the <code>locomotive</code>
     *
     * @param locomotive locomotive retreived from server
     */
    @Override
    public void locomotiveChanged(Locomotive locomotive) {
        // visualize current speed
        // EditText speedView = (EditText) MainActivity.this.findViewById(R.id.speed);
        // speedView.setText("" + Math.abs(locomotive.getSpeed()));
    }

    @Override
    public void switchChanged(SwitchGroup switchGroup) {

    }
}
