/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
