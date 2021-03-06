package com.nikartix.fractal.fractal.mandelbrot;

import com.nikartix.fractal.fractal.FractalFunction;
import com.nikartix.fractal.math.Mcomplex;

import java.util.Iterator;

public class JuliaFractalFunction implements FractalFunction  {

    private Mcomplex c;

    public JuliaFractalFunction(Mcomplex c) {
        this.c = c;
    }

    @Override
    public Iterator<Mcomplex> evaluate(Mcomplex value) {
        return new Iterator<Mcomplex>() {
            Mcomplex prev = value;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Mcomplex next() {
                prev = prev.mul(prev).add(c);
                return prev;
            }
        };
    }
}
