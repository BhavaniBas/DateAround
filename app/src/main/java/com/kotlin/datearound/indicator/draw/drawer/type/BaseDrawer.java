package com.kotlin.datearound.indicator.draw.drawer.type;

import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.kotlin.datearound.indicator.draw.data.Indicator;

class BaseDrawer {

    Paint paint;
    Indicator indicator;

    BaseDrawer(@NonNull Paint paint, @NonNull Indicator indicator) {
        this.paint = paint;
        this.indicator = indicator;
    }
}
