# 通配符的匹配很全面, 但无法找到元素 'aop:config' 的声明。

P: 通配符的匹配很全面, 但无法找到元素 'aop:config' 的声明。
```java
aused by: org.xml.sax.SAXParseException; lineNumber: 22; columnNumber: 17; cvc-complex-type.2.4.c: 通配符的匹配很全面, 但无法找到元素 'aop:config' 的声明。
```

A: 该问题在于 xml 配置文件中配置错误，注意一下段代码：
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/aop   http://www.springframework.org/schema/aop/spring-aop.xsd">
/>
```

# org.junit.ComparisonFailure

P: 打印出来的文字出现了过多的日志内容，是由于日志级别设置的缘故。
1. 出现报错：
![[comparisonFailure-01.png]]
2. 点击 `<Click to see difference>` 对比打印内容发现，多余了一些日志内容
![[comparison-failure-play.png]]
A: 通过 `log.clearLog()` 打印前清理即可。
```java
// CDPlayerTest.java

@Test  
public void play () { 
	log.clearLog(); // 这里清除了多余的打印
    player.play();  
    assertEquals(  
            "Playing Sgt. Pepper's Lonely Hearts Club Band by The Beatles\r\n",  
            log.getLog()  
    );  
}
```