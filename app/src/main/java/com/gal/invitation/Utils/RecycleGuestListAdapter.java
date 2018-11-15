package com.gal.invitation.Utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Interfaces.UpdateGuestList;
import com.gal.invitation.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Gal on 18/02/2018.
 */

public class RecycleGuestListAdapter extends RecyclerView.Adapter<RecycleGuestListAdapter.ViewHolder> {

    private UpdateGuestList updateGuestList;
    private Context context;
    private int layoutResourceId;
    private ArrayList<Contact> data = new ArrayList<>();
    public ArrayList<Contact> searchData = new ArrayList<>();


    public RecycleGuestListAdapter(Context context,  int layoutResourceId, ArrayList<Contact> data,
                                   UpdateGuestList updateGuestList){

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data.addAll(data);
        this.searchData.addAll(data);
        this.updateGuestList = updateGuestList;


    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView rowName;
        TextView rowNumber;
        TextView rowStatus;
        ImageView rowImage;
        ImageView rowEdit;
        ImageView rowDelete;
        LinearLayout row;

        public ViewHolder(View row) {
            super(row);

            this.rowName = row.findViewById(R.id.profile_row_name);
            this.rowNumber = row.findViewById(R.id.profile_row_number);
            this.rowStatus = row.findViewById(R.id.profile_row_status);
            this.rowImage = row.findViewById(R.id.profile_row_image);
            this.rowEdit = row.findViewById(R.id.profile_row_edit);
            this.rowDelete = row.findViewById(R.id.profile_row_remove);
            this.row = row.findViewById(R.id.guest_list_row_container);

        }
    }

    @Override
    public RecycleGuestListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, parent, false);
        ViewHolder viewHolder = new ViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecycleGuestListAdapter.ViewHolder holder, int position) {

        final Contact contact = searchData.get(position);

        holder.row.setOnClickListener(new View.OnClickListener() {
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

            Animation slide = AnimationUtils.loadAnimation(context,R.anim.slide_item);
            holder.rowDelete.startAnimation(slide);
            holder.rowEdit.startAnimation(slide);

//            Animation expand = AnimationUtils.loadAnimation(context,R.anim.expand_item);
//            holder.row.startAnimation(expand);


//            row.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));


        } else {
//            row.setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));

            holder.rowDelete.setVisibility(View.INVISIBLE);
            holder.rowEdit.setVisibility(View.INVISIBLE);
        }

        holder.rowDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGuestList.deleteContact(contact);
                data.remove(contact);
                notifyDataSetChanged();


            }
        });
        holder.rowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGuestList.editContactDialog(contact);
                notifyDataSetChanged();

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
                holder.rowImage.setImageResource(R.mipmap.ic_contact);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
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

    public void add(Contact contact){
        data.add(contact);
    }

    public void remove(Contact contact){
        data.remove(contact);
    }

}
