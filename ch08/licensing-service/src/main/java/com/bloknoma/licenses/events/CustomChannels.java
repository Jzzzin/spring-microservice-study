package com.bloknoma.licenses.events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface CustomChannels {
    // 채널 이름 정의
    @Input("inboundOrgChanges")
    SubscribableChannel orgs();

    /*
    @Output("outboundOrg")
    MessageChannel outboundOrg();
    */
}
