package hvac.util;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

import java.util.List;


public class Interpolation {
    public static float calculateValueAt(float x, List<Float> knownX, List<Float> knownY) {
        if(knownX.size() != knownY.size()) {
            throw new IllegalArgumentException("input lists must be of the same length");
        }
        double[] xArray = knownX.stream().mapToDouble(Float::doubleValue).toArray();
        double[] yArray = knownY.stream().mapToDouble(Float::doubleValue).toArray();
        return (float) new SplineInterpolator().interpolate(xArray,yArray).value(x);
    }
}
