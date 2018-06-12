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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(
	exclude = { "parent", "children", "rules", "allowedUsers",
		"constitutingParameterValues" })
@JsonIgnoreProperties(
	value = { "parent", "children", "rules", "allowedUsers",
		"constitutingPrameterValues", "createdAt", "modifiedAt" })
public class Context implements ParentChildRelation<Context>, Modifieable {

    private @Id @GeneratedValue @Column(name = "CTX_ID") Long contextId;

    private String value, filePath;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt, modifiedAt, validTo, validFrom;

    @ManyToOne
    private Context parent;

    @ManyToOne
    private ContextModel instantiatesContextModel;

    @OneToMany(
	    mappedBy = "parent",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<Context> children = new HashSet<Context>();

    @OneToMany(
	    mappedBy = "relatesTo",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<Rule> rules = new HashSet<Rule>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
	    name = "parameters_contexts",
	    joinColumns = @JoinColumn(
		    name = "ctx_id",
		    referencedColumnName = "CTX_ID"),
	    inverseJoinColumns = @JoinColumn(
		    name = "parameter_id",
		    referencedColumnName = "parameter_id"))
    private Set<Parameter> constitutingParameterValues =
	    new HashSet<Parameter>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
	    name = "users_contexts",
	    joinColumns = @JoinColumn(
		    name = "ctx_id",
		    referencedColumnName = "CTX_ID"),
	    inverseJoinColumns = @JoinColumn(
		    name = "user_id",
		    referencedColumnName = "id"))
    private Set<User> allowedUsers;

    @Override
    public String toString() {
	return value;
    }

    public boolean hasChildren() {
	return !children.isEmpty();
    }

    public boolean hasParent() {
	return parent != null;
    }
}
