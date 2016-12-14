package ru.olgathebest.casper.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Ольга on 11.12.2016.
 */

public class RSA {
    public final static String EXPONENT = "65537";
    private BigInteger n, e, d;

    private int bitlen =1024;

    /** Create an instance that can encrypt using someone elses public key. */
    public RSA(BigInteger newn, BigInteger newe) {
        n = newn;
        e = newe;
    }

    /** Create an instance that can both encrypt and decrypt. */
    public RSA(int bits) {
        bitlen = bits;
        SecureRandom r = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(bitlen / 2, r);
        BigInteger q = BigInteger.probablePrime(bitlen / 2, r);
        n = p.multiply(q);
        int nsize = n.toByteArray().length;
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q
                .subtract(BigInteger.ONE));
        e = new BigInteger(EXPONENT);
//        while (m.gcd(e).intValue() > 1) {
//            e = e.add(new BigInteger("2"));
//        }
        d = e.modInverse(m);
    }

    /** Encrypt the given plaintext message. */

    public synchronized String encrypt(String message,BigInteger key) {
        return (new BigInteger(Coding.encode(message))).modPow(e, key).toString();
    }

    /** Decrypt the given ciphertext message. */
    public synchronized String decrypt(String message) {
        return new String((new BigInteger(message)).modPow(d, n).toByteArray());
    }


    /** Generate a new public and private key set. */
    public synchronized void generateKeys() {
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(bitlen / 2, 100, r);
        BigInteger q = new BigInteger(bitlen / 2, 100, r);
        n = p.multiply(q);
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q
                .subtract(BigInteger.ONE));
        e = new BigInteger("3");
        while (m.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }
        d = e.modInverse(m);
    }

    /** Return the modulus. */
    public synchronized BigInteger getN() {
        return n;
    }

    /** Return the public key. */
    public synchronized BigInteger getE() {
        return e;
    }


}

