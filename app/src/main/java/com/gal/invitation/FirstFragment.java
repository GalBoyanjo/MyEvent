package com.gal.invitation;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment implements View.OnClickListener {


    public FirstFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_first, container, false);

        Button btn1 = (Button)v.findViewById(R.id.btn1);
        Button btn2 = (Button)v.findViewById(R.id.btn2);


        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn1:
                Toast.makeText(getActivity(), "Button 1 clicked", Toast.LENGTH_SHORT).show();

                break;
            case R.id.btn2:
                //Toast.makeText(getActivity(), "Button 2 clicked", Toast.LENGTH_SHORT).show();



                break;
        }
    }
}
