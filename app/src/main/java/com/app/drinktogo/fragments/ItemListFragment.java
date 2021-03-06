package com.app.drinktogo.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.app.drinktogo.Adapter.ItemAdapter;
import com.app.drinktogo.CheckoutActivity;
import com.app.drinktogo.Entity.Item;
import com.app.drinktogo.MainActivity;
import com.app.drinktogo.PurchaseActivity;
import com.app.drinktogo.R;
import com.app.drinktogo.helper.Ajax;
import com.app.drinktogo.helper.AppConfig;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Ken on 11/01/2017.
 */

public class ItemListFragment extends ListFragment {

    ItemAdapter itemAdapter;
    private int store_id;
    private int user_id;

    FloatingActionButton fab;
    ArrayList<Integer> cart_id;
    ArrayList<String> cart_name;
    ArrayList<Integer> cart_amount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_list, container, false);
        store_id = getArguments().getInt("store_id");
        user_id = getArguments().getInt("user_id");

        fab = (FloatingActionButton) getActivity().findViewById(R.id.done_cart);
        cart_id = new ArrayList<>();
        cart_name = new ArrayList<>();
        cart_amount = new ArrayList<>();

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ItemAdapter.ViewHolder view = (ItemAdapter.ViewHolder) v.getTag();
        final Item i = view.item;

        if(!cart_id.contains(i.id)) {
            cart_id.add(i.id);
            cart_name.add(i.name);
            cart_amount.add(i.qty);
            view.item_cart.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            Toast.makeText(getActivity(), "Added to Cart.", Toast.LENGTH_SHORT).show();
        } else {
            int ind = cart_id.indexOf(i.id);

            cart_id.remove(ind);
            cart_name.remove(ind);
            cart_amount.remove(ind);
            view.item_cart.setColorFilter(Color.WHITE);
            Toast.makeText(getActivity(), "Removed to Cart.", Toast.LENGTH_SHORT).show();
        }

        if(cart_id.size() > 0) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        itemAdapter = new ItemAdapter(getActivity());

        Ajax.get("item/" + store_id, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if(response.length() > 0){
                    for(int x=0;x < response.length(); x++){
                        Item i = new Item();
                        try {
                            JSONObject o = response.getJSONObject(x);
                            i.id = o.getInt("id");
                            i.name = o.getString("name");
                            i.brand = o.getString("brand");
                            i.qty = o.getInt("qty");
                            i.store_id = o.getInt("store_id");
                            itemAdapter.addItem(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setListAdapter(itemAdapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Failed: ", ""+statusCode);
                Log.d("Error : ", "" + throwable);
                AppConfig.showDialog(getActivity(), "Message", "There is problem in your request. Please try again.");
            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CheckoutActivity.class);
                i.putExtra("cart_id", cart_id);
                i.putExtra("cart_name", cart_name);
                i.putExtra("cart_amount", cart_amount);
                i.putExtra("user_id", Integer.toString(user_id));
                i.putExtra("store_id", Integer.toString(store_id));
                startActivity(i);
            }
        });
    }
}
