package no.lqasse.zoff;

/**
 * Created by lassedrevland on 08.07.15.
 */
public class MockController extends ZoffController {

    public MockController(String channel) {
        super(channel);
        server = new MockServer(channel, this);
    }

    public MockServer getServer(){
        return (MockServer) server;
    }




}
