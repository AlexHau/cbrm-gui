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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(exclude = { "parent", "children", "detParamValues" })
public class Parameter implements ParentChildRelation<Parameter> {

    private @Id @GeneratedValue @Column(name = "parameter_id") Long parameterId;

    private String value;

    @ManyToOne
    private ContextModel belongsToContextModel;
    
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt, modifiedAt;

    @ManyToOne
    private Parameter parent;
    
    @OneToMany(
	    mappedBy = "parent",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<Parameter> children = new HashSet<Parameter>();

    @OneToMany(
	    mappedBy = "parameter",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<DetParamValue> detParamValues = new HashSet<DetParamValue>();
}
