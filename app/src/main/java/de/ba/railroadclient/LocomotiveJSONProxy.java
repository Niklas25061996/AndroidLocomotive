package de.ba.railroadclient;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import model.Locomotive;
import model.LocomotiveDAO;
import model.LocomotivePOJO;
import model.LocomotiveServer;


/**
 * Proxy to communicate with a web server. The proxy will repeatedly read JSON objects from the
 * server and update itself. After each update
 * {@link LocomotiveJSONProxy.LocomotiveListener#locomotiveChanged(Locomotive)} ({@link Locomotive})}
 * is called. To be notified about updates one should call the constructor with a valid listener.
 */
public class LocomotiveJSONProxy implements Locomotive {

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
     * Simple POJO to store locomotive state
     */
    private Locomotive pojo;

    /**
     * Listener will be inform,ed, if we receive a new object from the server
     */
    private LocomotiveListener locomotiveListener;

    private LocomotiveServer locomotiveServer;

    /**
     * Should be implemented from a listener of server changes. Used as parameter in
     * the constructor.
     */
    public interface LocomotiveListener {

        void locomotiveChanged(Locomotive locomotive);

    }

    /**
     * Create a proxy object which is connected to a server
     *
     * @param requestQueue A request dispatch queue with a thread pool of dispatchers
     * @param errorListener Callback interface for delivering error responses
     * @param locomotiveListener Should be implemented from a listener of server changes
     */
    public LocomotiveJSONProxy(RequestQueue requestQueue, Response.ErrorListener errorListener, LocomotiveListener locomotiveListener) {
        this.pojo = new LocomotivePOJO();

        this.requestQueue = requestQueue;
        this.errorListener = errorListener;
        this.locomotiveListener = locomotiveListener;

        // update the locomotive
        updateHandler = new Handler(Looper.getMainLooper());
        updateHandler.post(updateRunnable);
    }

    public void setLocomotiveServer(LocomotiveServer locomotiveServer) {
        this.locomotiveServer = locomotiveServer;
    }

    /**
     * The Updater will periodically read the current locomotive speed and update the view,
     * e.g. the speed slider and direction buttons
     */
    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            // call this method again in 2 seconds
            updateHandler.postDelayed(updateRunnable, 2000);

            // do we have a server?
            if (locomotiveServer == null) {
                return;
            }

            // connect to this server
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, locomotiveServer.getRestURL(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // read the current locomotive
                    Locomotive currentLocomotive = LocomotiveDAO.read(response.toString());
                    setId(currentLocomotive.getId());
                    LocomotiveDAO.copy(currentLocomotive, pojo);

                    if (locomotiveListener != null) {
                        locomotiveListener.locomotiveChanged(LocomotiveJSONProxy.this);
                    }
                }
            }, errorListener);

            // add the GET action to the request que
            requestQueue.add(getRequest);
        }
    };

    /**
     * The interface defines a method to change a locomotive. An instance can be used
     * as a parameter for {@link #postLocomotive(LocomotiveChanger)}.
     */
    private interface LocomotiveChanger {

        /**
         * @param locomotive Locomotive to change
         */
        void changeLocomotive(Locomotive locomotive);
    }

    /**
     * (1) Read a locomotive from current <code>locomotiveServer</code>
     * (2) Change the locomotive, using the {@link LocomotiveChanger}
     * (3) Write back the changed locomotive to current <code>locomotiveServer</code>
     */
    private void postLocomotive(final LocomotiveChanger locomotiveChanger) {

        // do we have a server?
        if (locomotiveServer == null) {
            return;
        }

        // send changes to this server
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET /* the HTTP method to use*/,
                locomotiveServer.getRestURL() /* URL to fetch the JSON from */,
                null /* A JSONObject to post with the request */,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject getResponse) {
                        // read the current locomotive and increase speed
                        Locomotive locomotive = LocomotiveDAO.read(getResponse.toString());
                        locomotiveChanger.changeLocomotive(locomotive);

                        // get a AbstractJsonArrayAdapter string from it
                        JSONObject jsonObject = LocomotiveDAO.toJSON(locomotive);

                        // debug the current locomotive
                        JsonObjectRequest postRequest = new JsonObjectRequest(
                                Request.Method.PATCH /* the HTTP method to use*/,
                                locomotiveServer.getRestURL() /* URL to fetch the JSON from */,
                                jsonObject /* JSONObject to post with the request */,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject postResponse) {
                                    }
                                } /* Empty listener to receive the JSON response */,
                                errorListener /* Error listener, or null to ignore errors. */);

                        // add the POST action to the request queue
                        requestQueue.add(postRequest);
                    }
                }, errorListener  /* Error listener, or null to ignore errors. */);

        // add the GET action to the request que
        requestQueue.add(getRequest);
    }

    @Override
    public String getId() {
        return pojo.getId();
    }

    @Override
    public void setId(final String id) {
        if (equalStrings(pojo.getId(), id)) {
            return;
        }

        pojo.setId(id);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setId(id);
            }
        });
    }

    @Override
    public int getSpeed() {
        return pojo.getSpeed();
    }

    @Override
    public void setSpeed(final int speed) {
        if (pojo.getSpeed() == speed) {
            return;
        }

        pojo.setSpeed(speed);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setSpeed(speed);
            }
        });
    }

    @Override
    public int getDirection() {
        return pojo.getDirection();
    }

    /**
     * Change the direction of the current movement. This will happen stepwise ist the motor is
     * already running.
     *
     * @param direction Either {@link Locomotive#DIRECTION_BACKWARD} or {@link Locomotive#DIRECTION_FORWARD}
     */
    @Override
    public void setDirection(final int direction) {
        if (direction == pojo.getDirection()) {
            return;
        }

        // store the new direction
        pojo.setDirection(direction);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setDirection(direction);
            }
        });
    }

    /**
     * @return Name of the locomotive
     */
    @Override
    public String getName() {
        return pojo.getName();
    }

    /**
     * @param name Name of the locomotive
     */
    @Override
    public void setName(final String name) {
        if (equalStrings(pojo.getName(), name)) {
            return;
        }

        pojo.setName(name);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setName(name);
            }
        });
    }

    @Override
    public String getNumber() {
        return pojo.getNumber();
    }

    @Override
    public void setNumber(final String number) {
        if (equalStrings(pojo.getNumber(), number)) {
            return;
        }

        pojo.setNumber(number);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setNumber(number);
            }
        });
    }

    @Override
    public boolean isHeadLight() {
        return pojo.isHeadLight();
    }

    @Override
    public void setHeadLight(final boolean headLight) {
        if (pojo.isHeadLight() == headLight) {
            return;
        }

        pojo.setHeadLight(headLight);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setHeadLight(headLight);
            }
        });
    }

    @Override
    public boolean isCabineLighting() {
        return pojo.isCabineLighting();
    }

    @Override
    public void setCabineLighting(final boolean cabineLighting) {
        if (pojo.isCabineLighting() == cabineLighting) {
            return;
        }

        pojo.setCabineLighting(cabineLighting);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setCabineLighting(cabineLighting);
            }
        });
    }

    @Override
    public void setHornSound(final boolean hornSound) {
        if (pojo.isHornSound() == hornSound) {
            return;
        }

        pojo.setHornSound(hornSound);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setHornSound(hornSound);
            }
        });
    }

    @Override
    public boolean isHornSound() {
        return pojo.isHornSound();
    }

    @Override
    public void setDrivingSound(final boolean drivingSound) {
        if (pojo.isDrivingSound() == drivingSound) {
            return;
        }

        pojo.setDrivingSound(drivingSound);
        postLocomotive(new LocomotiveChanger() {
            @Override
            public void changeLocomotive(Locomotive locomotive) {
                locomotive.setDrivingSound(drivingSound);
            }
        });
    }

    @Override
    public boolean isDrivingSound() {
        return pojo.isDrivingSound();
    }

    @Override
    public Locomotive getPOJO() {
        return pojo;
    }

    /**
     * @param s1 String to compare
     * @param s2 String to compare
     * @return true if s1 and s2 are equal
     */
    private boolean equalStrings(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }

        return s1.equals(s2);
    }
}
