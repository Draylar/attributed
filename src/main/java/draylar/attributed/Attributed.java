package draylar.attributed;

import draylar.attributed.dev.AttributedDevelopment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class Attributed implements ModInitializer {

    @Override
    public void onInitialize() {
        CustomEntityAttributes.init();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            System.out.println("\nThe Attributed development suite has been loaded.\n2 Development items will be added to the game, and sticks will always land a critical hit.\nThese test features will not occur outside dev.");
            new AttributedDevelopment().onInitialize();
        }
    }

    public static Identifier id(String id) {
        return new Identifier("attributed", id);
    }
}
