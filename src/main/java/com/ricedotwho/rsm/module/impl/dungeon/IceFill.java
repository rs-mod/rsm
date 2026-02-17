package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Line;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;


import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "IceFill", id = "IceFill", category = Category.DUNGEONS)
public class IceFill extends Module {
	private static final List<List<Solution>> patterns = List.of(List.of(new Solution(new BPPair(new BlockPos(1, 70, -8), new BlockPos(1, 70, -6)), List.of(new BlockPos(0, 70, -8), new BlockPos(1, 70, -8), new BlockPos(1, 70, -7), new BlockPos(-1, 70, -7), new BlockPos(-1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new BPPair(new BlockPos(-1, 70, -8), new BlockPos(-1, 70, -6)), List.of(new BlockPos(0, 70, -8), new BlockPos(-1, 70, -8), new BlockPos(-1, 70, -7), new BlockPos(1, 70, -7), new BlockPos(1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new BPPair(new BlockPos(-1, 70, -6), new BlockPos(-1, 70, -8)), List.of(new BlockPos(0, 70, -8), new BlockPos(0, 70, -7), new BlockPos(-1, 70, -7), new BlockPos(-1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new BPPair(new BlockPos(1, 70, -6), new BlockPos(1, 70, -8)), List.of(new BlockPos(0, 70, -8), new BlockPos(0, 70, -7), new BlockPos(1, 70, -7), new BlockPos(1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5)))), List.of(new Solution(new BPPair(new BlockPos(0, 71, -2), new BlockPos(1, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 0), new BlockPos(1, 71, 0), new BlockPos(1, 71, -1), new BlockPos(0, 71, -1), new BlockPos(0, 71, -2), new BlockPos(-1, 71, -2), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(-1, 71, 1), new BlockPos(-1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, 2))), new Solution(new BPPair(new BlockPos(-1, 71, -1), new BlockPos(0, 71, -1)), List.of(new BlockPos(0, 71, -4), new BlockPos(0, 71, -4), new BlockPos(-2, 71, -4), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(-1, 71, 1), new BlockPos(-1, 71, -3), new BlockPos(0, 71, -3), new BlockPos(0, 71, -2), new BlockPos(1, 71, -2), new BlockPos(1, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, 2))), new Solution(new BPPair(new BlockPos(-1, 71, -1), new BlockPos(1, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new BPPair(new BlockPos(2, 71, -3), new BlockPos(0, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, -2), new BlockPos(1, 71, -2), new BlockPos(1, 71, -1), new BlockPos(2, 71, -1), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new BPPair(new BlockPos(0, 71, 0), new BlockPos(2, 71, -3)), List.of(new BlockPos(0, 71, -3), new BlockPos(1, 71, -3), new BlockPos(1, 71, -2), new BlockPos(2, 71, -2), new BlockPos(2, 71, -1), new BlockPos(1, 71, -1), new BlockPos(1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new BPPair(new BlockPos(1, 71, -1), new BlockPos(0, 71, -1)), List.of(new BlockPos(0, 71, -4), new BlockPos(0, 71, -4), new BlockPos(2, 71, -4), new BlockPos(2, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, -3), new BlockPos(0, 71, -3), new BlockPos(0, 71, -2), new BlockPos(-1, 71, -2), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(-1, 71, 1), new BlockPos(-1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, 2)))), List.of(new Solution(new BPPair(new BlockPos(-2, 72, 8), new BlockPos(-1, 72, 8)), List.of(new BlockPos(0, 72, 3), new BlockPos(0, 72, 3), new BlockPos(2, 72, 3), new BlockPos(2, 72, 4), new BlockPos(2, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 5), new BlockPos(3, 72, 5), new BlockPos(3, 72, 6), new BlockPos(-3, 72, 6), new BlockPos(-3, 72, 10), new BlockPos(-2, 72, 10), new BlockPos(-2, 72, 7), new BlockPos(3, 72, 7), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(-1, 72, 9), new BlockPos(-1, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11))), new Solution(new BPPair(new BlockPos(1, 72, 7), new BlockPos(1, 72, 6)), List.of(new BlockPos(0, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 5), new BlockPos(-1, 72, 5), new BlockPos(-1, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 5), new BlockPos(-2, 72, 5), new BlockPos(-2, 72, 6), new BlockPos(-3, 72, 6), new BlockPos(-3, 72, 10), new BlockPos(-2, 72, 10), new BlockPos(-2, 72, 7), new BlockPos(1, 72, 7), new BlockPos(1, 72, 8), new BlockPos(-1, 72, 8), new BlockPos(-1, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 11))), new Solution(new BPPair(new BlockPos(-3, 72, 6), new BlockPos(-2, 72, 6)), List.of(new BlockPos(0, 72, 3), new BlockPos(0, 72, 3), new BlockPos(4, 72, 3), new BlockPos(4, 72, 11), new BlockPos(2, 72, 11), new BlockPos(2, 72, 10), new BlockPos(2, 72, 10), new BlockPos(3, 72, 10), new BlockPos(3, 72, 4), new BlockPos(2, 72, 4), new BlockPos(2, 72, 9), new BlockPos(1, 72, 9), new BlockPos(1, 72, 7), new BlockPos(0, 72, 7), new BlockPos(0, 72, 6), new BlockPos(1, 72, 6), new BlockPos(1, 72, 4), new BlockPos(-1, 72, 4), new BlockPos(-1, 72, 5), new BlockPos(-2, 72, 5), new BlockPos(-2, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 7), new BlockPos(-1, 72, 7), new BlockPos(-1, 72, 8), new BlockPos(-3, 72, 8), new BlockPos(-3, 72, 9), new BlockPos(-2, 72, 9), new BlockPos(-2, 72, 10), new BlockPos(-1, 72, 10), new BlockPos(-1, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 11))), new Solution(new BPPair(new BlockPos(0, 72, 7), new BlockPos(1, 72, 7)), List.of(new BlockPos(0, 72, 4), new BlockPos(0, 72, 5), new BlockPos(1, 72, 5), new BlockPos(1, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 5), new BlockPos(2, 72, 5), new BlockPos(2, 72, 6), new BlockPos(3, 72, 6), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 6), new BlockPos(-1, 72, 6), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11))), new Solution(new BPPair(new BlockPos(-2, 72, 7), new BlockPos(-1, 72, 7)), List.of(new BlockPos(0, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 5), new BlockPos(1, 72, 5), new BlockPos(1, 72, 6), new BlockPos(3, 72, 6), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(-1, 72, 9), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 6), new BlockPos(-1, 72, 6), new BlockPos(-1, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 9), new BlockPos(-2, 72, 9), new BlockPos(-2, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11)))));

	private final BooleanSetting solverEnabled = new BooleanSetting("Solver", true);

	protected List<Pos> path = null;
	protected Room room = null;

	public IceFill() {
		this.registerProperty(solverEnabled);
	}

	@SubscribeEvent
	public void onDungeonRoom(DungeonEvent.ChangeRoom event) {
		if (event.unique == null || event.room == null) return;
		if (!"Ice Fill".equals(event.unique.getName())) {
			path = null;
			room = null;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		path = new ArrayList<>();
		this.room = event.room;
		outer: for (List<Solution> pattern : patterns) {
			for (Solution solution : pattern) {
				if (mc.level.getBlockState(this.room.getRealPosition(solution.check.first)).getBlock() != Blocks.AIR) continue;
				if (mc.level.getBlockState(this.room.getRealPosition(solution.check.second)).getBlock() == Blocks.AIR) continue;
				for (BlockPos pos : solution.path) path.add(this.room.getRealPosition(getCentre(pos)));
				continue outer;
			}
			ChatUtils.chat("Unable to find solution!");
			path = null;
			return;
		}
		ChatUtils.chat("Solution found!");
	}

	@SubscribeEvent
	public void onRender(Render3DEvent.Last event) {
		if (!solverEnabled.getValue()) return;
		if (path == null) return;
		for (int i = 0; i < path.size() - 1; ++i) {
			Pos point1 = path.get(i);
			Pos point2 = path.get(i + 1);
			Renderer3D.addTask(new Line(point1.asVec3(), point2.asVec3(), Colour.GREEN, Colour.GREEN, false));
		}
	}

	private static class BPPair {
		public BlockPos first;
		public BlockPos second;

		public BPPair(BlockPos first, BlockPos second) {
			this.first = first;
			this.second = second;
		}
	}

	private static class Solution {
		public BPPair check;
		public List<BlockPos> path;

		public Solution(BPPair check, List<BlockPos> path) {
			this.check = check;
			this.path = path;
		}
	}

	private static Pos getCentre(BlockPos bp) {
		return new Pos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
	}
}
