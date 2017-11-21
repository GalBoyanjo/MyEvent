package com.gal.invitation.Utils;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.gal.invitation.R;

/**
 * Created by Gal on 14/10/2017.
 */

public class CustomCheckbox extends android.support.v7.widget.AppCompatCheckBox {

    public CustomCheckbox(Context context) {
        super(context);
        setButtonDrawable(new StateListDrawable());

    }
    @Override
    public void setChecked(boolean t){
        if(t)
        {
            this.setBackgroundResource(R.drawable.checkbox_select);
        }
        else
        {
            this.setBackgroundResource(R.drawable.checkbox_deselect);
        }
        super.setChecked(t);
    }
}
