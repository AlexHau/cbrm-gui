package dke.cbrm.persistence.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(exclude = "approvals")
public class ModificationOperation {

    private @Id @GeneratedValue @Column(name = "ID") Long id;

    private ModificationOperationType modificationOperationType;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime modifiedAt, createdAt;

    @OneToMany(
	    mappedBy = "modificationOperationApproved",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<ModificationApproval> approvals =
	    new HashSet<ModificationApproval>();

    private String contentBefore, contentAfter;

    @OneToOne
    @JoinColumn(name = "MOD_OP_ID_BEFORE")
    private ModificationOperation before;

    @OneToOne
    @JoinColumn(name = "MOD_OP_ID_AFTER")
    private ModificationOperation after;
}
