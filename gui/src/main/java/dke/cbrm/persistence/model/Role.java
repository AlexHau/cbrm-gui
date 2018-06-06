package dke.cbrm.persistence.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Role {

    public Role(String name) {
	this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    @Override
    public boolean equals(Object role2) {

	return role2 == null ? false : this.id.equals(((Role) role2).id);
    }

    @Override
    public int hashCode() {
	return id.hashCode() + name.hashCode();
    }

    @ManyToMany
    @JoinTable(
	    name = "roles_privileges",
	    joinColumns = @JoinColumn(
		    name = "role_id",
		    referencedColumnName = "id"),
	    inverseJoinColumns = @JoinColumn(
		    name = "privilege_id",
		    referencedColumnName = "id"))
    private Collection<Privilege> privileges;
}
