package dke.cbrm.persistence.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.Data;

@Entity
@Data
public class ModificationApproval {

    private @Id @GeneratedValue @Column(name = "ID") Long id;

    @ManyToOne
    @JoinColumn(name = "APPROVED_BY_USER_ID")
    private User approvedBy;

    @ManyToOne
    @JoinColumn(name = "CTX_APPROVED_ID")
    private Context approvedContext;
    
    @OneToOne
    @JoinColumn(name = "MOD_OP_FOR_APPROVAL")
    private ModificationOperation modOpForApproval;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime approvedAt, createdAt, modifiedAt;

    @ManyToOne
    @JoinColumn(name = "MOD_OP_APPROVED_ID")
    private ModificationOperation modificationOperationApproved;
}
