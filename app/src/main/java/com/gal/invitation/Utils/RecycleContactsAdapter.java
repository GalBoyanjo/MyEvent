package com.gal.invitation.Utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.gal.invitation.Interfaces.ContactsListCallback;
import com.gal.invitation.Interfaces.UpdateGuestList;
import com.gal.invitation.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Gal on 21/02/2018.
 */

public class RecycleContactsAdapter extends RecyclerView.Adapter<RecycleContactsAdapter.ViewHolder> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Contact> data = new ArrayList<>();
    public ArrayList<Contact> searchData = new ArrayList<>();

    private OnItemClicked onClick;

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public RecycleContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> data){

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data.addAll(data);
        this.searchData.addAll(data);


    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView rowName;
        TextView rowNumber;
        ImageView rowImage;
        ImageView rowCheck;
        LinearLayout row;

        public ViewHolder(View row) {
            super(row);

            this.rowName = row.findViewById(R.id.row_name);
            this.rowNumber = row.findViewById(R.id.row_number);
            this.rowImage = row.findViewById(R.id.row_image);
            this.rowCheck = row.findViewById(R.id.row_check);
            this.row = row.findViewById(R.id.row_container);

        }
    }


    @Override
    public RecycleContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, parent, false);
        ViewHolder viewHolder = new ViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Contact contact = searchData.get(position);


        if(contact.isSelected()) {
            final float[] from = new float[3],
                    to =   new float[3];

            Color.colorToHSV(ContextCompat.getColor(context, android.R.color.background_light), from);
            Color.colorToHSV(ContextCompat.getColor(context, R.color.colorDivider), to);

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
            anim.setDuration(300);                              // for 300 ms

            final float[] hsv  = new float[3];                  // transition color
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    // Transition along each axis of HSV (hue, saturation, value)
                    hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                    hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                    hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                    holder.row.setBackgroundColor(Color.HSVToColor(hsv));
                }
            });

            anim.start();
            holder.rowCheck.setVisibility(View.VISIBLE);
            Animation expand = AnimationUtils.loadAnimation(context, R.anim.expand_logo);
            holder.rowCheck.startAnimation(expand);

        } else {
            final float[] from = new float[3],
                    to =   new float[3];

            Color.colorToHSV(ContextCompat.getColor(context, R.color.colorDivider), from);
            Color.colorToHSV(ContextCompat.getColor(context, android.R.color.background_light), to);

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
            anim.setDuration(300);                              // for 300 ms

            final float[] hsv  = new float[3];                  // transition color
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    // Transition along each axis of HSV (hue, saturation, value)
                    hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                    hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                    hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                    holder.row.setBackgroundColor(Color.HSVToColor(hsv));
                }
            });

            anim.start();


            Animation collapse = AnimationUtils.loadAnimation(context, R.anim.collapse_logo);
            holder.rowCheck.startAnimation(collapse);

            holder.rowCheck.setVisibility(View.GONE);

        }


        holder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact.setSelected(!contact.isSelected());
                if(contact.isSelected()) {
                    final float[] from = new float[3],
                            to =   new float[3];

                    Color.colorToHSV(ContextCompat.getColor(context, android.R.color.background_light), from);
                    Color.colorToHSV(ContextCompat.getColor(context, R.color.colorDivider), to);

                    ValueAnimator anim = ValueAnimator.ofFloat(0, 1, 0, 1);   // animate from 0 to 1
                    anim.setDuration(400);                              // for 300 ms

                    final float[] hsv  = new float[3];                  // transition color
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override public void onAnimationUpdate(ValueAnimator animation) {
                            // Transition along each axis of HSV (hue, saturation, value)
                            hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                            hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                            hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                            holder.row.setBackgroundColor(Color.HSVToColor(hsv));
                        }
                    });

                    anim.start();
                    holder.rowCheck.setVisibility(View.VISIBLE);
                    Animation expand = AnimationUtils.loadAnimation(context, R.anim.expand_logo);
                    holder.rowCheck.startAnimation(expand);

                } else {
                    final float[] from = new float[3],
                            to =   new float[3];

                    Color.colorToHSV(ContextCompat.getColor(context, R.color.colorDivider), from);
                    Color.colorToHSV(ContextCompat.getColor(context, android.R.color.background_light), to);

                    ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
                    anim.setDuration(300);                              // for 300 ms

                    final float[] hsv  = new float[3];                  // transition color
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override public void onAnimationUpdate(ValueAnimator animation) {
                            // Transition along each axis of HSV (hue, saturation, value)
                            hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                            hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                            hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                            holder.row.setBackgroundColor(Color.HSVToColor(hsv));
                        }
                    });

                    anim.start();


                    Animation collapse = AnimationUtils.loadAnimation(context, R.anim.collapse_logo);
                    holder.rowCheck.startAnimation(collapse);

                    holder.rowCheck.setVisibility(View.GONE);

                }
                onClick.onItemClick(position);
//                for(Contact tempContact : searchData) {
//                    if (tempContact.equals(contact)){
//                        tempContact.setSelected(true);
//                    }
//                    else {
//                        tempContact.setSelected(false);
//                    }
//                }
//                contact.setSelected(true);
//                notifyDataSetChanged();
            }
        });



        try {
            holder.rowName.setText(contact.getName());
            holder.rowNumber.setText(contact.getPhone());

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

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }

}
