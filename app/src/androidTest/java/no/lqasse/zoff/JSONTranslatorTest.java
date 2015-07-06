package no.lqasse.zoff;

import junit.framework.TestCase;

import org.json.JSONObject;

import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Server.JSONTranslator;

/**
 * Created by lassedrevland on 05.07.15.
 */
public class JSONTranslatorTest extends TestCase{

    public void testCreateVideoFromJson() throws Exception{

        JSONObject dummyVideoString = new JSONObject("{\"_id\":\"55433bd537d12f5063f27ed0\",\"added\":339144,\"guids\":[],\"id\":\"N-o_is6eOyc\",\"now_playing\":false,\"title\":\"Yuna - Live Your Life (MELO-X MOTHERLAND GOD MIX)\",\"votes\":0,\"duration\":272}");



        Video video = JSONTranslator.createVideoFromJSON(dummyVideoString);


        assertEquals("N-o_is6eOyc",video.getId());
        assertFalse(video.isNowPlaying());
    }


}
