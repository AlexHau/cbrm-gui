package dke.cbrm.persistence.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "`User`")
@EqualsAndHashCode(exclude = { "roles" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;

    private String lastName;

    private String userName;

    private String email;

    private String password;

    private boolean enabled;

    private boolean tokenExpired;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
	    name = "users_roles",
	    joinColumns = @JoinColumn(
		    name = "user_id",
		    referencedColumnName = "id"),
	    inverseJoinColumns = @JoinColumn(
		    name = "role_id",
		    referencedColumnName = "id"))
    private Set<Role> roles;

    @Override
    public String toString() {
	return this.userName + " : " + this.email;
    }
}
