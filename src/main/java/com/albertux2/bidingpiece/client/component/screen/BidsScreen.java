package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BidsScreen extends Screen {
    private final AuctionScreen parentScreen;
    private final Auction auction;
    private SelectedListComponent bidsList;

    public BidsScreen(AuctionScreen parentScreen, Auction auction) {
        super(new StringTextComponent("Current Bids"));
        this.parentScreen = parentScreen;
        this.auction = auction;
    }

    @Override
    protected void init() {
        // Botón para volver
        this.addButton(new FancyButton(
            this.width / 2 - 100,
            this.height - 30,
            200, 20,
            new StringTextComponent("Back"),
            button -> minecraft.setScreen(parentScreen)
        ));

        // Inicializar la lista de ofertas
        updateBidsList();
    }

    private void updateBidsList() {
        List<BetItem> bidItems = new ArrayList<>();

        for (Map.Entry<UUID, List<BetItem>> entry : auction.getBids().entrySet()) {
            for (BetItem stack : entry.getValue()) {
                bidItems.add(new BetItem(stack.getStack(), stack.getQuantity()));
            }
        }

        bidsList = new SelectedListComponent(bidItems, this.font, true);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        // Título
        drawCenteredString(matrixStack, font,
            new StringTextComponent("Current Bids").withStyle(TextFormatting.GOLD),
            this.width / 2, 20, 0xFFFFFF);

        // Renderizar lista de ofertas
        if (bidsList != null) {
            int startY = 40;
            int itemH = Math.max(24, (int) Math.round(24 * 1.0));
            bidsList.setLayout(
                this.width,
                startY,
                itemH,
                16,
                16,
                1.0,
                Math.min(6, Math.max(3, (this.height - startY) / itemH))
            );
            bidsList.render(matrixStack);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bidsList != null && bidsList.clicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (bidsList != null && bidsList.mouseScrolled(mouseX, mouseY, delta)) return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
