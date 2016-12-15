package com.home.yassine.taxinow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.home.yassine.taxinow.protocol.SendTaxiRequestMessage;
import com.home.yassine.taxinow.types.TaxiInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yassine on 29/09/2016.
 */
public class TaxiChoiceAdapter extends BaseSwipeAdapter {

    private final ArrayList<TaxiInfo> mTaxiInfos;
    private final TaxiChoiceFragment mFrag;
    private Context mContext;
    private Drawable boy_icon;
    private Drawable girl_icon;
    private SwipeLayout swipeLayout;

    public TaxiChoiceAdapter(Context mContext, ArrayList<TaxiInfo>  taxiInfos, TaxiChoiceFragment frag) {
        this.mContext = mContext;
        this.mTaxiInfos = taxiInfos;
        this.mFrag = frag;
        boy_icon = mContext.getResources().getDrawable(R.drawable.boy_left);
        girl_icon = mContext.getResources().getDrawable(R.drawable.girl_left);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.taxi_choice_item, null);
        swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addSwipeListener(new SimpleSwipeListener());

        return v;
    }

    @Override
    public void fillValues(final int position, final View convertView) {

        swipeLayout = (SwipeLayout)convertView.findViewById(getSwipeLayoutResourceId(position));

        final TaxiChoiceAdapter adapter = this;

        convertView.findViewById(R.id.position).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TaxiInfo taxiInfo = mTaxiInfos.get(position);

                if (taxiInfo == null)
                    return;

                mFrag.showMap(taxiInfo.lat, taxiInfo.lon);
            }
        });

        final ImageView done = (ImageView) convertView.findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TaxiInfo taxiInfo = mTaxiInfos.get(position);

                if (taxiInfo == null)
                    return;

                SendTaxiRequestMessage message = new SendTaxiRequestMessage();
                message.driverId = taxiInfo.driverId;

                try {
                    done.setOnClickListener(null);
                    done.setBackgroundColor(Color.GRAY);

                    JSONObject jsonWriter = new JSONObject();
                    message.pack(jsonWriter);
                    mFrag.hostActivity.mWSClient.send(jsonWriter.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        convertView.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TaxiInfo taxiInfo = mTaxiInfos.get(position);

                if (taxiInfo == null)
                    return;

                adapter.closeAllItems();
                mTaxiInfos.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        TaxiInfo taxiInfo = mTaxiInfos.get(position);

        if (taxiInfo == null)
            return;

        final TextView textView = (TextView) convertView.findViewById(R.id.text_passengers);

        if (taxiInfo.passengers.size() == 0) {
            textView.setText(R.string.taxi_empty);
        }

        TextView textViews[] = new TextView[3];

        textViews[0] = (TextView)convertView.findViewById(R.id.rider1);
        textViews[1] = (TextView)convertView.findViewById(R.id.rider2);
        textViews[2] = (TextView)convertView.findViewById(R.id.rider3);

        for (int i = 0; i < taxiInfo.passengers.size(); i++) {

            if (taxiInfo.passengers.get(i).equals("F")) {
                textViews[i].setBackground(girl_icon);
            }
            else {
                textViews[i].setBackground(boy_icon);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textViews[i].setElevation(10);
            }

            textViews[i].setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getCount() {
        return mTaxiInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaxiInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
