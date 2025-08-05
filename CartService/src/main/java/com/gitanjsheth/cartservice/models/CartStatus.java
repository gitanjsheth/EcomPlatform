package com.gitanjsheth.cartservice.models;

public enum CartStatus {
    ACTIVE,          // Cart is active and can be modified
    CHECKED_OUT,     // Cart has been checked out (order created)
    ABANDONED,       // Cart has been abandoned (not touched for a long time)
    MERGED,          // Cart has been merged with another cart
    EXPIRED          // Cart has expired and will be cleaned up
}