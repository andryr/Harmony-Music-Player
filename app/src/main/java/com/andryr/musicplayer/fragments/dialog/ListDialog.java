package com.andryr.musicplayer.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.andryr.musicplayer.R;


public class ListDialog extends DialogFragment {

    private String mTitle;
    private RecyclerView.Adapter mAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list_dialog, null);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);

        builder.setView(rootView);
        return builder.create();
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    public void setAdapter(RecyclerView.Adapter adapter)
    {
        mAdapter = adapter;
    }
}
