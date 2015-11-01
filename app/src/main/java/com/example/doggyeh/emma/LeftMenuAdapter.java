package com.example.doggyeh.emma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//Custom class to implement custom row in ListView for navigation drawer

public class LeftMenuAdapter extends BaseAdapter{
	private Context mContext;
	public static String[] menulist;
	private static int mSelected = 1;
	public static final int[] DRAWABLES = {
			R.drawable.head,
			R.drawable.icon0,
			R.drawable.icon1,
			R.drawable.icon2,
			R.drawable.icon3,
			R.drawable.icon4,
			R.drawable.icon5,
			R.drawable.icon6,
	};


	public LeftMenuAdapter(Context context,String[] list) {
		super();
		this.mContext = context;
		this.menulist = list;
	}
	@Override
	public int getCount() {
		return menulist.length;
	}
	@Override
	public Object getItem(int position) {
		return menulist[position];
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Message message = (Message) this.getItem(position);

		ViewHolder holder = new ViewHolder();
		if(position == 0)
			convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_row_first, parent, false);
		else {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_row, parent, false);
			if (position == mSelected) {
				//iv.setImageResource(getItem(position).iconSelected);
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.selectedItem));
			}
		}
		holder.text = (TextView) convertView.findViewById(R.id.menu_text);
		holder.image = (ImageView) convertView.findViewById(R.id.menu_image);
		convertView.setTag(holder);

		holder.text.setText(menulist[position]);
		holder.image.setBackgroundResource(DRAWABLES[position]);

		return convertView;
	}
	private static class ViewHolder
	{
		TextView text;
		ImageView image;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}
	public void setSelected(int position) {
		this.mSelected = position;
		notifyDataSetChanged();
	}
}
