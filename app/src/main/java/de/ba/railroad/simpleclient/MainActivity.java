package de.ba.railroad.simpleclient;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.Button;
import android.os.SystemClock;

import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import de.ba.railroadclient.LocomotiveJSONProxy;
import de.ba.railroadclient.LocomotiveListAdapter;
import de.ba.railroadclient.SwitchJSONProxy;
import model.Locomotive;
import model.LocomotiveServer;
import model.SwitchGroup;

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


    private ArrayList<View> CodeEntry = new ArrayList<View>();
    private ArrayList<View> UIElements = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the two UI parts, code entry and the control UI get added to different lists so their
        // visibility can be toggled
        CodeEntry.add(findViewById(R.id.locomotiveSpinner));
        CodeEntry.add(findViewById(R.id.codeInput));
        CodeEntry.add(findViewById(R.id.codeButton));

        UIElements.add(findViewById(R.id.Zeit));
        UIElements.add(findViewById(R.id.timer));
        UIElements.add(findViewById(R.id.speedSlider));
        UIElements.add(findViewById(R.id.min));
        UIElements.add(findViewById(R.id.Max));
        UIElements.add(findViewById(R.id.direction));
        UIElements.add(findViewById(R.id.switches));
        UIElements.add(findViewById(R.id.stats));

        // hide most of the UI until code is entered
        for (View element : UIElements) {
            element.setVisibility(View.INVISIBLE);
        }

        setListener();
        setTimerListerner();



        // listener to display errors
        Response.ErrorListener locomotiveErrorListener = error -> {
            Context context = getApplicationContext();
            String text = "Fehler: " + error.toString();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            // toast.show();
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

                Context context = getApplicationContext();
                CharSequence text = "Verbunden mit" + locomotiveServer.getRestURL();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                setVideo((VideoView) findViewById(R.id.stream));


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("main", "nothing selected");
                locomotive.setLocomotiveServer(null);
            }
        });

        // associate direction buttons with their action methods


        // listener to display errors
        Response.ErrorListener switchErrorListener = error -> {
            // not implemented
        };

    }

    /**
     *
     * @param timer
     * @param time time in seconds
     */
    private void setTimer(Chronometer timer ,int time) {
        timer.setBase(SystemClock.elapsedRealtime() + time * 1000);
        timer.start();
    }

    /**
     *
     * @param videoView
     */
    private void setVideo(VideoView videoView) {
        Uri uri = Uri.parse("http://www.tt-modellbahn-weimar.de/show/videos/beladung.mp4");
        videoView.setVideoURI(uri);
        videoView.start();
    }

    /**
     *
     */
    private void setListener() {
        findViewById(R.id.forward).setOnClickListener(v -> locomotive.setDirection(Locomotive.DIRECTION_FORWARD));

        findViewById(R.id.backward).setOnClickListener(v -> locomotive.setDirection(Locomotive.DIRECTION_BACKWARD));

        findViewById(R.id.horn).setOnClickListener(v -> locomotive.setHornSound(!locomotive.isHornSound()));

        findViewById(R.id.sound).setOnClickListener(v -> locomotive.setDrivingSound(!locomotive.isDrivingSound()));

        findViewById(R.id.light).setOnClickListener(v -> locomotive.setHeadLight(!locomotive.isHeadLight()));

        findViewById(R.id.smoke).setOnClickListener(v -> locomotive.setCabineLighting(!locomotive.isCabineLighting()));

        findViewById(R.id.codeButton).setOnClickListener(v -> {
            EditText input = (EditText) findViewById(R.id.codeInput);
            Chronometer timer = (Chronometer) findViewById(R.id.timer);
            if (!TextUtils.isEmpty(input.getText())) {
                setTimer(timer, Integer.parseInt(input.getText().toString()));

                // toggle all view elements
                for (View element : UIElements) {
                    element.setVisibility(View.VISIBLE);
                }
                for (View element : CodeEntry) {
                    element.setVisibility(View.INVISIBLE);
                }
            }

        });


        SeekBar seekBar = (SeekBar) findViewById(R.id.speedSlider);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    int speed = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            speed = progress;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        locomotive.setSpeed((speed));
                    }
                }
        );
    }

    private void setTimerListerner() {
        Chronometer timer = (Chronometer) findViewById(R.id.timer);
        timer.setCountDown(true);

        timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
               @Override
               public void onChronometerTick(Chronometer chronometer) {
                   // toggle the UI when the timer reaches 0
                   if(Math.floor((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000) == 0.0){

                       // set speed to 0 when user loses control
                       locomotive.setSpeed(0);

                       for (View element : UIElements) {
                           element.setVisibility(View.INVISIBLE);
                       }

                       for (View element : CodeEntry) {
                           element.setVisibility(View.VISIBLE);
                       }
                   }
               }
        });
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

        Switch hornSwitch = (Switch) findViewById(R.id.horn);
        Switch soundSwitch = (Switch) findViewById(R.id.sound);
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
            hornSwitch.setChecked(true);
        } else{
            soundView.setText("Aus");
            hornSwitch.setChecked(false);
        }

        if(locomotive.isDrivingSound()) {
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

        speedSlider.setProgress(locomotive.getSpeed());
        speedView.setText("" + Math.abs(locomotive.getSpeed()));
    }

    @Override
    public void switchChanged(SwitchGroup switchGroup) {
        // not implemented
    }
}
