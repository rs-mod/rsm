package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class HitProcessEvent extends Event {
	public static class Position extends HitProcessEvent {
		@Getter
		private final Consumer<Vec3> positionVectorConsumer;

		public Position(Consumer<Vec3> positionVectorConsumer) {
			this.positionVectorConsumer = positionVectorConsumer;
		}
	}

	public static class Rotation extends HitProcessEvent {
		@Getter
		private final Consumer<Vec3> rotationVectorConsumer;

		public Rotation(Consumer<Vec3> rotationVectorConsumer) {
			this.rotationVectorConsumer = rotationVectorConsumer;
		}
	}
}
