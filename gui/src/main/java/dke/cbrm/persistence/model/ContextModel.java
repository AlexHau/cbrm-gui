package dke.cbrm.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextModel {

    private @Id @GeneratedValue @Column(name = "ID") Long id;

    private String name;
    
    private String contextModelFilePath;
    
    private String contextsFolderPath;
    
    private String moduleName;
    
    @Override
    public String toString() {
	return name;
    }
}
