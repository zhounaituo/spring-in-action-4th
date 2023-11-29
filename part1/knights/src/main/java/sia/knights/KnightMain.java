package sia.knights;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KnightMain {

    public static void main(String[] args) throws Exception {
        // 这里通过 ClassPathXmlApplicationContext 将 xml 的配置装配起来使用
        ClassPathXmlApplicationContext context
                = new ClassPathXmlApplicationContext("META-INF/spring/knight.xml");
        Knight knight = context.getBean(Knight.class);
        knight.embarkOnQuest();
        context.close();
    }
}
