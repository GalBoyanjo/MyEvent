package com.gal.invitation.Utils;

/**
 * Created by Gal on 11/04/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gal.invitation.Entities.Contact;
import com.gal.invitation.R;

import java.util.ArrayList;
import java.util.Locale;

public class ContactsAdapter extends ArrayAdapter<Contact>{

    private Context context;
    private int layoutResourceId;
    private ArrayList<Contact> data = new ArrayList<>();
    public ArrayList<Contact> searchData = new ArrayList<>();

    public ContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data.addAll(data);
        this.searchData.addAll(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContactHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContactHolder();
            holder.rowName = row.findViewById(R.id.row_name);
            holder.rowNumber = row.findViewById(R.id.row_number);
            holder.rowImage = row.findViewById(R.id.row_image);

            row.setTag(holder);

        } else {
            holder = (ContactHolder) row.getTag();
        }

        Contact contact = searchData.get(position);
        if(contact.isSelected()) {
            row.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));
        } else {
            row.setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));
        }
        try {
            holder.rowName.setText(contact.getName());
            holder.rowNumber.setText(contact.getPhone());

            if (contact.getImage()!=null)
                holder.rowImage.setImageBitmap(contact.getImage());
            else
                holder.rowImage.setImageResource(R.mipmap.ic_launcher);

            return row;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return row;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return searchData.size();
    }

    private static class ContactHolder {
        TextView rowName;
        TextView rowNumber;
        ImageView rowImage;

    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        searchData.clear();
        if (charText.length() == 0) {
            searchData.addAll(data);
        } else {
            for (Contact contact : data) {
                if (contact.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    searchData.add(contact);
                }else if(contact.getPhone().toLowerCase(Locale.getDefault()).contains(charText)){
                    searchData.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }



}