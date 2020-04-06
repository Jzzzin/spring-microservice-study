package com.bloknoma.organization.events.source;

import brave.Tracer;
import com.bloknoma.organization.events.models.OrganizationChangeModel;
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

    @Autowired
    private Tracer tracer;

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
                tracer.currentSpan().context().traceIdString());

        source
                .output()
                .send(MessageBuilder
                        .withPayload(change)
                        .build());
    }


}
