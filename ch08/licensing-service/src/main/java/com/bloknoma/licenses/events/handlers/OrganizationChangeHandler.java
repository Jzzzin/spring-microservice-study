package com.bloknoma.licenses.events.handlers;

import com.bloknoma.licenses.events.CustomChannels;
import com.bloknoma.licenses.events.models.OrganizationChangeModel;
import com.bloknoma.licenses.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CustomChannels.class) // Sink.class 대신 custom Input channel 인 CustomChannels class 사용
public class OrganizationChangeHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    @Autowired
    private OrganizationRedisRepository organizationRedisRepository;

    // message listener
    @StreamListener("inboundOrgChanges") // Sink.INPUT 대신 custom input channel name 전달
    public void loggerSink(OrganizationChangeModel orgChange) {
        logger.debug("Received a message of type " + orgChange.getType());
        switch (orgChange.getAction()) {
            case "GET":
                logger.debug("Received a GET event from the organization service for organization id {}",
                        orgChange.getOrganizationId());
                break;
            case "SAVE":
                logger.debug("Received a SAVE event from the organization service for organization id {}",
                        orgChange.getOrganizationId());
                break;
            case "UPDATE": // 조직 데이터 변경 시 cache 삭제
                logger.debug("Received a UPDATE event from the organization service for organization id {}",
                        orgChange.getOrganizationId());
                organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                break;
            case "DELETE": // 조직 데이터 변경 시 cache 삭제
                logger.debug("Received a DELETE event from the organization service for organization id {}",
                        orgChange.getOrganizationId());
                organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                break;
            default:
                logger.error("Received an UNKNOWN event from the organization service of type {}",
                        orgChange.getType());
                break;
        }
    }
}
