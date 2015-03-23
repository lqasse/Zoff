package no.lqasse.zoff.Test;

import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Helpers.JSONTranslator;
import no.lqasse.zoff.Models.ZoffVideo;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class JSONTranslatorTest extends InstrumentationTestCase{
    String testData = "{\"nowPlaying\":{\"5jMDYX17lTg\":{\"id\":\"5jMDYX17lTg\",\"title\":\"Something There (Beauty & The Beast) - Karen YDJ\",\"votes\":0,\"added\":1426189402,\"guids\":[\"NzA0NjAx\"]}},\"songs\":{\"CXbK0718UEg\":{\"id\":\"CXbK0718UEg\",\"title\":\"YDJ \\u0110\\u00e0i truy\\u1ec1n h\\u00ecnh VN VTV1 n\\u00f3i v\\u1ec1 ch\\u1ee7 t\\u1ecbch t\\u1eadp \\u0111o\\u00e0n YDJ v\\u00e0 xu h\\u01b0\\u1edbng ph\\u00e1t tri\\u1ec3n c\\u1ee7a t\\u0111 YDJ t\\u1ea1i VN\",\"votes\":1,\"added\":1426189404,\"guids\":[\"NzA0NjAx\"]},\"3P5-f1oY3Ms\":{\"id\":\"3P5-f1oY3Ms\",\"title\":\"Prensa cizalla YDJ-5000\",\"votes\":1,\"added\":1426189405,\"guids\":[\"NzA0NjAx\"]},\"zR70WAIzseo\":{\"id\":\"zR70WAIzseo\",\"title\":\"Cashmere - Love's What I Want\",\"votes\":1,\"added\":1426197662,\"guids\":[\"MzM4ODQ5\"]},\"xKCek6_dB0M\":{\"id\":\"xKCek6_dB0M\",\"title\":\"Taylor Swift - Teardrops On My Guitar\",\"votes\":1,\"added\":1426197675,\"guids\":[\"MzM4ODQ5\"]},\"NrdxD-qF_LU\":{\"id\":\"NrdxD-qF_LU\",\"title\":\"Eminem - Business (Matoma Remix)\",\"votes\":0,\"added\":1425898282,\"guids\":[]},\"KbjTEthseDg\":{\"id\":\"KbjTEthseDg\",\"title\":\"Deandra The New Girl ft. Daft Poop - Here Comes the Poop\",\"votes\":0,\"added\":1425898475,\"guids\":[]},\"qnt5Lmj3NKI\":{\"id\":\"qnt5Lmj3NKI\",\"title\":\"Cashmere Cat - Mirror Maru (Melvv Bootleg) | Free Download\",\"votes\":0,\"added\":1425898689,\"guids\":[]},\"nkI8BOO_bKM\":{\"id\":\"nkI8BOO_bKM\",\"title\":\"IKT Servicefag ved Nannestad vgs\",\"votes\":0,\"added\":1425898886,\"guids\":[]},\"V8h0H77enmM\":{\"id\":\"V8h0H77enmM\",\"title\":\"Nannestad elevavis Bloopers 2014\",\"votes\":0,\"added\":1425899083,\"guids\":[]},\"UtF6Jej8yb4\":{\"id\":\"UtF6Jej8yb4\",\"title\":\"Avicii - The Nights\",\"votes\":0,\"added\":1425899277,\"guids\":[]},\"ISiGtxsN5d0\":{\"id\":\"ISiGtxsN5d0\",\"title\":\"Avicii - Lay Me Down\",\"votes\":0,\"added\":1425899503,\"guids\":[]},\"0AxgZBZbMkQ\":{\"id\":\"0AxgZBZbMkQ\",\"title\":\"Avicii - The Nights (Official Music Video)\",\"votes\":0,\"added\":1425899707,\"guids\":[]},\"CaqHoBDUYmI\":{\"id\":\"CaqHoBDUYmI\",\"title\":\"At least 18 dead, dozens injured after Port-au-Prince Carnival accident\",\"votes\":0,\"added\":1425899764,\"guids\":[]},\"TvyWRevLG5I\":{\"id\":\"TvyWRevLG5I\",\"title\":\"'Ethereal Dreams' - Chill Mix\",\"votes\":0,\"added\":1426353967,\"guids\":[]},\"iRPiL5OTXFI\":{\"id\":\"iRPiL5OTXFI\",\"title\":\"V\\u0102N PH\\u00d2NG YDJ T\\u1ea0I TH\\u00c1I LAN\",\"votes\":0,\"added\":1426423257,\"guids\":[]}},\"conf\":{\"startTime\":1426423257,\"views\":[\"MjIzNTM0\",\"NDU5MzYy\"],\"skips\":[],\"vote\":\"false\",\"addsongs\":\"false\",\"longsongs\":\"false\",\"frontpage\":\"true\",\"allvideos\":\"true\",\"removeplay\":\"false\",\"adminpass\":\"$6$rounds=9001$lqasseFuck0ffuSn$IBLXEgNPmZMr6u\\/mYnusj5U9cw..QNcmddQb6pPdC1AyRqqao4fAsytNHXt2TukIj6i2UpeRvdUtTgo\\/H14QW.\",\"skip\":null}}";

    public void test() throws Exception {

        ArrayList<ZoffVideo> videos = JSONTranslator.toZoffVideos(testData);


        testConf();

    }

    public void testConf(){
        HashMap<String,Boolean> settings  = new HashMap<>();

        settings.putAll(JSONTranslator.toSettingsMap(testData));


    }
}
