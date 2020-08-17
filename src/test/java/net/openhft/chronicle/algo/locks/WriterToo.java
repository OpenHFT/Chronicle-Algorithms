package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.map.ChronicleMap;
import org.junit.Assert;

import java.util.Scanner;

import static net.openhft.chronicle.values.Values.newNativeReference;

class WriterToo implements Runnable {

    @Override
    public void run() {

        Scanner sc = new Scanner(System.in);

        try {
            String isoLevel = "WRITER";
            long sleepT = Long.parseLong("0");
            long holdTime = Long.parseLong("20");

            ChronicleMap<String, BondVOInterface> chm =
                    DirtyReadTolerance.offHeap(
                            "C:\\Users\\buddy\\dev\\shm\\" +
                                    "OPERAND_CHRONICLE_MAP"
                    );
            System.out.println(
                    "WRITER TOO" +
                            " @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender established chm "
            );
            ChronicleStampedLock offHeapLock = new ChronicleStampedLock(
                    "C:\\Users\\buddy\\dev\\shm\\"
                            + "OPERAND_ChronicleStampedLock"
            );
            Assert.assertNotEquals(offHeapLock, null);
            BondVOInterface bond = newNativeReference(BondVOInterface.class);
            //BondVOInterface cslMock = newNativeReference(BondVOInterface.class);
            chm.acquireUsing("369604101", bond);
            //chm.acquireUsing("Offender ", cslMock); // mock ChronicleStampLock
            System.out.println(
                    "WRITER TOO" +
                            " @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender sleeping " + sleepT + " seconds "
            );
            Thread.sleep(sleepT * 1_000);
            System.out.println(
                    "WRITER TOO" +
                            " @t=" + System.currentTimeMillis() +
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
                    "WRITER TOO" +
                            " @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender ACQUIRING offHeapLock.writeLock();"
            );
            while ((stamp = offHeapLock.writeLock()) == 0) {
                ;
            }
            System.out.println(
                    "WRITER TOO" +
                            " @t=" + System.currentTimeMillis() +
                            " DirtyReadOffender ACQUIRED offHeapLock.writeLock();"
            );
            try {
                double newCoupon = 3.5 + Math.random();
                System.out.println(
                        "WRITER TOO" +
                                " @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender " +
                                " calling chm.put('369604101'," + newCoupon + ") "
                );
                bond.setCoupon(newCoupon);
                chm.put("369604101", bond);
                //cslMock.setEntryLockState(System.currentTimeMillis()); //mock'd
                // chm.put("Offender ",cslMock); //mock'd
                System.out.println(
                        "WRITER TOO" +
                                " @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender coupon=[" +
                                bond.getCoupon() +
                                "] written. "
                );
            } finally {
                System.out.println(
                        "WRITER TOO" +
                                " @t=" + System.currentTimeMillis() +
                                " DirtyReadOffender sleeping " + holdTime + " seconds "
                );
                Thread.sleep(holdTime * 1_000);
                offHeapLock.unlockWrite(stamp);
                System.out.println(
                        "WRITER TOO" +
                                " @t=" + System.currentTimeMillis() +
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
            offHeapLock.closeChronicle();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            System.out.println(
                    "WRITER TOO" +
                            " ,,@t=" + System.currentTimeMillis() +
                            " DirtyReadOffender COMMITTED"
            );
        }
    }


}

