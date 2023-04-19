package com.nowcoder.demo1.entity;

/**
 * 封装分页信息
 */
public class Page {
//    当前页
    private int current = 1;
//    单个页面行的显示上限
    private int limit = 10;
//    总行数
    private int rows;
//    查询路径（分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset(){
//        current * limit - limit
        return (current - 1) * limit;
    }

    /**
     *获取总页数
     * @return
     */
    public int getTotal(){
//        rows/limit
        if (rows%limit==0){
            return rows/limit;
        }else {
            return rows/limit + 1;
        }
    }
    /**
     * 获取当前页面的起始页和结束页
     * 每页最多显示五个页面链接
     */
    public int getFrom() {
        int total = getTotal();
        int from = Math.min(Math.max(1, current - 2), total - 4);
        return Math.max(from, 1);
    }

    public int getTo() {
        int total = getTotal();
        int to = Math.max(Math.min(total, current + 2), 5);
        return Math.min(to, total);
    }

}
