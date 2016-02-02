/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
