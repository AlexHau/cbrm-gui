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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(exclude = "approvals")
@JsonIgnoreProperties(
	value = { "before", "after", "approvals", "modifiedAt", "createdAt" })
public class ModificationOperation {

    private @Id @GeneratedValue @Column(name = "ID") Long id;

    private ModificationOperationType modificationOperationType;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime modifiedAt, createdAt;

    @OneToOne
    @JoinColumn(name = "CREATED_BY")
    private User createdBy;

    @OneToMany(
	    mappedBy = "modificationOperationApproved",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<ModificationApproval> approvals =
	    new HashSet<ModificationApproval>();

    @OneToOne
    @JoinColumn(name = "MOD_OP_BEFORE_ID")
    private ModificationOperation before;

    @OneToOne
    @JoinColumn(name = "CTX_AFFECTED_ID")
    private Context contextAffected;

    @OneToOne
    @JoinColumn(name = "CTX_1ST_SPLIT_ID")
    private Context firstSplitContext;

    @OneToOne
    @JoinColumn(name = "CTX_2ND_SPLIT_ID")
    private Context secondSplitContext;

    @OneToOne
    @JoinColumn(name = "RULE_ID")
    private Rule ruleAffected;

    @OneToOne
    @JoinColumn(name = "PARAMETER_ID")
    private Parameter parameterAffected;

    public String toString() {
	return modificationOperationType.name() + " is being conducted";
    }
}
