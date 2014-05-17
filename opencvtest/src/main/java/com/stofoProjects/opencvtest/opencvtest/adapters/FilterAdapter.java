package com.stofoProjects.opencvtest.opencvtest.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stofoProjects.opencvtest.opencvtest.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Martin Stofanak on 8.5.2014.
 */
public class FilterAdapter extends BaseAdapter {

    public static final int NONE = 0;
    public static final int CANNY = 1;
    public static final int SOBEL_HORIZONTAL = 2;
    public static final int SOBEL_VERTICAL = 3;
    public static final int SOBEL_BOTH = 4;
    public static final int SCHARR = 5;

    private String[] mFilters;
    private Context mContext;

    public FilterAdapter(Context context) {
        super();
        mContext = context;
        mFilters = mContext.getResources().getStringArray(R.array.preview_filters);
    }

    @Override
    public int getCount() {
        if(mFilters != null && mFilters.length != 0)
            return mFilters.length;
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mFilters != null && mFilters.length != 0)
            return mFilters[position];
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder holder;
        if(view != null)
            holder = (ViewHolder)view.getTag();
        else {
            view = View.inflate(mContext, R.layout.list_item_filter, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.mFilterName.setText(mFilters[position]);

        return view;
    }



    static class ViewHolder {
        @InjectView(R.id.filter_name)
        TextView mFilterName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
