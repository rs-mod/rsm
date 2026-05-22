package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.simonsays;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

@Getter
@SubModuleInfo(name = "Solver", alwaysDisabled = false)
public class Solver extends SubModule<SimonSays> {
    public final ModeSetting renderMode = new ModeSetting("Render Mode", "Filled Outline", List.of("Outline", "Filled Outline", "Filled"));
    public final ColourSetting first = new ColourSetting("First", new Colour(0, 255, 0));
    public final ColourSetting second = new ColourSetting("Second", new Colour(255, 255, 0));
    public final ColourSetting third = new ColourSetting("Third", new Colour(255, 0, 0));
    public final ColourSetting fourth = new ColourSetting("Fourth", new Colour(255, 0, 0));
    public final ColourSetting fifth = new ColourSetting("Fifth", new Colour(255, 0, 0));

    public final MultiBoolSetting blockClicks = new MultiBoolSetting("Block Wrong Clicks", List.of("Solution", "Server Tick"), List.of());
    public final NumberSetting lagTicks = new NumberSetting("Lag Ticks", 0, 10, 2, 1, () -> blockClicks.get("Server Tick"));

    // Maybe u can change this to a hardcoded value, we have never needed to change this from 7
    // ill probably just leave it bcs idk
    public final NumberSetting lanternTicks = new NumberSetting("Lantern Ticks", 0, 10, 7, 1);
    public final ModeSetting singleSkipFix = new ModeSetting("Single Skip Fix", "Auto", List.of("Off", "Auto", "On"));
    public final BooleanSetting memory = new BooleanSetting("Remember Solution", true);

    public final DragSetting stateHud = new DragSetting("SS State", new Vector2d(50, 50), new Vector2d(50, 10));
    public final ModeSetting stateEnabled = new ModeSetting("State HUD", "Off", List.of("Off", "Hide at SS", "Hide at I4", "Hide at Both", "Always"));
    public final MultiBoolSetting stateSettings = new MultiBoolSetting("State Settings", List.of("Break", "Round", "Done"), () -> !stateEnabled.is("Off"));
    public final MultiBoolSetting messages = new MultiBoolSetting("Messages", List.of("Complete (Chat)", "Break (Chat)", "Break (Party Chat)", "Round (Chat)", "Round (Party Chat)"), List.of());

    public Solver(SimonSays module) {
        super(module);
        this.registerProperty(renderMode, first, second, third, fourth, fifth,
                blockClicks, lagTicks, lanternTicks, singleSkipFix, memory,
                stateHud, stateEnabled, stateSettings, messages);

        createStates();
    }

    @Override
    public void reset() {
        p3Start = -1;
        ticks = 0;
        inS1 = false;
        previousState = false;

        message = null;
        resetSolver();
    }

    public long p3Start = -1;
    public int ticks = 0;
    public boolean inS1 = false;
    public int lagTicksRemaining = 0;

    public final List<State> states = new CopyOnWriteArrayList<>();
    public final List<State> solution = new CopyOnWriteArrayList<>();
    public final List<State> render = new CopyOnWriteArrayList<>();
    public boolean buttonsExist = false;
    public boolean onSkip = true;
    public boolean onFirstRound = true;
    public boolean previousState = false;
    public int totalLanterns = 0;
    public int totalClicks = 0;
    public int startPress = -1;

    public String message = null;

    @SubscribeEvent
    public void onBlockUpdate(BlockChangeEvent event) {
        Pos pos = event.getPos();
        Block block = event.getNewState().getBlock();
        int x = (int)pos.x;
        int y = (int)pos.y;
        int z = (int)pos.z;

//        // This is very wip, it seems to work but we didnt test it much. maybe js remove it idk
//        if (x == 110 && y == 121 && z == 91 && event.getNewState().getBlock() == Blocks.STONE_BUTTON) {
//            boolean buttonState = event.getNewState().getValue(ButtonBlock.POWERED);
//
//            if (buttonState == previousState && module.isAtSS()) {
//                states.forEach(s -> {
//                    s.lastSpawn = -67;
//                });
//
//                TaskComponent.onServerTick(2, () -> {
//                    states.forEach(s -> {
//                        s.clicked = true;
//                        s.canKeep = false;
//                        s.isFirst = false;
//                        s.shouldFixRender = false;
//                    });
//
//                    solution.clear();
//                    render.clear();
//                });
//            }
//
//            startPress = ticks;
//            previousState = buttonState;
//        }

        // Lanterns
        if (x == 111 && y >= 120 && y < 124 && z >= 92 && z < 96) {
            State state = coordsToState(x, y, z);

            if (state == null) {
                return;
            }

            boolean previousState = state.state;
            state.state = block == Blocks.SEA_LANTERN;

            if (previousState && !state.state) {
                if (state.lanternTicks >= lanternTicks.getValue().intValue() && onSkip) {
                    state.add();
                    state.lastTurn = ticks - state.lanternTicks;
                    onSkip = false;
                }
            }

            else if (!previousState && state.state) {
                totalLanterns++;
                state.obsidianTicks = 0;
                state.lastSpawn = ticks;

                // If the stone buttons don't disappear on a new round, the solver won't recognise the new solution.
                if (state.clicked) {
                    state.clicked = false;
                }

                solution.remove(state);
            }
        }

        // Stone buttons.
        else if (x == 110 && y >= 120 && y < 124 && z >= 92 && z < 96) {
            State state = coordsToState(111, y, z);

            if (state == null) return;
            boolean previousState = state.buttonExists;
            state.buttonExists = block == Blocks.STONE_BUTTON;
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        ticks++;
        if (lagTicksRemaining > 0) lagTicksRemaining--;

        boolean lanternExists = states.stream().anyMatch(state -> state.state);
        boolean buttons = states.stream().filter(state -> state.buttonExists).count() > 8;

        if (!buttonsExist && buttons) {
            buttonsExist = true;
        }

        else if (buttonsExist && !buttons) {
            if (!lanternExists) {
                if (!module.ssDone) {
                    onSkip = true;
                    onFirstRound = true;
                    totalLanterns = 0;
                    resetSolver();
                    if (inS1) {
                        if (messages.get("Break (Chat)")) SimonSays.chat(ChatFormatting.RED + "SS Broke!");
                        if (messages.get("Break (Party Chat)")) mc.getConnection().sendCommand("pc SS Broke!");
                        setBreakMessage();
                    }
                }
                solution.clear();
                render.clear();
            }

            else {
                onRoundStart();
            }

            states.forEach(state -> {
                state.lanternTicks = 0;
                state.obsidianTicks = 67;
                state.clicked = false;
            });

            buttonsExist = false;
        }

        states.forEach(state -> {
            if (state.state) {
                state.lanternTicks++;

                if (!onSkip && !solution.contains(state) && state.lanternTicks > 0 && state.lastSpawn >= startPress) {
                    boolean solutionEmpty = solution.isEmpty();

                    state.add();
                    if (ticks - state.lastTurn > 20 && state.isFirst && !solutionEmpty && buttonsExist) {
                        onRoundStart();
                    }

                    state.lastTurn = ticks;
                }

                // I dont remember what this does. its probably not important but i dont wanna risk removing it
                // ok i removed it surely its fine
                // if (state.clicked) state.setClicked();
            }

            else state.obsidianTicks++;
        });
    }

    @SubscribeEvent
    public void onChat(ChatEvent event) {
        String msg = event.getMessage().getString();

        // GOLDOR_START
        if ("[BOSS] Goldor: Who dares trespass into my domain?".equals(msg)) {
            resetSolver();
            p3Start = System.currentTimeMillis();
            inS1 = true;
        }
        Matcher matcher;
        if ((matcher = Dungeon.TERM.matcher(msg)).find()) {
            if ("7".equals(matcher.group(3))) inS1 = false;
        }
    }

    @SubscribeEvent
    public void onMouseEvent(PlayerInputEvent.Use event) {
        if (mc.player == null || event.getResult() == null) return;

        Vec3 pos = event.getResult().getLocation();
        int x = (int)pos.x;
        int y = (int)pos.y;
        int z = (int)pos.z;

        if (x == 110 && y >= 120 && y < 124 && z >= 92 && z < 96) {
            State state = coordsToState(111, y, z);
            State first = render.stream().filter(s -> !s.clicked).findFirst().orElse(null);

            boolean isFirst;
            if (state != null) isFirst = first == state;
            else {
                isFirst = render.isEmpty();
            }

            boolean canBlock = onFirstRound ? blockClicks.get("Solution") : (blockClicks.get("Solution") || blockClicks.get("Solution (Not on Skip)"));
            boolean solutionBwc = (canBlock && !isFirst);

            boolean lagBwc = (blockClicks.get("Server Tick") && lagTicksRemaining > 0);

            if ((solutionBwc || lagBwc) && !mc.player.isShiftKeyDown()) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlacement(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket packet)) return;

        BlockPos blockPos = packet.getHitResult().getBlockPos();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        // Start button
        if (x == 110 && y == 121 && z == 91) {
            totalClicks++;
            states.forEach(s -> {
                s.clicked = true;
                s.canKeep = false;
                s.shouldFixRender = false;
            });

            return;
        }

        states.stream().filter(s -> 110 == x && s.y == y && s.z == z).findFirst().ifPresent(state -> {
            lagTicksRemaining = lagTicks.getValue().intValue();
            state.setClicked();
        });
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {

        // I should really make a text hud setting

        if (!this.stateEnabled.is("Off") && message != null) {
            boolean shouldRenderState = !isAtI4() || (!this.stateEnabled.is("Hide at I4") && !this.stateEnabled.is("Hide at Both"));
            if (module.isAtSS() && (this.stateEnabled.is("Hide at SS") || this.stateEnabled.is("Hide at Both")) && !module.ssDone) shouldRenderState = false;
            if (shouldRenderState) {
                stateHud.renderScaledGFX(event.getGfx(), () -> stateHud.text(event.getGfx(), message, DragSetting.Align.LEFT, 0, 0, Colour.WHITE, false));
            }
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Extract event) {
        renderSolver(render.stream().filter(b -> !b.clicked).map(btn -> new Pair<>(btn.y, btn.z)).toList());
    }

    public void renderSolver(List<Pair<Integer, Integer>> positions) {
        int i = 0;
        for (Pair<Integer, Integer> pair : positions) {
            Colour colour = fifth.getValue();
            colour = switch (i) {
                case 0 -> first.getValue();
                case 1 -> second.getValue();
                case 2 -> third.getValue();
                case 3 -> fourth.getValue();
                default -> colour;
            };
            i++;

            renderButton(pair, colour);
        }
    }

    public void renderButton(Pair<Integer, Integer> position, Colour colour) {
        AABB aabb = getButtonAABB(position.getFirst(), position.getSecond());

        Renderer3D.addTask(switch (renderMode.getValue()) {
            case "Outline" -> new OutlineBox(aabb, colour.alpha(255), true);
            case "Filled Outline" -> new FilledOutlineBox(aabb, colour.alpha((float)colour.getAlpha() / 2), colour.alpha(255), true);
            default -> new FilledBox(aabb, colour, true);
        });
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    public void resetSolver() {
        states.forEach(state -> {
            state.state = false;
            state.lanternTicks = 0;
            state.obsidianTicks = 67;
            state.lastSpawn = -67;
            state.lastTurn = -67;
            state.clicked = false;
            state.canKeep = false;
            state.buttonExists = false;
            state.isFirst = false;
            state.shouldFixRender = true;
        });

        solution.clear();
        render.clear();
        buttonsExist = false;
        onSkip = true;
        onFirstRound = true;
        totalLanterns = 0;
        totalClicks = 0;
        startPress = -1;
    }

    public void createStates() {
        for (int z = 0; z < 4; z++) {
            for (int y = 0; y < 4; y++) {
                int blockY = y + 120;
                int blockZ = z + 92;

                states.add(new State(111, blockY, blockZ));
            }
        }
    }

    public State coordsToState(int x, int y, int z) {
        return states.stream().filter(state -> state.x == x && state.y == y && state.z == z).findFirst().orElse(null);
    }

    public void doSingleSkipFix() {
        if (solution.size() == 3 && totalLanterns == 3 && onFirstRound && (singleSkipFix.is("Auto") || singleSkipFix.is("On"))) {
            if (singleSkipFix.is("On") || totalClicks <= 3) {
                State first = solution.getFirst();
                first.isFirst = false;
                solution.remove(first);
                render.remove(first);

                solution.getFirst().isFirst = true;
            }
        }
    }

    public void fixRender() {
        List<State> newList = new CopyOnWriteArrayList<>(solution.stream().filter(s -> s.shouldFixRender).toList());
        newList.addAll(render.stream().filter(s -> !newList.contains(s) && s.canKeep && s.lastSpawn >= startPress).toList());
        render.clear();
        render.addAll(newList);
    }

    public AABB getButtonAABB(int y, int z) {
        double x1 = 110.875;
        double y1 = (y + 0.375);
        double z1 = (z + 0.3125);
        return new AABB(x1 - 0.001, y1 - 0.001, z1 - 0.001, x1 + 0.4 + 0.001, y1 + 0.25 + 0.001, z1 + 0.375 + 0.001);
    }

    public void onRoundStart() {
        onFirstRound = false;
        int length = render.stream().filter(s -> s.lastSpawn >= startPress).toList().size();
        if (length > 0 && length < 5) {
            String time = NumberUtils.millisToSSMS((System.currentTimeMillis() - p3Start) / 1000L);
            String finalTime = p3Start != -1 ? ChatFormatting.GRAY + "(" + ChatFormatting.GREEN + time + "s" + ChatFormatting.GRAY + ")" : "";

            if (messages.get("Round (Chat)")) SimonSays.chat(length + "/5 " + finalTime);
            if (messages.get("Round (Party Chat)")) mc.getConnection().sendCommand("pc SS " + length + "/5");
            setRoundMessage(String.valueOf(length), p3Start == -1 ? null : time + "s");
        }

        states.forEach(state -> {
            state.isFirst = false;
            state.shouldFixRender = false;
        });

        if (!this.memory.getValue()) render.clear();
        else {
            states.forEach(s -> {
                if ((render.contains(s) && !s.canKeep) || !solution.contains(s)) {
                    render.remove(s);
                }
            });
        }

        solution.clear();
    }

    public void onSSDone() {
        if (messages.get("Complete (Chat)") && p3Start != -1) {
            String time = NumberUtils.millisToSSMS((System.currentTimeMillis() - p3Start) / 1000L);
            SimonSays.chat("Simon Says completed in " + ChatFormatting.GREEN + time + "s" + ChatFormatting.RESET + ".");
            setDoneMessage(time + "s");
        }
    }

    public void setStateMessage(String message) {
        this.message = message.trim();
    }

    public void setBreakMessage() {
        if (!stateSettings.get("Break")) return;
        setStateMessage(ChatFormatting.RED + "SS Broke!");
    }

    public void setRoundMessage(String round, String time) {
        if (!stateSettings.get("Round")) return;
        String msg = ChatFormatting.DARK_AQUA + round + "/5 ";

        if (time != null) {
            msg += ChatFormatting.GRAY + "(" +
                    ChatFormatting.GREEN + time +
                    ChatFormatting.GRAY + ")";
        }
        setStateMessage(msg);
    }

    public void setDoneMessage(String time) {
        if (!stateSettings.get("Done")) return;
        String msg = ChatFormatting.GREEN + "Done! ";

        if (time != null) {
            msg += ChatFormatting.GRAY + "(" +
                    ChatFormatting.GREEN + time +
                    ChatFormatting.GRAY + ")";
        }

        setStateMessage(msg);
    }

    public boolean isAtI4() {
        if (mc.player == null) return false;
        return mc.player.position().x < 70 && mc.player.position().z < 50;
    }

    public class State {
        public boolean state = false;
        public boolean buttonExists = false;
        public boolean clicked = false;
        public boolean canKeep = false;
        public boolean isFirst = false;
        public boolean shouldFixRender = true;
        public int lastSpawn = -67;
        public int lanternTicks = 0;
        public int obsidianTicks = 67;
        public int lastTurn = -67;

        public int x;
        public int y;
        public int z;

        public BlockPos pos;

        public State(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            pos = new BlockPos(110, y, z);
        }

        public void add() {
            shouldFixRender = true;
            if (solution.isEmpty() && onFirstRound) {
                states.forEach(state -> state.isFirst = false);
                isFirst = true;
            }
            render();
            if (solution.contains(this)) {
                if (this.clicked) {
                    solution.remove(this);
                    solution.add(this);
                }

                return;
            }

            solution.add(this);
            doSingleSkipFix();
            fixRender();
        }

        public void render() {
            if (render.contains(this)) {
                if (memory.getValue() && this.canKeep) {
                    return;
                }
                render.remove(this);
            }
            render.add(this);
        }

        public void setClicked() {
            this.clicked = true;
            this.canKeep = true;

            if (!solution.contains(this)) return;

            int buttonIndex = solution.indexOf(this);
            solution.forEach(b -> {
                if (solution.indexOf(b) <= buttonIndex) {
                    b.clicked = true;
                }
            });
        }
    }
}
