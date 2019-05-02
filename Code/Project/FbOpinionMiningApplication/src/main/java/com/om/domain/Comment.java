package com.om.domain;

public class Comment {

	private String created_time;

	private From from;

	private String message;

	private boolean can_remove;

	private String like_count;

	private boolean user_likes;

	private String id;

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isCan_remove() {
		return can_remove;
	}

	public void setCan_remove(boolean can_remove) {
		this.can_remove = can_remove;
	}

	public String getLike_count() {
		return like_count;
	}

	public void setLike_count(String like_count) {
		this.like_count = like_count;
	}

	public boolean isUser_likes() {
		return user_likes;
	}

	public void setUser_likes(boolean user_likes) {
		this.user_likes = user_likes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Comment [created_time=" + created_time + ", from=" + from + ", message=" + message + ", can_remove="
				+ can_remove + ", like_count=" + like_count + ", user_likes=" + user_likes + ", id=" + id + "]";
	}

}
