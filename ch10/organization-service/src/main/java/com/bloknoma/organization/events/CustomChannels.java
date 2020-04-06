package com.bloknoma.organization.events;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CustomChannels {
    // SubscribableChannel 은 @Input 에서 사용함
    @Output("outboundOrgChanges")
    MessageChannel outboundOrgChanges();
}
