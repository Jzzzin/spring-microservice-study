package com.bloknoma.authentication.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user_orgs")
public class UserOrganization implements Serializable {

    @Id
    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "organization_id", nullable = false)
    String organizationId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
