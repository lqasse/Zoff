package no.lqasse.zoff;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.TestCase;

import no.lqasse.zoff.Helpers.Sha256;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends TestCase {

    public void testSha() throws Exception {

        String input = "abc";
        String hash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";


        assertEquals(hash, Sha256.getHash(input));


    }
}