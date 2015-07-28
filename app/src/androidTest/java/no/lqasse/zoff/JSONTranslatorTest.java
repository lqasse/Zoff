package no.lqasse.zoff;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.VideoChangeMessage;
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

    public void testGetVideoChangeMessage() throws Exception{

        JSONArray message = new JSONArray("[[{\"_id\":\"559bd67baae3796102b5d45b\",\"added\":1436279194,\"guids\":[],\"id\":\"tKi9Z-f6qX4\",\"now_playing\":true,\"title\":\"deadmau5 - Strobe\",\"votes\":0,\"duration\":637}],[{\"_id\":\"559bd5d4aae3796102b5d458\",\"addsongs\":false,\"adminpass\":\"\",\"allvideos\":false,\"frontpage\":true,\"longsongs\":false,\"removeplay\":false,\"shuffle\":true,\"skip\":false,\"skips\":[],\"startTime\":1436279194,\"views\":[],\"vote\":false}],1436279275]");

        VideoChangeMessage videoChangeMessage = JSONTranslator.getVideoChangeMessage(message);

        //assertEquals("tKi9Z-f6qX4",videoChangeMessage.video.getId());
        assertEquals(1436279275,videoChangeMessage.timeChanged);

    }







}
