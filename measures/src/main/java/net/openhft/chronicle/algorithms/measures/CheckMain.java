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

package net.openhft.chronicle.algorithms.measures;

/**
 * Created by peter on 21/08/15.
 */
/*
VANILLA
Orthogonal bits: 99%tile score: 6066
Speed: The 99%tile for latency was 0.223 us
Avalanche: The 99%tile of the drift from 50% was 0.55%
Mask of Hash: 99%tile collisions: 1815

CITY_1_1
Orthogonal bits: 99%tile score: 7395
Speed: The 99%tile for latency was 0.267 us
Avalanche: The 99%tile of the drift from 50% was 0.55%
Mask of Hash: 99%tile collisions: 1817

MURMUR_3
Orthogonal bits: 99%tile score: 7524
Speed: The 99%tile for latency was 0.378 us
Avalanche: The 99%tile of the drift from 50% was 0.54%
Mask of Hash: 99%tile collisions: 1815

STRING32
Orthogonal bits: 99%tile score: 295906433
Speed: The 99%tile for latency was 1.580 us
Avalanche: The 99%tile of the drift from 50% was 1.02%
Mask of Hash: 99%tile collisions: 1814

STRING64
Orthogonal bits: 99%tile score: 1939167
Speed: The 99%tile for latency was 1.520 us
Avalanche: The 99%tile of the drift from 50% was 0.61%
Mask of Hash: 99%tile collisions: 1816
 */
public class CheckMain {
    public static void main(String[] args) {
        for (AddressWrappers aw : AddressWrappers.values()) {
            System.out.println(aw);
            OrtogonalBitsScore.score(aw);
            AvalancheScore.score(aw);
            MaskHashScore.score(aw);
            System.out.println();
        }
    }
}
