package dke.cbrm.gui;

import java.util.ArrayList;
import java.util.List;

public class ViewUpdater {

    private static List<CbrmView> registeredViews = new ArrayList<CbrmView>();
    
    public static void registerView(CbrmView view) {
	registeredViews.add(view);
    }
    
    public static void updateViews() {
	for(CbrmView view : registeredViews) {
	    view.updateView();
	}
    }
    
}
