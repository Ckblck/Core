package uk.lewdev.standmodels.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

public class ModelManager {

	private final JavaPlugin plugin;

	private final List<Model> staticModels = new ArrayList<>();
	private final List<AnimatedModel> animatedModels = new ArrayList<>();

	public ModelManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public JavaPlugin getPlugin() {
		return this.plugin;
	}

	public void spawnModel(Model model) {
		if (model instanceof AnimatedModel) {
			AnimatedModel animated = (AnimatedModel) model;
			
			if (this.animatedModels.contains(animated)) {
				throw new IllegalStateException("Trying to spawn model that has already been spawned");
			}
			
			this.animatedModels.add(animated);
		} else {
			if (this.staticModels.contains(model)) {
				throw new IllegalStateException("Trying to spawn model that has already been spawned");
			}
			
			this.staticModels.add(model);
		}
	}

	public void removeModel(Model model) {
		if (model instanceof AnimatedModel) {
			this.animatedModels.remove(model);
		} else {
			this.staticModels.remove(model);
		}

		model.unRender();
	}

	/**
	 * Get a Model a ArmorStand belongs to. Can return static or animated model.
	 * 
	 * @return Model | Null if stand doesn't belong to a model
	 */
	public Optional<Model> getModel(ArmorStand stand) {
		for (Model m : this.staticModels) {
			if (m.isStandPart(stand)) {
				return Optional.of(m);
			}
		}
		
		for (Model m : this.animatedModels) {
			if (m.isStandPart(stand)) {
				return Optional.of(m);
			}
		}

		return Optional.empty();
	}

	/**
	 * @return List<Model> Containing just static (un-animated) models
	 */
	public List<Model> getStaticModels() {
		return this.staticModels;
	}

	/**
	 * @return List<Model> Containing just animated models
	 */
	public List<AnimatedModel> getAnimatedModels() {
		return this.animatedModels;
	}
}