package com.bloknoma.organization.events.source;

import com.bloknoma.organization.events.models.OrganizationChangeModel;
import com.bloknoma.organization.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SimpleSourceBean {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSourceBean.class);

    private Source source;

    // spring cloud stream 에서 주입
    @Autowired
    public SimpleSourceBean(Source source) {
        this.source = source;
    }

    public void publishOrgChange(String action, String orgId) {
        logger.debug("Sending Kafka message {} for Organization Id: {}", action, orgId);
        OrganizationChangeModel change = new OrganizationChangeModel(
                OrganizationChangeModel.class.getTypeName(),
                action,
                orgId,
                UserContextHolder.getContext().getCorrelationId());
        // 단일 채널에 메시지 전송
        source
                .output()
                .send(MessageBuilder
                        .withPayload(change)
                        .build());
    }
}
