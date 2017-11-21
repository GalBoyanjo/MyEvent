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
import com.gal.invitation.Interfaces.UpdateProfileContacts;
import com.gal.invitation.R;
import com.gal.invitation.Screens.Profile;

import java.util.ArrayList;
import java.util.Locale;

public class ProfileContactsAdapter extends ArrayAdapter<Contact> {

    private UpdateProfileContacts updateProfileContacts;
    private Context context;
    private int layoutResourceId;
    private ArrayList<Contact> data = new ArrayList<>();
    public ArrayList<Contact> searchData = new ArrayList<>();

    public ProfileContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> data,
                                  UpdateProfileContacts updateProfileContacts) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data.addAll(data);
        this.searchData.addAll(data);
        this.updateProfileContacts = updateProfileContacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContactHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContactHolder();
            holder.rowName = (TextView) row.findViewById(R.id.profile_row_name);
            holder.rowNumber = (TextView) row.findViewById(R.id.profile_row_number);
            holder.rowStatus = (TextView)row.findViewById(R.id.profile_row_status);
            holder.rowImage = (ImageView) row.findViewById(R.id.profile_row_image);
            holder.rowEdit = (ImageView) row.findViewById(R.id.profile_row_edit);
            holder.rowDelete = (ImageView) row.findViewById(R.id.profile_row_remove);

            row.setTag(holder);

        } else {
            holder = (ContactHolder) row.getTag();
        }


        final Contact contact = searchData.get(position);

     row.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             for(Contact tempContact : searchData){
                 if (tempContact.equals(contact))
                     tempContact.setSelected(true);
                 else
                     tempContact.setSelected(false);
             }
             contact.setSelected(true);
             notifyDataSetChanged();
         }
     });
        if(contact.isSelected()) {

            holder.rowDelete.setVisibility(View.VISIBLE);
            holder.rowEdit.setVisibility(View.VISIBLE);
            row.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));


        } else {
            row.setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));

            holder.rowDelete.setVisibility(View.INVISIBLE);
            holder.rowEdit.setVisibility(View.INVISIBLE);
        }

        holder.rowDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileContacts.deleteContact(contact);

            }
        });
        holder.rowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileContacts.editContactDialog(contact);
            }
        });

        try {
            holder.rowName.setText(contact.getName());
            holder.rowNumber.setText(contact.getPhone());
            switch (contact.getStatus()){
                case -1:
                    holder.rowStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.yellow_circle));
                    break;
                case 0:
                    holder.rowStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.red_circle));
                    break;
                default:
                    holder.rowStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.green_circle));
//                    holder.rowStatus.setText(String.valueOf(contact.getStatus()));
                    break;
            }

            if(contact.getStatus()>0)
                holder.rowStatus.setText(String.valueOf(contact.getStatus()));
            else
                holder.rowStatus.setText("");




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
        return position;
    }

    private static class ContactHolder {
        TextView rowName;
        TextView rowNumber;
        TextView rowStatus;
        ImageView rowImage;
        ImageView rowEdit;
        ImageView rowDelete;

    }
    @Override
    public int getCount() {
        return searchData.size();
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        searchData.clear();

        switch (charText) {
            case "yes":
                searchData.clear();
                for (Contact contact : data) {
                    if (contact.getStatus() >0 )
                        searchData.add(contact);
                }
                break;
            case "no":
                for (Contact contact : data) {
                    if (contact.getStatus() == 0) {
                        searchData.add(contact);
                    } else if (contact.getStatus() != 0) {
                        searchData.remove(contact);
                    }
                }
                break;
            case "maybe":
                for (Contact contact : data) {
                    if (contact.getStatus() < 0) {
                        searchData.add(contact);
                    } else if (contact.getStatus() >= 0) {
                        searchData.remove(contact);
                    }
                }
                break;
            case "all":
                searchData.addAll(data);
                break;
            default:
                searchData.addAll(data);
                break;

        }



        notifyDataSetChanged();
    }


}