package cn.rufus.java8583sample;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
//import org.junit.Ignore;
import org.junit.Test;

import cn.ajsgn.common.java8583.core.Iso8583Message;
import cn.ajsgn.common.java8583.core.Iso8583MessageFactory;
import cn.rufus.java8583sample.quickstart.SingletonFactory;
/**
* @author 林创标  linchuangbiao@ebankunion.com
* @version 创建时间：2021年3月22日 下午5:55:25
* 类说明
*/
public class IsoTest {
    //execute only once, in the starting 
    @BeforeClass
    public static void beforeClass() {
       System.out.println("in before class");
    }

    //execute only once, in the end
    @AfterClass
    public static void  afterClass() {
       System.out.println("in after class");
    }

    //execute for each test, before executing test
    @Before
    public void before() {
       System.out.println("in before");
    }

    //execute for each test, after executing test
    @After
    public void after() {
       System.out.println("in after");
    }

    //test case 1
    @Test
    public void testCase1() {
       System.out.println("in test case 1");
       String qiandaoString = "007960000000046031003100000810003800010AC000140134870918340322084861584032313033323230393138333430303030323130303031383631343430333630313230303231001100000001003F0040A0F4BAB158444777DCCEFA04F3B4652318ECC792EF3AEC72A5011D350000000000000000F1737A9E";
       Iso8583MessageFactory posFactory = SingletonFactory.forQuickStart();
       Iso8583Message iso8583 = posFactory.parse(qiandaoString);
       System.out.println(iso8583.toFormatString());
       System.out.println("["+iso8583.getValue(60).getValue()+"]");
    }

    //test case 2
    @Test
    public void testCase2() {
        System.out.println("in test case 2");
        Iso8583MessageFactory posFactory = SingletonFactory.forQuickStart();
        Iso8583Message iso8583 = new Iso8583Message(posFactory);
        iso8583.setTpdu("0000000000")
                .setHeader("111111111111")
                .setMti("0200")
                .setValue(2,"6225887801155074001")
                .setValue(3, "123451234")
                .setValue(4, "11111")
                .setValue(41, "1234567")
                .setValue(60, "12345678901")
                .setValue(63, "哈哈哈哈~");
         System.out.println(iso8583.getBytesString());
         System.out.println(iso8583.toFormatString());
    }
}
