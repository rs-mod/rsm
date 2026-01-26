package com.ricedotwho.rsm;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;

@Getter
public class RSM implements ClientModInitializer {

    @Getter
    private static RSM instance;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        instance = this;
	}
}