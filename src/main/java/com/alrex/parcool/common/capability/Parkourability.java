package com.alrex.parcool.common.capability;

import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.ActionList;
import com.alrex.parcool.common.action.AdditionalProperties;
import com.alrex.parcool.common.action.CancelMarks;
import com.alrex.parcool.common.capability.capabilities.Capabilities;
import com.alrex.parcool.common.info.ActionInfo;
import com.alrex.parcool.common.info.ClientSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class Parkourability {
	@Nullable
	public static Parkourability get(Player player) {
		LazyOptional<Parkourability> optional = player.getCapability(Capabilities.PARKOURABILITY_CAPABILITY);
		return optional.orElse(null);
	}

    private final ActionInfo info;
	private final AdditionalProperties properties = new AdditionalProperties();
    private final CancelMarks cancelMarks = new CancelMarks();
	private final List<Action> actions = ActionList.constructActionsList();
	private final HashMap<Class<? extends Action>, Action> actionsMap;

	public Parkourability() {
		actionsMap = new HashMap<>((int) (actions.size() * 1.5));
        for (Action action : actions) {
			actionsMap.put(action.getClass(), action);
		}
        info = new ActionInfo();
	}

	public <T extends Action> T get(Class<T> action) {
		T value = (T) actionsMap.getOrDefault(action, null);
		if (value == null) {
			throw new IllegalArgumentException("The Action instance is not registered:" + action.getSimpleName());
		}
		return value;
	}

	public short getActionID(Action instance) {
		return ActionList.getIndexOf(instance.getClass());
	}

	@Nullable
	public Action getActionFromID(short id) {
		if (0 <= id && id < actions.size()) {
			return actions.get(id);
		}
		return null;
	}

	public AdditionalProperties getAdditionalProperties() {
		return properties;
	}

    public CancelMarks getCancelMarks() {
        return cancelMarks;
    }

	public ActionInfo getActionInfo() {
		return info;
	}

    public ClientSetting getClientInfo() {
        return info.getClientSetting();
	}

	public List<Action> getList() {
		return actions;
	}

	public void CopyFrom(Parkourability original) {
        getActionInfo().setClientSetting(original.getActionInfo().getClientSetting());
        getActionInfo().setServerLimitation(original.getActionInfo().getServerLimitation());
	}
}
