package com.cs550.teama.spotflickr;

import com.cs550.teama.spotflickr.services.Utils;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void getUrlParameters() {
        Map<String,String> actual;
        Map<String,String> expected;

        actual = Utils.getUrlParameters("https://www.youtube.com/watch?v=7C2z4GqqS5E");
        expected= new HashMap<String,String>(){{put("v","7C2z4GqqS5E");}};
        assertMapEquals(expected,actual);

        actual = Utils.getUrlParameters("https://www.google.com/search?q=google&oq=google&aqs=chrome..69i57j69i60l2j0l2j69i60.603j0j4&sourceid=chrome&ie=UTF-8");
        expected= new HashMap<String,String>(){{
            put("q","google");
            put("oq","google");
            put("aqs","chrome..69i57j69i60l2j0l2j69i60.603j0j4");
            put("sourceid","chrome");
            put("ie","UTF-8");
        }};
        assertMapEquals(expected,actual);

        actual = Utils.getUrlParameters("http://p.com/search?q=google&oq=google&aqs=chrome..69i57j69i60l2j0l2j69i60.603j0j4&ie=UTF-8");
        expected= new HashMap<String,String>(){{
            put("q","google");
            put("oq","google");
            put("aqs","chrome..69i57j69i60l2j0l2j69i60.603j0j4");
            put("ie","UTF-8");
        }};

        // url without protocol should return empty set
        actual = Utils.getUrlParameters("p.com/search?q=p");
        assert(actual.size() == 0);
    }

    private void assertMapEquals(Map<String, String> expected, Map<String, String> actual){
        assertEquals("Size mismatch for maps;", expected.size(), actual.size());
        expected.keySet().forEach((key) -> assertEquals("Value mismatch for key '" + key + "';", expected.get(key), actual.get(key)));
    }

    @Test
    public void separateParameters() {
        Map<String,String> actual;
        Map<String,String> expected;
        actual = Utils.separateParameters("q=google&oq=google&aqs=chrome..69i57j69i60l2j0l2j69i60.603j0j4&ie=UTF-8");
        expected= new HashMap<String,String>(){{
            put("q","google");
            put("oq","google");
            put("aqs","chrome..69i57j69i60l2j0l2j69i60.603j0j4");
            put("ie","UTF-8");
        }};
        assertMapEquals(actual,expected);

        actual = Utils.separateParameters("v=7C2z4GqqS5E");
        expected= new HashMap<String,String>(){{put("v","7C2z4GqqS5E");}};
        assertMapEquals(expected,actual);

        assertEquals(1,Utils.separateParameters("p.com/search?q=p").size());
        assertEquals(0,Utils.separateParameters("p.comøljkljl.com/").size());
        try{
            Utils.separateParameters(null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Test
    public void oauthEncode() {
        assertEquals("jkfldsage%2F8ru9jfads",Utils.oauthEncode("jkfldsage/8ru9jfads"));
        assertEquals("flickr.com%2Fservices%2Fhello-world%3Fnot%20working",Utils.oauthEncode("flickr.com/services/hello-world?not working"));
        assertEquals("ji98uu7a%28%2F%C2%A4%2F%29%22NJ~~dsrew3",Utils.oauthEncode("ji98uu7a(/¤/)\"NJ~~dsrew3"));
    }

    @Test
    public void oauthDecode() {
        assertEquals("jkfldsage/8ru9jfads",Utils.oauthDecode("jkfldsage%2F8ru9jfads"));
        assertEquals("flickr.com/services/hello-world?not working",Utils.oauthDecode("flickr.com%2Fservices%2Fhello-world%3Fnot%20working"));
        assertEquals("ji98uu7a(/¤/)\"NJ~~dsrew3",Utils.oauthDecode("ji98uu7a%28%2F%C2%A4%2F%29%22NJ~~dsrew3"));
    }
}