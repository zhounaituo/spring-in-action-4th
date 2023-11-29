package sia.knights;

// 具有耦合性的旧项目模式
public class DamselRescuingKnight implements Knight{

    private RescueDamselQuest quest;

    public DamselRescuingKnight() {
        // 这里直接在构造器中创建了一个 RescueDamselQuest,
        // 导致 RescueDamselQuest 和 DamselRescuingKnight 耦合在一起。
        this.quest = new RescueDamselQuest();
    }

    public void embarkOnQuest() {
        quest.embark();
    }
}
