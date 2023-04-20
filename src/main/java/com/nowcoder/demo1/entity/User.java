package com.nowcoder.demo1.entity;
import java.util.Date;
public class User {
  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", salt='" + salt + '\'' +
            ", email='" + email + '\'' +
            ", type=" + type +
            ", status=" + status +
            ", activationCode='" + activationCode + '\'' +
            ", headerUrl='" + headerUrl + '\'' +
            ", createTime=" + createTime +
            '}';
  }

  private Integer id;
  private String username;
  private String password;
//  "盐"，在用户设置的密码后添加一串无序字符串，保证安全性
  private String salt;
  private String email;
//  0-普通用户; 1-超级管理员; 2-版主;
  private Integer type;
//  0-未激活; 1-已激活;
  private Integer status;
  private String activationCode;
//  头像
  private String headerUrl;
  private Date createTime;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }


  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }


  public String getActivationCode() {
    return activationCode;
  }

  public void setActivationCode(String activationCode) {
    this.activationCode = activationCode;
  }


  public String getHeaderUrl() {
    return headerUrl;
  }

  public void setHeaderUrl(String headerUrl) {
    this.headerUrl = headerUrl;
  }


  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

}
