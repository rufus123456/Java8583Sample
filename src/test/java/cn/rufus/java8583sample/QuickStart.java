/*
 * Copyright (c) 2017, Ajsgn 杨光 (Ajsgn@foxmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.rufus.java8583sample;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.LinkedList;

import cn.ajsgn.common.java8583.core.Iso8583Message;
import cn.ajsgn.common.java8583.core.Iso8583MessageFactory;
import cn.ajsgn.common.java8583.ext.tlv.TlvObject;
import cn.ajsgn.common.java8583.ext.tlv.TlvParser;
import cn.ajsgn.common.java8583.ext.tlv.TlvValue;
import cn.ajsgn.common.java8583.ext.tlv.parser.TlvParserFactory;
import cn.ajsgn.common.java8583.ext.tlv.parser.TlvParserType;
import cn.ajsgn.common.java8583.ext.tlv.parser.TlvValueFactory;
import cn.ajsgn.common.java8583.field.Iso8583DataHeader;
import cn.ajsgn.common.java8583.field.Iso8583FieldType;
import cn.ajsgn.common.java8583.field.Iso8583FillBlankStrategy;
import cn.ajsgn.common.java8583.field.Iso8583FieldType.FieldTypeValue;
import cn.ajsgn.common.java8583.util.EncodeUtil;
import cn.rufus.java8583sample.quickstart.SingletonFactory;
import cn.rufus.java8583sample.quickstart.special.SpecialField62;

/**
 * 快速上手指南
 * @ClassName: QuickStart
 * @Description: 快速上手指南
 * @author Ajsgn@foxmail.com
 * @date 2017年8月3日 下午4:42:35
 */
public class QuickStart {
	
	//某发卡方的签到请求报文
	public static final String MTI0800 = "0041600000000060220000000008000038000000C000120001721705100510353030383030303133303933313030383339383530303800110000017200300003435550";
	//某发卡方的签到响应请求报文
	public static final String MTI0810 = "007960000000006022000000000810003800010AC0001400017210014105100803099988303531303030303030323033303035303038303030313330393331303038333938353030380011000000420030004051DBA323AF60599287F61C7E5A9484AEEF8DE29BD3E614FCB51122D4E9B84E2A608CD9C2A4DAABF2";
	
	/**
	 * 快速上手指南 —— 入口
	 * 看完下面这个main方法，你将可以完全掌握Java8583
	 * @Title: main
	 * @Description: 快速上手指南 —— 入口
	 * @author g.yang@i-vpoints.com
	 * @date 2017年8月3日 下午8:43:02
	 */
	public static void main(String[] args) throws Exception {
		//第一章，初识Java8583
		chapter1();
		//第二章，认识ISO8583报文结构与Iso8583DataHeader类
		chapter2();
		//第三章，认识Iso8583MessageFactory以及填充策略Iso8583FillBlankStrategy
		chapter3();
		//第四章，认识Iso8583Message
		chapter4();
		//4.1 同一个数据，不同的描述，不同的结果
		chapter4_1();
		//第五章，再识Iso8583MessageFactory——报文解析
		chapter5();
		//第六章，使用不同的字符编码导致的中文乱码问题
		chapter6();
		//第七章，Iso8583MessageFactory单例的使用
		chapter7();
		//第八章，Java8583的单元测试
		chapter8();
		//第九章，Java8583扩展功能介绍
		//9.1、TLV字段读取支持，以及55域解析支持
		chapter9_1();
	}

	/*
	 * 认识Java8583中的数据类型：
	 * 从本意上讲，Java8583是没有数据类型概念。
	 * 只是简单的从数据编码类型与长度描述上做区分为：ASCII编码类型与BCD编码类型；字节长度，字符长度。
	 * 其中每种编码类型又在分为定长与变长两种类型。
	 * 
	 * 字段类型描述：
	 * CHAR：ASCII编码，1个字符占用1个字节，
	 * LLVAR_CHAR：1个字节描述长度（字节长度），ASCII编码
	 * LLLVAR_CHAR：2个字节描述长度（字节长度），ASCII编码
	 * LLLLVAR_CHAR：3个字节描述长度（字节长度），ASCII编码
	 * 
	 * NUMERIC：BCD编码，2个字符占用1个字节
	 * LLVAR_NUMERIC：1个字节描述长度（字符长度），BCD编码
	 * LLLVAR_NUMERIC：2个字节描述长度（字符长度），BCD编码
	 * LLLLVAR_NUMERIC:3个字节描述长度（字符长度），BCD编码
	 * 
	 * BYTE_NUMERIC：BCD编码，2个字符占用1个字节
	 * LLVAR_BYTE_NUMERIC：1个字节描述长度（字节长度），BCD编码
	 * LLLVAR_BYTE_NUMERIC：2个字节描述长度（字节长度），BCD编码
	 * LLLLVAR_BYTE_NUMERIC：3个字节描述长度（字节长度），BCD编码
	 * 
	 * 如果不懂，继续往下看，后续会展示同一个数据在不同数据类型中的计算结果
	 */
	@SuppressWarnings("unused")
	public static void chapter1() {
		// ASCII
		FieldTypeValue character = FieldTypeValue.CHAR;
		FieldTypeValue llvar = FieldTypeValue.LLVAR_CHAR;
		FieldTypeValue lllvar = FieldTypeValue.LLLVAR_CHAR;
		FieldTypeValue llllvar = FieldTypeValue.LLLLVAR_CHAR;
		// BCD
		FieldTypeValue numeric = FieldTypeValue.NUMERIC;
		FieldTypeValue llvarNumeric = FieldTypeValue.LLVAR_NUMERIC;
		FieldTypeValue lllvarNumeric = FieldTypeValue.LLLVAR_NUMERIC;
		FieldTypeValue llllvarNumeric = FieldTypeValue.LLLLVAR_NUMERIC;
		// BCD
		FieldTypeValue byteNumeric = FieldTypeValue.BYTE_NUMERIC;
		FieldTypeValue llvarByteNumeric = FieldTypeValue.LLVAR_BYTE_NUMERIC;
		FieldTypeValue lllvarByteNumeric = FieldTypeValue.LLLVAR_BYTE_NUMERIC;
		FieldTypeValue llllvarByteNumeric = FieldTypeValue.LLLLVAR_BYTE_NUMERIC;
		
	}
	
	/*
	 * ISO8583报文协议相关官方描述，自行走搜索引擎~
	 * 其中报文结构为：dataLength + data 形式，即  报文长度描述+报文体实际内容
	 * 
	 * 报文体data包括：tpdu + dataHeader + 应用数据  三部分
	 * 应用数据  部分包括：mti + bitmap + 域数据
	 * 域数据又分为：64域规范与128域规范
	 * 
	 * 所以，一个8583报文实际应该包含以下结构：
	 * 
	 * 报文总长度 + tpdu + dataHeader + mti + bitmap + N 个域数据
	 * 
	 * 编码处理：
	 * ASCII编码会把一个字符解析成1个字节，两个长度；
	 * BCD编码会把2个字符解析成1个字节，两个长度。
	 * 
	 */
	public static Iso8583DataHeader chapter2() {
		//定义报文头数据格式
		Iso8583DataHeader header = new Iso8583DataHeader(
			// tpdu		BCD编码，5个字节（10个字符长度）
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,10),
			// header	BCD编码，6个字节长度
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,12),
			// mti		BCD编码，2个字节长度
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,4),
			// bitmap	BCD编码，8个字节长度
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,16)
		);
		return header;
	}
	
	/*
	 * Iso8583MessageFactory 
	 * 
	 * 作为Java8583的核心类文件之一，也是其他相关框架的XML替代方案。报文解析，对象构建成功与否都取决于factory的配置。
	 * 当然，这样也导致了该对象的配置会略显复杂。
	 * 
	 * 下面会做一个简单的factory配置，若希望看一个比较完整的，可以参考：cn.ajsgn.common.java8583.quickstart.QuickStartFactory
	 * 
	 * 填充策略:
	 * 因为在解析报文时，BCD编码会把两个字符压缩成为1个字节，所以，当报文字段数据为BDC编码，并且长度为奇数时，便需要去考虑填充位的问题了。
	 * 填充方式分为：左对齐，右填充模式  与   右对齐，左填充模式。（填充内容为[0-F]，如果数据溢出byte表示范围，将为FF）
	 * 
	 * 默认使用填充策略：左对齐，右补零
	 * 
	 * 部分字段类型未做演示，后续章节会介绍，慢慢看
	 * 
	 */
	public static Iso8583MessageFactory chapter3() {
		Iso8583MessageFactory facotry = new Iso8583MessageFactory(2, false, Charset.forName("UTF-8"),chapter2());
		facotry
			   //使用默认填充策略
			   .set(2, new Iso8583FieldType(FieldTypeValue.NUMERIC, 8))
			   //使用左对齐，右补A
			   .set(3, new Iso8583FieldType(FieldTypeValue.NUMERIC, 10).setFillBlankStrategy(Iso8583FillBlankStrategy.rightAppendStrategy('A', false)))
			   //使用右对齐，左补B
			   .set(4, new Iso8583FieldType(FieldTypeValue.LLVAR_NUMERIC, 0).setFillBlankStrategy(Iso8583FillBlankStrategy.leftAppendStrategy('B', false)))
			   //使用左对齐，右补X
			   //超过byte范围的一律表示为FF
			   .set(5, new Iso8583FieldType(FieldTypeValue.LLLVAR_NUMERIC, 0).setFillBlankStrategy(Iso8583FillBlankStrategy.rightAppendStrategy('X', true)))
			   .set(6, new Iso8583FieldType(FieldTypeValue.LLLLVAR_NUMERIC, 0).setFillBlankStrategy(Iso8583FillBlankStrategy.rightAppendStrategy('D', true)))
			   .set(7, new Iso8583FieldType(FieldTypeValue.CHAR, 4))
			   .set(8, new Iso8583FieldType(FieldTypeValue.LLVAR_CHAR, 0))
			   .set(9, new Iso8583FieldType(FieldTypeValue.LLLVAR_CHAR, 0))
			   .set(10, new Iso8583FieldType(FieldTypeValue.LLLLVAR_CHAR, 0));
		return facotry;
	}
	
	/*
	 * Iso8583Message
	 * 
	 * 作为Java8583的另一个核心类文件，它承载了整个报文数据结构的封装与报文数据的组装。
	 * 
	 * 与Iso8583MessageFactory比较，Iso8583Message具备构造函数简单，使用方便，易于写单元测试，接口友好等几大特点。
	 * 
	 * 下面是一个简单的报文数据封装。
	 * 
	 * 注意，定长字段，数据长度与长度描述一定要一致
	 * 
	 */
	public static Iso8583Message chapter4() {
		Iso8583Message message = new Iso8583Message(chapter3());
		message.setTpdu("0000000000")
			   .setHeader("111111111111")
			   .setMti("0200")
			   //12345670
			   .setValue(2,"1234567")
			   //123451234A
			   .setValue(3, "123451234")
			   //05B11111
			   .setValue(4, "11111")
			   //000422FF
			   .setValue(5, "222")
			   //000010123456789E
			   .setValue(6, "123456789")
			   //这里根据设置的字符集不同，会有不同的结果长度与编码结果，都可能不同
			   .setValue(7, "qwer")
			   //0731323334353637
			   .setValue(8, "1234567")
			   //0006414243444546
			   .setValue(9, "ABCDEF")
			   //这里根据设置的字符集不同，会有不同的结果长度与编码结果，都可能不同
			   .setValue(10, "哈哈哈哈~");
		//setXX ... 
//		message.setValue(32, "0123456789");		// NullPointerException 没有对当前索引做配置
		System.out.println(message.getBytesString());
		return message;
	}
	
	/*
	 * 下面将展示对数据 “1234506789”不同的类型表示的配置与相应的报文结果
	 * 
	 * 为什么使用数据“1234506789”做演示？
	 * 因为该组数据长度位偶数，可以减少代码量，不用添加自动填充策略
	 * 
	 * 为什么是“1234506789”，而不是“1234567890”？
	 * 因为“1234567890”末尾是0结尾，而对于BCD编码的类型，默认会使用“左对齐，右补0，长度位不计算填充位”，
	 * 所以，如果使用的是“1234567890”这个数据，则计算出来的长度为9而不是10，就可能会出现BUG。
	 * 
	 * 所以，当在使用BCD编码的数据的时候，一定要清楚自己所使用的填充策略。否则，可能会出现解析后的数据与解析前的数据不一致，原因就是长度为计算的错误。
	 * 
	 */
	public static void chapter4_1() {
		
		String value = "1234506789";
		
		Iso8583DataHeader header = new Iso8583DataHeader(
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,0),
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,0),
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,0),
			new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,0)
		);
		
		Iso8583MessageFactory facotry = new Iso8583MessageFactory(2,false,Charset.forName("GBK"),header);
		
		/*
		 * 下面的配置即为对于value值所需要做的配置示例
		 * 
		 * 下面的注释即为不同的配置对value的拼装的结果
		 * 
		 */
		facotry
				//1234506789
				.set(2, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.NUMERIC,10))
				//1234506789
				.set(3, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.BYTE_NUMERIC,5))
				//31323334353036373839
				.set(4, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.CHAR,10))
				//空格为将长度描述与数据描述分开表示，实际解析结果中没有空格
				//10 1234506789
				.set(5, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLVAR_NUMERIC,0))
				//05 1234506789
				.set(6, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLVAR_BYTE_NUMERIC,0))
				//10 31323334353637383930
				.set(7, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLVAR_CHAR,0))
				//0010 1234506789
				.set(8, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLVAR_NUMERIC,0))
				//0005 1234506789
				.set(9, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLVAR_BYTE_NUMERIC,0))
				//0010 31323334353036373839
				.set(10, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLVAR_CHAR,0))
				//000010 1234506789
				.set(11, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLLVAR_NUMERIC,0))
				//000005 1234506789
				.set(12, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLLVAR_BYTE_NUMERIC,0))
				//000010 31323334353036373839
				.set(13, new Iso8583FieldType(Iso8583FieldType.FieldTypeValue.LLLLVAR_CHAR,0));
		
		Iso8583Message message = new Iso8583Message(facotry);
		
		for(int i=2;i<14;i++){
			message.setValue(i, value);
		}
		
		System.out.println(message.getBytesString());
	}
	
	/*
	 * Iso8583MessageFactory
	 * 作为Java8583的核心类，除了负责约束报文格式，报文解析也在其职责范围内。
	 * 其中解析方法不考虑重载的情况下有两个：
	 * parse：解析包含报文长度描述的报文内容
	 * 	|--parse(String data)
	 * 		|--通过报文字符串解析报文，其中报文字符串包含报文长度描述
	 * 	|--parse(byte[] data)
	 * 		|--通过报文字节数组解析报文，其中报文字节数组包含报文长度描述
	 * parseWithoutMsgLength：解析不包含报文长度描述的报文内容
	 * 	|--parseWithoutMsgLength(String data)
	 * 		|--通过报文字符串解析报文，其中报文字符串不包含报文长度描述
	 * 	|--parseWithoutMsgLength(byte[] data)
	 * 		|--通过报文字节数组解析报文，其中报文字节数组不包含报文长度描述
	 * 
	 * 通常在报文测试阶段，可以明文看到报文内容与计算出报文内容，所以用parse()方法可能多点，
	 * 开发阶段，报文数据通常都已经读取出来，不再具备整体报文长度描述，所以用parseWithoutMsgLength()方法多点
	 */
	public static void chapter5() {
		Iso8583MessageFactory factory = chapter3();
		Iso8583Message message = chapter4();
		
		System.out.println(EncodeUtil.hex(message.getBytes()));
		System.out.println(message.getBytesString());
		
		Iso8583Message m1 = factory.parse(message.getBytes());
		Iso8583Message m2 = factory.parse(message.getBytesString());
		
		System.out.println(m1.compareWith(m2));
		
	}
	
	/*
	 * 如果服务端与客户端编码字符集不统一，则在转换时，就会发生乱码问题。
	 * 
	 * 所以，如果要避免乱码问题，客户端一定要和服务端编码字符集保持一致~！！！
	 * 
	 * 友情提醒，到时候出错，别赖框架！~~
	 * 
	 * 下面是错误示例
	 */
	public static void chapter6() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Iso8583MessageFactory factory = chapter3();
		Iso8583Message message = chapter4();
		
		System.out.println(message.getValue(10).getValue());
		
		Field field = Iso8583MessageFactory.class.getDeclaredField("charset");
		field.setAccessible(true);
		field.set(factory, Charset.forName("GBK"));
		
		Iso8583Message result = factory.parse(message.getBytes());
		System.out.println(result.getValue(10).getValue());
	}
	
	/*
	 * 单例的优点与好处，此处不做多余赘述。
	 * 
	 * 为什么不能直接使用单例？有什么需要注意的？
	 * 都希望服务方的报文字段返回均为相同编码方式，但其实并不是这样，下面代码会列举一个某发卡方的签到报文。
	 * 该签到报文响应中，62域为BCD编码，但在其他响应中为ASCII编码。
	 * 所以，如果不做特殊处理的话，至少需要用到两个facotry。一个用于拼装请求报文，一个用于拼装响应报文。
	 * 下例中用到了一个简单的特殊字段，62域。其中62域在factory中配置为ASCII编码。
	 * 而在 SpecialField62 类中给出判断，如果mti=0810，则使用BCD编码格式。
	 * 
	 * 具体写法，参考 cn.ajsgn.common.java8583.quickstart.special.SpecialField62 源码
	 * 
	 */
	public static void chapter7() {
		Iso8583MessageFactory factory = SingletonFactory.forQuickStart();
		Iso8583Message message0800 = factory.parse(MTI0800);
		System.out.println(message0800.toFormatString());
		
		//62字段的特殊处理逻辑
		factory.setSpecialFieldHandle(62, new SpecialField62());
		
		Iso8583Message message0810 = factory.parse(MTI0810);
		System.out.println(message0810.toFormatString());
		
	}
	
	/*
	 * 看到这里，应该已经完全掌握了Java8583的使用。
	 * 
	 * 这里多说一句，如何保证自己配置的factory是一个正确的factory呢？
	 * 答案很简单，那就是一个报文在解析后调用getBytesString()与原报文字符串的 hex equals 为true
	 * 
	 */
	public static void chapter8() {
		Iso8583MessageFactory factory = SingletonFactory.forQuickStart();
		Iso8583Message message0800 = factory.parse(MTI0800);
		System.out.println(message0800.getBytesString());
		System.out.println(MTI0800.equals(message0800.getBytesString()));
		//62字段的特殊处理逻辑
		factory.setSpecialFieldHandle(62, new SpecialField62());
		
		Iso8583Message message0810 = factory.parse(MTI0810);
		System.out.println(message0810.getBytesString());
		System.out.println(MTI0810.equals(message0810.getBytesString()));
	}
	
	/*
	 * tlv-55域使用演示
	 */
	public static void chapter9_1() {
		//一个55域字符串表示
		//报文解析示例
		String data = "9F2608AAA4D321391F16849F2701809F10160706A203206E00010DA11111111100001509071900809F3704B983E6289F36020004950500000000009A031605189C01009F02060000000000015F2A020156820200409F1A0201569F03060000000000009F3303E0E9C89F34030000009F3501229F1E0831323320202020208408F0000006660101019F090202009F631031343239333330300020040000000000";
		TlvParser parser = TlvParserFactory.forTlvParse(TlvParserType.ISO8583_FIELD_55_VERSION5);
		TlvObject tlvObject = parser.tlvParse(data);
		System.out.println(tlvObject.get("9F26".toUpperCase()));
		System.out.println(tlvObject.toLocalString().equals(data));
		
		//值更新
		//因为TlvValue实现对象有包访问限制，所以，使用工厂类创建对象
		tlvObject.put(TlvValueFactory.field55TlvValueInstance("9F26", "1234567890123456"));
		System.out.println(tlvObject.toLocalString());
		
		//值删除
		tlvObject.remove(TlvValueFactory.field55TlvValueInstance("9F26", "1234567890123456"));
		System.out.println(tlvObject.toLocalString());
		
		//数据还原
		tlvObject = parser.tlvParse(data);
		
		//报文组装示例
		LinkedList<TlvValue> result = new LinkedList<TlvValue>();
		for(TlvValue value : tlvObject.values()){
			result.add(TlvValueFactory.field55TlvValueInstance(value.getTagName(), value.getTagValue()));
		}
		//拼装完成，做toString
		StringBuilder resultStr = new StringBuilder();
		for(TlvValue value : result){
			resultStr.append(value.toLocalString());
		}
		System.out.println(resultStr.toString());
		System.out.println(data.equals(resultStr.toString()));
		
	}

}
