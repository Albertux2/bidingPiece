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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemBettingScreen extends Screen {
    private final Screen parent;

    private final PlayerEntity player;

    private final List<BetItem> betItems;

    private TextFieldWidget quantityField;

    private ItemStack selectedStack = ItemStack.EMPTY;

    private int scrollOffset = 0;

    private final int ITEMS_PER_ROW = 9;

    private final int MAX_ROWS = 4;

    private final List<ItemStack> flatennedInventory;

    private String errorMessage;

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

        this.quantityField = new TextFieldWidget(this.font, this.width / 2 - 50, this.height - 80, 100, 20, new StringTextComponent(""));
        this.quantityField.setValue("1");
        this.quantityField.setFilter(s -> s.matches("\\d*") && (s.isEmpty() || Long.parseLong(s) < Integer.MAX_VALUE));
        this.children.add(this.quantityField);

        // Botón Add
        this.addButton(new FancyButton(
            this.width / 2 + 60,
            this.height - 82,
            40,
            24,
            new StringTextComponent("Add"),
            (button) -> addSelectedItem()
        ));

        // Botón Back
        this.addButton(new FancyButton(
            10,
            10,
            60,
            24,
            new StringTextComponent("← Back"),
            (button) -> Minecraft.getInstance().setScreen(parent)
        ));

        // Botón Submit Bet
        this.addButton(new FancyButton(
            this.width / 2 - 60,
            this.height - 40,
            120,
            24,
            new StringTextComponent("Submit Bet"),
            (button) -> submitBet()
        ));
    }

    private void addSelectedItem() {
        if (!selectedStack.isEmpty() && !quantityField.getValue().isEmpty()) {
            int quantity = Integer.parseInt(quantityField.getValue());
            if (quantity > 0) {
                BetItem existing = betItems.stream()
                    .filter(item -> ItemStack.isSame(item.stack, selectedStack))
                    .findFirst()
                    .orElse(null);

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
            }
        }
    }

    private Boolean hasEnoughQuantity(int quantity) {
        Boolean b = flatennedInventory.stream()
            .filter(item -> ItemStack.isSame(item, selectedStack))
            .map(item -> item.getCount() >= quantity)
            .findFirst()
            .orElse(true);

        if (!b) {
            this.errorMessage = Messages.QUANTITY_EXCEEDED;
        }
        return b;
    }

    private void submitBet() {
        //    if (betItems.isEmpty()) return;
//
        //    List<SubmitBetPacket.BetEntry> entries = new ArrayList<>();
        //    for (BetItem item : betItems) {
        //        entries.add(new SubmitBetPacket.BetEntry(item.stack, item.quantity));
        //    }
//
        //    NetworkHandler.INSTANCE.sendToServer(new SubmitBetPacket(entries));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0x80000000, 0x80202020);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        renderInventoryGrid(matrixStack, mouseX, mouseY);
        renderBetList(matrixStack, mouseX, mouseY);

        this.quantityField.render(matrixStack, mouseX, mouseY, partialTicks);

        this.font.draw(matrixStack, "Your Items:", 20, 50, 0xFFFFFF);
        this.font.draw(matrixStack, "Quantity:", this.width / 2 - 100, this.height - 75, 0xFFFFFF);
        this.font.draw(matrixStack, "Selected Items:", 20, this.height / 2 + 20, 0xFFFFFF);
        if (StringUtils.isNotBlank(this.errorMessage)) {
            DrawUtils.drawTextWithBorder(matrixStack, this.font, this.errorMessage, this.width / 2 - 100, this.height - 95, 0xffab2a3e, 0xFFFFFF);
        }
    }

    private void renderInventoryGrid(MatrixStack matrixStack, int mouseX, int mouseY) {
        List<ItemStack> inventory = this.flatennedInventory;
        int startX = 20;
        int startY = 70;

        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < ITEMS_PER_ROW; col++) {
                int index = (row + scrollOffset) * ITEMS_PER_ROW + col;
                if (index >= inventory.size()) break;

                ItemStack stack = inventory.get(index);
                if (stack.isEmpty()) continue;

                int x = startX + col * 36;
                int y = startY + row * 36;

                boolean isSelected = ItemStack.isSame(stack, selectedStack);
                int color = isSelected ? 0xFF4A90E2 : 0xFF404040;
                fill(matrixStack, x - 2, y - 2, x + 34, y + 34, color);
                fill(matrixStack, x, y, x + 32, y + 32, 0xFF2A2A2A);

                Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x + 8, y + 8);
                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.font, stack, x + 8, y + 12);

                if (mouseX >= x && mouseX <= x + 32 && mouseY >= y && mouseY <= y + 32) {
                    fill(matrixStack, x, y, x + 32, y + 32, 0x80FFFFFF);
                }
            }
        }
    }

    private void renderBetList(MatrixStack matrixStack, int mouseX, int mouseY) {
        int listY = this.height / 2 + 40;
        int itemHeight = 25;

        for (int i = 0; i < betItems.size(); i++) {
            BetItem item = betItems.get(i);
            int y = listY + i * itemHeight;

            if (y > this.height - 120) break; // No mostrar fuera de pantalla

            fill(matrixStack, 20, y, this.width - 20, y + 22, 0xFF333333);

            Minecraft.getInstance().getItemRenderer().renderGuiItem(item.stack, 25, y + 3);

            String name = item.stack.getHoverName().getString();
            String quantity = " x" + item.quantity;
            this.font.draw(matrixStack, name, 50, y + 7, 0xFFFFFF);
            this.font.draw(matrixStack, quantity, this.width - 100, y + 7, 0xFFFFFF);

            int deleteX = this.width - 50;
            fill(matrixStack, deleteX, y + 2, deleteX + 18, y + 20, 0xFFCC4444);
            this.font.draw(matrixStack, "×", deleteX + 6, y + 7, 0xFFFFFF);

            if (mouseX >= deleteX && mouseX <= deleteX + 18 && mouseY >= y + 2 && mouseY <= y + 20) {
                fill(matrixStack, deleteX, y + 2, deleteX + 18, y + 20, 0xFFFF6666);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ItemStack> inventory = this.flatennedInventory;
        int startX = 20;
        int startY = 70;

        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < ITEMS_PER_ROW; col++) {
                int index = (row + scrollOffset) * ITEMS_PER_ROW + col;
                if (index >= inventory.size()) break;

                ItemStack stack = inventory.get(index);
                if (stack.isEmpty()) continue;

                int x = startX + col * 36;
                int y = startY + row * 36;

                if (mouseX >= x && mouseX <= x + 32 && mouseY >= y && mouseY <= y + 32) {
                    selectedStack = stack;
                    return true;
                }
            }
        }

        // Click en eliminar de la lista
        int listY = this.height / 2 + 40;
        int itemHeight = 25;

        for (int i = 0; i < betItems.size(); i++) {
            int y = listY + i * itemHeight;
            int deleteX = this.width - 50;

            if (mouseX >= deleteX && mouseX <= deleteX + 18 && mouseY >= y + 2 && mouseY <= y + 20) {
                betItems.remove(i);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseY < this.height / 2) {
            int maxScroll = Math.max(0, (player.inventory.items.size() / ITEMS_PER_ROW) - MAX_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<ItemStack> flattenInventory(PlayerEntity player) {
        NonNullList<ItemStack> items = player.inventory.items;
        Map<String, BetItem> flattened = new HashMap<>();

        items.stream()
            .filter(i -> !i.isEmpty())
            .forEach(x -> flattened.computeIfAbsent(x.getItem().getDescriptionId(), s -> new BetItem(x, 0)).sumQuantity(x.getCount()));

        return flattened.values()
            .stream()
            .map(x -> new ItemStack(x.stack.getItem(), x.quantity))
            .collect(Collectors.toList());
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