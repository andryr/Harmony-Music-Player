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

package com.andryr.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Andry on 28/10/15.
 */
public abstract class BaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mOnItemClickListener = listener;
    }

    protected void triggerOnItemClickListener(int position, View view)
    {
        if(mOnItemClickListener != null)
        {
            mOnItemClickListener.onItemClick(position, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}
