package de.ba.railroadclient;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

/**
 * Abstract adapter for server communication.
 * The adapter stores items which it got from a web server. The server sends a JSON string where
 * the adapter can read from. This class is abstract, child classes will overwrite
 * {@link #updateListItems(JSONArray)} and decide what to do with the servers response.
 *
 * @param <T>
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractJsonArrayAdapter<T> extends ArrayAdapter<T> {

    /**
     * URL for communication.
     */
    private String serverURL;

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
     * Create an adapter for a drop doen list.
     *
     * @param context
     * @param serverURL
     * @param requestQueue
     * @param errorListener
     */
    public AbstractJsonArrayAdapter(Context context, String serverURL, RequestQueue requestQueue, Response.ErrorListener errorListener) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        this.serverURL = serverURL;
        this.requestQueue = requestQueue;
        this.errorListener = errorListener;

        // update the list of active switch servers
        updateHandler = new Handler(Looper.getMainLooper());
        updateHandler.post(updateRunnable);
    }

    /**
     * This method will be called after the JSON request
     *
     * @param response Contains the items from server as a JSON string
     */
    protected abstract void updateListItems(JSONArray response);

    /**
     * The Updater will periodically read all active locomotive servers and update the selection
     * spinner. The user can select a server from this list to connect to a switch server.
     */
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // call this method again in 10 seconds
            updateHandler.postDelayed(updateRunnable, 10000);

            // get all active locomotive servers
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, serverURL, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    // update the items
                    updateListItems(response);

                    // update the view element
                    notifyDataSetChanged();
                }
            }, errorListener);

            // add the GET action to the request que
            requestQueue.add(getRequest);
        }
    };
}
