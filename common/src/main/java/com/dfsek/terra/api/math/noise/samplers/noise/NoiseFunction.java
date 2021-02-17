package com.dfsek.terra.api.math.noise.samplers.noise;

import com.dfsek.terra.api.math.noise.NoiseSampler;
import net.jafama.FastMath;

@SuppressWarnings("ManualMinMaxCalculation")
public abstract class NoiseFunction implements NoiseSampler {
    // Hashing
    protected static final int PRIME_X = 501125321;
    protected static final int PRIME_Y = 1136930381;
    protected static final int PRIME_Z = 1720413743;


    protected double frequency = 0.02d;
    protected int seed = 2403;

    public NoiseFunction(int seed) {
        this.seed = seed;
    }

    protected static int fastFloor(double f) {
        return f >= 0 ? (int) f : (int) f - 1;
    }

    protected static int hash(int seed, int xPrimed, int yPrimed, int zPrimed) {
        int hash = seed ^ xPrimed ^ yPrimed ^ zPrimed;

        hash *= 0x27d4eb2d;
        return hash;
    }

    protected static int hash(int seed, int xPrimed, int yPrimed) {
        int hash = seed ^ xPrimed ^ yPrimed;

        hash *= 0x27d4eb2d;
        return hash;
    }

    protected static int fastRound(double f) {
        return f >= 0 ? (int) (f + 0.5f) : (int) (f - 0.5);
    }

    protected static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    protected static double interpHermite(double t) {
        return t * t * (3 - 2 * t);
    }

    protected static double interpQuintic(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    protected static double cubicLerp(double a, double b, double c, double d, double t) {
        double p = (d - c) - (a - b);
        return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
    }

    protected static double fastMin(double a, double b) {
        return a < b ? a : b;
    }

    protected static double fastMax(double a, double b) {
        return a > b ? a : b;
    }

    protected static double fastAbs(double f) {
        return f < 0 ? -f : f;
    }

    protected static double fastSqrt(double f) {
        return FastMath.sqrt(f);
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public double getNoise(double x, double y) {
        return getNoiseSeeded(seed, x * frequency, y * frequency);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseSeeded(seed, x * frequency, y * frequency, z * frequency);
    }
}
