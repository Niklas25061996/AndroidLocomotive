package de.ba.railroadclient;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import model.SwitchGroup;
import model.SwitchGroupDAO;
import model.SwitchGroupPOJO;
import model.SwitchServer;

/**
 * Proxy to communicate with a web server. The proxy will repeatedly read JSON objects from the
 * server and update itself. After each update {@link SwitchListener#switchChanged(SwitchGroup)}
 * is called. To be notified about updates one should call the constructor with a valid listener.
 */
@SuppressWarnings("WeakerAccess")
public class SwitchJSONProxy implements SwitchGroup {

    /**
     * URL for communication.
     */
    private SwitchServer switchServer;

    /**
     * HTTP request que. This queue can be shared with other tasks.
     */
    private RequestQueue requestQueue;

    /**
     * the Update Handler is responsible for calling the update task
     */
    private Handler updateHandler;

    /**
     * Will receive the error messages
     */
    private Response.ErrorListener errorListener;

    /**
     * Simple POJO to store switches state
     */
    private SwitchGroup pojo;

    /**
     * Interface should be implemented from the client. It is used as a parameter
     * for {@link #postSwitch(SwitchChanger)}.
     */
    public interface SwitchListener {

        void switchChanged(SwitchGroup switchGroup);

    }

    /**
     * Listener will be inform,ed, if we receive a new object from the server
     */
    private SwitchListener switchListener;

    /**
     * Create a proxy object which is connected to a server
     *
     * @param requestQueue
     * @param errorListener
     * @param switchListener
     */
    public SwitchJSONProxy(RequestQueue requestQueue, Response.ErrorListener errorListener, SwitchListener switchListener) {
        this.pojo = new SwitchGroupPOJO();

        this.requestQueue = requestQueue;
        this.errorListener = errorListener;
        this.switchListener = switchListener;

        // update the list of active switch servers
        updateHandler = new Handler(Looper.getMainLooper());
        updateHandler.post(updateRunnable);
    }

    public void setSwitchServer(SwitchServer switchServer) {
        this.switchServer = switchServer;
    }

    /**
     * The Updater will periodically read the current switch state and update the view
     */
    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            // call this method again in 1 second
            updateHandler.postDelayed(updateRunnable, 2000);

            // do we have a server?
            if (switchServer == null) {
                return;
            }
            // connect to this server
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, switchServer.getRestURL(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // read the current locomotive
                    SwitchGroup currentSwitchGroup = SwitchGroupDAO.read(response.toString());
                    setId(currentSwitchGroup.getId());
                    SwitchGroupDAO.copy(currentSwitchGroup, pojo);

                    if (switchListener != null) {
                        switchListener.switchChanged(SwitchJSONProxy.this);
                    }

                }
            }, errorListener);

            // add the GET action to the request que
            requestQueue.add(getRequest);
        }
    };

    /**
     * The interface defines a method to change a switch. An instance can be used
     * as a parameter for {@link #postSwitch(SwitchChanger)}.
     */
    private interface SwitchChanger {

        /**
         * @param switchGroup SwitchGroup to change
         */
        void changeSwitch(SwitchGroup switchGroup);
    }

    /**
     * (1) Read a switch group from current <code>switchServer</code>
     * (2) Change the locomotive, using the {@link SwitchChanger}
     * (3) Write back the changed switch group to current <code>switchServer</code>
     */
    private void postSwitch(final SwitchChanger switchChanger) {

        // do we have a server?
        if (switchServer == null) {
            return;
        }

        // send changes to this server
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, switchServer.getRestURL(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // read the current locomotive and increase speed
                SwitchGroup currentSwitch = SwitchGroupDAO.read(response.toString());
                switchChanger.changeSwitch(currentSwitch);

                // get a AbstractJsonArrayAdapter string from it
                JSONObject jsonObject = SwitchGroupDAO.toJSON(currentSwitch);

                // debug the current locomotive
                JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PATCH, switchServer.getRestURL(), jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // empty listener
                    }
                }, errorListener);

                // add the POST action to the request queue
                requestQueue.add(postRequest);
            }
        }, errorListener);

        // add the GET action to the request que
        requestQueue.add(getRequest);
    }

    @Override
    public int getSwitchTrack1() {
        return pojo.getSwitchTrack1();
    }

    @Override
    public void setSwitchTrack1(final int track) {
        if (pojo.getSwitchTrack1() == track) {
            return;
        }

        pojo.setSwitchTrack1(track);
        postSwitch(new SwitchChanger() {
            @Override
            public void changeSwitch(SwitchGroup switchGroup) {
                switchGroup.setSwitchTrack1(track);
            }
        });
    }

    @Override
    public int getSwitchTrack2() {
        return pojo.getSwitchTrack2();
    }

    @Override
    public void setSwitchTrack2(final int track) {
        if (pojo.getSwitchTrack2() == track) {
            return;
        }

        pojo.setSwitchTrack2(track);
        postSwitch(new SwitchChanger() {
            @Override
            public void changeSwitch(SwitchGroup switchGroup) {
                switchGroup.setSwitchTrack2(track);
            }
        });
    }

    @Override
    public int getSwitchTrack3() {
        return pojo.getSwitchTrack3();
    }

    @Override
    public void setSwitchTrack3(final int track) {
        if (pojo.getSwitchTrack3() == track) {
            return;
        }

        pojo.setSwitchTrack3(track);
        postSwitch(new SwitchChanger() {
            @Override
            public void changeSwitch(SwitchGroup switchGroup) {
                switchGroup.setSwitchTrack3(track);
            }
        });
    }

    @Override
    public int getSwitchTrack4() {
        return pojo.getSwitchTrack4();
    }

    @Override
    public void setSwitchTrack4(final int track) {
        if (pojo.getSwitchTrack4() == track) {
            return;
        }

        pojo.setSwitchTrack4(track);
        postSwitch(new SwitchChanger() {
            @Override
            public void changeSwitch(SwitchGroup switchGroup) {
                switchGroup.setSwitchTrack4(track);
            }
        });
    }

    @Override
    public SwitchGroup getPOJO() {
        return pojo;
    }

    @Override
    public void setId(String id) {
        pojo.setId(id);
    }

    @Override
    public String getId() {
        return pojo.getId();
    }
}
