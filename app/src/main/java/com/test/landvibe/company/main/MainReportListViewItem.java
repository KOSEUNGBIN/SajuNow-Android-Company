package com.test.landvibe.company.main;

/**
 * Created by jik on 2016-03-08.
 * 리뷰에 보여줄 item - 닉네임 작성시간 후기내용 별점
 */
public class MainReportListViewItem {
    private String name;
    private String date;
    private String context;
    private float rating_bar;

    public MainReportListViewItem(String Name, String Date, String Context, float rating )
    {
        name = Name;
        date = Date;
        context=Context;
        rating_bar = rating;
    }

    public float getRating_bar() {
        return rating_bar;
    }

    public void setRating_bar(float rating_bar) {
        this.rating_bar = rating_bar;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }


}
