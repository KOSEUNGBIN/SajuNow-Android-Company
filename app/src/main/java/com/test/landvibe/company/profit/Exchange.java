package com.test.landvibe.company.profit;

/**
 * Created by Administrator on 2016-06-22.
 */
public class Exchange {
    private long exchange_no;
    private String exchange_amount;
    private String status;
    private String exchange_date;

    public Exchange(long exchange_no, String exchange_amount, String status, String exchange_date) {
        this.exchange_no = exchange_no;
        this.exchange_amount = exchange_amount;
        this.status = status;
        this.exchange_date = exchange_date;
    }

    public long getExchange_no() {
        return exchange_no;
    }

    public void setExchange_no(long exchange_no) {
        this.exchange_no = exchange_no;
    }

    public String getExchange_amount() {
        return exchange_amount;
    }

    public void setExchange_amount(String exchange_amount) {
        this.exchange_amount = exchange_amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExchange_date() {
        return exchange_date;
    }

    public void setExchange_date(String exchange_date) {
        this.exchange_date = exchange_date;
    }
}
