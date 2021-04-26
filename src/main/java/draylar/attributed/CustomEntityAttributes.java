package draylar.attributed;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.registry.Registry;

public class CustomEntityAttributes {

    public static final EntityAttribute DIG_SPEED = register("generic.dig_speed", new ClampedEntityAttribute("attribute.name.generic.dig_speed", 0.0D, 0.0D, 2048.0D).setTracked(true));
    public static final EntityAttribute CRIT_CHANCE = register("generic.crit_chance", new ClampedEntityAttribute("attribute.name.generic.crit_chance", 0.0D, 0.0D, 5.0D).setTracked(true));

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return Registry.register(Registry.ATTRIBUTE, Attributed.id(id), attribute);
    }

    public static void init() {
        // NO-OP
    }

    private CustomEntityAttributes() {
        // NO-OP
    }
}
