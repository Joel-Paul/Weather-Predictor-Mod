package issame.weatherpredictor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WeatherPredictorMod implements ModInitializer {

	public static final WeatherPredictorItem WEATH_PRED_ITEM = new WeatherPredictorItem(new FabricItemSettings().group(ItemGroup.TOOLS));
	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("weatherpredictor", "weather_predictor"), WEATH_PRED_ITEM);
	}
}
