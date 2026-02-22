package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
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
@SubModuleInfo(name = "IceFill", alwaysDisabled = false)
public class IceFill extends SubModule<Puzzles> {
	private static final List<List<Solution>> patterns = List.of(List.of(new Solution(new Pair<>(new BlockPos(1, 70, -8), new BlockPos(1, 70, -6)), List.of(new BlockPos(0, 70, -8), new BlockPos(1, 70, -8), new BlockPos(1, 70, -7), new BlockPos(-1, 70, -7), new BlockPos(-1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new Pair<>(new BlockPos(-1, 70, -8), new BlockPos(-1, 70, -6)), List.of(new BlockPos(0, 70, -8), new BlockPos(-1, 70, -8), new BlockPos(-1, 70, -7), new BlockPos(1, 70, -7), new BlockPos(1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new Pair<>(new BlockPos(-1, 70, -6), new BlockPos(-1, 70, -8)), List.of(new BlockPos(0, 70, -8), new BlockPos(0, 70, -7), new BlockPos(-1, 70, -7), new BlockPos(-1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5))), new Solution(new Pair<>(new BlockPos(1, 70, -6), new BlockPos(1, 70, -8)), List.of(new BlockPos(0, 70, -8), new BlockPos(0, 70, -7), new BlockPos(1, 70, -7), new BlockPos(1, 70, -6), new BlockPos(0, 70, -6), new BlockPos(0, 70, -5)))), List.of(new Solution(new Pair<>(new BlockPos(0, 71, -2), new BlockPos(1, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 0), new BlockPos(1, 71, 0), new BlockPos(1, 71, -1), new BlockPos(0, 71, -1), new BlockPos(0, 71, -2), new BlockPos(-1, 71, -2), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(-1, 71, 1), new BlockPos(-1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, 2))), new Solution(new Pair<>(new BlockPos(-1, 71, -1), new BlockPos(0, 71, -1)), List.of(new BlockPos(0, 71, -3), new BlockPos(0, 71, -2), new BlockPos(1, 71, -2), new BlockPos(1, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, 0), new BlockPos(-1, 71, 0), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new Pair<>(new BlockPos(-1, 71, -1), new BlockPos(1, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new Pair<>(new BlockPos(2, 71, -3), new BlockPos(0, 71, -2)), List.of(new BlockPos(0, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, -2), new BlockPos(1, 71, -2), new BlockPos(1, 71, -1), new BlockPos(2, 71, -1), new BlockPos(2, 71, 1), new BlockPos(1, 71, 1), new BlockPos(1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new Pair<>(new BlockPos(0, 71, 0), new BlockPos(2, 71, -3)), List.of(new BlockPos(0, 71, -3), new BlockPos(1, 71, -3), new BlockPos(1, 71, -2), new BlockPos(2, 71, -2), new BlockPos(2, 71, -1), new BlockPos(1, 71, -1), new BlockPos(1, 71, 0), new BlockPos(0, 71, 0), new BlockPos(0, 71, -1), new BlockPos(-1, 71, -1), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2))), new Solution(new Pair<>(new BlockPos(1, 71, -1), new BlockPos(0, 71, -1)), List.of(new BlockPos(0, 71, -3), new BlockPos(0, 71, -2), new BlockPos(-1, 71, -2), new BlockPos(-1, 71, -3), new BlockPos(-2, 71, -3), new BlockPos(-2, 71, 1), new BlockPos(-1, 71, 1), new BlockPos(-1, 71, 0), new BlockPos(1, 71, 0), new BlockPos(1, 71, -3), new BlockPos(2, 71, -3), new BlockPos(2, 71, 1), new BlockPos(0, 71, 1), new BlockPos(0, 71, 2)))), List.of(new Solution(new Pair<>(new BlockPos(-2, 72, 8), new BlockPos(-1, 72, 8)), List.of(new BlockPos(0, 72, 4), new BlockPos(0, 72, 7), new BlockPos(1, 72, 7), new BlockPos(1, 72, 4), new BlockPos(2, 72, 4), new BlockPos(2, 72, 5), new BlockPos(3, 72, 5), new BlockPos(3, 72, 6), new BlockPos(2, 72, 6), new BlockPos(2, 72, 7), new BlockPos(3, 72, 7), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(-2, 72, 9), new BlockPos(-2, 72, 7), new BlockPos(-1, 72, 7), new BlockPos(-1, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 5), new BlockPos(-2, 72, 5), new BlockPos(-2, 72, 6), new BlockPos(-3, 72, 6), new BlockPos(-3, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11))), new Solution(new Pair<>(new BlockPos(1, 72, 7), new BlockPos(1, 72, 6)), List.of(new BlockPos(0, 72, 4), new BlockPos(0, 72, 5), new BlockPos(1, 72, 5), new BlockPos(1, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 5), new BlockPos(2, 72, 5), new BlockPos(2, 72, 6), new BlockPos(3, 72, 6), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 7), new BlockPos(1, 72, 7), new BlockPos(1, 72, 8), new BlockPos(0, 72, 8), new BlockPos(0, 72, 7), new BlockPos(-1, 72, 7), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 5), new BlockPos(-1, 72, 5), new BlockPos(-1, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 10), new BlockPos(-2, 72, 10), new BlockPos(-2, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 11))), new Solution(new Pair<>(new BlockPos(-3, 72, 6), new BlockPos(-2, 72, 6)), List.of(new BlockPos(0, 72, 4), new BlockPos(-1, 72, 4), new BlockPos(-1, 72, 5), new BlockPos(-2, 72, 5), new BlockPos(-2, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 9), new BlockPos(-2, 72, 9), new BlockPos(-2, 72, 10), new BlockPos(-1, 72, 10), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 7), new BlockPos(0, 72, 7), new BlockPos(0, 72, 6), new BlockPos(1, 72, 6), new BlockPos(1, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 5), new BlockPos(2, 72, 5), new BlockPos(2, 72, 6), new BlockPos(3, 72, 6), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 7), new BlockPos(1, 72, 7), new BlockPos(1, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 11))), new Solution(new Pair<>(new BlockPos(0, 72, 7), new BlockPos(1, 72, 7)), List.of(new BlockPos(0, 72, 4), new BlockPos(0, 72, 5), new BlockPos(1, 72, 5), new BlockPos(1, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 5), new BlockPos(2, 72, 5), new BlockPos(2, 72, 6), new BlockPos(3, 72, 6), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(0, 72, 9), new BlockPos(0, 72, 6), new BlockPos(-1, 72, 6), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11))), new Solution(new Pair<>(new BlockPos(-2, 72, 7), new BlockPos(-1, 72, 7)), List.of(new BlockPos(0, 72, 4), new BlockPos(1, 72, 4), new BlockPos(1, 72, 6), new BlockPos(2, 72, 6), new BlockPos(2, 72, 4), new BlockPos(3, 72, 4), new BlockPos(3, 72, 10), new BlockPos(2, 72, 10), new BlockPos(2, 72, 8), new BlockPos(1, 72, 8), new BlockPos(1, 72, 9), new BlockPos(-1, 72, 9), new BlockPos(-1, 72, 8), new BlockPos(-2, 72, 8), new BlockPos(-2, 72, 6), new BlockPos(-1, 72, 6), new BlockPos(-1, 72, 4), new BlockPos(-3, 72, 4), new BlockPos(-3, 72, 9), new BlockPos(-2, 72, 9), new BlockPos(-2, 72, 10), new BlockPos(0, 72, 10), new BlockPos(0, 72, 11)))));

	private final BooleanSetting solverEnabled = new BooleanSetting("Solver", true);

	protected List<Pos> path = null;

    public IceFill(Puzzles puzzles) {
        super(puzzles);
        this.registerProperty(solverEnabled);
	}

	@SubscribeEvent
	public void onDungeonRoom(DungeonEvent.ChangeRoom event) {
		if (event.unique == null) return;
		if (!"Ice Fill".equals(event.unique.getName())) {
			path = null;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		path = new ArrayList<>();
		outer: for (List<Solution> pattern : patterns) {
			for (Solution solution : pattern) {
				if (mc.level.getBlockState(event.room.getRealPosition(solution.check.getFirst())).getBlock() != Blocks.AIR) continue;
				if (mc.level.getBlockState(event.room.getRealPosition(solution.check.getSecond())).getBlock() == Blocks.AIR) continue;
				for (BlockPos pos : solution.path) path.add(event.room.getRealPosition(getCentre(pos)));
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

	private static class Solution {
		public Pair<BlockPos, BlockPos> check;
		public List<BlockPos> path;

		public Solution(Pair<BlockPos, BlockPos> check, List<BlockPos> path) {
			this.check = check;
			this.path = path;
		}
	}

	private static Pos getCentre(BlockPos bp) {
		return new Pos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
	}
}
