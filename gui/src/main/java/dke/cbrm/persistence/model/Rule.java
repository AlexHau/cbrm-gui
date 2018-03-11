package dke.cbrm.persistence.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.Data;

@Entity
@Data
public class Rule {

    private @GeneratedValue @Id @Column(name = "RULE_ID") Long ruleId;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt, modifiedAt;

    private String ruleName, ruleContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTX_ID")
    private Context relatesTo;

}
