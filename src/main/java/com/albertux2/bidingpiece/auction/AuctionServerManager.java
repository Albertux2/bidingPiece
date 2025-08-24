package com.albertux2.bidingpiece.auction;

import java.util.Objects;

public class AuctionServerManager {

    private static AuctionServerManager INSTANCE;

    public static AuctionServerManager getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = new AuctionServerManager();
        }
        return INSTANCE;
    }

    public boolean hasAuctionStarted() {
        // TODO: calculate with server data
        return true;
    }
}
