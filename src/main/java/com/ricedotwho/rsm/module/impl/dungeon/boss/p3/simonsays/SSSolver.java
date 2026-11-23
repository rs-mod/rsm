package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.simonsays;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.OFA;
import com.ricedotwho.rsm.type.RenderMode;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

@Getter
@SubModuleInfo(name = "Solver")
public class SSSolver extends SubModule<SimonSays> {
    // the box extends inside the wall
    private static final AABB BUTTON = new AABB(0.875, 0.375, 0.3125, 1.1, 0.625, 0.6875);

    private final BooleanSetting solver = new BooleanSetting("Solver", true);
    private final EnumSetting<RenderMode> renderMode = new EnumSetting<>("Render Mode", RenderMode.FILLED_OUTLINE);
    private final DualColorSetting first = new DualColorSetting("First", Color.GREEN, Color.GREEN.withAlpha(0.5F), false);
    private final DualColorSetting second = new DualColorSetting("Second", Color.YELLOW, Color.YELLOW.withAlpha(0.5F), false);
    private final DualColorSetting third = new DualColorSetting("Third", Color.RED, Color.RED.withAlpha(0.5F), false);
    private final DualColorSetting fourth = new DualColorSetting("Fourth", Color.RED, Color.RED.withAlpha(0.5F), false);
    private final DualColorSetting fifth = new DualColorSetting("Fifth", Color.RED, Color.RED.withAlpha(0.5F), false);
    private final BooleanSetting depth = new BooleanSetting("Depth", true);

    private final EnumSetSetting<BlockWrong> blockClicks = new EnumSetSetting<>("Block Wrong Clicks", BlockWrong.class, List.of(BlockWrong.SOLUTION));

    private final NumberSetting<Integer> lagTicks = new NumberSetting<>("Lag Ticks", 0, 10, 2, 1)
            .isVisible(() -> blockClicks.contains(BlockWrong.SERVER_TICK));


    // Maybe u can change this to a hardcoded value, we have never needed to change this from 7
    // decided to remove from config gui, can still be edited via the file
    private final NumberSetting<Integer> lanternTicks = new NumberSetting<>("Lantern Ticks", 0, 10, 7, 1).isVisible(() -> false);
    private final EnumSetting<OFA> singleSkipFix = new EnumSetting<>("Single Skip Fix", OFA.AUTO);
    private final BooleanSetting memory = new BooleanSetting("Remember Solution", true);

    private final EnumSetting<StateHUD> stateEnabled = new EnumSetting<>("State HUD", StateHUD.OFF);
    private final EnumSetSetting<StateSetting> stateSettings = new EnumSetSetting<>("State Settings", StateSetting.class, List.of()).isVisible(() -> !stateEnabled.is(StateHUD.OFF));
    private final EnumSetSetting<Message> messages = new EnumSetSetting<>("Messages", Message.class, List.of());

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

    public final HudSetting stateHud = new HudSetting("SS State", new Vector2f(50, 50)) {
        @Override
        protected @NonNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            if (example) return dims(this.text(gfx, "6/7", 0, 0));
            return dims(stateHud.text(gfx, message, 0, 0));
        }
    }.isVisible(() ->
            !this.stateEnabled.is(StateHUD.OFF) && message != null
                    && ((isAtI4() && (this.stateEnabled.is(StateHUD.HIDE_AT_I4) || this.stateEnabled.is(StateHUD.HIDE_AT_BOTH)))
                    || (module.isAtSS() && (this.stateEnabled.is(StateHUD.HIDE_AT_SIMON_SAYS)
                    || this.stateEnabled.is(StateHUD.HIDE_AT_BOTH)))
            ) && !module.ssDone);

    public SSSolver(SimonSays module) {
        super(module);
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

    @SubscribeEvent
    private void onBlockUpdate(BlockChangeEvent event) {
        Vec3 vec3 = event.getVec3();
        Block block = event.getNewState().getBlock();
        int x = (int) vec3.x;
        int y = (int) vec3.y;
        int z = (int) vec3.z;

//        // This is very wip, it seems to work, but we didn't test it much. maybe js remove it idk
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
                if (state.lanternTicks >= lanternTicks.getValue() && onSkip) {
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
    private void onServerTick(TickEvent.Server event) {
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
                        if (messages.contains(Message.BREAK_CLIENT)) SimonSays.chat(ChatFormatting.RED + "SS Broke!");
                        if (messages.contains(Message.BREAK_PARTY)) PlayerUtils.command("pc SS Broke!");
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
                // :pray:
                // if (state.clicked) state.setClicked();
            }

            else state.obsidianTicks++;
        });
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
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
    private void onMouseEvent(PlayerInputEvent.Use event) {
        if (mc.player == null || event.getResult() == null || !solver.getValue()) return;

        net.minecraft.world.phys.Vec3 vec3 = event.getResult().getLocation();
        int x = (int) vec3.x;
        int y = (int) vec3.y;
        int z = (int) vec3.z;

        if (x == 110 && y >= 120 && y < 124 && z >= 92 && z < 96) {
            State state = coordsToState(111, y, z);
            State first = render.stream().filter(s -> !s.clicked).findFirst().orElse(null);

            boolean isFirst;
            if (state != null) isFirst = first == state;
            else {
                isFirst = render.isEmpty();
            }

            boolean canBlock = blockClicks.contains(BlockWrong.SOLUTION);
            boolean solutionBwc = (canBlock && !isFirst);

            boolean lagBwc = (blockClicks.contains(BlockWrong.SERVER_TICK) && lagTicksRemaining > 0);

            if ((solutionBwc || lagBwc) && !mc.player.isShiftKeyDown()) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    private void onBlockPlacement(PacketEvent.Send event, ServerboundUseItemOnPacket packet) {
        BlockPos blockPos = packet.hitResult().getBlockPos();
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
            lagTicksRemaining = lagTicks.getValue();
            state.setClicked();
        });
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent.Extract event) {
        if (!solver.getValue()) return;
        int i = 0;
        for (State button : render) {
            if (button.clicked) continue;
            var color = switch (i) {
                case 0 -> first;
                case 1 -> second;
                case 2 -> third;
                case 3 -> fourth;
                default -> fifth;
            };
            i++;

            renderButton(event, button.y, button.z, color.getValue(), color.getOtherValue());
        }
    }

    public void renderButton(Render3DEvent.Extract event, int y, int z, Color outline, Color fill) {
        AABB aabb = BUTTON.move(110, y, z);
        renderMode.getValue().draw(event, aabb, fill, outline, depth.getValue());
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
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
        if (solution.size() == 3 && totalLanterns == 3 && onFirstRound && singleSkipFix.any(OFA.AUTO, OFA.ON)) {
            if (singleSkipFix.is(OFA.ON) || totalClicks <= 3) {
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

    public void onRoundStart() {
        onFirstRound = false;
        int length = render.stream().filter(s -> s.lastSpawn >= startPress).toList().size();
        if (length > 0 && length < 5) {
            String time = NumberUtils.millisToSSMS(System.currentTimeMillis() - p3Start);
            String finalTime = p3Start != -1 ? ChatFormatting.GRAY + "(" + ChatFormatting.GREEN + time + "s" + ChatFormatting.GRAY + ")" : "";

            if (messages.contains(Message.ROUND_CLIENT)) SimonSays.chat("$length/5 $finalTime");
            if (messages.contains(Message.ROUND_PARTY)) PlayerUtils.command("pc SS $length/5");
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
        String time = NumberUtils.millisToSSMS(System.currentTimeMillis() - p3Start);
        setDoneMessage(time + "s");
        if (p3Start != -1) {
            if (messages.contains(Message.COMPLETE_CLIENT)) SimonSays.chat("Simon Says completed in " + ChatFormatting.GREEN + time + "s" + ChatFormatting.RESET + ".");
            if (messages.contains(Message.COMPLETE_PARTY)) PlayerUtils.command("pc Simon Says completed in ${time}s.");
        }
    }

    public void setStateMessage(String message) {
        this.message = message.trim();
    }

    public void setBreakMessage() {
        if (!stateSettings.contains(StateSetting.BREAK)) return;
        setStateMessage(ChatFormatting.RED + "SS Broke!");
    }

    public void setRoundMessage(String round, String time) {
        if (!stateSettings.contains(StateSetting.ROUND)) return;
        String msg = ChatFormatting.DARK_AQUA + round + "/5 ";

        if (time != null) {
            msg += ChatFormatting.GRAY + "(" +
                    ChatFormatting.GREEN + time +
                    ChatFormatting.GRAY + ")";
        }
        setStateMessage(msg);
    }

    public void setDoneMessage(String time) {
        if (!stateSettings.contains(StateSetting.DONE)) return;
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

    private enum StateHUD {
        OFF,
        HIDE_AT_SIMON_SAYS,
        HIDE_AT_I4,
        HIDE_AT_BOTH,
        ALWAYS
    }

    private enum StateSetting {
        BREAK,
        ROUND,
        DONE
    }

    private enum Message {
        COMPLETE_CLIENT,
        BREAK_CLIENT,
        ROUND_CLIENT,
        COMPLETE_PARTY,
        BREAK_PARTY,
        ROUND_PARTY
    }

    private enum BlockWrong {
        SOLUTION,
        SERVER_TICK
    }
}
