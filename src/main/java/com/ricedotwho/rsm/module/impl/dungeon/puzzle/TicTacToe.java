package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.ticktactoe.AlphaBetaAdvanced;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.ticktactoe.Board;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.*;

@Getter
@ModuleInfo(aliases = "TTT", id = "TicTacToe", category = Category.DUNGEONS)
public class TicTacToe extends Module {

    private final ColourSetting colour = new ColourSetting("Solution", new Colour(0, 255, 0, 90));
    private final BooleanSetting fullBlock = new BooleanSetting("Full Block", false);

    private Direction roomFacing = null;
    private Board board = null;
    private final Map<Integer, ItemFrame> mappedPositions = new HashMap<>();
    private BlockPos bestMove = null;

    private final Pos MIN = new Pos(-13, 65, -3);
    private final Pos MAX = new Pos(-5, 75, 6);
    private final Pos TOP_LEFT = new Pos(-7, 72, 2);

    public TicTacToe() {
        this.registerProperty(
                colour,
                fullBlock
        );
    }

    @Override
    public void reset() {
        roomFacing = null;
        board = null;
        bestMove = null;
        mappedPositions.clear();
    }

    // todo: make this run on not tick

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (!Location.getArea().is(Island.Dungeon) || mc.player == null || mc.level == null || Dungeon.isInBoss() || com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom() == null) return;
        UniqueRoom room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom().getUniqueRoom();

        if (room != null && "Tic Tac Toe".equals(room.getName())) {
            roomFacing = getDirectionForRotation(room.getRotation());
            AABB aabb = new AABB(room.getMainRoom().getRealPositionFixed(MIN).asVec3(), room.getMainRoom().getRealPositionFixed(MAX).asVec3());
            List<ItemFrame> frames = mc.level.getEntitiesOfClass(ItemFrame.class, aabb, this::filterFrame);
            BlockPos topLeft = room.getMainRoom().getRealPositionFixed(TOP_LEFT).asBlockPos();

            if (topLeft == null || roomFacing == null || board == null) {
                for (ItemFrame frame : frames) {

                    BlockPos realPos = Pos.blockPos(frame.position());

                    int row;
                    switch (realPos.getY()) {
                        case 72: row = 0; break;
                        case 71: row = 1; break;
                        case 70: row = 2; break;
                        default: continue;
                    }

                    int column = switch ((int) room.getMainRoom().getRelativePositionFixed(new Pos(realPos)).z()) {
                        case 2 -> 0;
                        case 1 -> 1;
                        case 0 -> 2;
                        default -> -1;
                    };

                    MapItemSavedData mapData = MapItem.getSavedData(frame.getItem(), mc.level);
                    if (mapData == null) continue;

                    int colorInt = mapData.colors[8256] & 0xFF;
                    Board.State owner = (colorInt == 114) ? Board.State.X : Board.State.O;

                    if (board == null) {
                        board = new Board();
                    }

                    try {
                        board.place(column, row, owner);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    mappedPositions.put(row * Board.BOARD_WIDTH + column, frame);
                }

                if (board != null) {
                    board.turn = (frames.size() % 2 == 0) ? Board.State.X : Board.State.O;
                }
            } else if (!board.isGameOver) {
                board.turn = (frames.size() % 2 == 0) ? Board.State.X : Board.State.O;

                if (board.turn == Board.State.O) {
                    for (ItemFrame frame : frames) {

                        if (!mappedPositions.containsValue(frame)) {
                            MapItemSavedData mapData = MapItem.getSavedData(frame.getItem(), mc.level);
                            if (mapData == null) continue;

                            int colorInt = mapData.colors[8256] & 0xFF;
                            Board.State owner = (colorInt == 114) ? Board.State.X : Board.State.O;

                            BlockPos realPos = Pos.blockPos(frame.position());

                            int row = switch (realPos.getY()) {
                                case 72 -> 0;
                                case 71 -> 1;
                                case 70 -> 2;
                                default -> -1;
                            };

                            int column = switch ((int) room.getMainRoom().getRelativePositionFixed(new Pos(realPos)).z()) {
                                case 2 -> 0;
                                case 1 -> 1;
                                case 0 -> 2;
                                default -> -1;
                            };

                            try {
                                board.place(column, row, owner);
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }

                            mappedPositions.put(row * Board.BOARD_WIDTH + column, frame);
                        }
                    }

                    AlphaBetaAdvanced.run(board);

                    int move = board.algorithmBestMove;

                    if (move != -1) {
                        int column = move % Board.BOARD_WIDTH;
                        int row = move / Board.BOARD_WIDTH;
                        bestMove = offset(roomFacing.getCounterClockWise(), topLeft.below(row), column);
                        postSolve();
                    }
                } else {
                    bestMove = null;
                }
            } else {
                bestMove = null;
            }
        } else {
            bestMove = null;
        }
    }

    protected void postSolve() {

    }

    private boolean filterFrame(ItemFrame frame) {
        if (frame.getItem().getItem() instanceof MapItem && frame.getRotation() == 0) {

            BlockPos realPos = Pos.blockPos(frame.position());
            MapItemSavedData mapData = MapItem.getSavedData(frame.getItem(), mc.level);
            if (mapData == null) return false;

            int colorInt = mapData.colors[8256] & 0xFF;
            if (colorInt != 114 && colorInt != 33) return false;

            BlockPos blockBehind = offset(roomFacing.getOpposite(), realPos);
            Block block = mc.level.getBlockState(blockBehind).getBlock();
            return block == Blocks.IRON_BLOCK;
        }
        return false;
    }

    private BlockPos offset(Direction direction, BlockPos pos) {
        return offset(direction, pos, 1);
    }

    private BlockPos offset(Direction direction, BlockPos pos, int n) {
        return pos.offset(direction.getStepX() * n, direction.getStepY() * n, direction.getStepZ() * n);
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || bestMove == null) return;
        BlockState state = mc.level.getBlockState(bestMove);
        if (!(state.getBlock() instanceof ButtonBlock)) return;

        VoxelShape shape = (this.fullBlock.getValue() ? Shapes.block() : state.getShape(mc.level, bestMove));
        AABB aabb = (shape.isEmpty() ? Shapes.block().bounds() : shape.bounds()).move(bestMove);

        Renderer3D.addTask(new FilledBox(aabb, this.colour.getValue(), true));
    }

    private Direction getDirectionForRotation(RoomRotation rotation) {
        return switch (rotation) {
            case TOPLEFT -> Direction.EAST;
            case BOTRIGHT -> Direction.WEST;
            case TOPRIGHT -> Direction.SOUTH;
            case BOTLEFT -> Direction.NORTH;
            case null, default -> null;
        };
    }

}
