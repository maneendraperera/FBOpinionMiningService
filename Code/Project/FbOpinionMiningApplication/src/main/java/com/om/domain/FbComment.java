package com.om.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FbComment {

	private List<Comment> data;

	public List<Comment> getData() {
		return data;
	}

	public void setData(List<Comment> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "FbComments [data=" + data + "]";
	}

}
