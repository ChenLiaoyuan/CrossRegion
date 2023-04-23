package com.haiyisoft.crossregion.test;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author CLY
 * @date 2023/2/24 10:47
 **/
public class UriTest {

    @Test
    public void testUri(){

        try {
            URI uri = new URI("http://localhost:8080/proxy?name=chenliaoyuan");
            System.out.println(uri.getFragment());
            System.out.println(uri.getHost());
            System.out.println(uri.getPath());
            System.out.println(uri.getPort());
            System.out.println(uri.getAuthority());
            System.out.println(uri.getQuery());

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUrl(){
        try {
            URL url = new URL("http://localhost:8080/proxy?name=chenliaoyuan");
//            url = new URL("http", "localhost", 8080, "proxy?name=chenliaoyuan");
            System.out.println(url.getHost());
            System.out.println(url.getPath());
            System.out.println(url.getPort());
            System.out.println(url.getAuthority());
            System.out.println(url.getQuery());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void URIComponentTest(){
        // 要有?才能解析
        String queryString = "?name=cly&age=44324";
        final UriComponents uriComponents = UriComponentsBuilder.fromUriString(queryString).build();
        final MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        System.out.println(queryParams);
    }



}
