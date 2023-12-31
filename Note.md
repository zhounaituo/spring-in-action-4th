# 1. Spring 的核心

1. Spring 的核心主要依赖于：
	1. 依赖注入（dependency injection, DI）
	2. 面向切面编程（aspect-oriented programming, AOP）
2. Spring 的主要解决的问题：
	1. 基于简单的老式 Java 对象或低入侵式编程
	2. 通过注入依赖和面向接口编程实现程序解耦
	3. 利用切面和惯例进行[声明式编程](https://en.wikipedia.org/wiki/Declarative_programming)
	4. 通过切面和模板减少样板式代码

## 1.1. 依赖注入

1. 依赖注入解决的是代码的 **耦合性** 问题。耦合性代码有两个主要的问题：
	1. 难以测试、难以复用、难以理解，并且存在修改一个 bug 会引起新的 bug 的可能。
	2. 耦合无可避免。复杂的系统中不同的代码之前或多或少的需要耦合。
2. 主要的依赖注入方式：
	1. 构造器注入
### 1.1.1. 关键概念

1. 依赖注入：将相互耦合的业务分离，由第三方组件管理并协调对象之间的关系的一种编程范式。
2. 装配（wiring）：创建应用组件之间协作的行为。
### 1.1.2. 使用 DI 与否之间的对比

1. 没有使用 DI 的方式

这是一段老式的业务写法，里面存在的耦合来源于 `RescueDamselQuest` 在 `DamselRescuingKnight` 中自行创建了：
```java
package sia.knights;

public class DamselRescuingKnight implements Knight {

  private RescueDamselQuest quest;

  // 这里存在耦合关系
  public DamselRescuingKnight() {
    this.quest = new RescueDamselQuest();
  }

  public void embarkOnQuest() {
    quest.embark();
  }
  
}
```

2. 利用 DI 的代码

这是利用注入解耦后的代码：
```java
package sia.knights;
  
public class BraveKnight implements Knight {

  private Quest quest;

  // 主要关注这里
  public BraveKnight(Quest quest) {
    this.quest = quest;
  }

  public void embarkOnQuest() {
    quest.embark();
  }

}
```

### 1.1.3. 使用依赖注入的步骤

```bash 
1. 创建解耦代码
2. 装配应用之间的协作关系
3. 组装程序
```

1. 创建解耦代码

```java
package sia.knights;
  
public class BraveKnight implements Knight {

  private Quest quest;

  // 这里使用的是接口 Quest，而不是实际的实现类。
  public BraveKnight(Quest quest) {
    this.quest = quest;
  }

  public void embarkOnQuest() {
    quest.embark();
  }

}
```

```java
/*
 * SlayDragonQuest 是需要注入到 BraveKnight 中的 Quest 的实现类
 */
package sia.knights;

import java.io.PrintStream;

public class SlayDragonQuest implements Quest {

  private PrintStream stream;

  public SlayDragonQuest(PrintStream stream) {
    this.stream = stream;
  }

  public void embark() {
    stream.println("Embarking on quest to slay the dragon!");
  }

}
```
2. 装配应用之间的协作关系

```xml
<!-- 利用 XML 装配 SlayDragonQuest 和 BraveKnight 之间的依赖关系 -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="
    http://www.springframework.org/schema/beans 
    http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="knight" class="sia.knights.BraveKnight">
    <constructor-arg ref="quest" /> <!-- 这里使用 id 将 SlayDragonQuest 注入了 -->
  </bean>

  <bean id="quest" class="sia.knights.SlayDragonQuest">
    <constructor-arg value="#{T(System).out}" />
  </bean>

</beans>
```

```java
/*
 * 这里使用 Java 来描述配置
 */
package sia.knights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sia.knights.BraveKnight;
import sia.knights.Knight;
import sia.knights.Quest;
import sia.knights.SlayDragonQuest;

@Configuration
public class KnightConfig {

  @Bean
  public Knight knight() {
    return new BraveKnight(quest());
  }
  
  @Bean
  public Quest quest() {
    return new SlayDragonQuest(System.out);
  }

}
```

3. 组装程序

```java
/*
 * 使用 ClassPathXmlApplicationContext 创建和组装 XML 中的 bean
 */
package sia.knights;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KnightMain {

  public static void main(String[] args) throws Exception {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/knight.xml");
    Knight knight = context.getBean(Knight.class);
    knight.embarkOnQuest();
    context.close();
  }

}
```
## 1.2. 面向切面编程

面向切面编程往往被定义为促使软件系统实现关注点的分离一项技，将不同的应用模块化，并以声明的方式将应用放入需要的组件中。主要实现诸如日志、事务管理和安全这样的系统服务。
### 1.2.1. 关键概念

1. 面向切面编程：将遍布各处的功能分离出来形成可复用的组件的一种编程范式。

### 1.2.2. 对比

1. 创建一个 Minstrel 用于在 BraveKnight.embark 前后调用某个方法
```java
/*
 * 定义一个 Minstrel 将在 BraveKnight 前后使用方法
*/
package sia.knights;  
  
import java.io.PrintStream;  
  
public class Minstrel {  
  
    private PrintStream stream;  
  
    public Minstrel(PrintStream stream) {  
        this.stream = stream;  
    }  
  
    public void singBeforeQuest () {  
        stream.println("Fa la la, the knight is so brave!");  
    }  
  
    public void singAfterQuest() {  
        stream.println("Tee hee hee, the brave knight " +  
                "did embark on a quest!");  
    }  
}
```

2. 创建了 BraveKnight 利用旧的方式去调用 Minstrel 内的方法，形成了紧密的耦合，并且加大了 BraveKnight 的复杂性。
```java
/*
 * 
 */
package sia.knights;  
  
public class BraveKnight implements Knight {  
  
    private Quest quest;  
    private Minstrel minstrel;  
  
    public BraveKnight(Quest quest, Minstrel minstrel) {  
        this.quest = quest;  
        this.minstrel = minstrel;  
    }  
  
    public void embarkOnQuest() {  
        minstrel.singBeforeQuest();  
        quest.embark();  
        minstrel.singAfterQuest();  
    }  
}
```

### 1.2.3. 使用切面编程步骤

```bash 
1. 编写关键组件
2. 声明切片
3. 组装程序（同依赖注入）
```

1. 编写关键组件
```java
package sia.knights;  
  
import java.io.PrintStream;  
  
public class Minstrel {  
  
    private PrintStream stream;  
  
    public Minstrel(PrintStream stream) {  
        this.stream = stream;  
    }  
  
    public void singBeforeQuest () {  
        stream.println("Fa la la, the knight is so brave!");  
    }  
  
    public void singAfterQuest() {  
        stream.println("Tee hee hee, the brave knight " +  
                "did embark on a quest!");  
    }  
}
```

2. 声明切片

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:aop="http://www.springframework.org/schema/aop"
  xsi:schemaLocation="http://www.springframework.org/schema/aop 
  http://www.springframework.org/schema/aop/spring-aop.xsd
  http://www.springframework.org/schema/beans 
  http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="knight" class="sia.knights.BraveKnight">
    <constructor-arg ref="quest" />
  </bean>

  <bean id="quest" class="sia.knights.SlayDragonQuest">
    <constructor-arg value="#{T(System).out}" />
  </bean>

  <!-- 将 Minstrel 声明成 Bean -->
  <bean id="minstrel" class="sia.knights.Minstrel">
    <constructor-arg value="#{T(System).out}" />
  </bean>

  <aop:config>
	<!-- 将 minstrel 声明为切片 -->
    <aop:aspect ref="minstrel">
      <aop:pointcut id="embark"
          expression="execution(* *.embarkOnQuest(..))"/>
        
      <aop:before pointcut-ref="embark" 
          method="singBeforeQuest"/>

      <aop:after pointcut-ref="embark" 
          method="singAfterQuest"/>
    </aop:aspect>
  </aop:config>
  
</beans>
```

3. 组装程序（同依赖注入）

## 1.3. 容器

1. Spring 容器用于创建、配置和组装 Bean。Spring 自带了多种容器实现：
	1. bean 工厂。最简单的容器，提供基本的依赖注入支持，由 org.springframework.beans.factory.BeanFactory 接口定义。
	2. 应用上下文。基于 BeanFactory 构建，并提供应用级别的服务，由 org.springframework.context.ApplicationContext 接口定义

### 1.3.1. 使用上下文

| 类型                                  | 描述                                                                                    |
| ------------------------------------- | --------------------------------------------------------------------------------------- |
| AnnotationConfigApplicationContext    | 从 Java 配置类中加载上下文                                                              |
| AnnotationConfigWebApplicationContext | 从 Java 的配置类中加载 Spring Web 应用上下文                                            |
| ClassPathXmlApplicationContext        | 从类路径下的一个或多个 XML 配置文件中加载上下文定义，把应用上下文的定义文件作为类资源。 |
| FileSystemXmlapplicationcontext       | 从文件系统下的一 个或多个 XML 配置文件中加载上下文定义。                                |
| XmlWebApplicationContext              | 从 Web 应用下的一个或多个 XML 配置文件中加载上下文定义。                                |
|                                       |                                                                                         |

```java
ApplicationContext context = new FileSystemXmlApplicationContext("c:/knight.xml");
```

```java
ApplicationContext context = new ClassPathXmlApplicationContext("knight.xml");
```

```java
ApplicationContext context = new AnnotationConfigApplicationContext(com.springinaction.knights.config.KnightConfig.class);
```

### 1.3.2. bean 的生命周期

最简的生命周期：
```java
实例化 -> 填充属性 -> 调用自定义的初始化方法 | bean 可以使用了 -> 调用用自定义的销毁方法
```

![[life-cycle-bean.jpg]]

# 2. Spring 风景线

## 2.1. Spring 模块

1. 下面是完整的 Spring JAR 文件：
   
```bash
	spring-aop-4.0.0.RELEASE.jar
spring-aspects-4.0.0.RELEASE.jar
spring-beans-4.0.0RELEASE.jar
spring-context-4.0.0.RELEASE.jar
spring-context-support-4.0.0.RELEASE.jar
spring-core-4.0.0.RELEASE.jar
spring-expression-4.0.0.RELEASE.jar
spring-instrument-4.0.0.RELEASE.jar
spring-instrument-tomcat-4.0.0.RELEASE.jar
spring-jdbc-4.0.0.RELEASE.jar
spring-jms-4.0.0.RELEASE.jar
spring-messaging-4.0.0.RELEASE.jar
spring-orm-4.0.0.RELEASE.jar
spring-oxm-4.0.0.RELEASE.jar
spring-test-4.0.0.RELEASE.jar
spring-tx-4.0.0.RELEASE.jar
spring-web-4.0.0.RELEASE.jar
spring-webmvC-4.0.0.RELEASE.jar
spring-webmvc-portlet-4.0.0.RELEASE.jar
spring-websocket-4.0.0.RELEASE.jar
```

2. 下面是这些库的结构图：
   
![[spring-bean-lib.jpg]]

## 2.2. Spring Portfolio

| 类型               | 描述                                                                          |
| ------------------ | ----------------------------------------------------------------------------- |
| Spring Web Flow    | 基于流程的会话式 Web 应用                                                     |
| Spring Web Service | 提供了契约优先的 Web Service 模型，服务的实现都是为了满足服务的契约而编写的。 |
| Spring Security    | 为 Spring 应用提供了声明式的安全机制。                                        |
| Spring Integration | 提供了多种通用应用集成模式的 Spring 声明式风格实现。                          |
| Spring Batch       | 可以对数据进行大量操作，面向 POJO 的编程模型                                  |
| Spring Data        | 为不同类型的数据库持久化提供了一种简单的编程模型。                            |
| Spring Social      | 一个社交网络扩展模块                                                          |
| Spring Mobile      | 用于支持移动 Web 应用开发                                                     |
| Spring for Android | 与 Spring Mobile 相关的是 Spring Android 项目                                 |
| Spring Boot        | 它以 Spring 的视角， 致力于简化 Spring 本身。                                                                              |

# 3. 装配 Bean
## 3.1. 配置 Spring 的常用方法

1. Spring 主要提供了 3 种方式：
	1. XML 显式配置
	2. Java 显示配置
	3. 隐式的 Bean 发现机制和自动装配
> 3 种方式可以混搭，但是作者建议： 隐式的 Bean 发现机制和自动装配 > Java 显式配置 > XML 显式配置。

## 3.2. 自动装配

1. Spring 从两个角度来实现自动装配：
	1. 组件扫描（component scanning）：Spring 会自动发现应用上下文中所创建的 bean。
	2. 自动装配（autowiring）：Spring 自动满足 bean 之间的依赖。

### 3.2.1 创建自动装配的步骤

```bash
1. 利用 @Component 创建需要的 Bean 类, 利用 @Autowrite 申明自动装配
2. 利用 @ComponentScan 创建扫描类
```

1. 利用 @Bean 创建需要的 Bean 类
```java
package soundsystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CDPlayer implements MediaPlayer {
  private CompactDisc cd;

  @Autowired
  public CDPlayer(CompactDisc cd) {
    this.cd = cd;
  }

  public void play() {
    cd.play();
  }

}
```

2. 利用 @ComponentScan 创建扫描类。
```java
package sia.soundsystem;  
  
import org.springframework.context.annotation.ComponentScan;  
import org.springframework.context.annotation.Configuration;  
  
@Configuration  
@ComponentScan  
public class CDPlayerConfig {  
}
```

### 3.2.2 为组件扫描的 bean 命名

```bash 
1. Spring 应用上下文会默认给每一个 bean 一个 ID（通常为首字母小写的类名）。
2. 使用 @Component 命名
3. 使用 @Named 命名。
```

1. Spring 应用上下文会默认给每一个 bean 一个 ID（通常为首字母小写的类名）。
```java
SgtPeppers -> sgtPeppers
```

2. 使用 @Component 命名
```java
@Componet("lonelyHeartsClub")
public class SgtPeppers implements CompactDisc {
  ...
}
```

3. 使用 @Named 命名。
```java
package sia.soundsystem;

import javax.inject.Named;

@Named("lonelyHeartsClub")
public class SgtPeppers implements CompactDisc {
  ......
}
```
> 可以使用但是不建议，因为这个名字对于开发者来说没有明显的用意。

### 3.2.3. 设置扫描的基础包

```bash
1. 扫描会向 @ComponentScan 设置默认属性（以配置类所在的包作为基础包来扫描组件）。
2. 指定扫描的基础包
3. 更加清晰的表明基础包
4. 配置多个扫描包
5. 指定包中的接口或是类。
```

1. 扫描会向 @ComponentScan 设置默认属性（以配置类所在的包作为基础包来扫描组件）。
2. 指定扫描的基础包
```java
@Configuration
@ComponentScan("soundsystem")
public class CDPlayerConfig { }
```

3. 更加清晰的表明基础包
```java
@Configuration
@ComponentScan(basePackages="soundsystem")
public class CDPlayerConfig { }
```

4. 配置多个扫描包
```java
@Configuration
@ComponentScan(basePackages={"soundsystem", "video"})
public class CDPlayerConfig { }
```
> 这样的设置并不安全，如果变更了类名，那么这里的设置也需要修改。

5. 指定包中的接口或是类。
```java
@Configuration
@ComponentScan(basePackageClasses={CDPlayer.class, DVDPlayer.clas})
public class CDPlayerConfig { }
```

### 3.2.4. 通过为 Bean 添加注解实现自动装配

```bash
1. 通过构造器上的 @Autowired 自动装配
2. 也可以时 setting 方法
3. 也可以是其他任何类
4. 如果没有匹配的 bean 被注入，Spring 将会报错，可以通过 required 属性进行设置
```

1. 通过构造器上的 @Autowired 自动装配
```java
package soundsystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CDPlayer implements MediaPlayer {
  private CompactDisc cd;

  @Autowired
  public CDPlayer(CompactDisc cd) {
    this.cd = cd;
  }

  public void play() {
    cd.play();
  }

}
```
> 这里再构造器上方添加一个 @Autowired 注解，表示在创建 CDPlayerBean 时，注入一个 CompactDiscBean。

2. 也可以时 setting 方法
```java
@Autowired
public void setCompactDisc(CompactDisc cd){
  this.cd = cd;
}
```

3. 也可以是其他任何类
```java
@Autowired
public void insertDisc(CompactDisc cd){
  this.cd = cd;
}
```

4. 如果没有匹配的 bean 被注入，Spring 将会报错，可以通过 required 属性进行设置
```java
@Autowired(required=false)
public CDPlayer(CompactDisc cd) {
  this.cd = cd;
}
```
> 以上的代码，如果没有匹配的 bean 注入，那么这个 bean 将会是未装配状态，未装配状态的 bean 可能会引起 NullPointerException。

5. @Autowired 的替换方案 @Inject
```java
package soundsystem;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CDPlayer {
  ...

  @Inject
  public CDPlayer(CompactDisc cd) {
    this.cd = cd;
  }
  
  ...
}
```

### 3.2.5. 使用 xml 配置自动装配

```java
<?xml version="1.0" encoding="UTF-8"?>  
<beans xmlns="http://www.springframework.org/schema/beans"  
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
       xmlns:context="http://www.springframework.org/schema/context"  
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">  
  
    <context:component-scan base-package="sia.soundsystem" />  
</beans>
```

1. 在 xml 头部引入 context 元素
```java
<beans xmlns:context="http://www.springframework.org/schema/context"
```

2. 利用 context 指定需要扫描的包
```java
<beans>
	<context:component-scan base-package="sia.soundsystem" />  
</beans>
```

## 3.3. Java 代码装配

### 3.3.1. Java 代码配置步骤

```bash
1. 创建好需要注入的类（同 3.2.1 中的 1 方法），这里不同的是不需要使用 @Component 进行注解
2. 创建配置类，并注入
```

1. 创建好需要注入的类（同 3.2.1 中的 1 方法），这里不同的是不需要使用 @Component 进行注解
```java
package soundsystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class CDPlayer implements MediaPlayer {
  private CompactDisc cd;

  public CDPlayer(CompactDisc cd) {
    this.cd = cd;
  }

  public void play() {
    cd.play();
  }

}
```

2. 创建配置类，并注入
```java
package soundsystem;

import org.spingframework.context.annotation.Configuration;

@Configuration
public class CDPlayerConfig {

	@Bean
	public CompactDisc sgtPeppers() {
		return new SgtPeppers();
	}

	@Bean 
	public CDPlayer cdPlayer() {
		// 方法一: 这里使用了 sgtPeppers 的 bean ID 进行注入
		return new CDPlayer(sgtPeppers());
	}

	@Bean 
	public CDPlayer cdPlayer(CompactDisc compactDisc) {
		// 方法二：这里直接通过 CompactDisc 进行注入。
		return new CDPlayer(compactDisc)
	}

	@Bean
	public CDPlayer cdPlayer(CompactDisc compactDisc) {
		// 这里将 CompactDisc 注入到 CDPlayer 的 setter 方法中
		CDPlayer cdPlayer = new CDPlayer(compactDisc);
		cdPlayer.setCompactDisc(compactDisc);
		return cdPlayer;
	}
}
```
> 这里有几步：
> 	1. 去掉自动扫描注释 @ComponentScan
> 	2. 添加 Bean（由 @Bean 标志）
> 	3. 注入

## 3.4. 使用 XML 装配

> 获取一个基础的 Spring.xml 文件可以使用 [Spring Tool Suite](https://spring.io/tools/sts)。

### 3.4.1 装配一个简单的 Bean 的步骤

```bash
1. 注入一个简单的 bean 
2. 注入依赖关系，使用的是 `constructor-arg` 标签。
```

1. 注入一个简单的 bean 
```java 
<?xml version="1.0" encoding="UTF-8"?>  
<beans xmlns="http://www.springframework.org/schema/beans"  
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">  
  
    <bean id="compactDisc" class="sia.soundsystem.SgtPeppers" />  
</beans>
```
> 如果给出 id，那么默认 id 为类名加数组形式，该组默认为 `sia.soundsystem.SgtPeppers#0` 后面的 `#0` 表示第几个同类 bean。建议为每个明确需要注入的 Bean 添加 ID。

2. 注入依赖关系，使用的是 `constructor-arg` 标签。
```java
<beans ...>
	<bean id="compactDisc" class="sia.soundsystem.SgtPeppers" />  
	
	<bean id="cdPlay" class="sia.soundsystem.CDPlayer">  
		<constructor-arg ref="compactDisc" />  
	</bean>
</beans>
``` 

### 3.4.2. 使用 c-命名空间 的构造器注入

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans 
	xmlns="http://www.springframework.org/schema/beans"
	xmlns:c="http://www.springframework.org/schema/c"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd" 
>
	<bean id="cdPlayer" class="soundsystem.CDPlayer" c:cd-ref="compactDisc" />
</beans>
```

1. 需要声明 c 命名控件的模式
```xml
<beans 
	xmlns:c="http://www.springframework.org/schema/c"
	...
>
</beans>
```

2. 使用参数 `c:cd-ref` 注入
```xml
<bean id="cdPlayer" class="soundsystem.CDPlayer" c:cd-ref="compactDisc" />
```
#### 3.4.2.1 c-命名空间

![[c-name.jpg]]

c-命名空间的两种注入方式：
1. 这里直接引用了 cd (构造器参数名)
```java
c:cd-ref="compactDisc"
```
> 构造器参数名需要在编译代码的时候，将调试标识保存在类代码中。

2. 参数在整个列表的位置信息。
```java
c:_0-ref="compactDisc"
```
> `0` 表示参数的索引，但是 xml 不允许数字作为第一个字符，所以在前面加上了下划线 `_`。如果只有一个参数，甚至可以去掉 `0`，改为 `c:_-ref="compactDisc"`

### 3.4.3. 注入字面量

```bash
1. 创建一个基础类
2. 使用 constructor-arg 中的 value 属性注入
3. 使用 c-命名空间 注入
4. 注入空值
5. 声明一个列表
```

1. 创建一个基础类
```java
package soundsystem;

import java.util.List;

public class BlankDisc implements CompactDisc {

  private String title;
  private String artist;

  public BlankDisc(String title, String artist) {
    this.title = title;
    this.artist = artist;
  }

  public void play() {
    System.out.println("Playing " + title + " by " + artist);
  }

}
```
> 与之前不同，这里没有设置显示值给 title 和 artist 字段。

2. 使用 constructor-arg 中的 value 属性注入
```xml
<bean id="compactDisc" class="soundsystem.BlankDisc">
    <constructor-arg value="Sgt. Pepper's Lonely Hearts Club Band" />
    <constructor-arg value="The Beatles" />
</bean>
```

3. 使用 c-命名空间 注入
```xml
<bean id="compactDisc" class="soundsystem.BlankDisc"
      c:_tsitle="Sgt. Pepper's Lonely Hearts Club Band" 
      c:_artist="The Beatles" />
```
> 这里同样可以使用参数，或者省略数字（如果这里只有唯一的属性的话）。

4. 注入空值
```xml 
<constructor-arg><null/></constructor-arg>
```
> 这不是一个很好的方案，会遇到 NullPointerException 异常。

5. 声明一个列表
```xml
<constructor-arg>
<list>
<value>Sgt. Pepper's Lonely Hearts Club Band</value>
<value>With a Little Help from My Friends</value>
<value>Lucy in the Sky with Diamonds</value>
<value>Getting Better</value>
<value>Fixing a Hole</value>
<!-- ...other tracks omitted for brevity... -->
</list>
</constructor-arg>
```
> 这里可以将 value 标签换成 ref，实现列表 bean 引用列表的装载。同时，这也是 c-命名空间无法做到的地方。

### 3.4.4. 属性注入

```bash
1. 利用 property 注入属性
2. 利用 p-命名空间注入
3. 利用属性注入参数
4. 利用 p-命名空间 注入参数
```

1. 利用 property 注入属性
```xml
<bean id="cdPlayer" class="soundsystem.CDPlayer" >
  <property name="compactDisc" ref="compactDisc" />
</bean>
```

2. 利用 p-命名空间注入
```xml 
<bean id="cdPlayer" class="soundsystem.CDPlayer" p:compactDisc-ref="compactDisc" />
```

![[p-name.jpg]]

3. 利用属性注入参数
```xml
<bean id="compactDisc" class="soundsystem.BlankDisc">
  <property name="title" value="Sgt. Pepper's Lonely Hearts Club Band" />
  <property name="artist" value="The Beatles">
  <property name="tracks">
    <list>
      <value>Sgt. Pepper's Lonely Hearts Club Band</value>
      <value>With a Little Help from My Friends</value>
      <value>Lucy in the Sky with Diamonds</value>
      <value>Getting Better</value>
      <value>Fixing a Hole</value>
      <!-- ...other tracks omitted for brevity... -->
    </list>
  </property>
</bean>
```

4. 利用 p-命名空间 注入参数
```xml 
<bean id="compactDisc" class="soundsystem.BlankDisc"
      p:title="Sgt. Pepper's Lonely Hearts Club Band"
      p:artist="The Beatles" >
</bean>
```
> 与 `c-` 命名空间一样，装配 bean 引用与装配字面量的唯一区别在于是否带有 `-ref` 后缀。如果没有 `-ref` 后缀的话，所装配的就是字面量。

5. 使用 util- 标签来装载集合

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans 
  ...
  xmlns:util="http://www.springframework.org/schema/util"
  xsi:schemaLocation="
  http://www.springframework.org/schema/util
  http://www.springframework.org/schema/util/spring-util.xsd">
  ...
</beans>
```

```xml
<util:list id="trackList">
  <value>Sgt. Pepper's Lonely Hearts Club Band</value>
  <value>With a Little Help from My Friends</value>
  <value>Lucy in the Sky with Diamonds</value>
  <value>Getting Better</value>
  <value>Fixing a Hole</value>
  <!-- ...other tracks omitted for brevity... -->
</util:list>
```

|                    |                                                       |
| ------------------ | ----------------------------------------------------- |
| <util:constant>    | 引用某个类型的 public static 域，并将其暴露为 bean    |
| util:list          | 创建一个 java.util.List 类型的 bean，其中包含值或引用 |
| util:map           | 创建一个 java.util.Map 类型的 bean，其中包含值或引用  |
| util:properties    | 创建一个 java.util.Properties 类型的 bean             |
| util:property-path | 引用一个 bean 的属性（或内嵌属性），并将其暴露为 bean |
| util:set           | 创建一个 java.util.Set 类型的 bean，其中包含值或引用  |                                                       |

## 3.5. 导入和混合配置

