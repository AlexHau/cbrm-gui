package dke.cbrm.persistence.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "contexts", "parameters" })
@JsonIgnoreProperties(
	value = { "contexts", "parameters" })
public class ContextModel {

    private @Id @GeneratedValue @Column(name = "ID") Long id;

    private String name;

    private String contextModelFilePath;

    private String contextsFolderPath;

    private String contextModelModuleName;

    private String businessContextFilePath;

    private String businessContextModuleName;

    private String businessCaseClass;

    @OneToMany(
	    mappedBy = "instantiatesContextModel",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<Context> contexts = new HashSet<Context>();
    
    @OneToMany(
	    mappedBy = "belongsToContextModel",
	    cascade = CascadeType.ALL,
	    fetch = FetchType.LAZY)
    private Set<Parameter> parameters = new HashSet<Parameter>();

    @Override
    public String toString() {
	return name;
    }
}
