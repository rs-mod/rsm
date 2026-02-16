package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Line;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;


import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "IceFillSolver", id = "IceFillSolver", category = Category.DUNGEONS)
public class IceFillSolver extends Module {
	private static final List<List<Solution>> patterns = List.of(List.of(new Solution(new BPPair(new BlockPos(16, 70, 7), new BlockPos(16, 70, 9)), List.of(new BlockPos(15, 70, 7), new BlockPos(16, 70, 7), new BlockPos(16, 70, 8), new BlockPos(14, 70, 8), new BlockPos(14, 70, 9), new BlockPos(15, 70, 9), new BlockPos(15, 70, 10))), new Solution(new BPPair(new BlockPos(14, 70, 7), new BlockPos(14, 70, 9)), List.of(new BlockPos(15, 70, 7), new BlockPos(14, 70, 7), new BlockPos(14, 70, 8), new BlockPos(16, 70, 8), new BlockPos(16, 70, 9), new BlockPos(15, 70, 9), new BlockPos(15, 70, 10))), new Solution(new BPPair(new BlockPos(14, 70, 9), new BlockPos(14, 70, 7)), List.of(new BlockPos(15, 70, 7), new BlockPos(15, 70, 8), new BlockPos(14, 70, 8), new BlockPos(14, 70, 9), new BlockPos(15, 70, 9), new BlockPos(15, 70, 10))), new Solution(new BPPair(new BlockPos(16, 70, 9), new BlockPos(16, 70, 7)), List.of(new BlockPos(15, 70, 7), new BlockPos(15, 70, 8), new BlockPos(16, 70, 8), new BlockPos(16, 70, 9), new BlockPos(15, 70, 9), new BlockPos(15, 70, 10)))), List.of(new Solution(new BPPair(new BlockPos(15, 71, 13), new BlockPos(16, 71, 13)), List.of(new BlockPos(15, 71, 12), new BlockPos(17, 71, 12), new BlockPos(17, 71, 15), new BlockPos(16, 71, 15), new BlockPos(16, 71, 14), new BlockPos(15, 71, 14), new BlockPos(15, 71, 13), new BlockPos(14, 71, 13), new BlockPos(14, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(14, 71, 16), new BlockPos(14, 71, 15), new BlockPos(15, 71, 15), new BlockPos(15, 71, 17))), new Solution(new BPPair(new BlockPos(14, 71, 14), new BlockPos(15, 71, 14)), List.of(new BlockPos(15, 71, 11), new BlockPos(15, 71, 11), new BlockPos(13, 71, 11), new BlockPos(13, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(14, 71, 16), new BlockPos(14, 71, 12), new BlockPos(15, 71, 12), new BlockPos(15, 71, 13), new BlockPos(16, 71, 13), new BlockPos(16, 71, 12), new BlockPos(17, 71, 12), new BlockPos(17, 71, 16), new BlockPos(16, 71, 16), new BlockPos(16, 71, 15), new BlockPos(15, 71, 15), new BlockPos(15, 71, 17))), new Solution(new BPPair(new BlockPos(14, 71, 14), new BlockPos(16, 71, 13)), List.of(new BlockPos(15, 71, 12), new BlockPos(17, 71, 12), new BlockPos(17, 71, 16), new BlockPos(16, 71, 16), new BlockPos(16, 71, 14), new BlockPos(14, 71, 14), new BlockPos(14, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(15, 71, 16), new BlockPos(15, 71, 17))), new Solution(new BPPair(new BlockPos(17, 71, 12), new BlockPos(15, 71, 13)), List.of(new BlockPos(15, 71, 12), new BlockPos(17, 71, 12), new BlockPos(17, 71, 13), new BlockPos(16, 71, 13), new BlockPos(16, 71, 14), new BlockPos(17, 71, 14), new BlockPos(17, 71, 16), new BlockPos(16, 71, 16), new BlockPos(16, 71, 15), new BlockPos(15, 71, 15), new BlockPos(15, 71, 14), new BlockPos(14, 71, 14), new BlockPos(14, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(15, 71, 16), new BlockPos(15, 71, 17))), new Solution(new BPPair(new BlockPos(15, 71, 15), new BlockPos(17, 71, 12)), List.of(new BlockPos(15, 71, 12), new BlockPos(16, 71, 12), new BlockPos(16, 71, 13), new BlockPos(17, 71, 13), new BlockPos(17, 71, 14), new BlockPos(16, 71, 14), new BlockPos(16, 71, 15), new BlockPos(15, 71, 15), new BlockPos(15, 71, 14), new BlockPos(14, 71, 14), new BlockPos(14, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(15, 71, 16), new BlockPos(15, 71, 17))), new Solution(new BPPair(new BlockPos(16, 71, 14), new BlockPos(15, 71, 14)), List.of(new BlockPos(15, 71, 11), new BlockPos(15, 71, 11), new BlockPos(17, 71, 11), new BlockPos(17, 71, 12), new BlockPos(17, 71, 12), new BlockPos(17, 71, 16), new BlockPos(16, 71, 16), new BlockPos(16, 71, 12), new BlockPos(15, 71, 12), new BlockPos(15, 71, 13), new BlockPos(14, 71, 13), new BlockPos(14, 71, 12), new BlockPos(13, 71, 12), new BlockPos(13, 71, 16), new BlockPos(14, 71, 16), new BlockPos(14, 71, 15), new BlockPos(15, 71, 15), new BlockPos(15, 71, 17)))), List.of(new Solution(new BPPair(new BlockPos(13, 72, 23), new BlockPos(14, 72, 23)), List.of(new BlockPos(15, 72, 18), new BlockPos(15, 72, 18), new BlockPos(17, 72, 18), new BlockPos(17, 72, 19), new BlockPos(17, 72, 19), new BlockPos(12, 72, 19), new BlockPos(12, 72, 20), new BlockPos(18, 72, 20), new BlockPos(18, 72, 21), new BlockPos(12, 72, 21), new BlockPos(12, 72, 25), new BlockPos(13, 72, 25), new BlockPos(13, 72, 22), new BlockPos(18, 72, 22), new BlockPos(18, 72, 25), new BlockPos(17, 72, 25), new BlockPos(17, 72, 23), new BlockPos(16, 72, 23), new BlockPos(16, 72, 24), new BlockPos(14, 72, 24), new BlockPos(14, 72, 25), new BlockPos(15, 72, 25), new BlockPos(15, 72, 26))), new Solution(new BPPair(new BlockPos(16, 72, 22), new BlockPos(16, 72, 21)), List.of(new BlockPos(15, 72, 19), new BlockPos(18, 72, 19), new BlockPos(18, 72, 25), new BlockPos(17, 72, 25), new BlockPos(17, 72, 20), new BlockPos(14, 72, 20), new BlockPos(14, 72, 19), new BlockPos(12, 72, 19), new BlockPos(12, 72, 20), new BlockPos(13, 72, 20), new BlockPos(13, 72, 21), new BlockPos(12, 72, 21), new BlockPos(12, 72, 25), new BlockPos(13, 72, 25), new BlockPos(13, 72, 22), new BlockPos(16, 72, 22), new BlockPos(16, 72, 23), new BlockPos(14, 72, 23), new BlockPos(14, 72, 24), new BlockPos(15, 72, 24), new BlockPos(15, 72, 26))), new Solution(new BPPair(new BlockPos(12, 72, 21), new BlockPos(13, 72, 21)), List.of(new BlockPos(15, 72, 18), new BlockPos(15, 72, 18), new BlockPos(19, 72, 18), new BlockPos(19, 72, 26), new BlockPos(17, 72, 26), new BlockPos(17, 72, 25), new BlockPos(17, 72, 25), new BlockPos(18, 72, 25), new BlockPos(18, 72, 19), new BlockPos(17, 72, 19), new BlockPos(17, 72, 24), new BlockPos(16, 72, 24), new BlockPos(16, 72, 22), new BlockPos(15, 72, 22), new BlockPos(15, 72, 21), new BlockPos(16, 72, 21), new BlockPos(16, 72, 19), new BlockPos(14, 72, 19), new BlockPos(14, 72, 20), new BlockPos(13, 72, 20), new BlockPos(13, 72, 19), new BlockPos(12, 72, 19), new BlockPos(12, 72, 22), new BlockPos(14, 72, 22), new BlockPos(14, 72, 23), new BlockPos(12, 72, 23), new BlockPos(12, 72, 24), new BlockPos(13, 72, 24), new BlockPos(13, 72, 25), new BlockPos(14, 72, 25), new BlockPos(14, 72, 24), new BlockPos(15, 72, 24), new BlockPos(15, 72, 26))), new Solution(new BPPair(new BlockPos(15, 72, 22), new BlockPos(16, 72, 22)), List.of(new BlockPos(15, 72, 19), new BlockPos(15, 72, 20), new BlockPos(16, 72, 20), new BlockPos(16, 72, 19), new BlockPos(18, 72, 19), new BlockPos(18, 72, 20), new BlockPos(17, 72, 20), new BlockPos(17, 72, 21), new BlockPos(18, 72, 21), new BlockPos(18, 72, 25), new BlockPos(17, 72, 25), new BlockPos(17, 72, 23), new BlockPos(16, 72, 23), new BlockPos(16, 72, 24), new BlockPos(15, 72, 24), new BlockPos(15, 72, 21), new BlockPos(14, 72, 21), new BlockPos(14, 72, 23), new BlockPos(13, 72, 23), new BlockPos(13, 72, 19), new BlockPos(12, 72, 19), new BlockPos(12, 72, 25), new BlockPos(15, 72, 25), new BlockPos(15, 72, 26))), new Solution(new BPPair(new BlockPos(13, 72, 22), new BlockPos(14, 72, 22)), List.of(new BlockPos(15, 72, 19), new BlockPos(18, 72, 19), new BlockPos(18, 72, 20), new BlockPos(16, 72, 20), new BlockPos(16, 72, 21), new BlockPos(18, 72, 21), new BlockPos(18, 72, 25), new BlockPos(17, 72, 25), new BlockPos(17, 72, 23), new BlockPos(16, 72, 23), new BlockPos(16, 72, 24), new BlockPos(14, 72, 24), new BlockPos(14, 72, 23), new BlockPos(13, 72, 23), new BlockPos(13, 72, 21), new BlockPos(14, 72, 21), new BlockPos(14, 72, 19), new BlockPos(12, 72, 19), new BlockPos(12, 72, 24), new BlockPos(13, 72, 24), new BlockPos(13, 72, 25), new BlockPos(15, 72, 25), new BlockPos(15, 72, 26)))));

	private List<Pos> path = null;

	public IceFillSolver() {

	}

	@SubscribeEvent
	public void onDungeonRoom(DungeonEvent.ChangeRoom event) {
		if (!"Ice Fill".equals(event.unique.getName())) {
			path = null;
			ChatUtils.chat("icefill reset");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		path = new ArrayList<>();
		outer: for (List<Solution> pattern : patterns) {
			for (Solution solution : pattern) {
				if (mc.level.getBlockState(event.room.getRealPosition(solution.check.first)).getBlock() != Blocks.AIR) continue;
				if (mc.level.getBlockState(event.room.getRealPosition(solution.check.second)).getBlock() == Blocks.AIR) continue;
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
