package com.banking.channelconfig.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Master table for feature flags with ACL configuration.
 */
@Entity
@Table(name = "MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FEATURE_FLAG", nullable = false, unique = true)
    private String featureFlag;

    @Column(name = "IS_ACL_ENABLED", nullable = false)
    private Boolean isAclEnabled;

    @Column(name = "ACL_ID")
    private Long aclId;
}
