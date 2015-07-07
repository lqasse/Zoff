package no.lqasse.zoff;

import junit.framework.TestCase;

import no.lqasse.zoff.Models.Zoff;

/**
 * Created by lassedrevland on 06.07.15.
 */
public class ZoffControllerTests extends TestCase {
    public void testGetInstance() throws Exception {
        ZoffController firstInstance = ZoffController.getInstance("lqasse");
        ZoffController secondInstance = ZoffController.getInstance("NOTSAME");

        assertNotSame(firstInstance,secondInstance);

    }


}
