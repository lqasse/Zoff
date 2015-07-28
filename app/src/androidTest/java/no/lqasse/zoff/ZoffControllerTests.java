package no.lqasse.zoff;

import junit.framework.TestCase;

import no.lqasse.zoff.Models.Zoff;

/**
 * Created by lassedrevland on 06.07.15.
 */
public class ZoffControllerTests extends TestCase {
    MockController controller;
    MockServer server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        controller = new MockController("lqasse");
        server = controller.getServer();

    }



}
