package com.alrex.parcool.proxy;

import net.minecraftforge.network.SimpleChannel;

public abstract class CommonProxy {
    public boolean ParCoolIsActive() {
        return true;
    }
	public abstract void registerMessages(SimpleChannel instance);

	public void init() {
	}
}
