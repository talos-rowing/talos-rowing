/*
 * Copyright (c) 2024 Tal Shalif
 *
 * This file is part of Talos-Rowing.
 *
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.android.app.roll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.nargila.robostroke.android.app.R;
import org.nargila.robostroke.common.Pair;

class RollView extends FrameLayout {

    enum ValueType {
        MAX,
        AVG,
        CUR
    }

    enum Mode {
        SMALL,
        BIG
    }

    private final Pair<ValueType, ValueType> rollValueTypes;

    private Mode mode = Mode.SMALL;

    private RollViewlet primaryView;
    private RollViewlet secondaryView;

    private TextView type_text;

    RollView(Context context, Pair<ValueType, ValueType> rollValueTypes) {
        super(context);

        this.rollValueTypes = rollValueTypes;
    }

    void setup(LayoutInflater inflater) {
        View layout = inflater.inflate(R.layout.roll_view2, null);
        addView(layout, new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        this.type_text = (TextView) layout.findViewById(R.id.roll_type_lable);
        this.primaryView = new RollViewlet(getContext(), layout.findViewById(R.id.roll_primary_view));
        this.secondaryView = new RollViewlet(getContext(), layout.findViewById(R.id.roll_secondary_view));
    }


    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {

        if (rollValueTypes.second == null) { // CURRENT roll does not have a secondary roll value to display
            mode = Mode.SMALL;
        }

        secondaryView.hide(mode != Mode.BIG);

        switch (mode) {
            case BIG:
                primaryView.setLabel(rollValueTypes.first.name().toLowerCase());
                secondaryView.setLabel(rollValueTypes.second.name().toLowerCase());
                break;
            case SMALL:
                primaryView.setLabel(null);
                break;
        }

        this.mode = mode;
    }

    void setLabel(String s) {
        type_text.setText(s);
    }

    public void setRoll(final float... rollValues) {
        switch (mode) {
            case BIG:
                primaryView.setRoll(rollValues[0]);
                secondaryView.setRoll(rollValues[1]);
                break;
            case SMALL:
                primaryView.setRoll(rollValues);
        }
    }
}
