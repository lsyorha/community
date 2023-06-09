package com.nowcoder.demo1.entity;


import java.util.Date;

public class Comment {

  private int id;
  private int userId;
//  评论类型，帖子评论，用户评论的回复
  private int entityType;
//  帖子id
  private int entityId;
//  回复对象
  private int targetId;
  private String content;
//  0-正常;1-删除;
  private int status;
  private Date createTime;

  @Override
  public String toString() {
    return "Comment{" +
            "id=" + id +
            ", userId=" + userId +
            ", entityType=" + entityType +
            ", entityId=" + entityId +
            ", targetId=" + targetId +
            ", content='" + content + '\'' +
            ", status=" + status +
            ", createTime=" + createTime +
            '}';
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getEntityType() {
    return entityType;
  }

  public void setEntityType(int entityType) {
    this.entityType = entityType;
  }

  public int getEntityId() {
    return entityId;
  }

  public void setEntityId(int entityId) {
    this.entityId = entityId;
  }

  public int getTargetId() {
    return targetId;
  }

  public void setTargetId(int targetId) {
    this.targetId = targetId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}
