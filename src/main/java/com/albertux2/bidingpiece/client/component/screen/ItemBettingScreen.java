package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.client.component.DrawUtils;
import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.albertux2.bidingpiece.messaging.Messages;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ItemBettingScreen extends Screen {
    private static final int SCROLLBAR_W = 6;
    private static final int PAD_L = 16;
    private static final int PAD_R = 16;

    private final Screen parent;
    private final PlayerEntity player;
    private final List<BetItem> betItems = new ArrayList<>();

    private TextFieldWidget quantityField;
    private FancyButton addBtn;
    private FancyButton backBtn;
    private FancyButton submitBtn;

    private ItemStack selectedStack = ItemStack.EMPTY;
    private int scrollOffsetInv = 0;
    private int scrollOffsetList = 0;
    private final List<ItemStack> flatInventory;

    private String errorMessage;

    private int gridStartX, gridStartY, itemsPerRow, maxRows, cellSize, cellPad, cellPitch;
    private int listStartY, listVisibleCount, listItemHeight;
    private int bottomPanelH, bottomPanelPad;

    public ItemBettingScreen(Screen parent) {
        super(new StringTextComponent("Item Betting"));
        this.parent = parent;
        this.player = Minecraft.getInstance().player;
        this.flatInventory = flattenInventory(this.player);
    }

    @Override
    protected void init() {
        relayout();

        quantityField = new TextFieldWidget(this.font, 0, 0, (int) Math.round(120 * uiScale()), (int) Math.round(20 * uiScale()), new StringTextComponent(""));
        quantityField.setValue("1");
        quantityField.setFilter(s -> s.matches("\\d*") && (s.isEmpty() || Long.parseLong(s) < Integer.MAX_VALUE));
        this.children.add(quantityField);

        renderButtons();
    }

    private void renderButtons() {
        addBtn = new FancyButton(0, 0, (int) Math.round(64 * uiScale()), (int) Math.round(24 * uiScale()), new StringTextComponent("Add"), (b) -> addSelectedItem());
        backBtn = new FancyButton((int) Math.round(10 * uiScale()), (int) Math.round(10 * uiScale()), (int) Math.round(70 * uiScale()), (int) Math.round(24 * uiScale()), new StringTextComponent("← Back"), (b) -> Minecraft.getInstance().setScreen(parent));
        submitBtn = new FancyButton(0, 0, (int) Math.round(180 * uiScale()), (int) Math.round(26 * uiScale()), new StringTextComponent("Submit Bet"), (b) -> submitBet());

        addButton(addBtn);
        addButton(backBtn);
        addButton(submitBtn);
    }

    private double uiScale() {
        double guiScale = (double) Minecraft.getInstance().getWindow().getHeight() / (double) this.height;
        double s = 1.35 / guiScale;
        return Math.max(1.00, Math.min(1.40, s));
    }

    private int minListRowsDynamic() {
        return (this.height <= 320) ? 3 : 4;
    }

    private void relayout() {
        double s = uiScale();

        cellSize = Math.max(22, (int) Math.round(28 * s));
        cellPad = Math.max(2, (int) Math.round(4 * s));
        cellPitch = cellSize + cellPad + cellPad;

        listItemHeight = Math.max(cellSize + 8, (int) Math.round(24 * s));
        bottomPanelPad = Math.max(4, (int) Math.round(6 * s));
        bottomPanelH = Math.max((int) Math.round(40 * s), this.font.lineHeight + (int) Math.round(22 * s));

        int left = Math.max(12, (int) Math.round(20 * s));
        int right = left;
        int top = Math.max(40, (int) Math.round(56 * s));
        int gap = Math.max(16, (int) Math.round(20 * s));
        int reserveSB = SCROLLBAR_W + 4;

        itemsPerRow = Math.max(1, Math.min(9, (this.width - left - right - reserveSB) / cellPitch));

        int availableH = Math.max(cellPitch, this.height - top - bottomPanelH - bottomPanelPad - gap);
        int reservedForList = minListRowsDynamic() * listItemHeight + gap;

        int gridHeight = Math.max(cellPitch, availableH - reservedForList);
        maxRows = Math.max(1, gridHeight / cellPitch);

        while (maxRows > 1) {
            int usedByGrid = maxRows * cellPitch;
            int leftForList = availableH - usedByGrid - gap;
            if (leftForList >= minListRowsDynamic() * listItemHeight) break;
            maxRows--;
        }

        gridStartX = left + ((this.width - left - right - reserveSB) - (itemsPerRow * cellPitch - cellPad - cellPad)) / 2;
        gridStartY = top;

        listStartY = gridStartY + maxRows * cellPitch + gap;
        int listSpace = Math.max(0, this.height - bottomPanelH - bottomPanelPad - listStartY);
        listVisibleCount = Math.max(minListRowsDynamic(), listSpace / listItemHeight);

        clampScrolls();
    }

    private int visibleListSlots() {
        int panelTop = this.height - bottomPanelH;
        int maxRowsBySpace = Math.max(0, (panelTop - bottomPanelPad - listStartY) / listItemHeight);
        return Math.max(minListRowsDynamic(), Math.min(listVisibleCount, maxRowsBySpace));
    }

    private int totalInventoryRows() {
        return Math.max(1, (int) Math.ceil(flatInventory.size() / (double) itemsPerRow));
    }

    private void clampScrolls() {
        int invRows = totalInventoryRows();
        int maxScrollInv = Math.max(0, invRows - maxRows);
        scrollOffsetInv = clamp(scrollOffsetInv, 0, maxScrollInv);

        int vis = visibleListSlots();
        int maxScrollList = Math.max(0, betItems.size() - vis);
        scrollOffsetList = clamp(scrollOffsetList, 0, maxScrollList);
    }

    private void addSelectedItem() {
        if (selectedStack.isEmpty()) return;
        if (quantityField.getValue().isEmpty()) return;

        int quantity = Integer.parseInt(quantityField.getValue());
        if (quantity <= 0) return;

        BetItem existing = betItems.stream().filter(i -> ItemStack.isSame(i.stack, selectedStack)).findFirst().orElse(null);
        if (existing != null) {
            int newQ = existing.quantity + quantity;
            if (hasEnoughQuantity(newQ)) {
                existing.quantity = newQ;
                errorMessage = null;
            }
        } else {
            if (hasEnoughQuantity(quantity)) {
                betItems.add(new BetItem(selectedStack.copy(), quantity));
                errorMessage = null;
            }
        }
        quantityField.setValue("1");
        relayout();
        int vis = visibleListSlots();
        scrollOffsetList = Math.min(scrollOffsetList, Math.max(0, betItems.size() - vis));
    }

    private Boolean hasEnoughQuantity(int quantity) {
        Boolean ok = flatInventory.stream().filter(i -> ItemStack.isSame(i, selectedStack)).map(i -> i.getCount() >= quantity).findFirst().orElse(true);
        if (!ok) errorMessage = Messages.QUANTITY_EXCEEDED;
        return ok;
    }

    private void submitBet() {
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(ms, 0, 0, this.width, this.height, 0x80000000, 0x80202020);
        relayout();

        int panelTop = this.height - bottomPanelH;
        fill(ms, PAD_L, panelTop, this.width - PAD_R, this.height, 0xB0202020);

        layoutBottomRow(ms, panelTop);

        renderInventory(ms, mouseX, mouseY);
        renderSelectedList(ms, mouseX, mouseY);

        super.render(ms, mouseX, mouseY, partialTicks);
        quantityField.render(ms, mouseX, mouseY, partialTicks);

        int yourItemsX = backBtn.x + backBtn.getWidth() + (int) Math.round(12 * uiScale());
        int yourItemsY = backBtn.y + (backBtn.getHeight() - this.font.lineHeight) / 2 - 1;
        this.font.draw(ms, "Your Items:", yourItemsX, yourItemsY, 0xFFFFFF);
        this.font.draw(ms, "Selected Items:", PAD_L, listStartY - (int) Math.round(18 * uiScale()), 0xFFFFFF);

        if (StringUtils.isNotBlank(errorMessage)) {
            DrawUtils.drawTextWithBorder(ms, this.font, errorMessage, PAD_L, panelTop - (int) Math.round(18 * uiScale()), 0xffab2a3e, 0xFFFFFF);
        }
    }

    private void layoutBottomRow(MatrixStack ms, int panelTop) {
        int leftPad = PAD_L;
        int gap = (int) Math.round(8 * uiScale());
        int rowH = Math.max(quantityField.getHeight(), addBtn.getHeight());
        int rowY = panelTop + (bottomPanelH - rowH) / 2;

        String qtyTxt = "Quantity:";
        int labelX = leftPad;
        int labelW = this.font.width(qtyTxt);
        this.font.draw(ms, qtyTxt, labelX, rowY + (rowH - this.font.lineHeight) / 2, 0xFFFFFF);

        int minInputW = (int) Math.round(120 * uiScale());
        int addW = (int) Math.round(64 * uiScale());
        int submitW = (int) Math.round(180 * uiScale());

        submitBtn.setWidth(submitW);
        submitBtn.y = rowY;
        submitBtn.x = this.width - PAD_R - submitW;

        int inputX = labelX + labelW + gap;
        int maxInputRight = submitBtn.x - gap - addW - gap;
        int inputW = Math.max(minInputW, maxInputRight - inputX);

        quantityField.setWidth(inputW);
        quantityField.x = inputX;
        quantityField.y = rowY;

        addBtn.setWidth(addW);
        addBtn.x = quantityField.x + quantityField.getWidth() + gap;
        addBtn.y = rowY;
    }

    private void renderInventory(MatrixStack ms, int mouseX, int mouseY) {
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                int index = (row + scrollOffsetInv) * itemsPerRow + col;
                if (index >= flatInventory.size()) break;
                ItemStack stack = flatInventory.get(index);
                if (stack.isEmpty()) continue;

                int x = gridStartX + col * cellPitch;
                int y = gridStartY + row * cellPitch;

                boolean isSelected = ItemStack.isSame(stack, selectedStack);
                int color = isSelected ? 0xFF4A90E2 : 0xFF404040;
                fill(ms, x - 2, y - 2, x + cellSize + 2, y + cellSize + 2, color);
                fill(ms, x, y, x + cellSize, y + cellSize, 0xFF2A2A2A);

                int ix = x + Math.max(2, cellSize / 4);
                int iy = y + Math.max(2, cellSize / 4);
                Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, ix, iy);
                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.font, stack, ix, iy + 4);

                if (hit(mouseX, mouseY, x, y, cellSize, cellSize)) {
                    fill(ms, x, y, x + cellSize, y + cellSize, 0x80FFFFFF);
                }
            }
        }

        int totalRows = totalInventoryRows();
        if (totalRows > maxRows) {
            int barX = gridStartX + itemsPerRow * cellPitch + 2;
            int barY = gridStartY;
            int barH = maxRows * cellPitch;
            drawScrollbar(ms, barX, barY, barH, totalRows, maxRows, scrollOffsetInv);
        }
    }

    private void renderSelectedList(MatrixStack ms, int mouseX, int mouseY) {
        int visible = visibleListSlots();

        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffsetList;
            int y = listStartY + i * listItemHeight;

            fill(ms, PAD_L, y, this.width - PAD_R, y + listItemHeight, 0xFF333333);

            if (idx < betItems.size()) {
                BetItem item = betItems.get(idx);

                int iconX = PAD_L + 5;
                int iconY = y + (listItemHeight - 16) / 2;
                Minecraft.getInstance().getItemRenderer().renderGuiItem(item.stack, iconX, iconY);

                int textY = y + (listItemHeight - this.font.lineHeight) / 2;
                String name = item.stack.getHoverName().getString();
                String quantity = " x" + item.quantity;

                int nameX = PAD_L + 30;
                this.font.draw(ms, name, nameX, textY, 0xFFFFFF);

                int deleteX = this.width - PAD_R - (int) Math.round(60 * uiScale());
                int delH = listItemHeight - 6;
                int delW = delH;
                int qtyX = deleteX - (int) Math.round(16 * uiScale()) - this.font.width(quantity);

                this.font.draw(ms, quantity, qtyX, textY, 0xFFFFFF);

                int delY = y + (listItemHeight - delH) / 2;
                fill(ms, deleteX, delY, deleteX + delW, delY + delH, 0xFFCC4444);
                this.font.draw(ms, "×", deleteX + delW / 2 - 3, delY + delH / 2 - this.font.lineHeight / 2, 0xFFFFFF);

                if (hit(mouseX, mouseY, deleteX, delY, delW, delH)) {
                    fill(ms, deleteX, delY, deleteX + delW, delY + delH, 0xFFFF6666);
                }
            } else {
                int textY = y + (listItemHeight - this.font.lineHeight) / 2;
                this.font.draw(ms, "-", PAD_L + 30, textY, 0x88FFFFFF);
            }
        }

        if (betItems.size() > visible) {
            int barX = this.width - PAD_R - SCROLLBAR_W;
            int barY = listStartY;
            int barH = visible * listItemHeight;
            drawScrollbar(ms, barX, barY, barH, betItems.size(), visible, scrollOffsetList);
        }
    }

    private void drawScrollbar(MatrixStack ms, int x, int y, int h, int total, int visible, int offset) {
        fill(ms, x, y, x + SCROLLBAR_W, y + h, 0xFF202020);
        int thumbH = Math.max(20, (int) (h * (visible / (float) total)));
        int thumbY = y + (int) ((h - thumbH) * (offset / (float) (total - visible)));
        fill(ms, x, thumbY, x + SCROLLBAR_W, thumbY + thumbH, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                int index = (row + scrollOffsetInv) * itemsPerRow + col;
                if (index >= flatInventory.size()) break;
                ItemStack stack = flatInventory.get(index);
                if (stack.isEmpty()) continue;

                int x = gridStartX + col * cellPitch;
                int y = gridStartY + row * cellPitch;

                if (hit(mouseX, mouseY, x, y, cellSize, cellSize)) {
                    selectedStack = stack;
                    return true;
                }
            }
        }

        int visible = visibleListSlots();
        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffsetList;
            if (idx >= betItems.size()) continue;

            int y = listStartY + i * listItemHeight;
            int deleteX = this.width - PAD_R - (int) Math.round(60 * uiScale());
            int delH = listItemHeight - 6;
            int delW = delH;
            int delY = y + (listItemHeight - delH) / 2;

            if (hit(mouseX, mouseY, deleteX, delY, delW, delH)) {
                betItems.remove(idx);
                int maxScroll = Math.max(0, betItems.size() - visible);
                scrollOffsetList = Math.min(scrollOffsetList, maxScroll);
                relayout();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int gridBottom = gridStartY + maxRows * cellPitch;
        if (mouseY >= gridStartY && mouseY <= gridBottom) {
            int maxScroll = Math.max(0, totalInventoryRows() - maxRows);
            scrollOffsetInv = clamp(scrollOffsetInv - (int) Math.signum(delta), 0, maxScroll);
            return true;
        }

        int panelTop = this.height - bottomPanelH;
        int listBottom = panelTop - bottomPanelPad;
        if (mouseY >= listStartY && mouseY <= listBottom) {
            int visible = visibleListSlots();
            int maxScroll = Math.max(0, betItems.size() - visible);
            scrollOffsetList = clamp(scrollOffsetList - (int) Math.signum(delta), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        relayout();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<ItemStack> flattenInventory(PlayerEntity player) {
        NonNullList<ItemStack> items = player.inventory.items;
        Map<String, BetItem> flattened = new HashMap<>();
        items.stream().filter(i -> !i.isEmpty()).forEach(x -> flattened.computeIfAbsent(x.getItem().getDescriptionId(), s -> new BetItem(x, 0)).sumQuantity(x.getCount()));
        return flattened.values().stream().map(x -> new ItemStack(x.stack.getItem(), x.quantity)).collect(Collectors.toList());
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static class BetItem {
        public final ItemStack stack;
        public int quantity;

        public BetItem(ItemStack stack, int quantity) {
            this.stack = stack;
            this.quantity = quantity;
        }

        public BetItem sumQuantity(int plus) {
            this.quantity += plus;
            return this;
        }
    }
}
