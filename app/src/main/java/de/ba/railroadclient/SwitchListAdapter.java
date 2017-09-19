package de.ba.railroadclient;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import model.SwitchServer;
import model.SwitchServerDAO;

/**
 * Simple adapter for a dropdown list. The adapter will update its content from a railroad server.
 * Items are SwitchServer objects used to control a group of railroad switches.
 */
@SuppressWarnings("WeakerAccess")
public class SwitchListAdapter extends AbstractJsonArrayAdapter<SwitchServer> {

    public SwitchListAdapter(@NonNull Context context, String serverURL, RequestQueue requestQueue, Response.ErrorListener errorListener) {
        super(context, serverURL, requestQueue, errorListener);
    }

    @Override
    protected void updateListItems(JSONArray response) {
        List<SwitchServer> newItems = new ArrayList<>();

        for (int i = 0; i < response.length(); i++)
            try {
                JSONObject object = response.getJSONObject(i);
                newItems.add(SwitchServerDAO.read(object.toString()));

                Log.d("main", "object: " + object.toString());
            } catch (Throwable t) {
                Log.e("main", "can not create JSON object", t);
            }

        // remove all older items
        for (int i = 0; i < getCount(); i++) {
            SwitchServer item = getItem(i);

            if (!newItems.contains(item)) {
                remove(item);
            }
        }

        // add new items
        for (int i = 0; i < newItems.size(); i++) {
            SwitchServer item = newItems.get(i);

            if (getPosition(item) < 0) {
                add(item);
            }
        }
    }
}
