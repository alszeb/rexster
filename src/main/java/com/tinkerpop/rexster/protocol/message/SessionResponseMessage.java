package com.tinkerpop.rexster.protocol.message;

import com.tinkerpop.rexster.protocol.BitWorks;

import java.util.UUID;

public class SessionResponseMessage extends RexProMessage {
    public SessionResponseMessage(RexProMessage message) {
        super(message.getVersion(), message.getType(), message.getFlag(),
                message.getSession(), message.getRequest(), message.getBody());

        if (this.getType() != MessageType.SESSION_RESPONSE) {
            throw new IllegalArgumentException("The message is not of type SESSION_RESPONSE");
        }
    }

    public SessionResponseMessage(UUID sessionKey, UUID request) {
        super(RexProMessage.CURRENT_VERSION, MessageType.SESSION_RESPONSE, (byte) 0,
                BitWorks.convertUUIDToByteArray(sessionKey),
                BitWorks.convertUUIDToByteArray(request),
                new byte[0]);
    }
}
