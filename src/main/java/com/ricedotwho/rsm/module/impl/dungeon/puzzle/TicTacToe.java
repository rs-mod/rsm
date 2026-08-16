package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.Renderer3D;
import com.ricedotwho.rsm.managers.dungeon.map.map.Room;
import com.ricedotwho.rsm.managers.dungeon.map.map.RoomRotation;
import com.ricedotwho.rsm.managers.dungeon.map.utils.ScanUtils;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.type.Pos;
import com.ricedotwho.rsm.ui.old.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.old.clickgui.settings.impl.ColorSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

@Getter
@SubModuleInfo(name = "TTT", alwaysDisabled = false)
public class TicTacToe extends SubModule<Puzzles> {
    private final ColorSetting color = new ColorSetting("Solution", new Color(0, 255, 0, 90));
    private final ColorSetting gamble = new ColorSetting("Gamble", new Color(255, 255, 0, 90));
    private final BooleanSetting fullBlock = new BooleanSetting("Render full block", false);

    private static final Queue<BlockPos> scheduled = new LinkedList<>();
    protected static boolean isGamble = false;
    protected static final List<BlockPos> buttons = new ArrayList<>();
    protected static final List<Move> board = new ArrayList<>(Collections.nCopies(9, Move.NONE));

    private static final Set<List<Integer>> winSets = Set.of(
            List.of(0,1,2), List.of(3,4,5), List.of(6,7,8),
            List.of(0,4,8), List.of(6,4,2), List.of(0,3,6),
            List.of(1,4,7), List.of(2,5,8)
    );

    public TicTacToe(Puzzles puzzles) {
        super(puzzles);
    }

    @Override
    public void reset() {
        buttons.clear();
        board.clear();
        board.addAll(Collections.nCopies(9, Move.NONE));
        scheduled.clear();
    }

    public static void onSetEntityData(int id) {
        if (!Location.getArea().is(Island.Dungeon)) return;
        assert mc.level != null;
        var entity = mc.level.getEntity(id);
        if (!(entity instanceof ItemFrame frame) || !frame.getItem().is(Items.FILLED_MAP)) return;
        var mapId = frame.getItem().get(DataComponents.MAP_ID);
        if (mapId == null) return;
        int mId = mapId.id();

        var move = Move.fromID(mId);
        if (move == Move.NONE) return;

        var bp = frame.blockPosition();
        Room room = ScanUtils.getRoomFromPos(bp.getX(), bp.getZ());
        if (room == null || room.getUniqueRoom() == null || room.getUniqueRoom().getRotation() == RoomRotation.UNKNOWN) {
            scheduled.add(bp);
            return;
        }

        if (!scheduled.isEmpty()) {
            var m = Move.X;
            BlockPos curr;
            while ((curr = scheduled.poll()) != null) {
                int index = index(curr, room);
                if (index < 0) continue;
                board.set(index, m);
                m = m.opposite();
            }
        }

        buttons.clear();
        isGamble = false;

        int index = index(bp, room);
        if (index < 0) return;
        board.set(index, move);

        State state = State.fromBoard();
        var movesLeft = state.movesLeft();

        switch (movesLeft) {
            case 7 -> {
                return;
            }
            case 8 -> {
                if (board.get(4) != Move.NONE) {
                    add(new BlockPos(-7, 72, 0), room);
                    add(new BlockPos(-7, 70, 0), room);
                    add(new BlockPos(-7, 72, 2), room);
                    add(new BlockPos(-7, 70, 2), room);
                } else {
                    add(new BlockPos(-7, 71, 1), room);
                }
                return;
            }
        }

        var solutions = solutionsFor(state);
        if (solutions.isEmpty()) return;
        if (state.player == Move.X) {
            var newSolutions = solutions.stream().map(it -> new Pair<>(it.getFirst(), solutionsFor(state.move(it.getFirst())))).toList();

            if (newSolutions.size() == 2) {
                var play1 = newSolutions.getFirst();
                var play2 = newSolutions.get(1);
                if (play1.getSecond().size() != 1 || play2.getSecond().size() != 1) return;
                if (!Objects.equals(play1.getFirst(), play2.getSecond().getFirst().getSecond()) || !Objects.equals(play2.getFirst(), play1.getSecond().getFirst().getSecond())) return;

                isGamble = true;
                solutions = List.of(new Pair<>(play1.getFirst(), 0), new Pair<>(play2.getFirst(), 0));
            } else {
                var first = newSolutions.getFirst();
                var firstSet = new HashSet<>(first.getSecond());
                if (!newSolutions.stream().allMatch(it -> firstSet.containsAll(it.getSecond()))) return;

                solutions = first.getSecond();
            }
        }

        solutions.forEach(p -> {
            int bpx = 2 - p.getFirst() / 3;
            int bpz = 2 - p.getFirst() % 3;
            add(new BlockPos(-7, 70 + bpx, bpz), room);
        });
    }

    private static List<Pair<Integer, Integer>> solutionsFor(State state) {
        var moves = moves(state);
        var max = moves.stream().max(Comparator.comparingInt(Pair::getSecond)).map(Pair::getSecond).orElse(null);
        return moves.stream().filter(it -> Objects.equals(it.getSecond(), max)).toList();
    }

    private static void add(BlockPos pos, Room room) {
        buttons.add(room.getRealPosition(pos));
    }

    @SubscribeEvent
    private void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || mc.level == null) return;
        Color color = isGamble ? this.gamble.getValue() : this.color.getValue();

        buttons.forEach(bp -> {
            BlockState state = mc.level.getBlockState(bp);
            if (!(state.getBlock() instanceof ButtonBlock)) return;
            VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : state.getShape(mc.level, bp));
            AABB aabb = (shape.isEmpty() ? Shapes.block().bounds() : shape.bounds()).move(bp);
            Renderer3D.addTask(new FilledBox(aabb, color, true));
        });
    }

    private static int row(BlockPos pos) {
        return switch (pos.getY()) { // -7 70 1
            case 72 -> 0;
            case 71 -> 1;
            case 70 -> 2;
            default -> -1;
        };
    }

    private static int column(Room room, BlockPos pos) {
        return switch ((int) room.getUniqueRoom().getMainRoom().getRelativePositionFixed(new Pos(pos)).z()) {
            case 2 -> 0;
            case 1 -> 1;
            case 0 -> 2;
            default -> -1;
        };
    }

    private static int index(BlockPos pos, Room room) {
        int row = row(pos);
        int column = column(room, pos);
        return (row * 3) + column;
    }

    public enum Move {
        X,
        O,
        NONE;

        public Move opposite() {
            return switch (this) {
                case O -> X;
                case X -> O;
                default -> NONE;
            };
        }

        public static Move toMoveOffCount(int count) {
            return count % 2 == 1 ? O : X;
        }

        public static Move fromID(int id) {
            return switch (id) {
                case 30876 -> X;
                case 30877 -> O;
                default -> NONE;
            };
        }
    }

    @AllArgsConstructor
    public static class State {
        List<Move> positions;
        Move player;

        public List<Integer> available() {
            List<Integer> available = new ArrayList<>();
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i) == Move.NONE) {
                    available.add(i);
                }
            }
            return available;
        }

        public int movesLeft() {
            return Math.toIntExact(positions.stream().filter(m -> m == Move.NONE).count());
        }

        public State move(int at) {
            List<Move> copy = new ArrayList<>(positions);
            copy.set(at, player);
            return new State(copy, player.opposite());
        }

        public boolean win() {
            return winSets.stream().anyMatch(it -> positions.get(it.getFirst()) != Move.NONE && positions.get(it.getFirst()) == positions.get(it.get(1)) && positions.get(it.getFirst()) == positions.get(it.get(2)));
        }

        public static State fromBoard() {
            int count = Math.toIntExact(board.stream().filter(m -> m != Move.NONE).count());
            Move player = Move.toMoveOffCount(count);
            return new State(new ArrayList<>(board), player);
        }
    }

    public static List<Pair<Integer, Integer>> moves(State state) {
        return state.available().stream().map(move -> new Pair<>(move, minimax(state.move(move), false, 0))).toList();
    }

    public static int minimax(State state, boolean maximizing, int depth) {
        var win = state.win();
        if (win || state.movesLeft() == 0) {
            if (win && maximizing) return -1;
            else if (win) return 1;
            return 0;
        }

        var moves = state.available();
        if (maximizing) {
            var maxVal = Integer.MIN_VALUE;
            for (var move : moves) {
                var nextState = state.move(move);
                maxVal = Math.max(maxVal, minimax(nextState, false, depth + 1));
            }
            return maxVal;
        }

        var minVal = Integer.MAX_VALUE;
        for (var move : moves) {
            var nextState = state.move(move);
            minVal = Math.min(minVal, minimax(nextState, true, depth + 1));
        }
        return minVal;
    }
}
