package com.genepoint.datapack;

import java.io.Serializable;

public class Floor_fromJSON implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 487364623804315647L;
	public int index;
	public String name;
	public Floor_fromJSON() {
	}
	public Floor_fromJSON(int id,String name){
		this.index=id;
		this.name=name;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

}
