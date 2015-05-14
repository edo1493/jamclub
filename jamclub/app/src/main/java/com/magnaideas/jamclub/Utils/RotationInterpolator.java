package com.magnaideas.jamclub.Utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by antoniomarino on 14/05/15.
 */
public interface RotationInterpolator {
    public float interpolate(float fraction, float a, float b);

    public class Linear implements RotationInterpolator {
        @Override
        public float interpolate(float fraction, float a, float b) {
            return (b - a) * fraction + a;
        }
    }
}

