package com.haiyisoft.crossregion.test;

import org.apache.tomcat.Jar;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author CLY
 * @date 2023/3/1 22:24
 **/
public class ReadJarTest {

    @Test
    public void matchTest(){
        String s = "\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>";
        Pattern keyPattern = Pattern.compile("<(.+?)>");
        final Matcher matcher = keyPattern.matcher(s);
        System.out.println(matcher.find());
        System.out.println(matcher.group(1));
    }

    @Test
    public void processArtifactId(){
        Pattern keyPattern = Pattern.compile("<(.+?)>");
        Pattern namePattern = Pattern.compile(">(.+?)<");
        Pattern versionPattern = Pattern.compile("\\$\\{(.+)\\}");

        AtomicBoolean atomicBoolean = new AtomicBoolean();
        atomicBoolean.set(false);
        HashMap<String, String> versionMap = new HashMap<>();
        HashMap<String, String> jarMap = new HashMap<>();

        Path path = Paths.get("E:\\Git\\oms2\\基础服务平台\\电厂服务\\21-系统依赖jar包\\cloud-web-2.6.2.RELEASE.pom");
        try {
            final List<String> strings = Files.readAllLines(path);


            for (int i = 0; i < strings.size(); i++) {

                String e = strings.get(i);

    //            strings.forEach(e ->{
                if (e.contains("<properties>")) {
                    atomicBoolean.set(true);
                    continue;
                }
                if (e.contains("</properties>")) {
                    atomicBoolean.set(false);
                    continue;
                }
                if (atomicBoolean.get()) {
                    final Matcher matcher = keyPattern.matcher(e);
                    if (matcher.find()){
                        String key = matcher.group(1);
                        final Matcher matcher1 = namePattern.matcher(e);
                        if (matcher1.find()){
                            String name = matcher1.group(1);
                            versionMap.put(key, name);
                        }
                        continue;
                    }
                }

                if (e.contains("<artifactId>")) {
                    final String s = e.replaceAll("((<artifactId>)|(</artifactId>)|(\\s*))", "");
                    String e1 = strings.get(++i);
                    if (e1.contains("<version>")) {
                        final Matcher matcher = versionPattern.matcher(e1);
                        if (matcher.find()){
                            jarMap.put(s,versionMap.get(matcher.group(1)));
                        }
                    }else{
                        jarMap.put(s, "");
                    }
//                    System.out.println(s);
                    continue;
                }

            }
//            })  ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, String> stringEntry : jarMap.entrySet()) {
            System.out.println(stringEntry.getKey()+" "+stringEntry.getValue());
        }

    }

    public static void main(String[] args) {
//        final Path path = Paths.get("E:\\Git\\dmad-project\\16-系统部署\\20221124部署\\service\\dmad-base-service-1.0-SNAPSHOT\\BOOT-INF\\lib");
        final Path path = Paths.get("E:\\Git\\dmad-project\\16-系统部署\\20221124部署\\service\\dmad-base-service-1.0-SNAPSHOT\\BOOT-INF\\lib");
        try(final Stream<Path> list = Files.list(path)) {

            list.forEach(e -> System.out.println(e.getFileName().toString().replaceAll("-\\d.*jar$","")+" "+e.getFileName().toString().replaceAll("(^.+-)|(.jar$)", "")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStringMatches(){
        String path = "/user";
        System.out.println(path.matches("/.*"));
    }

}
