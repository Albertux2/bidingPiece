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
    private final Screen parent;
    private final PlayerEntity player;
    private final List<BetItem> betItems;
    private TextFieldWidget quantityField;
    private ItemStack selectedStack = ItemStack.EMPTY;
    private int scrollOffsetInv = 0;
    private int scrollOffsetList = 0;
    private final List<ItemStack> flatennedInventory;
    private String errorMessage;

    private int gridStartX;
    private int gridStartY;
    private int itemsPerRow;
    private int maxRows;
    private int cellSize;
    private int cellPad;
    private int cellPitch;

    private int listStartY;
    private int listVisibleCount;
    private int listItemHeight;

    private int bottomPanelH;
    private int bottomPanelPad;

    private FancyButton addBtn;
    private FancyButton backBtn;
    private FancyButton submitBtn;

    public ItemBettingScreen(Screen parent) {
        super(new StringTextComponent("Item Betting"));
        this.parent = parent;
        this.player = Minecraft.getInstance().player;
        this.betItems = new ArrayList<>();
        this.flatennedInventory = flattenInventory(Minecraft.getInstance().player);
    }

    @Override
    protected void init() {
        super.init();
        recalcLayout();
        this.quantityField = new TextFieldWidget(this.font, 0, 0, (int) Math.round(120 * uiScale()), (int) Math.round(20 * uiScale()), new StringTextComponent(""));
        this.quantityField.setValue("1");
        this.quantityField.setFilter(s -> s.matches("\\d*") && (s.isEmpty() || Long.parseLong(s) < Integer.MAX_VALUE));
        this.children.add(this.quantityField);

        this.addBtn = new FancyButton(0, 0, (int) Math.round(64 * uiScale()), (int) Math.round(24 * uiScale()), new StringTextComponent("Add"), (b) -> addSelectedItem());
        this.backBtn = new FancyButton((int) Math.round(10 * uiScale()), (int) Math.round(10 * uiScale()), (int) Math.round(70 * uiScale()), (int) Math.round(24 * uiScale()), new StringTextComponent("← Back"), (b) -> Minecraft.getInstance().setScreen(parent));
        this.submitBtn = new FancyButton(0, 0, (int) Math.round(180 * uiScale()), (int) Math.round(26 * uiScale()), new StringTextComponent("Submit Bet"), (b) -> submitBet());

        this.addButton(addBtn);
        this.addButton(backBtn);
        this.addButton(submitBtn);
    }

    private double uiScale() {
        double sw = this.width / 854.0;
        double sh = this.height / 480.0;
        double s = Math.min(sw, sh);
        return Math.max(0.85, Math.min(1.25, s));
    }

    private void recalcLayout() {
        double s = uiScale();

        this.cellSize = Math.max(22, (int) Math.round(28 * s));
        this.cellPad = Math.max(2, (int) Math.round(4 * s));
        this.cellPitch = cellSize + cellPad + cellPad;

        this.listItemHeight = Math.max(cellSize + 8, (int) Math.round(24 * s));
        this.bottomPanelPad = Math.max(4, (int) Math.round(6 * s));
        this.bottomPanelH = Math.max((int) Math.round(40 * s), this.font.lineHeight + (int) Math.round(22 * s));


        int left = Math.max(12, (int) Math.round(20 * s));
        int right = left;
        int top = Math.max(40, (int) Math.round(56 * s));
        int gap = Math.max(8, (int) Math.round(12 * s));

        this.itemsPerRow = Math.max(1, Math.min(9, (this.width - left - right) / this.cellPitch));

        int availableH = Math.max(this.cellPitch, this.height - top - bottomPanelH - gap);
        int minListRows = 4;
        int reservedForList = minListRows * this.listItemHeight + gap;

        int gridHeight = Math.max(this.cellPitch, availableH - reservedForList);
        this.maxRows = Math.max(1, gridHeight / this.cellPitch);

        while (this.maxRows > 1) {
            int usedByGrid = this.maxRows * this.cellPitch;
            int leftForList = availableH - usedByGrid - gap;
            if (leftForList >= minListRows * this.listItemHeight) break;
            this.maxRows--;
        }

        this.gridStartX = left + ((this.width - left - right) - (this.itemsPerRow * this.cellPitch - cellPad - cellPad)) / 2;
        this.gridStartY = top;

        this.listStartY = this.gridStartY + this.maxRows * this.cellPitch + gap;
        int listSpace = Math.max(0, this.height - bottomPanelH - this.listStartY);
        this.listVisibleCount = Math.max(minListRows, listSpace / this.listItemHeight);

        int invRows = Math.max(1, (int) Math.ceil(flatennedInventory.size() / (double) itemsPerRow));
        int maxScrollInv = Math.max(0, invRows - maxRows);
        this.scrollOffsetInv = Math.max(0, Math.min(this.scrollOffsetInv, maxScrollInv));

        int maxScrollList = Math.max(0, betItems.size() - listVisibleCount);
        this.scrollOffsetList = Math.max(0, Math.min(this.scrollOffsetList, maxScrollList));
    }

    private void addSelectedItem() {
        if (!selectedStack.isEmpty() && !quantityField.getValue().isEmpty()) {
            int quantity = Integer.parseInt(quantityField.getValue());
            if (quantity > 0) {
                BetItem existing = betItems.stream().filter(item -> ItemStack.isSame(item.stack, selectedStack)).findFirst().orElse(null);
                if (existing != null) {
                    int newQuantity = existing.quantity + quantity;
                    if (hasEnoughQuantity(newQuantity)) {
                        existing.quantity += quantity;
                        this.errorMessage = null;
                    }
                } else {
                    if (hasEnoughQuantity(quantity)) {
                        betItems.add(new BetItem(selectedStack.copy(), quantity));
                        this.errorMessage = null;
                    }
                }
                quantityField.setValue("1");
                recalcLayout();
            }
        }
    }

    private Boolean hasEnoughQuantity(int quantity) {
        Boolean b = flatennedInventory.stream().filter(item -> ItemStack.isSame(item, selectedStack)).map(item -> item.getCount() >= quantity).findFirst().orElse(true);
        if (!b) this.errorMessage = Messages.QUANTITY_EXCEEDED;
        return b;
    }

    private void submitBet() {
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(ms, 0, 0, this.width, this.height, 0x80000000, 0x80202020);
        recalcLayout();

        int panelTop = this.height - bottomPanelH;
        fill(ms, 0, panelTop, this.width, this.height, 0xB0202020);

        int leftPad = 20;
        int gap = (int) Math.round(8 * uiScale());
        int rowH = Math.max(this.quantityField.getHeight(), this.addBtn.getHeight());
        int rowY = panelTop + (bottomPanelH - rowH) / 2;

        String qtyTxt = "Quantity:";
        int labelX = leftPad;
        int labelW = this.font.width(qtyTxt);
        this.font.draw(ms, qtyTxt, labelX, rowY + (rowH - this.font.lineHeight) / 2, 0xFFFFFF);

        int minInputW = (int) Math.round(100 * uiScale());
        int addW = (int) Math.round(64 * uiScale());
        int submitW = (int) Math.round(180 * uiScale());

        this.submitBtn.setWidth(submitW);
        this.submitBtn.y = rowY;
        this.submitBtn.x = this.width - submitW - leftPad;

        int inputX = labelX + labelW + gap;
        int maxInputRight = this.submitBtn.x - gap - addW - gap;
        int inputW = Math.max(minInputW, maxInputRight - inputX);

        this.quantityField.setWidth(inputW);
        this.quantityField.x = inputX;
        this.quantityField.y = rowY;

        this.addBtn.setWidth(addW);
        this.addBtn.x = this.quantityField.x + this.quantityField.getWidth() + gap;
        this.addBtn.y = rowY;

        renderInventoryGrid(ms, mouseX, mouseY);
        renderBetList(ms, mouseX, mouseY);

        super.render(ms, mouseX, mouseY, partialTicks);
        this.quantityField.render(ms, mouseX, mouseY, partialTicks);

        int yourItemsX = backBtn.x + backBtn.getWidth() + (int) Math.round(12 * uiScale());
        int yourItemsY = backBtn.y + (backBtn.getHeight() - this.font.lineHeight) / 2;
        this.font.draw(ms, "Your Items:", yourItemsX, yourItemsY, 0xFFFFFF);
        this.font.draw(ms, "Selected Items:", 20, this.listStartY - (int) Math.round(18 * uiScale()), 0xFFFFFF);

        if (StringUtils.isNotBlank(this.errorMessage)) {
            DrawUtils.drawTextWithBorder(ms, this.font, this.errorMessage, 20, panelTop - (int) Math.round(18 * uiScale()), 0xffab2a3e, 0xFFFFFF);
        }
    }


    private void renderInventoryGrid(MatrixStack ms, int mouseX, int mouseY) {
        List<ItemStack> inventory = this.flatennedInventory;
        for (int row = 0; row < this.maxRows; row++) {
            for (int col = 0; col < this.itemsPerRow; col++) {
                int index = (row + scrollOffsetInv) * itemsPerRow + col;
                if (index >= inventory.size()) break;
                ItemStack stack = inventory.get(index);
                if (stack.isEmpty()) continue;

                int x = gridStartX + col * this.cellPitch;
                int y = gridStartY + row * this.cellPitch;

                boolean isSelected = ItemStack.isSame(stack, selectedStack);
                int color = isSelected ? 0xFF4A90E2 : 0xFF404040;
                fill(ms, x - 2, y - 2, x + cellSize + 2, y + cellSize + 2, color);
                fill(ms, x, y, x + cellSize, y + cellSize, 0xFF2A2A2A);

                Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x + Math.max(2, cellSize / 4), y + Math.max(2, cellSize / 4));
                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.font, stack, x + Math.max(2, cellSize / 4), y + Math.max(2, cellSize / 4) + 4);

                if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize) {
                    fill(ms, x, y, x + cellSize, y + cellSize, 0x80FFFFFF);
                }
            }
        }
    }

    private void renderBetList(MatrixStack ms, int mouseX, int mouseY) {
        int slots = Math.max(listVisibleCount, 4);
        int panelTop = this.height - bottomPanelH;
        int maxY = panelTop - bottomPanelPad;
        for (int i = 0; i < slots; i++) {
            int idx = i + scrollOffsetList;
            int y = listStartY + i * listItemHeight;
            if (y + listItemHeight > maxY) break;

            fill(ms, 20, y, this.width - 20, y + listItemHeight, 0xFF333333);

            if (idx < betItems.size()) {
                BetItem item = betItems.get(idx);

                int iconY = y + (listItemHeight - 16) / 2;
                Minecraft.getInstance().getItemRenderer().renderGuiItem(item.stack, 25, iconY);

                int textY = y + (listItemHeight - this.font.lineHeight) / 2;
                String name = item.stack.getHoverName().getString();
                String quantity = " x" + item.quantity;

                this.font.draw(ms, name, 50, textY, 0xFFFFFF);
                int deleteX = this.width - (int) Math.round(50 * uiScale());
                int delH = listItemHeight - 6;
                int delW = delH;
                int qtyX = deleteX - (int) Math.round(16 * uiScale()) - this.font.width(quantity);

                this.font.draw(ms, quantity, qtyX, textY, 0xFFFFFF);
                int delY = y + (listItemHeight - delH) / 2;
                fill(ms, deleteX, delY, deleteX + delW, delY + delH, 0xFFCC4444);
                this.font.draw(ms, "×", deleteX + delW / 2 - 3, delY + delH / 2 - this.font.lineHeight / 2, 0xFFFFFF);

                if (mouseX >= deleteX && mouseX <= deleteX + delW && mouseY >= delY && mouseY <= delY + delH) {
                    fill(ms, deleteX, delY, deleteX + delW, delY + delH, 0xFFFF6666);
                }
            } else {
                int textY = y + (listItemHeight - this.font.lineHeight) / 2;
                this.font.draw(ms, "-", 50, textY, 0x88FFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ItemStack> inventory = this.flatennedInventory;

        for (int row = 0; row < this.maxRows; row++) {
            for (int col = 0; col < this.itemsPerRow; col++) {
                int index = (row + scrollOffsetInv) * itemsPerRow + col;
                if (index >= inventory.size()) break;
                ItemStack stack = inventory.get(index);
                if (stack.isEmpty()) continue;

                int x = gridStartX + col * this.cellPitch;
                int y = gridStartY + row * this.cellPitch;

                if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize) {
                    selectedStack = stack;
                    return true;
                }
            }
        }

        int slots = Math.max(listVisibleCount, 4);
        int panelTop = this.height - bottomPanelH;
        for (int i = 0; i < slots; i++) {
            int idx = i + scrollOffsetList;
            if (idx >= betItems.size()) continue;
            int y = listStartY + i * listItemHeight;
            if (y + listItemHeight > panelTop - bottomPanelPad) break;

            int deleteX = this.width - (int) Math.round(50 * uiScale());
            int delH = listItemHeight - 6;
            int delW = delH;
            int delY = y + (listItemHeight - delH) / 2;
            if (mouseX >= deleteX && mouseX <= deleteX + delW && mouseY >= delY && mouseY <= delY + delH) {
                betItems.remove(idx);
                recalcLayout();
                return true;
            }

        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int gridBottom = gridStartY + maxRows * cellPitch;
        if (mouseY >= gridStartY && mouseY <= gridBottom) {
            int invRows = Math.max(1, (int) Math.ceil(flatennedInventory.size() / (double) itemsPerRow));
            int maxScroll = Math.max(0, invRows - maxRows);
            scrollOffsetInv = Math.max(0, Math.min(maxScroll, scrollOffsetInv - (int) Math.signum(delta)));
            return true;
        }
        int listBottom = (this.height - bottomPanelH) - bottomPanelPad;
        if (mouseY >= listStartY && mouseY <= listBottom) {
            int maxScroll = Math.max(0, betItems.size() - listVisibleCount);
            scrollOffsetList = Math.max(0, Math.min(maxScroll, scrollOffsetList - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        recalcLayout();
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
