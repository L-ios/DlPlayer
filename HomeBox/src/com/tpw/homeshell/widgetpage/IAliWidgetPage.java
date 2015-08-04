package com.tpw.homeshell.widgetpage;

public interface IAliWidgetPage {
    void onPause();
    void onResume();
    void onPageBeginMoving();
    void enterWidgetPage(int page);
    void leaveWidgetPage(int page);
}
