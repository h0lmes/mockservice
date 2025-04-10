package com.mockachu.repository;

import com.mockachu.domain.OutboundRequest;

public interface OutboundRequestObserver {
    void onRequestCreated(OutboundRequest request);
    void onRequestDeleted(OutboundRequest request);
}
