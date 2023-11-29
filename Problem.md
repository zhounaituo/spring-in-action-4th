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
