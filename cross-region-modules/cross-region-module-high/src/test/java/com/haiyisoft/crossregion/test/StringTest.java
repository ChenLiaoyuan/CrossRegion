package com.haiyisoft.crossregion.test;

/**
 * @author CLY
 * @date 2023/3/22 17:47
 **/
public class StringTest {

    public static void testString() throws InterruptedException {
//        SnowFlakeId snowFlakeId = new SnowFlakeId(1,1);
//        String snowFlakeIdStr = snowFlakeId.nextStringId();

        String a = "aaaaaa";

        new Thread(()->{
            synchronized (a){
                try {
                    a.wait(10000);
                    System.out.println("a收到了通知");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        Executors.newSingleThreadExecutor().execute(()->{
//            synchronized (a){
//                try {
//                    a.wait(10000);
//                    System.out.println("a收到了通知");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });


        Thread.sleep(500);
        String b = "aaaaaa";
        System.out.println(a == b);
        synchronized (b){
            b.notify();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        testString();
    }

}
