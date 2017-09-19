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

import model.LocomotiveServer;
import model.LocomotiveServerDAO;

/**
 * Simple adapter for a dropdown list. The adapter will update its content from a railroad server.
 * Items are #LocomotiveServer objects used to control a group of railroad locomotives.
 */
@SuppressWarnings("WeakerAccess")
public class LocomotiveListAdapter extends AbstractJsonArrayAdapter<LocomotiveServer> {

    public LocomotiveListAdapter(@NonNull Context context, String serverURL, RequestQueue requestQueue, Response.ErrorListener errorListener) {
        super(context, serverURL, requestQueue, errorListener);
    }

    @Override
    protected void updateListItems(JSONArray response) {
        List<LocomotiveServer> newItems = new ArrayList<>();

        for (int i = 0; i < response.length(); i++)
            try {
                JSONObject object = response.getJSONObject(i);
                newItems.add(LocomotiveServerDAO.read(object.toString()));
            } catch (Throwable t) {
                Log.e("main", "can not create JSON object", t);
            }

        // remove all older items
        for (int i = 0; i < getCount(); i++) {
            LocomotiveServer item = getItem(i);

            if (!newItems.contains(item)) {
                remove(item);
            }
        }

        // add new items
        for (int i = 0; i < newItems.size(); i++) {
            LocomotiveServer item = newItems.get(i);

            if (getPosition(item) < 0) {
                add(item);
            }
        }
    }

}
