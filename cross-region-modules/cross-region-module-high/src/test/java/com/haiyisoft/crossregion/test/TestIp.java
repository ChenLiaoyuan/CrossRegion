package com.haiyisoft.crossregion.test;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @author CLY
 * @date 2023/3/21 16:04
 **/
public class TestIp {

    @Test
    public void getWorkId() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            int[] ints = StringUtils.toCodePoints(hostAddress);
            int sums = 0;
            for (int b : ints) {
                sums += b;
            }
            System.out.println( (long) (sums % 32));
        } catch (UnknownHostException e) {
            // 如果获取失败，则使用随机数备用
            System.out.println( RandomUtils.nextLong(0, 31));
        }
    }

    @Test
    public void getDataCenterId() {
        System.out.println(SystemUtils.getHostName());
        int[] ints = StringUtils.toCodePoints(SystemUtils.getHostName());
        int sums = 0;
        if (ints != null) {
            for (int i : ints) {
                sums += i;
            }
        }
        System.out.println( (long) (sums % 32));
    }

}
