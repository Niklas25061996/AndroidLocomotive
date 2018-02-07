package de.ba.railroad.simpleclient;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Button;

import android.net.Uri;

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

        VideoView videoView = (VideoView) findViewById(R.id.stream);
        Uri uri = Uri.parse("http://www.tt-modellbahn-weimar.de/show/videos/beladung.mp4");
        videoView.setVideoURI(uri);
        videoView.start();

        SeekBar seekBar = (SeekBar) findViewById(R.id.speedSlider);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

            int speed = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = progress;
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

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        // change the train once so the ui gets updated
        //locomotive.setDrivingSound(!locomotive.isDrivingSound());
        //locomotive.setDrivingSound(!locomotive.isDrivingSound());

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



        // associate light buttons
        findViewById(R.id.horn).setOnClickListener(v -> locomotive.setHornSound(!locomotive.isHornSound()));

        findViewById(R.id.light).setOnClickListener(v -> locomotive.setHeadLight(!locomotive.isHeadLight()));

        // listener to display errors
        Response.ErrorListener switchErrorListener = error -> {
            TextView errorView = (TextView) MainActivity.this.findViewById(R.id.locomotiveErrors);
            errorView.setText(error.getMessage());
        };

    }

    /**
     * Update the content of all GUI elements with the <code>locomotive</code>
     *
     * @param locomotive locomotive retreived from server
     */
    @Override
    public void locomotiveChanged(Locomotive locomotive) {
        TextView lightView = (TextView) findViewById(R.id.stat2);
        TextView soundView = (TextView) findViewById(R.id.stat4);
        TextView smokeView = (TextView) findViewById(R.id.stat6);
        TextView trainView = (TextView) findViewById(R.id.stat8);
        TextView directionView = (TextView) findViewById(R.id.stat10);
        TextView speedView = (TextView) findViewById(R.id.stat12);

        Switch soundSwitch = (Switch) findViewById(R.id.horn);
        Switch lightSwitch = (Switch) findViewById(R.id.light);
        Switch smokeSwitch = (Switch) findViewById(R.id.smoke);

        Button forward = (Button) findViewById(R.id.forward);
        Button backward = (Button) findViewById(R.id.backward);

        SeekBar speedSlider = (SeekBar) findViewById(R.id.speedSlider);


        if(locomotive.isHeadLight()) {
            lightView.setText("An");
            lightSwitch.setChecked(true);
        } else{
            lightView.setText("Aus");
            lightSwitch.setChecked(false);
        }

        if(locomotive.isHornSound()) {
            soundView.setText("An");
            soundSwitch.setChecked(true);
        } else{
            soundView.setText("Aus");
            soundSwitch.setChecked(false);
        }

        if(locomotive.isCabineLighting()) {
            smokeView.setText("An");
            smokeSwitch.setChecked(true);
        } else{
            smokeView.setText("Aus");
            smokeSwitch.setChecked(false);
        }

        lightView.setText(locomotive.isHeadLight() ? "An" : "Aus");
        soundView.setText(locomotive.isHornSound() ? "An" : "Aus");
        smokeView.setText(locomotive.isCabineLighting() ? "An" : "Aus");
        trainView.setText(locomotive.getName());

        int direction = locomotive.getDirection();
        if(direction == 0) {
            directionView.setText("Vorwärts");
            forward.setTextColor(Color.BLACK);
            backward.setTextColor(Color.GRAY);
        } else if (direction == 1) {
            directionView.setText("Rückwärts");
            forward.setTextColor(Color.GRAY);
            backward.setTextColor(Color.BLACK);
        }

        speedView.setText("" + Math.abs(locomotive.getSpeed()));
    }

    @Override
    public void switchChanged(SwitchGroup switchGroup) {

    }
}
