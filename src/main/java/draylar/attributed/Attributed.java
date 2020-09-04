package draylar.attributed;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Attributed implements ModInitializer {

    @Override
    public void onInitialize() {
        CustomEntityAttributes.init();
    }

    public static Identifier id(String id) {
        return new Identifier("attributed", id);
    }
}
