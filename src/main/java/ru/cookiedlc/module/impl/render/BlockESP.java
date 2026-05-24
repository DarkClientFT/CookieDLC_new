package ru.cookiedlc.module.impl.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.WorldChunk;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ColorSetting;
import ru.cookiedlc.module.api.setting.implement.GroupSetting;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.render.Render3DUtil;
import ru.cookiedlc.event.events.block.BlockUpdateEvent;
import ru.cookiedlc.event.events.render.WorldLoadEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockESP extends Module {

    private final BooleanSetting chest = new BooleanSetting("Сундук", "Подсвечивать сундуки")
            .setValue(true);

    private final BooleanSetting trappedChest = new BooleanSetting("Сундук-ловушка", "Подсвечивать сундуки-ловушки")
            .setValue(true);

    private final BooleanSetting enderChest = new BooleanSetting("Эндер сундук", "Подсвечивать эндер сундуки")
            .setValue(true);

    private final BooleanSetting barrel = new BooleanSetting("Бочка", "Подсвечивать бочки")
            .setValue(true);

    private final BooleanSetting shulker = new BooleanSetting("Шалкер", "Подсвечивать шалкеры")
            .setValue(true);

    private final BooleanSetting furnace = new BooleanSetting("Печка", "Подсвечивать печки")
            .setValue(true);

    private final BooleanSetting blastFurnace = new BooleanSetting("Плавильня", "Подсвечивать плавильни")
            .setValue(true);

    private final BooleanSetting smoker = new BooleanSetting("Коптильня", "Подсвечивать коптильни")
            .setValue(true);

    private final BooleanSetting hopper = new BooleanSetting("Воронка", "Подсвечивать воронки")
            .setValue(true);

    private final BooleanSetting dropper = new BooleanSetting("Выбрасыватель", "Подсвечивать выбрасыватели")
            .setValue(true);

    private final BooleanSetting dispenser = new BooleanSetting("Раздатчик", "Подсвечивать раздатчики")
            .setValue(true);

    private final GroupSetting containersGroup = new GroupSetting("Контейнеры", "Выбор контейнеров для подсветки")
            .settings(chest, trappedChest, enderChest, barrel, shulker, furnace, blastFurnace, smoker, hopper, dropper, dispenser)
            .setValue(true);

    private final ColorSetting chestColor = new ColorSetting("Цвет сундуков", "Цвет для сундуков")
            .setColor(0xFFFFC800).presets(0xFFFFC800, 0xFFFFAA00, 0xFFFF8C00);

    private final ColorSetting enderChestColor = new ColorSetting("Цвет эндер сундуков", "Цвет для эндер сундуков")
            .setColor(0xFF800080).presets(0xFF800080, 0xFF9932CC, 0xFF8B008B);

    private final ColorSetting shulkerColor = new ColorSetting("Цвет шалкеров", "Цвет для шалкеров")
            .setColor(0xFFFF69B4).presets(0xFFFF69B4, 0xFFFF1493, 0xFFDB7093);

    private final ColorSetting furnaceColor = new ColorSetting("Цвет печек", "Цвет для печек")
            .setColor(0xFF808080).presets(0xFF808080, 0xFFA9A9A9, 0xFF696969);

    private final ColorSetting hopperColor = new ColorSetting("Цвет воронок", "Цвет для воронок")
            .setColor(0xFF646464).presets(0xFF646464, 0xFF505050, 0xFF787878);

    private final ColorSetting barrelColor = new ColorSetting("Цвет бочек", "Цвет для бочек")
            .setColor(0xFF8B5A2B).presets(0xFF8B5A2B, 0xFFA0522D, 0xFF6B4226);

    private final GroupSetting colorsGroup = new GroupSetting("Цвета", "Настройки цветов")
            .settings(chestColor, enderChestColor, shulkerColor, furnaceColor, hopperColor, barrelColor)
            .setValue(false);

    private final BooleanSetting drawFill = new BooleanSetting("Заливка", "Рисовать заливку")
            .setValue(true);

    private final GroupSetting renderGroup = new GroupSetting("Рендер", "Настройки рендера")
            .settings(drawFill)
            .setValue(false);

    private final Map<BlockPos, Pair<VoxelShape, Integer>> boxes = new HashMap<>();

    private static final Set<Block> SHULKER_BOXES = new HashSet<>();

    static {
        SHULKER_BOXES.add(Blocks.SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.WHITE_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.ORANGE_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.MAGENTA_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.YELLOW_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.LIME_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.PINK_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.GRAY_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.LIGHT_GRAY_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.CYAN_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.PURPLE_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.BLUE_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.BROWN_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.GREEN_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.RED_SHULKER_BOX);
        SHULKER_BOXES.add(Blocks.BLACK_SHULKER_BOX);
    }

    public BlockESP() {
        super("BlockESP", "BlockESP", ModuleCategory.RENDER);
        setup(containersGroup, colorsGroup, renderGroup);
    }

    @Override
    public void activate() {
        super.activate();
        boxes.clear();
        scanLoadedChunks();
    }

    @Override
    public void deactivate() {
        boxes.clear();
        super.deactivate();
    }

    private void scanLoadedChunks() {
        if (mc.world == null || mc.player == null) return;

        int renderDistance = mc.options.getViewDistance().getValue();
        ChunkPos playerChunk = mc.player.getChunkPos();

        for (int cx = playerChunk.x - renderDistance; cx <= playerChunk.x + renderDistance; cx++) {
            for (int cz = playerChunk.z - renderDistance; cz <= playerChunk.z + renderDistance; cz++) {
                if (mc.world.isChunkLoaded(cx, cz)) {
                    WorldChunk chunk = mc.world.getChunk(cx, cz);
                    if (chunk != null) {
                        scanChunk(chunk);
                    }
                }
            }
        }
    }

    private void scanChunk(WorldChunk chunk) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int minY = mc.world.getBottomY();
        int maxY = mc.world.getHeight();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();

                    if (isTargetBlock(block)) {
                        putBox(pos, state, block);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.player == null || mc.world == null) return;

        boxes.forEach((pos, pair) -> {
            if (drawFill.isValue()) {
                Render3DUtil.drawShape(pos, pair.getLeft(), pair.getRight(), 1);
            } else {
                Render3DUtil.drawShapeAlternative(pos, pair.getLeft(), pair.getRight(), 1, false, false);
            }
        });
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        boxes.clear();
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent e) {
        if (!isEnabled()) return;

        BlockPos pos = e.pos();
        BlockState state = e.state();
        Block block = state.getBlock();

        switch (e.type()) {
            case LOAD -> {
                if (isTargetBlock(block)) {
                    putBox(pos, state, block);
                }
            }
            case UPDATE -> {
                if (isTargetBlock(block) && !boxes.containsKey(pos)) {
                    putBox(pos, state, block);
                }
                if (boxes.containsKey(pos) && (state.isAir() || !isTargetBlock(block))) {
                    boxes.remove(pos);
                }
            }
            case UNLOAD -> boxes.remove(pos);
        }
    }

    private boolean isTargetBlock(Block block) {
        if (chest.isValue() && block == Blocks.CHEST) return true;
        if (trappedChest.isValue() && block == Blocks.TRAPPED_CHEST) return true;
        if (enderChest.isValue() && block == Blocks.ENDER_CHEST) return true;
        if (barrel.isValue() && block == Blocks.BARREL) return true;
        if (shulker.isValue() && SHULKER_BOXES.contains(block)) return true;
        if (furnace.isValue() && block == Blocks.FURNACE) return true;
        if (blastFurnace.isValue() && block == Blocks.BLAST_FURNACE) return true;
        if (smoker.isValue() && block == Blocks.SMOKER) return true;
        if (hopper.isValue() && block == Blocks.HOPPER) return true;
        if (dropper.isValue() && block == Blocks.DROPPER) return true;
        if (dispenser.isValue() && block == Blocks.DISPENSER) return true;

        return false;
    }

    private int getBlockColor(Block block) {
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            return chestColor.getColor();
        }
        if (block == Blocks.ENDER_CHEST) {
            return enderChestColor.getColor();
        }
        if (SHULKER_BOXES.contains(block)) {
            return shulkerColor.getColor();
        }
        if (block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER) {
            return furnaceColor.getColor();
        }
        if (block == Blocks.HOPPER || block == Blocks.DROPPER || block == Blocks.DISPENSER) {
            return hopperColor.getColor();
        }
        if (block == Blocks.BARREL) {
            return barrelColor.getColor();
        }

        return ColorUtil.getClientColor();
    }

    private void putBox(BlockPos pos, BlockState state, Block block) {
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        int color = getBlockColor(block);
        boxes.put(pos, new Pair<>(shape, color));
    }
}