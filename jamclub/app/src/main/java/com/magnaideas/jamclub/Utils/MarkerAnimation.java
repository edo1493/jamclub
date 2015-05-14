package com.magnaideas.jamclub.Utils;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by edoardomoreni on 07/05/15.
 */
public class MarkerAnimation {
    public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final float finalRotation,
                                         final LatLngInterpolator latLngInterpolator, final RotationInterpolator rotationInterpolator) {

        final LatLng startPosition = marker.getPosition();
        final float startRotation = marker.getRotation();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        final float durationInMs = 4000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));
                //marker.setRotation(rotationInterpolator.interpolate(new LinearInterpolator().getInterpolation(t), startRotation, finalRotation));
                marker.setRotation(finalRotation);

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

}
