/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.algo.hashing;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by peter on 14/09/15.
 */
public class RandomOptimiser<T> {
    final Supplier<T> inputSupplier;
    private final Comparator<T> tieBreaker;
    private final int samples;
    T lowest;
    int lowestScore = Integer.MAX_VALUE;
    T highest;
    int highestScore = Integer.MIN_VALUE;

    public RandomOptimiser(Supplier<T> inputSupplier, Comparator<T> tieBreaker, int samples) {
        this.inputSupplier = inputSupplier;
        this.tieBreaker = tieBreaker;
        this.samples = samples;
    }

    public void randomSearch(Function<T, Integer> test) {
        lowest = highest = null;
        lowestScore = Integer.MAX_VALUE;
        highestScore = Integer.MIN_VALUE;
        IntStream.range(0, samples).parallel().forEach(i -> {
            T num = inputSupplier.get();
            int score = test.apply(num);
            synchronized (this) {
                if (lowestScore > score || (lowestScore == score && tieBreaker.compare(lowest, num) > 0)) {
                    lowestScore = score;
                    lowest = num;
                }
                if (highestScore < score || (highestScore == score && tieBreaker.compare(highest, num) > 0)) {
                    highestScore = score;
                    highest = num;
                }
            }
        });
    }

    @Override
    public String toString() {
        return "RandomOptimiser{" +
                "samples=" + samples +
                ", lowest=" + lowest +
                ", lowestScore=" + lowestScore +
                ", highest=" + highest +
                ", highestScore=" + highestScore +
                '}';
    }
}
