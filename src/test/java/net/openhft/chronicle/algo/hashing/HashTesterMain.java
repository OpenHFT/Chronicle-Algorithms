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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by peter on 14/09/15.
 * For a post of customizing hashing strategies.
 */
public class HashTesterMain {
    static final Set<String> FTSE;

    static {
        try {
            URI uri = HashTesterMain.class.getClassLoader().getResource("ftse350.csv").toURI();
            FTSE = Files.lines(Paths.get(uri)).map(l -> l.split(",", 2)[0]).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        new HashTesterRunner(HashTesterMain.class).run();
    }

    @HashTest("String.hashCode()")
    public static void generateStringHashCode(Consumer<Integer> hashes) {
        FTSE.stream().map(String::hashCode).forEach(hashes);
    }

    @HashTest("String.hashCode() & 511")
    public static void generateStringHashCodeAnd511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> s.hashCode() & 511).forEach(hashes);
    }

    @HashTest("String.hashCode() & 1023")
    public static void generateStringHashCodeAnd1023(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> s.hashCode() & 1023).forEach(hashes);
    }

    @HashTest("String.hashCode() & 2047")
    public static void generateStringHashCodeAnd2047(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> s.hashCode() & 2047).forEach(hashes);
    }

    @HashTest("String.hashCode() & 4095")
    public static void generateStringHashCodeAnd4095(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> s.hashCode() & 4095).forEach(hashes);
    }

    @HashTest("HashMap.hash(String.hashCode()) & 511")
    public static void generateStringHashCodeXorShift(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hashMap_hash(s.hashCode()) & 511).forEach(hashes);
    }

    @HashTest("HashMap.hash(String.hashCode()) & 1023")
    public static void generateStringHashCodeXorShiftAnd1023(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hashMap_hash(s.hashCode()) & 1023).forEach(hashes);
    }

    @HashTest("HashMap.hash(String.hashCode()) & 2047")
    public static void generateStringHashCodeXorShiftAnd2047(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hashMap_hash(s.hashCode()) & 2047).forEach(hashes);
    }

    @HashTest("hashCode(String, 1) & 511")
    public static void generateMultiplierHash1And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 1) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 2) & 511")
    public static void generateMultiplierHash2And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 2) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 3) & 511")
    public static void generateMultiplierHash3And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 3) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 4) & 511")
    public static void generateMultiplierHash4And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 4) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 5) & 511")
    public static void generateMultiplierHash5And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 5) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 6) & 511")
    public static void generateMultiplierHash6And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 6) & 511).forEach(hashes);
    }

    @HashTest("hashCode(String, 7) & 511")
    public static void generateMultiplierHash7And511(Consumer<Integer> hashes) {
        FTSE.stream().map(s -> hash(s, 7) & 511).forEach(hashes);
    }

    public static int hashMap_hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public static int hash(String s, int multiplier) {
        int h = 0;
        for (int i = 0; i < s.length(); i++)
            h = multiplier * h + s.charAt(i);
        return h;
    }
}
