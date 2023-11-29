# 阅读笔记

## 1. Spring 的核心

1. Spring 的核心主要依赖于：
	1. 依赖注入（dependency injection, DI）
	2. 面向切面编程（aspect-oriented programming, AOP）
2. Spring 的主要解决的问题：
	1. 基于简单的老式 Java 对象或低入侵式编程
	2. 通过注入依赖和面向接口编程实现程序解耦
	3. 利用切面和惯例进行[声明式编程](https://en.wikipedia.org/wiki/Declarative_programming)
	4. 通过切面和模板减少样板式代码

### 1.1. 依赖注入

1. 依赖注入解决的是代码的 **耦合性** 问题。耦合性代码有两个主要的问题：
	1. 难以测试、难以复用、难以理解，并且存在修改一个 bug 会引起新的 bug 的可能。
	2. 耦合无可避免。复杂的系统中不同的代码之前或多或少的需要耦合。
2. 主要的依赖注入方式：
	1. 构造器注入
#### 1.1.1. 关键概念

1. 依赖注入：将相互耦合的业务分离，由第三方组件管理并协调对象之间的关系的一种编程范式。
2. 装配（wiring）：创建应用组件之间协作的行为。
#### 1.1.2. 使用 DI 与否之间的对比

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

#### 1.1.3. 使用依赖注入的步骤

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
### 1.2. 面向切面编程

面向切面编程往往被定义为促使软件系统实现关注点的分离一项技，将不同的应用模块化，并以声明的方式将应用放入需要的组件中。主要实现诸如日志、事务管理和安全这样的系统服务。
#### 1.2.1. 关键概念

1. 面向切面编程：将遍布各处的功能分离出来形成可复用的组件的一种编程范式。

#### 1.2.2. 对比

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

#### 1.2.3. 使用切面编程步骤

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

### 1.3. 容器

1. Spring 容器用于创建、配置和组装 Bean。Spring 自带了多种容器实现：
	1. bean 工厂。最简单的容器，提供基本的依赖注入支持，由 org.springframework.beans.factory.BeanFactory 接口定义。
	2. 应用上下文。基于 BeanFactory 构建，并提供应用级别的服务，由 org.springframework.context.ApplicationContext 接口定义

#### 1.3.1. 使用上下文

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

#### 1.3.2. bean 的生命周期

最简的生命周期：
```java
实例化 -> 填充属性 -> 调用自定义的初始化方法 | bean 可以使用了 -> 调用用自定义的销毁方法
```

![[life-cycle-bean.jpg]]

## 2. Spring 风景线

### 2.1. Spring 模块

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

### 2.2. Spring Portfolio

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

## 3. 装配 Bean
### 3.1. 配置 Spring 的常用方法

1. Spring 主要提供了 3 种方式：
	1. XML 显式配置
	2. Java 显示配置
	3. 隐式的 Bean 发现机制和自动装配
> 3 种方式可以混搭，但是作者建议： 隐式的 Bean 发现机制和自动装配 > Java 显式配置 > XML 显式配置。

### 3.2. 自动装配

1. Spring 从两个角度来实现自动装配：
	1. 组件扫描（component scanning）：Spring 会自动发现应用上下文中所创建的 bean。
	2. 自动装配（autowiring）：Spring 自动满足 bean 之间的依赖。

#### 3.2.1 创建自动装配的步骤

```bash
1. 利用 @Component 创建需要的 Bean 类
2. 利用 @ComponentScan 创建扫描类
```

1. 利用 @Bean 创建需要的 Bean 类
```java
package sia.soundsystem;  
  
import org.springframework.stereotype.Component;  
  
@Component  
public class SgtPeppers implements CompactDisc{  
    private String title = "Sgt. Pepper's Lonely Hearts Club Band";  
    private String artist = "The Beatles";  
  
    public void play() {  
        System.out.println("Playing " + title + " by " + artist);  
    }  
}
```

2. 利用 @ComponentScan 创建扫描类
```java
package sia.soundsystem;  
  
import org.springframework.context.annotation.ComponentScan;  
import org.springframework.context.annotation.Configuration;  
  
@Configuration  
@ComponentScan  
public class CDPlayerConfig {  
}
```

#### 3.2.2 为组件扫描的 bean 命名

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

#### 3.2.3. 设置扫描的基础包

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

#### 3.2.4. 通过为 Bean 添加注解实现自动装配

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