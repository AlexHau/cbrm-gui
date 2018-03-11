package dke.cbrm.persistence.model;

import java.util.Set;

public interface ParentChildRelation<T> {

	public T getParent();

	public Set<T> getChildren();
	
	public void setParent(T parent);

	public void setChildren(Set<T> children);

	public String getValue();
}
