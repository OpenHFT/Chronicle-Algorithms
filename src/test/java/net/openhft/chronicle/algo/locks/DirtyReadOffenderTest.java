package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.map.ChronicleMap;
//import net.openhft.chronicle.map.fromdocs.BondVOInterface;

import java.util.Scanner;
import java.util.concurrent.locks.StampedLock;

import static net.openhft.chronicle.values.Values.newNativeReference;

class DirtyReadOffenderTest implements Runnable {

    public void run() {
        Scanner sc = new Scanner(System.in);

        try {
            String isoLevel = "WRITER";
            long sleepT = Long.parseLong("5");
            long holdTime = Long.parseLong("10");

            ChronicleMap<String, BondVOInterface> chm =
                    DirtyReadTolerance.offHeap(
                            "C:\\Users\\buddy\\dev\\shm\\"
                                    + "OPERAND_CHRONICLE_MAP"
                    );
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender established chm "
            );
            StampedLock offHeapLock = new ChronicleStampedLock(
                    "C:\\Users\\buddy\\dev\\shm\\"
                            + "OPERAND_ChronicleStampedLock"
            );
            BondVOInterface bond = newNativeReference(BondVOInterface.class);

            chm.acquireUsing("369604101", bond);
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender sleeping " + sleepT + " seconds "
            );
            Thread.sleep(sleepT * 1_000);
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender awakening "
            );
            /**
             *  ben.cotton@rutgers.edu  ... anticipate Chronicle (www.OpenHFT.net)
             *  providing a j.u.c.l.StampedLock API for off-heap enthusiasts
             *
             *  START
             *
             */
            long stamp = 0;
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender ACQUIRING offHeapLock.writeLock();"
            );
            while ((stamp = offHeapLock.writeLock()) == 0) {
                ;
            }
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender ACQUIRED offHeapLock.writeLock();"
            );
            try {
                double newCoupon = 3.5 + Math.random();
                System.out.println(
                        "..... @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender " +
                                " calling chm.put('369604101'," + newCoupon + ") "
                );
                bond.setCoupon(newCoupon);
                chm.put("369604101", bond);
                //cslMock.setEntryLockState(System.currentTimeMillis()); //mock'd
                // chm.put("Offender ",cslMock); //mock'd
                System.out.println(
                        "..... @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender coupon=[" +
                                bond.getCoupon() +
                                "] written. "
                );
            } finally {
                System.out.println(
                        "..... @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender sleeping " + holdTime + " seconds "
                );
                Thread.sleep(holdTime * 1_000);
                offHeapLock.unlockWrite(stamp);
                System.out.println(
                        "..... @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender called " +
                                "offHeapLock.unlockWrite(" + stamp + ");"
                );
            }
            /**
             *  ben.cotton@rutgers.edu
             *
             *  END
             *
             */
            chm.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            System.out.println(
                    "..... @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender COMMITTED"
            );

        }
    }
}
