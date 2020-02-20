package com.rhine.terminal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author LDZ
 * @date 2020-02-20 00:04
 */
public class Helper {


    public static <S> List<S> loadServices(ClassLoader loader, Class<S> serviceClass) {
        ArrayList<S> services = new ArrayList<>();
        for (S s : ServiceLoader.load(serviceClass, loader)) {
            try {
                services.add(s);
            } catch (Exception ignore) {
                // Log me
            }
        }
        return services;
    }


    /**
     * Compute the position of the char at the specified {@literal offset} of the {@literal codePoints} given a
     * {@literal width} and a relative {@literal origin} position.
     *
     * @param origin the relative position to start from
     * @param width  the screen width
     * @return the height
     */
    public static Vector computePosition(int[] codePoints, Vector origin, int offset, int width) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Offset cannot be negative");
        }
        if (offset > codePoints.length) {
            throw new IndexOutOfBoundsException("Offset cannot bebe greater than the length");
        }
        int col = origin.x();
        int row = origin.y();
        for (int i = 0; i < offset; i++) {
            int cp = codePoints[i];
            int w = Wcwidth.of(cp);
            if (w == -1) {
                if (cp == '\r') {
                    col = 0;
                } else if (cp == '\n') {
                    col = 0;
                    row++;
                }
            } else {
                if (col + w > width) {
                    if (w > width) {
                        throw new UnsupportedOperationException("Handle this case gracefully");
                    }
                    col = 0;
                    row++;
                }
                col += w;
                if (col >= width) {
                    col -= width;
                    row++;
                }
            }
        }
        return new Vector(col, row);
    }
}
