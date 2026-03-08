package com.banking.channelconfig.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACL configuration table mapping users to ACL IDs.
 */
@Entity
@Table(name = "ACL_CONFIG")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AclConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "ACL_ID", nullable = false)
    private Long aclId;
}
