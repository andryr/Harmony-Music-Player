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

package com.andryr.musicplayer.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Andry on 09/11/15.
 */
public class RecyclerViewUtils {

    public static View inflateChild(LayoutInflater inflater, int layoutId, RecyclerView recyclerView) {
        View view = inflater.inflate(layoutId, recyclerView,
                false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        view.setLayoutParams(recyclerView.getLayoutManager().generateLayoutParams(lp));
        return view;
    }
}
