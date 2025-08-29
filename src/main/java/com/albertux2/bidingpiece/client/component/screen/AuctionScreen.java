package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.ToggleAuctionMessage;
import com.albertux2.bidingpiece.network.UpdateExhibitedItemsPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionScreen extends ContainerScreen<AuctionContainer> {
    private final List<BetItem> exhibitedItems = new ArrayList<>();
    private Auction currentAuction;
    private SelectedListComponent selectedListComponent;
    private Button toggleAuctionButton;
    private Button showBidsButton;

    public AuctionScreen(AuctionContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        requestExhibitedItems();
    }

    private void requestExhibitedItems() {
        NetworkHandler.INSTANCE.sendToServer(new UpdateExhibitedItemsPacket());
    }

    @Override
    public void tick() {
        super.tick();
        // Solicitar items periódicamente para mantener la UI actualizada
        if (minecraft.player.tickCount % 20 == 0) { // Cada segundo
            requestExhibitedItems();
        }
    }

    public void updateExhibitedItems(List<ItemStack> items) {
        List<ItemStack> currentItems = this.exhibitedItems.stream()
            .map(BetItem::getStack)
            .collect(Collectors.toList());

        if (!items.equals(currentItems)) {
            this.exhibitedItems.clear();
            Map<String, BetItem> flattened = new HashMap<>();

            items.stream()
                .filter(i -> !i.isEmpty())
                .forEach(x -> flattened.computeIfAbsent(
                    x.getItem().getDescriptionId(),
                    s -> new BetItem(x, 0)
                ).sumQuantity(x.getCount()));

            this.exhibitedItems.addAll(flattened.values());
            this.init();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        this.leftPos = 0;
        this.topPos = 0;

        selectedListComponent = new SelectedListComponent(exhibitedItems, this.font, false);

        // Botón para iniciar/cancelar subasta
        toggleAuctionButton = this.addButton(new FancyButton(
            this.width / 2 - 100,
            this.height - 50,
            200, 20,
            new StringTextComponent(currentAuction != null && currentAuction.isActive() ? "Cancel Auction" : "Start Auction"),
            button -> {
                NetworkHandler.INSTANCE.sendToServer(new ToggleAuctionMessage(menu.getPodiumPos()));
                if (currentAuction == null) {
                    currentAuction = new Auction(minecraft.player.getUUID(), menu.getExhibitedItems(), true);
                } else {
                    currentAuction.setActive(!currentAuction.isActive());
                }
                updateButtonStates();
            }
        ));

        // Botón para mostrar ofertas
        showBidsButton = this.addButton(new FancyButton(
            this.width / 2 - 100,
            this.height - 25,
            200, 20,
            new StringTextComponent("Show Bids"),
            button -> openBidsScreen()
        ));

        updateButtonStates();
    }

    private void updateButtonStates() {
        if (toggleAuctionButton != null) {
            toggleAuctionButton.setMessage(
                new StringTextComponent(currentAuction != null && currentAuction.isActive() ? "Cancel Auction" : "Start Auction")
            );
        }

        if (showBidsButton != null) {
            showBidsButton.active = currentAuction != null && currentAuction.isActive();
        }
    }

    public void updateAuction(Auction auction) {
        this.currentAuction = auction;
        updateButtonStates();
    }

    private void openBidsScreen() {
        if (currentAuction != null && currentAuction.isActive()) {
            Minecraft.getInstance().setScreen(new BidsScreen(this, currentAuction));
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        if (selectedListComponent != null) {
            int startY = 40;
            int itemH = Math.max(24, (int) Math.round(24 * 1.0));
            selectedListComponent.setLayout(
                this.width,
                startY,
                itemH,
                16,
                16,
                1.0,
                Math.min(6, Math.max(3, (this.height - startY) / itemH))
            );
            selectedListComponent.render(ms);
        }

        drawCenteredString(ms, this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(MatrixStack ms, int x, int y) {
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int x, int y) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedListComponent != null && selectedListComponent.clicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (selectedListComponent != null && selectedListComponent.mouseScrolled(mouseX, mouseY, delta)) return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
